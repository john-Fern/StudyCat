package com.studyflow.ypt.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.ypt.data.AppTheme
import com.studyflow.ypt.data.DataRepository
import com.studyflow.ypt.data.PersistedStudyState
import com.studyflow.ypt.data.PomodoroConfig
import com.studyflow.ypt.data.PomodoroPhase
import com.studyflow.ypt.data.StudySession
import com.studyflow.ypt.data.Subject
import com.studyflow.ypt.data.TimerMode
import com.studyflow.ypt.data.cloud.CloudAccountUiState
import com.studyflow.ypt.data.cloud.CloudSyncService
import com.studyflow.ypt.data.cloud.CloudSyncStatus
import com.studyflow.ypt.data.cloud.SessionExpiredException
import com.studyflow.ypt.data.cloud.mergeStudyStates
import com.studyflow.ypt.data.cloud.sanitizeStudyState
import com.studyflow.ypt.ui.main.MainScreenUiState.Success
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Espera tras el último cambio local antes de subir el respaldo (agrupa cambios seguidos). */
internal const val AUTO_SYNC_DEBOUNCE_MILLIS = 3_000L

enum class PomodoroAlarmType {
  FOCUS_COMPLETED,
  BREAK_COMPLETED,
}

data class PomodoroAlarmState(
  val id: Long,
  val type: PomodoroAlarmType,
  val completedPhase: PomodoroPhase,
  val nextPhase: PomodoroPhase,
  val subjectId: String? = null,
)

data class ActiveTimerState(
  val subjectId: String? = null,
  val sessionStartMillis: Long = 0L,
  val elapsedSeconds: Long = 0L,
  val isOledFocusMode: Boolean = false,
  val timerMode: TimerMode = TimerMode.STOPWATCH,
  val pomodoroPhase: PomodoroPhase = PomodoroPhase.FOCUS,
  val pomodoroTargetSeconds: Long = 25 * 60L,
  val isBreakRunning: Boolean = false,
  val activeAlarm: PomodoroAlarmState? = null,
) {
  val isRunning: Boolean
    get() = subjectId != null || isBreakRunning

  val isStudying: Boolean
    get() = subjectId != null && !isBreakRunning

  val remainingPomodoroSeconds: Long
    get() = (pomodoroTargetSeconds - elapsedSeconds).coerceAtLeast(0L)
}

data class YptDashboardState(
  val subjects: List<Subject> = emptyList(),
  val todaySessions: List<StudySession> = emptyList(),
  val subjectSecondsToday: Map<String, Long> = emptyMap(),
  val totalSecondsToday: Long = 0L,
  val maxContinuousSecondsToday: Long = 0L,
  val firstStartTimeFormatted: String = "--:--",
  val lastEndTimeFormatted: String = "--:--",
  val dailyGoalMinutes: Int = 360,
  val activeTimer: ActiveTimerState = ActiveTimerState(),
  val activeAlarm: PomodoroAlarmState? = null,
  // 24 horas (0..23) -> 6 bloques de 10 minutos cada una -> colorHex de la materia dominante
  val tenMinuteBlocks: Map<Int, List<Long?>> = emptyMap(),
  val selectedTheme: AppTheme = AppTheme.DARK_YPT,
  val timerMode: TimerMode = TimerMode.STOPWATCH,
  val pomodoroConfig: PomodoroConfig = PomodoroConfig(),
  val completedPomodorosToday: Int = 0,
  val isHeaderCollapsed: Boolean = false,
)

