package com.studyflow.ypt.data.cloud

import com.studyflow.ypt.data.PersistedStudyState
import com.studyflow.ypt.data.StudySession
import com.studyflow.ypt.data.Subject
import com.studyflow.ypt.data.defaultSubjects
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.SecureRandom

internal const val MAX_SUBJECT_NAME_LENGTH = 60

/**
 * Fusiona sin pérdida el estado local del dispositivo con el respaldo de la nube.
 * - Sesiones: unión por ID (si existe en ambos lados gana la versión de la nube).
 * - Materias: gana la versión de la nube por ID y se conservan las creadas solo en local.
 * - Pomodoros por día: se conserva el máximo registrado.
 */
internal fun mergeStudyStates(
  local: PersistedStudyState,
  remote: PersistedStudyState,
): PersistedStudyState {
  val subjectsById = LinkedHashMap<String, Subject>()
  remote.subjects.forEach { subjectsById[it.id] = it }
  local.subjects.forEach { subjectsById.putIfAbsent(it.id, it) }

  val sessionsById = LinkedHashMap<String, StudySession>()
  local.sessions.forEach { sessionsById[it.id] = it }
  remote.sessions.forEach { sessionsById[it.id] = it }

  val pomodoros = HashMap(local.completedPomodorosByDate)
  remote.completedPomodorosByDate.forEach { (day, count) ->
    pomodoros[day] = maxOf(pomodoros[day] ?: 0, count)
  }

  // Las preferencias (tema, metas, Pomodoro) se toman de la nube solo si ya tenía actividad real
  val remoteHasActivity = remote.sessions.isNotEmpty() || remote.subjects != defaultSubjects()
  val prefs = if (remoteHasActivity) remote else local

  return PersistedStudyState(
    subjects = subjectsById.values.toList(),
    sessions = sessionsById.values.sortedBy { it.startTimeMillis },
    dailyGoalMinutes = prefs.dailyGoalMinutes,
    selectedTheme = prefs.selectedTheme,
    preferredTimerMode = prefs.preferredTimerMode,
    pomodoroConfig = prefs.pomodoroConfig,
    completedPomodorosByDate = pomodoros,
    isHeaderCollapsed = local.isHeaderCollapsed,
  )
}

/**
 * Valida en la frontera del sistema los datos descargados de la nube antes de guardarlos:
 * descarta sesiones corruptas u huérfanas y acota valores fuera de rango.
 */
internal fun sanitizeStudyState(state: PersistedStudyState): PersistedStudyState {
  val subjects =
    state.subjects
      .filter { it.id.isNotBlank() && it.name.isNotBlank() }
      .distinctBy { it.id }
      .map {
        it.copy(
          name = it.name.trim().take(MAX_SUBJECT_NAME_LENGTH),
          targetMinutesPerDay = it.targetMinutesPerDay.coerceIn(5, 720),
        )
      }
  val subjectIds = subjects.map { it.id }.toSet()
  val sessions =
    state.sessions.filter {
      it.endTimeMillis > it.startTimeMillis && it.durationSeconds > 0 && it.subjectId in subjectIds
    }
  val config = state.pomodoroConfig
  return state.copy(
    subjects = subjects,
    sessions = sessions,
    dailyGoalMinutes = state.dailyGoalMinutes.coerceIn(30, 16 * 60),
    pomodoroConfig =
      config.copy(
        focusMinutes = config.focusMinutes.coerceIn(1, 180),
        shortBreakMinutes = config.shortBreakMinutes.coerceIn(1, 60),
        longBreakMinutes = config.longBreakMinutes.coerceIn(1, 90),
        cyclesBeforeLongBreak = config.cyclesBeforeLongBreak.coerceIn(1, 12),
      ),
    completedPomodorosByDate = state.completedPomodorosByDate.mapValues { it.value.coerceIn(0, 200) },
  )
}

internal fun isValidEmailInput(email: String): Boolean {
  val trimmed = email.trim()
  if (trimmed.length !in 6..254 || trimmed.any { it.isWhitespace() }) return false
  val at = trimmed.indexOf('@')
  if (at <= 0 || at != trimmed.lastIndexOf('@')) return false
  val domain = trimmed.substring(at + 1)
  return domain.contains('.') && !domain.startsWith('.') && !domain.endsWith('.')
}

/** Genera un `code_verifier` PKCE aleatorio criptográficamente seguro (RFC 7636). */
internal fun generatePkceVerifier(): String {
  val bytes = ByteArray(32)
  SecureRandom().nextBytes(bytes)
  return base64UrlNoPadding(bytes)
}

