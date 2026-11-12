package com.studyflow.ypt.ui.main

import com.studyflow.ypt.data.DefaultDataRepository
import com.studyflow.ypt.data.PersistedStudyState
import com.studyflow.ypt.data.StudySession
import com.studyflow.ypt.data.cloud.CloudAuthSession
import com.studyflow.ypt.data.cloud.CloudSyncService
import com.studyflow.ypt.data.cloud.CloudSyncStatus
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

/** Servicio de nube falso en memoria para probar el ViewModel sin red. */
private class FakeCloudSyncService(
  var savedSession: CloudAuthSession? = null,
  var remoteState: PersistedStudyState? = null,
  var signUpRequiresEmailConfirmation: Boolean = false,
) : CloudSyncService {
  val pushedStates = mutableListOf<PersistedStudyState>()
  var signedOut = false
  var cloudDeleted = false
  var lastGoogleCallback: String? = null

  private val validPassword = "ClaveSegura123"

  override fun loadSavedSession(): CloudAuthSession? = savedSession

  override suspend fun signInWithEmail(email: String, password: String): Result<CloudAuthSession> {
    if (password != validPassword) return Result.failure(Exception("Correo o contraseña incorrectos."))
    val session = CloudAuthSession("user-1", email, "access", "refresh")
    savedSession = session
    return Result.success(session)
  }

  override suspend fun signUpWithEmail(email: String, password: String): Result<CloudAuthSession?> {
    if (signUpRequiresEmailConfirmation) return Result.success(null)
    return signInWithEmail(email, password)
  }

  override fun createGoogleSignInUrl(): String = "https://example.supabase.co/auth/v1/authorize?provider=google"

  override suspend fun completeGoogleSignIn(callbackUri: String): Result<CloudAuthSession> {
    lastGoogleCallback = callbackUri
    if (callbackUri.contains("error")) {
      return Result.failure(Exception("El inicio de sesión con Google aún no está configurado en el panel de Supabase."))
    }
    val session = CloudAuthSession("google-user", "gato@gmail.com", "access", "refresh")
    savedSession = session
    return Result.success(session)
  }

  override suspend fun signOut() {
    signedOut = true
    savedSession = null
  }

  override suspend fun fetchStudyState(): Result<PersistedStudyState?> = Result.success(remoteState)

  override suspend fun pushStudyState(state: PersistedStudyState): Result<Unit> {
    pushedStates += state
    remoteState = state
    return Result.success(Unit)
  }

  override suspend fun deleteCloudStudyState(): Result<Unit> {
    cloudDeleted = true
    remoteState = null
    return Result.success(Unit)
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CloudAccountViewModelTest {

  private val baseTime = 1_700_000_000_000L

  private fun remoteSession(id: String, subjectId: String) =
    StudySession(
      id = id,
      subjectId = subjectId,
      startTimeMillis = baseTime - 3_600_000L,
      endTimeMillis = baseTime - 3_000_000L,
      durationSeconds = 600L,
      dateKey = "2023-11-14",
    )

  @Test(timeout = 10_000L)
  fun withoutCloudService_appStaysLocalOnly() = runTest {
    val viewModel = MainScreenViewModel(DefaultDataRepository(context = null), coroutineScope = backgroundScope)
    runCurrent()
    assertFalse(viewModel.cloudAccountState.value.isSignedIn)
    assertEquals(CloudSyncStatus.OFFLINE_LOCAL_ONLY, viewModel.cloudAccountState.value.syncStatus)
  }

  @Test(timeout = 10_000L)
  fun signInWithEmail_mergesCloudDataIntoLocalAndPushesMergedState() = runTest {
    val repository = DefaultDataRepository(context = null)
    val subjectId = repository.currentStudyState.subjects.first().id
    val cloud =
      FakeCloudSyncService(
        remoteState = repository.currentStudyState.copy(sessions = listOf(remoteSession("remote-1", subjectId)))
      )
    val viewModel =
      MainScreenViewModel(
        repository,
        currentTimeProvider = { baseTime + testScheduler.currentTime },
        coroutineScope = backgroundScope,
        cloudSyncService = cloud,
      )
    runCurrent()

    viewModel.signInWithEmail("gato@studycat.app", "ClaveSegura123")
    runCurrent()

    val account = viewModel.cloudAccountState.value
    assertTrue(account.isSignedIn)
    assertEquals("gato@studycat.app", account.userEmail)
    assertEquals(CloudSyncStatus.SYNCED, account.syncStatus)
    assertTrue(repository.currentStudyState.sessions.any { it.id == "remote-1" })
    assertTrue(cloud.pushedStates.last().sessions.any { it.id == "remote-1" })
  }

  @Test(timeout = 10_000L)
  fun signInWithWrongPassword_showsErrorAndStaysSignedOut() = runTest {
    val viewModel =
      MainScreenViewModel(
        DefaultDataRepository(context = null),
        coroutineScope = backgroundScope,
        cloudSyncService = FakeCloudSyncService(),
      )
    runCurrent()

    viewModel.signInWithEmail("gato@studycat.app", "incorrecta")
    runCurrent()

    assertFalse(viewModel.cloudAccountState.value.isSignedIn)
    assertEquals("Correo o contraseña incorrectos.", viewModel.cloudAccountState.value.errorMessage)
  }

  @Test(timeout = 10_000L)
  fun signUpRequiringEmailConfirmation_showsInfoMessageWithoutSigningIn() = runTest {
    val viewModel =
      MainScreenViewModel(
        DefaultDataRepository(context = null),
        coroutineScope = backgroundScope,
        cloudSyncService = FakeCloudSyncService(signUpRequiresEmailConfirmation = true),
      )
    runCurrent()

    viewModel.signUpWithEmail("nuevo@studycat.app", "ClaveSegura123")
    runCurrent()

    assertFalse(viewModel.cloudAccountState.value.isSignedIn)
    assertNotNull(viewModel.cloudAccountState.value.infoMessage)
  }

  @Test(timeout = 10_000L)
  fun googleCallback_signsInWithGoogleAccount() = runTest {
    val cloud = FakeCloudSyncService()
    val viewModel =
      MainScreenViewModel(DefaultDataRepository(context = null), coroutineScope = backgroundScope, cloudSyncService = cloud)
    runCurrent()

    assertNotNull(viewModel.startGoogleSignIn())
    viewModel.completeGoogleSignIn("studycat://auth-callback?code=abc")
    runCurrent()

    assertEquals("studycat://auth-callback?code=abc", cloud.lastGoogleCallback)
    assertTrue(viewModel.cloudAccountState.value.isSignedIn)
    assertEquals("gato@gmail.com", viewModel.cloudAccountState.value.userEmail)
  }

  @Test(timeout = 10_000L)
  fun googleCallbackWithError_showsErrorMessageAndStaysSignedOut() = runTest {
    val cloud = FakeCloudSyncService()
    val viewModel =
      MainScreenViewModel(DefaultDataRepository(context = null), coroutineScope = backgroundScope, cloudSyncService = cloud)
    runCurrent()

    viewModel.completeGoogleSignIn("studycat://auth-callback#error=unsupported_provider")
    runCurrent()

    assertFalse(viewModel.cloudAccountState.value.isSignedIn)
    assertNotNull(viewModel.cloudAccountState.value.errorMessage)
    assertTrue(viewModel.cloudAccountState.value.errorMessage!!.contains("Supabase"))
  }

  @Test(timeout = 10_000L)
  fun localChangesWhileSignedIn_areAutoUploadedAfterDebounce() = runTest {
    val repository = DefaultDataRepository(context = null)
    val cloud = FakeCloudSyncService(savedSession = CloudAuthSession("user-1", "gato@studycat.app", "a", "r"))
    val viewModel =
      MainScreenViewModel(
        repository,
        currentTimeProvider = { baseTime + testScheduler.currentTime },
        coroutineScope = backgroundScope,
        cloudSyncService = cloud,
      )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()
    assertTrue(viewModel.cloudAccountState.value.isSignedIn)
    val pushesAfterInitialSync = cloud.pushedStates.size

    viewModel.addSubject("Química Orgánica", 0xFFEC4899, 60)
    runCurrent()
    advanceTimeBy(AUTO_SYNC_DEBOUNCE_MILLIS + 100L)
    runCurrent()

    assertEquals(pushesAfterInitialSync + 1, cloud.pushedStates.size)
    assertTrue(cloud.pushedStates.last().subjects.any { it.name == "Química Orgánica" })
  }

  @Test(timeout = 10_000L)
  fun signOut_keepsLocalDataButStopsCloudSync() = runTest {
    val repository = DefaultDataRepository(context = null)
    val cloud = FakeCloudSyncService(savedSession = CloudAuthSession("user-1", "gato@studycat.app", "a", "r"))
    val viewModel =
      MainScreenViewModel(repository, coroutineScope = backgroundScope, cloudSyncService = cloud)
    runCurrent()

    viewModel.signOut()
    runCurrent()

    assertTrue(cloud.signedOut)
    assertFalse(viewModel.cloudAccountState.value.isSignedIn)
    assertNull(viewModel.cloudAccountState.value.userEmail)
    assertEquals(4, repository.currentStudyState.subjects.size)
  }

  @Test(timeout = 10_000L)
  fun deleteCloudData_removesBackupAndSignsOut() = runTest {
    val cloud = FakeCloudSyncService(savedSession = CloudAuthSession("user-1", "gato@studycat.app", "a", "r"))
    val viewModel =
      MainScreenViewModel(DefaultDataRepository(context = null), coroutineScope = backgroundScope, cloudSyncService = cloud)
    runCurrent()

    viewModel.deleteCloudData()
    runCurrent()

    assertTrue(cloud.cloudDeleted)
    assertFalse(viewModel.cloudAccountState.value.isSignedIn)
  }
}