class MainScreenViewModel(
  private val dataRepository: DataRepository,
  private val currentTimeProvider: () -> Long = { System.currentTimeMillis() },
  coroutineScope: CoroutineScope? = null,
  private val cloudSyncService: CloudSyncService? = null,
) : ViewModel() {
  private val scope: CoroutineScope = coroutineScope ?: viewModelScope

  private val _cloudAccount = MutableStateFlow(CloudAccountUiState())
  val cloudAccountState: StateFlow<CloudAccountUiState> = _cloudAccount.asStateFlow()
  private var lastSyncedState: PersistedStudyState? = null
  private var syncJob: Job? = null

  val uiState: StateFlow<MainScreenUiState> =
    dataRepository.data
      .map<List<String>, MainScreenUiState>(::Success)
      .catch { emit(MainScreenUiState.Error(it)) }
      .stateIn(scope, SharingStarted.WhileSubscribed(5000), MainScreenUiState.Loading)

  private val _activeTimer = MutableStateFlow(ActiveTimerState())
  private var tickerJob: Job? = null

  private val latestPersistedState: PersistedStudyState
    get() = dataRepository.currentStudyState

  val yptState: StateFlow<YptDashboardState> =
    combine(dataRepository.studyState, _activeTimer) { persisted, timer ->
        val syncedTimer =
          if (!timer.isRunning) {
            val targetSec =
              when (timer.pomodoroPhase) {
                PomodoroPhase.FOCUS -> persisted.pomodoroConfig.focusMinutes * 60L
                PomodoroPhase.SHORT_BREAK -> persisted.pomodoroConfig.shortBreakMinutes * 60L
                PomodoroPhase.LONG_BREAK -> persisted.pomodoroConfig.longBreakMinutes * 60L
              }
            timer.copy(
              timerMode = persisted.preferredTimerMode,
              pomodoroTargetSeconds = targetSec,
            )
          } else {
            timer
          }
        buildDashboardState(persisted, syncedTimer)
      }
      .stateIn(scope, SharingStarted.WhileSubscribed(5000), YptDashboardState())

  init {
    if (cloudSyncService != null) {
      cloudSyncService.loadSavedSession()?.let { saved ->
        _cloudAccount.value = CloudAccountUiState(isSignedIn = true, userEmail = saved.email)
        syncNow()
      }
      startAutoUpload()
    }
  }

  // ---------- Cuenta y respaldo en la nube ----------

  fun signInWithEmail(email: String, password: String) {
    val service = cloudSyncService ?: return
    runAuth { onSignedIn(service.signInWithEmail(email, password).getOrThrow().email) }
  }

  fun signUpWithEmail(email: String, password: String) {
    val service = cloudSyncService ?: return
    runAuth {
      val session = service.signUpWithEmail(email, password).getOrThrow()
      if (session != null) {
        onSignedIn(session.email)
      } else {
        _cloudAccount.value =
          CloudAccountUiState(
            infoMessage = "¡Cuenta creada! Revisa tu correo y confirma el enlace; luego inicia sesión aquí."
          )
      }
    }
  }

  /** Devuelve la URL que la UI debe abrir en el navegador para iniciar sesión con Google. */
  fun startGoogleSignIn(): String? = cloudSyncService?.createGoogleSignInUrl()

  fun completeGoogleSignIn(callbackUri: String) {
    val service = cloudSyncService ?: return
    runAuth { onSignedIn(service.completeGoogleSignIn(callbackUri).getOrThrow().email) }
  }

  fun signOut() {
    val service = cloudSyncService ?: return
    scope.launch {
      syncJob?.cancel()
      service.signOut()
      lastSyncedState = null
      // Los datos locales se conservan: la app sigue funcionando 100% offline
      _cloudAccount.value = CloudAccountUiState(infoMessage = "Sesión cerrada. Tus datos siguen en este dispositivo.")
    }
  }

  /** Borra el respaldo del usuario en la nube (derecho al olvido) y cierra la sesión. */
  fun deleteCloudData() {
    val service = cloudSyncService ?: return
    scope.launch {
      syncJob?.cancel()
      _cloudAccount.value = _cloudAccount.value.copy(isLoading = true, errorMessage = null)
      service
        .deleteCloudStudyState()
        .onSuccess {
          service.signOut()
          lastSyncedState = null
          _cloudAccount.value =
            CloudAccountUiState(infoMessage = "Tu respaldo en la nube fue eliminado. Los datos locales se conservan.")
        }
        .onFailure { showCloudError(it) }
    }
  }

  fun clearCloudMessages() {
    _cloudAccount.value = _cloudAccount.value.copy(errorMessage = null, infoMessage = null)
  }

  /** Descarga el respaldo, lo fusiona sin pérdida con los datos locales y sube el resultado. */
  fun syncNow() {
    val service = cloudSyncService ?: return
    if (!_cloudAccount.value.isSignedIn) return
    syncJob?.cancel()
    syncJob =
      scope.launch {
        _cloudAccount.value = _cloudAccount.value.copy(syncStatus = CloudSyncStatus.SYNCING, errorMessage = null)
        val remote =
          service.fetchStudyState().getOrElse {
            showCloudError(it)
            return@launch
          }
        val local = latestPersistedState
        val merged =
          if (remote == null) local else sanitizeStudyState(mergeStudyStates(local, sanitizeStudyState(remote)))
        if (merged != local) dataRepository.replaceState(merged)
        uploadState(merged)
      }
  }

  @OptIn(FlowPreview::class)
  private fun startAutoUpload() {
    scope.launch {
      dataRepository.studyState.debounce(AUTO_SYNC_DEBOUNCE_MILLIS).collect { state ->
        val account = _cloudAccount.value
        if (account.isSignedIn && account.syncStatus != CloudSyncStatus.SYNCING && state != lastSyncedState) {
          uploadState(state)
        }
      }
    }
  }

  private suspend fun uploadState(state: PersistedStudyState) {
    val service = cloudSyncService ?: return
    service
      .pushStudyState(state)
      .onSuccess {
        lastSyncedState = state
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(currentTimeProvider()))
        _cloudAccount.value =
          _cloudAccount.value.copy(syncStatus = CloudSyncStatus.SYNCED, lastSyncedAtFormatted = time, errorMessage = null)
      }
      .onFailure { showCloudError(it) }
  }

  private fun onSignedIn(email: String) {
    _cloudAccount.value = CloudAccountUiState(isSignedIn = true, userEmail = email)
    syncNow()
  }

  private fun runAuth(block: suspend () -> Unit) {
    scope.launch {
      _cloudAccount.value = _cloudAccount.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
      try {
        block()
      } catch (e: Exception) {
        _cloudAccount.value = CloudAccountUiState(errorMessage = e.message ?: "No se pudo iniciar sesión.")
      }
    }
  }

  private fun showCloudError(error: Throwable) {
    _cloudAccount.value =
      if (error is SessionExpiredException) {
        CloudAccountUiState(errorMessage = error.message)
      } else {
        _cloudAccount.value.copy(
          isLoading = false,
          syncStatus = CloudSyncStatus.ERROR,
          errorMessage = error.message ?: "No se pudo sincronizar. Tus datos siguen en el dispositivo.",
        )
      }
  }

  // ---------- Temporizadores ----------

  fun toggleSubjectTimer(subjectId: String) {
    val current = _activeTimer.value
    if (current.subjectId == subjectId) {
      stopCurrentTimer()
    } else {
      if (current.isRunning) {
        stopCurrentTimer()
      }
      startTimerForSubject(subjectId)
    }
  }

  private fun startTimerForSubject(subjectId: String) {
    val startMillis = currentTimeProvider()
    val mode = latestPersistedState.preferredTimerMode
    val focusSeconds = latestPersistedState.pomodoroConfig.focusMinutes * 60L
    _activeTimer.value =
      ActiveTimerState(
        subjectId = subjectId,
        sessionStartMillis = startMillis,
        elapsedSeconds = 0L,
        isOledFocusMode = false,
        timerMode = mode,
        pomodoroPhase = PomodoroPhase.FOCUS,
        pomodoroTargetSeconds = focusSeconds,
        isBreakRunning = false,
      )
    startTicker(startMillis)
  }

  fun togglePomodoroBreak() {
    val current = _activeTimer.value
    if (current.isBreakRunning) {
      stopCurrentTimer()
      return
    }
    if (current.isRunning) {
      stopCurrentTimer()
    }
    val phase =
      if (current.pomodoroPhase == PomodoroPhase.FOCUS) {
        PomodoroPhase.SHORT_BREAK
      } else {
        current.pomodoroPhase
      }
    val targetSeconds =
      when (phase) {
        PomodoroPhase.SHORT_BREAK -> latestPersistedState.pomodoroConfig.shortBreakMinutes * 60L
        PomodoroPhase.LONG_BREAK -> latestPersistedState.pomodoroConfig.longBreakMinutes * 60L
        PomodoroPhase.FOCUS -> latestPersistedState.pomodoroConfig.focusMinutes * 60L
      }
    val startMillis = currentTimeProvider()
    _activeTimer.value =
      ActiveTimerState(
        subjectId = null,
        sessionStartMillis = startMillis,
        elapsedSeconds = 0L,
        isOledFocusMode = false,
        timerMode = TimerMode.POMODORO,
        pomodoroPhase = phase,
        pomodoroTargetSeconds = targetSeconds,
        isBreakRunning = true,
      )
    startTicker(startMillis)
  }

  private fun startTicker(startMillis: Long) {
    tickerJob?.cancel()
    tickerJob =
      scope.launch {
        while (isActive) {
          delay(1000L)
          val current = _activeTimer.value
          if (!current.isRunning) break
          val now = currentTimeProvider()
          val elapsed = ((now - startMillis) / 1000L).coerceAtLeast(1L)

          if (current.timerMode == TimerMode.POMODORO && elapsed >= current.pomodoroTargetSeconds) {
            onPomodoroPhaseFinished(current.copy(elapsedSeconds = current.pomodoroTargetSeconds))
            break
          } else {
            _activeTimer.value = current.copy(elapsedSeconds = elapsed)
          }
        }
      }
  }

  private fun onPomodoroPhaseFinished(completedState: ActiveTimerState) {
    tickerJob?.cancel()
    tickerJob = null
    val config = latestPersistedState.pomodoroConfig
    val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(currentTimeProvider()))

    if (completedState.pomodoroPhase == PomodoroPhase.FOCUS && completedState.subjectId != null) {
      val duration = completedState.pomodoroTargetSeconds.coerceAtLeast(1L)
      val endMillis =
        maxOf(currentTimeProvider(), completedState.sessionStartMillis + duration * 1000L)
      dataRepository.recordSession(
        subjectId = completedState.subjectId,
        startTimeMillis = completedState.sessionStartMillis,
        endTimeMillis = endMillis,
        durationSeconds = duration,
        isPomodoro = true,
      )
      dataRepository.incrementCompletedPomodoros(todayKey)

      val completedToday = (latestPersistedState.completedPomodorosByDate[todayKey] ?: 0) + 1
      val nextPhase =
        if (completedToday % config.cyclesBeforeLongBreak == 0) {
          PomodoroPhase.LONG_BREAK
        } else {
          PomodoroPhase.SHORT_BREAK
        }
      val nextTargetSeconds =
        if (nextPhase == PomodoroPhase.LONG_BREAK) {
          config.longBreakMinutes * 60L
        } else {
          config.shortBreakMinutes * 60L
        }
      val alarm =
        if (config.alarmEnabled) {
          PomodoroAlarmState(
            id = currentTimeProvider(),
            type = PomodoroAlarmType.FOCUS_COMPLETED,
            completedPhase = PomodoroPhase.FOCUS,
            nextPhase = nextPhase,
            subjectId = completedState.subjectId,
          )
        } else {
          null
        }
      _activeTimer.value =
        ActiveTimerState(
          subjectId = null,
          timerMode = TimerMode.POMODORO,
          pomodoroPhase = nextPhase,
          pomodoroTargetSeconds = nextTargetSeconds,
          isBreakRunning = false,
          activeAlarm = alarm,
        )
    } else {
      // Terminó un descanso -> volver a fase de Enfoque
      val alarm =
        if (config.alarmEnabled) {
          PomodoroAlarmState(
            id = currentTimeProvider(),
            type = PomodoroAlarmType.BREAK_COMPLETED,
            completedPhase = completedState.pomodoroPhase,
            nextPhase = PomodoroPhase.FOCUS,
            subjectId = null,
          )
        } else {
          null
        }
      _activeTimer.value =
        ActiveTimerState(
          subjectId = null,
          timerMode = TimerMode.POMODORO,
          pomodoroPhase = PomodoroPhase.FOCUS,
          pomodoroTargetSeconds = config.focusMinutes * 60L,
          isBreakRunning = false,
          activeAlarm = alarm,
        )
    }
  }

  fun dismissAlarm() {
    if (_activeTimer.value.activeAlarm != null) {
      _activeTimer.value = _activeTimer.value.copy(activeAlarm = null)
    }
  }

  fun stopCurrentTimer() {
    tickerJob?.cancel()
    tickerJob = null
    val current = _activeTimer.value
    val config = latestPersistedState.pomodoroConfig

    if (current.subjectId != null && !current.isBreakRunning) {
      val endMillis =
        maxOf(currentTimeProvider(), current.sessionStartMillis + current.elapsedSeconds * 1000L)
      val durationSeconds =
        ((endMillis - current.sessionStartMillis) / 1000L).coerceAtLeast(current.elapsedSeconds)
      if (durationSeconds >= 1L) {
        dataRepository.recordSession(
          subjectId = current.subjectId,
          startTimeMillis = current.sessionStartMillis,
          endTimeMillis = endMillis,
          durationSeconds = durationSeconds,
          isPomodoro = current.timerMode == TimerMode.POMODORO,
        )
      }
    }

    val resetTarget =
      when (current.pomodoroPhase) {
        PomodoroPhase.FOCUS -> config.focusMinutes * 60L
        PomodoroPhase.SHORT_BREAK -> config.shortBreakMinutes * 60L
        PomodoroPhase.LONG_BREAK -> config.longBreakMinutes * 60L
      }

    _activeTimer.value =
      ActiveTimerState(
        subjectId = null,
        timerMode = current.timerMode,
        pomodoroPhase = current.pomodoroPhase,
        pomodoroTargetSeconds = resetTarget,
        isBreakRunning = false,
      )
  }

  fun skipPomodoroPhase() {
    val current = _activeTimer.value
    if (current.isRunning) {
      stopCurrentTimer()
    }
    val config = latestPersistedState.pomodoroConfig
    val nextPhase =
      when (current.pomodoroPhase) {
        PomodoroPhase.FOCUS -> PomodoroPhase.SHORT_BREAK
        PomodoroPhase.SHORT_BREAK,
        PomodoroPhase.LONG_BREAK -> PomodoroPhase.FOCUS
      }
    val nextTarget =
      when (nextPhase) {
        PomodoroPhase.FOCUS -> config.focusMinutes * 60L
        PomodoroPhase.SHORT_BREAK -> config.shortBreakMinutes * 60L
        PomodoroPhase.LONG_BREAK -> config.longBreakMinutes * 60L
      }
    _activeTimer.value =
      ActiveTimerState(
        subjectId = null,
        timerMode = TimerMode.POMODORO,
        pomodoroPhase = nextPhase,
        pomodoroTargetSeconds = nextTarget,
        isBreakRunning = false,
      )
  }

  fun resetPomodoro() {
    if (_activeTimer.value.isRunning) {
      stopCurrentTimer()
    }
    val config = latestPersistedState.pomodoroConfig
    _activeTimer.value =
      ActiveTimerState(
        subjectId = null,
        timerMode = TimerMode.POMODORO,
        pomodoroPhase = PomodoroPhase.FOCUS,
        pomodoroTargetSeconds = config.focusMinutes * 60L,
        isBreakRunning = false,
      )
  }

  fun setTimerMode(mode: TimerMode) {
    if (_activeTimer.value.isRunning) {
      stopCurrentTimer()
    }
    dataRepository.updateTimerMode(mode)
    val config = latestPersistedState.pomodoroConfig
    _activeTimer.value =
      _activeTimer.value.copy(
        timerMode = mode,
        pomodoroPhase = PomodoroPhase.FOCUS,
        pomodoroTargetSeconds = config.focusMinutes * 60L,
        isBreakRunning = false,
      )
  }

  fun setAppTheme(theme: AppTheme) {
    dataRepository.updateTheme(theme)
  }

  fun toggleHeaderCollapsed() {
    val current = latestPersistedState.isHeaderCollapsed
    dataRepository.updateHeaderCollapsed(!current)
  }

  fun setHeaderCollapsed(collapsed: Boolean) {
    dataRepository.updateHeaderCollapsed(collapsed)
  }

  fun updatePomodoroConfig(
    focusMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int = 15,
    alarmEnabled: Boolean = latestPersistedState.pomodoroConfig.alarmEnabled,
  ) {
    dataRepository.updatePomodoroConfig(
      focusMinutes = focusMinutes,
      shortBreakMinutes = shortBreakMinutes,
      longBreakMinutes = longBreakMinutes,
      alarmEnabled = alarmEnabled,
    )
    val current = _activeTimer.value
    if (!current.isRunning) {
      val updatedTarget =
        when (current.pomodoroPhase) {
          PomodoroPhase.FOCUS -> focusMinutes.coerceIn(1, 180) * 60L
          PomodoroPhase.SHORT_BREAK -> shortBreakMinutes.coerceIn(1, 60) * 60L
          PomodoroPhase.LONG_BREAK -> longBreakMinutes.coerceIn(1, 90) * 60L
        }
      _activeTimer.value = current.copy(pomodoroTargetSeconds = updatedTarget)
    }
  }

  fun updateDailyGoal(minutes: Int) {
    dataRepository.updateDailyGoal(minutes)
  }

  fun setOledFocusMode(enabled: Boolean) {
    if (_activeTimer.value.isRunning) {
      _activeTimer.value = _activeTimer.value.copy(isOledFocusMode = enabled)
    }
  }

  fun addSubject(name: String, colorHex: Long, targetMinutesPerDay: Int) {
    dataRepository.addSubject(name, colorHex, targetMinutesPerDay)
  }

  fun updateSubject(
    subjectId: String,
    name: String,
    colorHex: Long,
    targetMinutesPerDay: Int,
  ) {
    dataRepository.updateSubject(subjectId, name, colorHex, targetMinutesPerDay)
  }

  fun deleteSubject(subjectId: String) {
    if (_activeTimer.value.subjectId == subjectId) {
      stopCurrentTimer()
    }
    dataRepository.deleteSubject(subjectId)
  }

  fun updateSessionEndTime(sessionId: String, newEndTimeMillis: Long) {
    val maxAllowedEnd = currentTimeProvider()
    val clampedEnd =
      if (newEndTimeMillis > maxAllowedEnd) {
        maxAllowedEnd
      } else {
        newEndTimeMillis
      }
    dataRepository.updateSessionEndTime(sessionId, clampedEnd)
  }

  fun deleteSession(sessionId: String) {
    dataRepository.deleteSession(sessionId)
  }

  fun selectSessionForTenMinuteBlock(hour: Int, blockIdx: Int): String? {
    val active = _activeTimer.value
    if (active.subjectId != null && !active.isBreakRunning) {
      val liveEnd =
        maxOf(currentTimeProvider(), active.sessionStartMillis + active.elapsedSeconds * 1000L)
      val liveBlocks = getTenMinuteBlocksForRange(active.sessionStartMillis, liveEnd)
      if ((hour to blockIdx) in liveBlocks) {
        stopCurrentTimer()
      }
    }
    val todayKey =
      SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(currentTimeProvider()))
    val todaySessions = latestPersistedState.sessions.filter { it.dateKey == todayKey }
    return findSessionForTenMinuteBlock(todaySessions, hour, blockIdx)?.id
  }

  private fun buildDashboardState(
    persisted: PersistedStudyState,
    timer: ActiveTimerState,
  ): YptDashboardState {
    val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(currentTimeProvider()))
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val todaySessions = persisted.sessions.filter { it.dateKey == todayKey }
    val subjectMap = persisted.subjects.associateBy { it.id }

    val subjectSeconds = mutableMapOf<String, Long>()
    persisted.subjects.forEach { subjectSeconds[it.id] = 0L }

    todaySessions.forEach { session ->
      subjectSeconds[session.subjectId] =
        (subjectSeconds[session.subjectId] ?: 0L) + session.durationSeconds
    }

    if (timer.subjectId != null && !timer.isBreakRunning) {
      subjectSeconds[timer.subjectId] =
        (subjectSeconds[timer.subjectId] ?: 0L) + timer.elapsedSeconds
    }

    val totalSecondsToday = subjectSeconds.values.sum()

    val maxSavedSession = todaySessions.maxOfOrNull { it.durationSeconds } ?: 0L
    val liveStudySeconds = if (timer.isStudying) timer.elapsedSeconds else 0L
    val maxContinuous = maxOf(maxSavedSession, liveStudySeconds)

    val firstStartMillis =
      listOfNotNull(
          todaySessions.minOfOrNull { it.startTimeMillis },
          timer.sessionStartMillis.takeIf { timer.isStudying },
        )
        .minOrNull()

    val lastEndMillis =
      if (timer.isStudying) {
        currentTimeProvider()
      } else {
        todaySessions.maxOfOrNull { it.endTimeMillis }
      }

    // Construir cuadrícula diaria (24h x 6 bloques de 10 minutos)
    val allSessionsWithLive =
      if (timer.subjectId != null && !timer.isBreakRunning && timer.elapsedSeconds > 0) {
        todaySessions +
          StudySession(
            id = "live",
            subjectId = timer.subjectId,
            startTimeMillis = timer.sessionStartMillis,
            endTimeMillis = timer.sessionStartMillis + timer.elapsedSeconds * 1000L,
            durationSeconds = timer.elapsedSeconds,
            dateKey = todayKey,
            isPomodoro = timer.timerMode == TimerMode.POMODORO,
          )
      } else {
        todaySessions
      }

    val tenMinBlocks = computeTenMinuteGrid(allSessionsWithLive, subjectMap)
    val completedPomodoros =
      persisted.completedPomodorosByDate[todayKey] ?: todaySessions.count { it.isPomodoro }

    return YptDashboardState(
      subjects = persisted.subjects,
      todaySessions = todaySessions,
      subjectSecondsToday = subjectSeconds,
      totalSecondsToday = totalSecondsToday,
      maxContinuousSecondsToday = maxContinuous,
      firstStartTimeFormatted = firstStartMillis?.let { timeFormat.format(Date(it)) } ?: "--:--",
      lastEndTimeFormatted = lastEndMillis?.let { timeFormat.format(Date(it)) } ?: "--:--",
      dailyGoalMinutes = persisted.dailyGoalMinutes,
      activeTimer = timer,
      activeAlarm = timer.activeAlarm,
      tenMinuteBlocks = tenMinBlocks,
      selectedTheme = persisted.selectedTheme,
      timerMode = persisted.preferredTimerMode,
      pomodoroConfig = persisted.pomodoroConfig,
      completedPomodorosToday = completedPomodoros,
      isHeaderCollapsed = persisted.isHeaderCollapsed,
    )
  }

  private fun computeTenMinuteGrid(
    sessions: List<StudySession>,
    subjectMap: Map<String, Subject>,
  ): Map<Int, List<Long?>> {
    val grid = mutableMapOf<Int, MutableList<Long?>>()
    for (hour in 0..23) {
      grid[hour] = MutableList(6) { null }
    }
    sessions.forEach { session ->
      val color = subjectMap[session.subjectId]?.colorHex ?: 0xFFFF6B4A
      getTenMinuteBlocksForRange(session.startTimeMillis, session.endTimeMillis).forEach {
        (hour, blockIdx) ->
        grid[hour]?.set(blockIdx, color)
      }
    }
    return grid
  }
}

