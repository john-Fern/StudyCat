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
import com.studyflow.ypt.ui.main.MainScreenUiState.Success
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ActiveTimerState(
  val subjectId: String? = null,
  val sessionStartMillis: Long = 0L,
  val elapsedSeconds: Long = 0L,
  val isOledFocusMode: Boolean = false,
  val timerMode: TimerMode = TimerMode.STOPWATCH,
  val pomodoroPhase: PomodoroPhase = PomodoroPhase.FOCUS,
  val pomodoroTargetSeconds: Long = 25 * 60L,
  val isBreakRunning: Boolean = false,
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
  // 24 horas (0..23) -> 6 bloques de 10 minutos cada una -> colorHex de la materia dominante
  val tenMinuteBlocks: Map<Int, List<Long?>> = emptyMap(),
  val selectedTheme: AppTheme = AppTheme.DARK_YPT,
  val timerMode: TimerMode = TimerMode.STOPWATCH,
  val pomodoroConfig: PomodoroConfig = PomodoroConfig(),
  val completedPomodorosToday: Int = 0,
)

class MainScreenViewModel(
  private val dataRepository: DataRepository,
  private val currentTimeProvider: () -> Long = { System.currentTimeMillis() },
  coroutineScope: CoroutineScope? = null,
) : ViewModel() {
  private val scope: CoroutineScope = coroutineScope ?: viewModelScope

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
      _activeTimer.value =
        ActiveTimerState(
          subjectId = null,
          timerMode = TimerMode.POMODORO,
          pomodoroPhase = nextPhase,
          pomodoroTargetSeconds = nextTargetSeconds,
          isBreakRunning = false,
        )
    } else {
      // Terminó un descanso -> volver a fase de Enfoque
      _activeTimer.value =
        ActiveTimerState(
          subjectId = null,
          timerMode = TimerMode.POMODORO,
          pomodoroPhase = PomodoroPhase.FOCUS,
          pomodoroTargetSeconds = config.focusMinutes * 60L,
          isBreakRunning = false,
        )
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

  fun updatePomodoroConfig(
    focusMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int = 15,
  ) {
    dataRepository.updatePomodoroConfig(focusMinutes, shortBreakMinutes, longBreakMinutes)
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
      tenMinuteBlocks = tenMinBlocks,
      selectedTheme = persisted.selectedTheme,
      timerMode = persisted.preferredTimerMode,
      pomodoroConfig = persisted.pomodoroConfig,
      completedPomodorosToday = completedPomodoros,
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
    val cal = Calendar.getInstance()
    sessions.forEach { session ->
      val color = subjectMap[session.subjectId]?.colorHex ?: 0xFFFF6B4A
      var cursor = session.startTimeMillis
      val end = session.endTimeMillis.coerceAtLeast(cursor + 1000L)
      while (cursor <= end) {
        cal.timeInMillis = cursor
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val blockIdx = (minute / 10).coerceIn(0, 5)
        grid[hour]?.set(blockIdx, color)
        cursor += 60_000L // avanzar de minuto en minuto
      }
    }
    return grid
  }
}

sealed interface MainScreenUiState {
  object Loading : MainScreenUiState

  data class Error(val throwable: Throwable) : MainScreenUiState

  data class Success(val data: List<String>) : MainScreenUiState
}
