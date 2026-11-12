package com.studyflow.ypt.data.cloud

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.studyflow.ypt.data.PersistedStudyState
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable
data class CloudAuthSession(
  val userId: String,
  val email: String,
  val accessToken: String,
  val refreshToken: String,
)

enum class CloudSyncStatus {
  OFFLINE_LOCAL_ONLY,
  SYNCING,
  SYNCED,
  ERROR,
}

data class CloudAccountUiState(
  val isSignedIn: Boolean = false,
  val userEmail: String? = null,
  val syncStatus: CloudSyncStatus = CloudSyncStatus.OFFLINE_LOCAL_ONLY,
  val lastSyncedAtFormatted: String? = null,
  val errorMessage: String? = null,
  val infoMessage: String? = null,
  val isLoading: Boolean = false,
)

/** La sesión expiró y no pudo renovarse: el usuario debe volver a iniciar sesión. */
class SessionExpiredException : Exception("Tu sesión expiró. Vuelve a iniciar sesión.")

interface CloudSyncService {
  fun loadSavedSession(): CloudAuthSession?

  suspend fun signInWithEmail(email: String, password: String): Result<CloudAuthSession>

  /** Devuelve `null` cuando la cuenta se creó pero requiere confirmar el correo. */
  suspend fun signUpWithEmail(email: String, password: String): Result<CloudAuthSession?>

  /** URL de autorización de Google (flujo OAuth con PKCE). */
  fun createGoogleSignInUrl(): String

  suspend fun completeGoogleSignIn(callbackUri: String): Result<CloudAuthSession>

  suspend fun signOut()

  suspend fun fetchStudyState(): Result<PersistedStudyState?>

  suspend fun pushStudyState(state: PersistedStudyState): Result<Unit>

  suspend fun deleteCloudStudyState(): Result<Unit>
}

/**
 * [CloudSyncService] sobre Supabase Auth + PostgREST.
 *
 * Seguridad:
 * - Solo usa la clave **publicable** (pensada para apps cliente). Cada fila de
 *   `user_study_backups` está protegida por Row Level Security: `auth.uid() = user_id`.
 * - Google se inicia con OAuth 2.0 + PKCE: aunque otra app intercepte el `code` del deep link,
 *   no puede canjearlo sin el `code_verifier`, que nunca sale del dispositivo.
 * - Los tokens se guardan cifrados (AES-256-GCM) con una llave no exportable de `AndroidKeyStore`.
 * - Nunca se registran tokens ni contraseñas en logs.
 */