internal fun getTenMinuteBlocksForRange(
  startTimeMillis: Long,
  endTimeMillis: Long,
): Set<Pair<Int, Int>> {
  val blocks = LinkedHashSet<Pair<Int, Int>>()
  val cal = Calendar.getInstance()
  val end = endTimeMillis.coerceAtLeast(startTimeMillis + 1000L)
  var cursor = startTimeMillis
  while (cursor <= end) {
    cal.timeInMillis = cursor
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    blocks.add(hour to (minute / 10).coerceIn(0, 5))
    cursor += 60_000L
  }
  cal.timeInMillis = end
  blocks.add(
    cal.get(Calendar.HOUR_OF_DAY) to (cal.get(Calendar.MINUTE) / 10).coerceIn(0, 5)
  )
  return blocks
}

internal fun findSessionForTenMinuteBlock(
  sessions: List<StudySession>,
  hour: Int,
  blockIdx: Int,
): StudySession? {
  return sessions.lastOrNull { session ->
    (hour to blockIdx) in getTenMinuteBlocksForRange(session.startTimeMillis, session.endTimeMillis)
  }
}

sealed interface MainScreenUiState {
  object Loading : MainScreenUiState

  data class Error(val throwable: Throwable) : MainScreenUiState

  data class Success(val data: List<String>) : MainScreenUiState
}