/** `code_challenge` = BASE64URL(SHA-256(code_verifier)) sin relleno. */
internal fun pkceCodeChallenge(verifier: String): String =
  base64UrlNoPadding(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)))

/** Resultado del procesamiento del callback de OAuth con Google. */
sealed class GoogleAuthCallbackResult {
  data class DirectToken(val accessToken: String, val refreshToken: String) : GoogleAuthCallbackResult()
  data class PkceCode(val code: String) : GoogleAuthCallbackResult()
}

/**
 * Procesa la URL de retorno de Google/Supabase OAuth manejando:
 * - Parámetros de error con mensajes explicativos en español (ej. si el proveedor no está activo en Supabase).
 * - Tokens directos en fragmento (#access_token=...&refresh_token=...).
 * - Código de intercambio PKCE (?code=...).
 */
internal fun parseGoogleAuthCallback(
  callbackUri: String,
  expectedRedirectPrefix: String,
): Result<GoogleAuthCallbackResult> {
  if (!callbackUri.startsWith(expectedRedirectPrefix)) {
    return Result.failure(IllegalArgumentException("Respuesta de inicio de sesión no válida."))
  }
  val params = parseCallbackParams(callbackUri)

  val error = params["error"]
  val errorDesc = params["error_description"] ?: params["error_code"]
  if (!error.isNullOrBlank() || !errorDesc.isNullOrBlank()) {
    val raw = "${error.orEmpty()} ${errorDesc.orEmpty()}".lowercase()
    val message =
      when {
        raw.contains("unsupported_provider") || raw.contains("not enabled") || raw.contains("validation_failed") ->
          "El inicio de sesión con Google aún no está configurado en el panel de Supabase. Puedes crear tu cuenta o iniciar sesión aquí mismo con Correo y Contraseña en 5 segundos."
        raw.contains("access_denied") || raw.contains("cancelled") || raw.contains("canceled") ->
          "Inicio de sesión con Google cancelado por el usuario."
        !errorDesc.isNullOrBlank() -> errorDesc
        else -> "Error en la autenticación con Google: $error"
      }
    return Result.failure(IllegalStateException(message))
  }

  val directAccessToken = params["access_token"]
  if (!directAccessToken.isNullOrBlank()) {
    val refreshToken = params["refresh_token"].orEmpty()
    return Result.success(GoogleAuthCallbackResult.DirectToken(directAccessToken, refreshToken))
  }

  val code = params["code"]
  if (!code.isNullOrBlank()) {
    return Result.success(GoogleAuthCallbackResult.PkceCode(code))
  }

  return Result.failure(
    IllegalStateException("Google no devolvió credenciales válidas. Inténtalo nuevamente o inicia sesión con tu correo.")
  )
}

/** Lee parámetros tanto de la query (`?a=b`) como del fragmento (`#a=b`) de una URI de callback. */
internal fun parseCallbackParams(uri: String): Map<String, String> {
  val parts = uri.substringAfter('?', "").substringBefore('#') + "&" + uri.substringAfter('#', "")
  return parts
    .split('&')
    .filter { it.contains('=') }
    .associate { pair ->
      val key = URLDecoder.decode(pair.substringBefore('='), "UTF-8")
      val value = URLDecoder.decode(pair.substringAfter('='), "UTF-8")
      key to value
    }
}

private const val BASE64_URL_ALPHABET =
  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

// Implementación propia: java.util.Base64 no existe en minSdk 24 y android.util.Base64 no corre en tests JVM.
private fun base64UrlNoPadding(bytes: ByteArray): String {
  val out = StringBuilder()
  var i = 0
  while (i < bytes.size) {
    val b0 = bytes[i].toInt() and 0xFF
    val b1 = if (i + 1 < bytes.size) bytes[i + 1].toInt() and 0xFF else 0
    val b2 = if (i + 2 < bytes.size) bytes[i + 2].toInt() and 0xFF else 0
    out.append(BASE64_URL_ALPHABET[b0 shr 2])
    out.append(BASE64_URL_ALPHABET[((b0 and 0x03) shl 4) or (b1 shr 4)])
    if (i + 1 < bytes.size) out.append(BASE64_URL_ALPHABET[((b1 and 0x0F) shl 2) or (b2 shr 6)])
    if (i + 2 < bytes.size) out.append(BASE64_URL_ALPHABET[b2 and 0x3F])
    i += 3
  }
  return out.toString()
}