class SupabaseCloudSyncService(
  context: Context,
  private val supabaseUrl: String = DEFAULT_SUPABASE_URL,
  private val publishableKey: String = DEFAULT_SUPABASE_PUBLISHABLE_KEY,
) : CloudSyncService {

  private val prefs = context.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE)
  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }
  private var session: CloudAuthSession? = null

  override fun loadSavedSession(): CloudAuthSession? {
    session?.let { return it }
    val stored = readEncrypted(KEY_SESSION) ?: return null
    return runCatching { json.decodeFromString<CloudAuthSession>(stored) }.getOrNull().also { session = it }
  }

  private fun saveSession(newSession: CloudAuthSession?) {
    session = newSession
    if (newSession == null) {
      prefs.edit().remove(KEY_SESSION).apply()
    } else {
      writeEncrypted(KEY_SESSION, json.encodeToString(CloudAuthSession.serializer(), newSession))
    }
  }

  override suspend fun signInWithEmail(email: String, password: String): Result<CloudAuthSession> =
    io {
      val cleanEmail = validateCredentials(email, password)
      val response =
        request("POST", "/auth/v1/token?grant_type=password", credentialsBody(cleanEmail, password))
      parseSession(response).also(::saveSession)
    }

  override suspend fun signUpWithEmail(email: String, password: String): Result<CloudAuthSession?> =
    io {
      val cleanEmail = validateCredentials(email, password)
      val response = request("POST", "/auth/v1/signup", credentialsBody(cleanEmail, password))
      val obj = json.parseToJsonElement(response).jsonObject
      // Si el proyecto exige confirmar el correo, Supabase no entrega tokens todavía
      if (obj["access_token"] == null) null else parseSession(response).also(::saveSession)
    }

  override fun createGoogleSignInUrl(): String {
    val verifier = generatePkceVerifier()
    writeEncrypted(KEY_PKCE_VERIFIER, verifier)
    val redirect = URLEncoder.encode(OAUTH_REDIRECT_URI, "UTF-8")
    return "$supabaseUrl/auth/v1/authorize?provider=google&redirect_to=$redirect" +
      "&code_challenge=${pkceCodeChallenge(verifier)}&code_challenge_method=s256"
  }

  override suspend fun completeGoogleSignIn(callbackUri: String): Result<CloudAuthSession> =
    io {
      when (val outcome = parseGoogleAuthCallback(callbackUri, OAUTH_REDIRECT_URI).getOrThrow()) {
        is GoogleAuthCallbackResult.DirectToken -> {
          // El proveedor retornó los tokens directamente en el fragmento (implicit grant / redirect directo)
          val userResponse = request("GET", "/auth/v1/user", body = null, bearer = outcome.accessToken)
          val userObj = json.parseToJsonElement(userResponse).jsonObject
          val userId =
            userObj["id"]?.jsonPrimitive?.contentOrNull
              ?: throw IllegalStateException("Identificador de usuario faltante en la respuesta.")
          val email = userObj["email"]?.jsonPrimitive?.contentOrNull.orEmpty()
          val session =
            CloudAuthSession(
              userId = userId,
              email = email,
              accessToken = outcome.accessToken,
              refreshToken = outcome.refreshToken,
            )
          saveSession(session)
          session
        }
        is GoogleAuthCallbackResult.PkceCode -> {
          // Flujo PKCE: intercambiar auth_code por tokens usando el code_verifier seguro guardado en reposo
          val verifier =
            readEncrypted(KEY_PKCE_VERIFIER)
              ?: throw IllegalStateException("La solicitud de inicio de sesión expiró. Por favor inténtalo de nuevo.")
          prefs.edit().remove(KEY_PKCE_VERIFIER).apply() // un solo uso

          val body =
            buildJsonObject {
                put("auth_code", outcome.code)
                put("code_verifier", verifier)
              }
              .toString()
          val tokenResponse =
            try {
              request("POST", "/auth/v1/token?grant_type=pkce", body)
            } catch (e: HttpException) {
              if (e.responseBody.contains("flow_state_not_found") || e.responseBody.contains("invalid flow state")) {
                throw IllegalStateException("El código de autorización expiró o ya fue usado. Inténtalo de nuevo.")
              }
              throw e
            }
          parseSession(tokenResponse).also(::saveSession)
        }
      }
    }

  override suspend fun signOut() {
    withContext(Dispatchers.IO) {
      val current = loadSavedSession()
      saveSession(null) // primero se borra localmente: aunque falle la red, el dispositivo queda limpio
      if (current != null) {
        runCatching { request("POST", "/auth/v1/logout", body = null, bearer = current.accessToken) }
      }
    }
  }

  override suspend fun fetchStudyState(): Result<PersistedStudyState?> =
    io {
      val userId = requireSession().userId
      val response =
        authorizedRequest("GET", "/rest/v1/user_study_backups?user_id=eq.$userId&select=study_state")
      val rows = json.parseToJsonElement(response).jsonArray
      rows.firstOrNull()?.jsonObject?.get("study_state")?.let {
        json.decodeFromJsonElement(PersistedStudyState.serializer(), it)
      }
    }

  override suspend fun pushStudyState(state: PersistedStudyState): Result<Unit> =
    io {
      val current = requireSession()
      val body =
        buildJsonObject {
            put("user_id", current.userId)
            put("study_state", json.encodeToJsonElement(PersistedStudyState.serializer(), state))
          }
          .toString()
      authorizedRequest(
        "POST",
        "/rest/v1/user_study_backups?on_conflict=user_id",
        body,
        extraHeaders = mapOf("Prefer" to "resolution=merge-duplicates,return=minimal"),
      )
      Unit
    }

  override suspend fun deleteCloudStudyState(): Result<Unit> =
    io {
      val userId = requireSession().userId
      authorizedRequest("DELETE", "/rest/v1/user_study_backups?user_id=eq.$userId")
      Unit
    }

  // ---------- Red ----------

  private fun requireSession(): CloudAuthSession = loadSavedSession() ?: throw SessionExpiredException()

  /** Ejecuta con el JWT del usuario y, si expiró (401), lo renueva una sola vez con el refresh token. */
  private fun authorizedRequest(
    method: String,
    path: String,
    body: String? = null,
    extraHeaders: Map<String, String> = emptyMap(),
  ): String {
    val current = requireSession()
    return try {
      request(method, path, body, current.accessToken, extraHeaders)
    } catch (e: HttpException) {
      if (e.status != 401) throw e
      request(method, path, body, refreshSession(current).accessToken, extraHeaders)
    }
  }

  private fun refreshSession(current: CloudAuthSession): CloudAuthSession {
    val body = buildJsonObject { put("refresh_token", current.refreshToken) }.toString()
    return try {
      parseSession(request("POST", "/auth/v1/token?grant_type=refresh_token", body)).also(::saveSession)
    } catch (_: HttpException) {
      saveSession(null)
      throw SessionExpiredException()
    }
  }

  private class HttpException(val status: Int, val responseBody: String) : Exception("HTTP $status")

  private fun request(
    method: String,
    path: String,
    body: String?,
    bearer: String? = null,
    extraHeaders: Map<String, String> = emptyMap(),
  ): String {
    val connection = URL(supabaseUrl + path).openConnection() as HttpURLConnection
    try {
      connection.requestMethod = method
      connection.connectTimeout = 15_000
      connection.readTimeout = 15_000
      connection.instanceFollowRedirects = false
      connection.setRequestProperty("apikey", publishableKey)
      // La clave publicable no es un JWT: solo se envía Authorization con el token del usuario
      bearer?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
      connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
      extraHeaders.forEach { (k, v) -> connection.setRequestProperty(k, v) }
      if (body != null) {
        connection.doOutput = true
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
      }
      val status = connection.responseCode
      val stream = if (status in 200..299) connection.inputStream else connection.errorStream
      val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
      if (status !in 200..299) throw HttpException(status, text)
      return text
    } finally {
      connection.disconnect()
    }
  }

  private fun parseSession(response: String): CloudAuthSession {
    val obj = json.parseToJsonElement(response).jsonObject
    val user: JsonObject =
      obj["user"]?.jsonObject ?: throw IllegalStateException("No se recibieron datos de usuario del servidor.")
    return CloudAuthSession(
      userId =
        user["id"]?.jsonPrimitive?.contentOrNull
          ?: throw IllegalStateException("Identificador de usuario faltante en la respuesta."),
      email = user["email"]?.jsonPrimitive?.contentOrNull.orEmpty(),
      accessToken =
        obj["access_token"]?.jsonPrimitive?.contentOrNull
          ?: throw IllegalStateException("No se pudo obtener el token de acceso."),
      refreshToken = obj["refresh_token"]?.jsonPrimitive?.contentOrNull.orEmpty(),
    )
  }

  private fun validateCredentials(email: String, password: String): String {
    val clean = email.trim().lowercase()
    require(isValidEmailInput(clean)) { "Ingresa un correo electrónico válido." }
    require(password.length in 8..72) { "La contraseña debe tener entre 8 y 72 caracteres." }
    return clean
  }

  private fun credentialsBody(email: String, password: String) =
    buildJsonObject {
        put("email", email)
        put("password", password)
      }
      .toString()

  /** Ejecuta en IO y traduce cualquier error a un mensaje claro en español (sin filtrar detalles internos). */
  private suspend fun <T> io(block: () -> T): Result<T> =
    withContext(Dispatchers.IO) {
      try {
        Result.success(block())
      } catch (e: SessionExpiredException) {
        Result.failure(e)
      } catch (e: IllegalArgumentException) {
        Result.failure(Exception(e.message))
      } catch (e: IllegalStateException) {
        Result.failure(Exception(e.message))
      } catch (e: HttpException) {
        Result.failure(Exception(translateHttpError(e)))
      } catch (_: Exception) {
        Result.failure(Exception("Sin conexión con la nube. Tus datos siguen guardados en el dispositivo."))
      }
    }

  private fun translateHttpError(e: HttpException): String {
    val body = e.responseBody
    return when {
      body.contains("invalid_credentials") || body.contains("Invalid login credentials") ->
        "Correo o contraseña incorrectos."
      body.contains("email_not_confirmed") || body.contains("Email not confirmed") ->
        "Confirma tu correo con el enlace que te enviamos antes de iniciar sesión."
      body.contains("user_already_exists") || body.contains("already registered") ->
        "Este correo ya está registrado. Usa \"Iniciar sesión\"."
      body.contains("weak_password") -> "La contraseña es demasiado débil. Usa una más larga y variada."
      body.contains("flow_state_not_found") || body.contains("invalid flow state") ->
        "La sesión de autorización expiró. Por favor inténtalo de nuevo."
      body.contains("unsupported_provider") || body.contains("provider is not enabled") ->
        "El inicio de sesión con Google aún no está configurado en el panel de Supabase. Puedes usar Correo y Contraseña."
      e.status == 429 -> "Demasiados intentos. Espera un momento e inténtalo de nuevo."
      else -> "No se pudo completar la operación en la nube (código ${e.status})."
    }
  }

  // ---------- Cifrado en reposo (AndroidKeyStore) ----------

  private fun aesKey(): SecretKey {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
    val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
    generator.init(
      KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .build()
    )
    return generator.generateKey()
  }

  private fun writeEncrypted(key: String, plainText: String) {
    runCatching {
      val cipher = Cipher.getInstance(AES_GCM)
      cipher.init(Cipher.ENCRYPT_MODE, aesKey())
      val payload =
        Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
          Base64.encodeToString(cipher.doFinal(plainText.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
      prefs.edit().putString(key, payload).apply()
    }
  }

  private fun readEncrypted(key: String): String? {
    val payload = prefs.getString(key, null) ?: return null
    return runCatching {
        val (iv, data) = payload.split(":").map { Base64.decode(it, Base64.NO_WRAP) }
        val cipher = Cipher.getInstance(AES_GCM)
        cipher.init(Cipher.DECRYPT_MODE, aesKey(), GCMParameterSpec(128, iv))
        String(cipher.doFinal(data), Charsets.UTF_8)
      }
      .onFailure { prefs.edit().remove(key).apply() } // dato ilegible (p. ej. restaurado de backup): descartar
      .getOrNull()
  }

  companion object {
    const val DEFAULT_SUPABASE_URL = "https://gigjkhvbktbhczvxspzy.supabase.co"
    const val DEFAULT_SUPABASE_PUBLISHABLE_KEY = "sb_publishable_EhP92zMrUBfMMVl6d-0t-A_HSMCPCWy"
    const val OAUTH_REDIRECT_URI = "studycat://auth-callback"
    const val SECURE_PREFS_NAME = "studycat_secure_auth"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "studycat_session_key_v1"
    private const val AES_GCM = "AES/GCM/NoPadding"
    private const val KEY_SESSION = "session_v1"
    private const val KEY_PKCE_VERIFIER = "pkce_verifier_v1"
  }
}
