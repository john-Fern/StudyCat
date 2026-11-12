package com.studyflow.ypt.ui.main

import com.studyflow.ypt.data.AppTheme
import com.studyflow.ypt.data.DefaultDataRepository
import com.studyflow.ypt.data.PomodoroPhase
import com.studyflow.ypt.data.SubjectColorPalette
import com.studyflow.ypt.data.TimerMode
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

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {

  @Test(timeout = 10_000L)
  fun initialState_loadsDefaultSubjectsAndDarkYptTheme() = runTest {
    val repository = DefaultDataRepository(context = null)
    val viewModel = MainScreenViewModel(repository, coroutineScope = backgroundScope)
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    val state = viewModel.yptState.value
    assertEquals(4, state.subjects.size)
    assertEquals(AppTheme.DARK_YPT, state.selectedTheme)
    assertEquals(TimerMode.STOPWATCH, state.timerMode)
    assertFalse(state.activeTimer.isRunning)
  }

  @Test(timeout = 10_000L)
  fun addAndDeleteSubject_updatesSubjectsList() = runTest {
    val repository = DefaultDataRepository(context = null)
    val viewModel = MainScreenViewModel(repository, coroutineScope = backgroundScope)
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    viewModel.addSubject("Cálculo Vectorial", 0xFF3B82F6, 90)
    runCurrent()

    val afterAdd = viewModel.yptState.value
    assertEquals(5, afterAdd.subjects.size)
    val added = afterAdd.subjects.find { it.name == "Cálculo Vectorial" }
    assertNotNull(added)
    assertEquals(90, added?.targetMinutesPerDay)

    viewModel.deleteSubject(added!!.id)
    runCurrent()
    assertEquals(4, viewModel.yptState.value.subjects.size)
  }

  @Test(timeout = 10_000L)
  fun updateSubject_modifiesAllSubjectAspects() = runTest {
    val repository = DefaultDataRepository(context = null)
    val viewModel = MainScreenViewModel(repository, coroutineScope = backgroundScope)
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    val targetSubject = viewModel.yptState.value.subjects.first()
    val subjectId = targetSubject.id
    val newName = "Arquitectura de Software Limpia"
    val newColor = 0xFFEC4899 // Rosa
    val newTargetMinutes = 180

    viewModel.updateSubject(
      subjectId = subjectId,
      name = newName,
      colorHex = newColor,
      targetMinutesPerDay = newTargetMinutes,
    )
    runCurrent()

    val updatedState = viewModel.yptState.value
    val updatedSubject = updatedState.subjects.find { it.id == subjectId }
    assertNotNull(updatedSubject)
    assertEquals(newName, updatedSubject?.name)
    assertEquals(newColor, updatedSubject?.colorHex)
    assertEquals(newTargetMinutes, updatedSubject?.targetMinutesPerDay)
  }

  @Test(timeout = 10_000L)
  fun updateSubject_withBlankName_ignoresUpdate() = runTest {
    val repository = DefaultDataRepository(context = null)
    val viewModel = MainScreenViewModel(repository, coroutineScope = backgroundScope)
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    val targetSubject = viewModel.yptState.value.subjects.first()
    val subjectId = targetSubject.id
    val originalName = targetSubject.name

    viewModel.updateSubject(
      subjectId = subjectId,
      name = "   ",
      colorHex = 0xFF123456,
      targetMinutesPerDay = 120,
    )
    runCurrent()

    val updatedSubject = viewModel.yptState.value.subjects.find { it.id == subjectId }
    assertEquals(originalName, updatedSubject?.name)
  }

  @Test(timeout = 10_000L)
  fun stopwatchTimer_startsCountsUpAndRecordsSessionOnStop() = runTest {
    val baseTime = 1_700_000_000_000L
    val repository = DefaultDataRepository(context = null)
    val viewModel =
      MainScreenViewModel(
        repository,
        currentTimeProvider = { baseTime + testScheduler.currentTime },
        coroutineScope = backgroundScope,
      )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    val subjectId = viewModel.yptState.value.subjects.first().id

    // Iniciar cronómetro
    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()
    assertTrue(viewModel.yptState.value.activeTimer.isRunning)
    assertEquals(subjectId, viewModel.yptState.value.activeTimer.subjectId)

    // Avanzar 10 segundos
    advanceTimeBy(10_000L)
    runCurrent()

    assertEquals(10L, viewModel.yptState.value.activeTimer.elapsedSeconds)
    assertEquals(10L, viewModel.yptState.value.totalSecondsToday)

    // Pausar / detener cronómetro y verificar que se guarda la sesión
    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()

    val stateAfterStop = viewModel.yptState.value
    assertFalse(stateAfterStop.activeTimer.isRunning)
    assertEquals(1, stateAfterStop.todaySessions.size)
    assertEquals(10L, stateAfterStop.todaySessions.first().durationSeconds)
    assertFalse(stateAfterStop.todaySessions.first().isPomodoro)
    assertEquals(10L, stateAfterStop.totalSecondsToday)
  }

  @Test(timeout = 10_000L)
  fun oledFocusMode_togglesOnlyWhenTimerIsRunning() = runTest {
    val baseTime = 1_700_000_000_000L
    val repository = DefaultDataRepository(context = null)
    val viewModel =
      MainScreenViewModel(
        repository,
        currentTimeProvider = { baseTime + testScheduler.currentTime },
        coroutineScope = backgroundScope,
      )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    // Si no está corriendo, no activa OLED
    viewModel.setOledFocusMode(true)
    assertFalse(viewModel.yptState.value.activeTimer.isOledFocusMode)

    // Al iniciar una materia, permite activar y salir del modo OLED
    val subjectId = viewModel.yptState.value.subjects.first().id
    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()

    viewModel.setOledFocusMode(true)
    runCurrent()
    assertTrue(viewModel.yptState.value.activeTimer.isOledFocusMode)

    viewModel.setOledFocusMode(false)
    runCurrent()
    assertFalse(viewModel.yptState.value.activeTimer.isOledFocusMode)
    viewModel.stopCurrentTimer()
    runCurrent()
  }

  @Test(timeout = 10_000L)
  fun themeSwitching_persistsAndProvidesDistinctPalettes() = runTest {
    val repository = DefaultDataRepository(context = null)
    val viewModel = MainScreenViewModel(repository, coroutineScope = backgroundScope)
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    for (theme in AppTheme.entries) {
      viewModel.setAppTheme(theme)
      runCurrent()
      assertEquals(theme, viewModel.yptState.value.selectedTheme)
      val palette = paletteForTheme(theme)
      assertNotNull(palette.bg)
      assertNotNull(palette.cardBg)
      assertNotNull(palette.headerBg)
      assertNotNull(palette.accent)
      assertNotNull(palette.textPrimary)
      assertNotNull(palette.textSecondary)
      assertNotNull(palette.subtleSurface)
    }
    assertTrue("Debe contar con al menos 10 temas visuales", AppTheme.entries.size >= 10)
  }

  @Test(timeout = 10_000L)
  fun subjectColorPalette_hasExtensiveOptions_andAllColorsPersistOnSubjects() = runTest {
    assertTrue("La paleta de materias debe tener al menos 20 opciones de color", SubjectColorPalette.size >= 20)

    val repository = DefaultDataRepository(context = null)
    val viewModel = MainScreenViewModel(repository, coroutineScope = backgroundScope)
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    val targetSubject = viewModel.yptState.value.subjects.first()

    // Probar que cada color de la paleta expandida se puede asignar y persiste
    SubjectColorPalette.take(5).forEach { colorHex ->
      viewModel.updateSubject(
        subjectId = targetSubject.id,
        name = "Materia Test",
        colorHex = colorHex,
        targetMinutesPerDay = 45,
      )
      runCurrent()
      val updated = viewModel.yptState.value.subjects.find { it.id == targetSubject.id }
      assertEquals(colorHex, updated?.colorHex)
    }
  }

  @Test(timeout = 10_000L)
  fun pomodoroMode_countsDownCompletesFocusAndTransitionsToBreak() = runTest {
    val baseTime = 1_700_000_000_000L
    val repository = DefaultDataRepository(context = null)
    val viewModel =
      MainScreenViewModel(
        repository,
        currentTimeProvider = { baseTime + testScheduler.currentTime },
        coroutineScope = backgroundScope,
      )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    // Cambiar a modo Pomodoro con configuración de 1 minuto enfoque / 1 minuto descanso
    viewModel.setTimerMode(TimerMode.POMODORO)
    viewModel.updatePomodoroConfig(focusMinutes = 1, shortBreakMinutes = 1, longBreakMinutes = 5)
    runCurrent()

    assertEquals(TimerMode.POMODORO, viewModel.yptState.value.timerMode)
    assertEquals(60L, viewModel.yptState.value.activeTimer.pomodoroTargetSeconds)

    val subjectId = viewModel.yptState.value.subjects.first().id
    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()

    // Avanzar 25 segundos del Pomodoro de 60s
    advanceTimeBy(25_000L)
    runCurrent()

    assertEquals(35L, viewModel.yptState.value.activeTimer.remainingPomodoroSeconds)
    assertEquals(25L, viewModel.yptState.value.totalSecondsToday)

    // Avanzar los 35 segundos restantes para completar el Pomodoro
    advanceTimeBy(35_000L)
    runCurrent()

    val afterFocus = viewModel.yptState.value
    assertFalse(afterFocus.activeTimer.isRunning)
    assertEquals(PomodoroPhase.SHORT_BREAK, afterFocus.activeTimer.pomodoroPhase)
    assertEquals(1, afterFocus.completedPomodorosToday)
    assertEquals(1, afterFocus.todaySessions.size)
    assertTrue(afterFocus.todaySessions.first().isPomodoro)
    assertEquals(60L, afterFocus.totalSecondsToday)

    // Iniciar el descanso corto y luego saltarlo para volver a Enfoque
    viewModel.togglePomodoroBreak()
    runCurrent()
    assertTrue(viewModel.yptState.value.activeTimer.isBreakRunning)

    viewModel.skipPomodoroPhase()
    runCurrent()
    assertFalse(viewModel.yptState.value.activeTimer.isRunning)
    assertEquals(PomodoroPhase.FOCUS, viewModel.yptState.value.activeTimer.pomodoroPhase)
  }

  @Test(timeout = 10_000L)
  fun deleteSession_removesRecordedBlockAndUpdatesTotals() = runTest {
    val baseTime = 1_700_000_000_000L
    val repository = DefaultDataRepository(context = null)
    val viewModel =
      MainScreenViewModel(
        repository,
        currentTimeProvider = { baseTime + testScheduler.currentTime },
        coroutineScope = backgroundScope,
      )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    val subjectId = viewModel.yptState.value.subjects.first().id
    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()

    advanceTimeBy(120_000L)
    runCurrent()

    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()

    val recordedSession = viewModel.yptState.value.todaySessions.single()
    assertEquals(120L, viewModel.yptState.value.totalSecondsToday)

    viewModel.deleteSession(recordedSession.id)
    runCurrent()

    assertTrue(viewModel.yptState.value.todaySessions.isEmpty())
    assertEquals(0L, viewModel.yptState.value.totalSecondsToday)
    assertEquals(0L, viewModel.yptState.value.subjectSecondsToday[subjectId])
  }

  @Test(timeout = 10_000L)
  fun updateSessionEndTime_adjustsEndTimeAndRecalculatesDurationAndTotals() = runTest {
    val baseTime = 1_700_000_000_000L
    val repository = DefaultDataRepository(context = null)
    val viewModel =
      MainScreenViewModel(
        repository,
        currentTimeProvider = { baseTime + testScheduler.currentTime },
        coroutineScope = backgroundScope,
      )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    val subjectId = viewModel.yptState.value.subjects.first().id
    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()

    advanceTimeBy(600_000L) // 10 minutos
    runCurrent()

    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()

    val session = viewModel.yptState.value.todaySessions.single()
    assertEquals(600L, session.durationSeconds)
    assertEquals(baseTime + 600_000L, session.endTimeMillis)

    // Ajustar tiempo de término a 3 minutos después del inicio
    val newEndMillis = session.startTimeMillis + 180_000L
    viewModel.updateSessionEndTime(session.id, newEndMillis)
    runCurrent()

    val updatedSession = viewModel.yptState.value.todaySessions.single()
    assertEquals(session.startTimeMillis, updatedSession.startTimeMillis)
    assertEquals(newEndMillis, updatedSession.endTimeMillis)
    assertEquals(180L, updatedSession.durationSeconds)
    assertEquals(180L, viewModel.yptState.value.totalSecondsToday)
    assertEquals(180L, viewModel.yptState.value.subjectSecondsToday[subjectId])
  }

  @Test(timeout = 10_000L)
  fun updateSessionEndTime_clampsToMinimumOneSecondAfterStart() = runTest {
    val baseTime = 1_700_000_000_000L
    val repository = DefaultDataRepository(context = null)
    val viewModel =
      MainScreenViewModel(
        repository,
        currentTimeProvider = { baseTime + testScheduler.currentTime },
        coroutineScope = backgroundScope,
      )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    val subjectId = viewModel.yptState.value.subjects.first().id
    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()

    advanceTimeBy(60_000L)
    runCurrent()

    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()

    val session = viewModel.yptState.value.todaySessions.single()
    viewModel.updateSessionEndTime(session.id, session.startTimeMillis - 5_000L)
    runCurrent()

    val updatedSession = viewModel.yptState.value.todaySessions.single()
    assertEquals(session.startTimeMillis + 1_000L, updatedSession.endTimeMillis)
    assertEquals(1L, updatedSession.durationSeconds)
    assertEquals(1L, viewModel.yptState.value.totalSecondsToday)
  }

  @Test(timeout = 10_000L)
  fun selectSessionForTenMinuteBlock_returnsMatchingSessionAndStopsLiveTimerIfNeeded() = runTest {
    val baseTime = 1_700_000_000_000L
    val repository = DefaultDataRepository(context = null)
    val viewModel =
      MainScreenViewModel(
        repository,
        currentTimeProvider = { baseTime + testScheduler.currentTime },
        coroutineScope = backgroundScope,
      )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    val subjectId = viewModel.yptState.value.subjects.first().id
    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()

    // Avanzar 45 segundos con el cronómetro aún corriendo
    advanceTimeBy(45_000L)
    runCurrent()

    val paintedBlocks = getTenMinuteBlocksForRange(baseTime, baseTime + 45_000L)
    assertTrue(paintedBlocks.isNotEmpty())
    val (hour, blockIdx) = paintedBlocks.first()

    // El bloque debe estar pintado en el timeline
    assertNotNull(viewModel.yptState.value.tenMinuteBlocks[hour]?.get(blockIdx))

    // Al hacer click en ese bloque del timeline mientras corre, detiene el timer y devuelve la sesión para editarla
    val selectedSessionId = viewModel.selectSessionForTenMinuteBlock(hour, blockIdx)
    runCurrent()

    assertNotNull(selectedSessionId)
    assertFalse(viewModel.yptState.value.activeTimer.isRunning)
    assertEquals(1, viewModel.yptState.value.todaySessions.size)
    assertEquals(selectedSessionId, viewModel.yptState.value.todaySessions.first().id)
  }

  @Test(timeout = 10_000L)
  fun pomodoroAlarm_triggersWhenFocusEndsAndWhenBreakEnds_andCanBeDismissed() = runTest {
    val baseTime = 1_700_000_000_000L
    val repository = DefaultDataRepository(context = null)
    val viewModel =
      MainScreenViewModel(
        repository,
        currentTimeProvider = { baseTime + testScheduler.currentTime },
        coroutineScope = backgroundScope,
      )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    viewModel.setTimerMode(TimerMode.POMODORO)
    viewModel.updatePomodoroConfig(focusMinutes = 1, shortBreakMinutes = 1, longBreakMinutes = 5)
    runCurrent()

    val subjectId = viewModel.yptState.value.subjects.first().id
    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()
    assertNull(viewModel.yptState.value.activeAlarm)

    // Completar el Pomodoro de 1 minuto (60s)
    advanceTimeBy(60_000L)
    runCurrent()

    val alarmAfterFocus = viewModel.yptState.value.activeAlarm
    assertNotNull(alarmAfterFocus)
    assertEquals(PomodoroAlarmType.FOCUS_COMPLETED, alarmAfterFocus?.type)
    assertEquals(PomodoroPhase.FOCUS, alarmAfterFocus?.completedPhase)
    assertEquals(PomodoroPhase.SHORT_BREAK, alarmAfterFocus?.nextPhase)
    assertEquals(subjectId, alarmAfterFocus?.subjectId)

    // Silenciar alarma manualmente
    viewModel.dismissAlarm()
    runCurrent()
    assertNull(viewModel.yptState.value.activeAlarm)

    // Iniciar descanso corto (1 minuto = 60s) y avanzar hasta que termine
    viewModel.togglePomodoroBreak()
    runCurrent()
    assertTrue(viewModel.yptState.value.activeTimer.isBreakRunning)

    advanceTimeBy(60_000L)
    runCurrent()

    val alarmAfterBreak = viewModel.yptState.value.activeAlarm
    assertNotNull(alarmAfterBreak)
    assertEquals(PomodoroAlarmType.BREAK_COMPLETED, alarmAfterBreak?.type)
    assertEquals(PomodoroPhase.SHORT_BREAK, alarmAfterBreak?.completedPhase)
    assertEquals(PomodoroPhase.FOCUS, alarmAfterBreak?.nextPhase)

    viewModel.dismissAlarm()
    runCurrent()
    assertNull(viewModel.yptState.value.activeAlarm)
  }

  @Test(timeout = 10_000L)
  fun pomodoroAlarm_doesNotTriggerWhenDisabledInConfig() = runTest {
    val baseTime = 1_700_000_000_000L
    val repository = DefaultDataRepository(context = null)
    val viewModel =
      MainScreenViewModel(
        repository,
        currentTimeProvider = { baseTime + testScheduler.currentTime },
        coroutineScope = backgroundScope,
      )
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    viewModel.setTimerMode(TimerMode.POMODORO)
    viewModel.updatePomodoroConfig(
      focusMinutes = 1,
      shortBreakMinutes = 1,
      longBreakMinutes = 5,
      alarmEnabled = false,
    )
    runCurrent()

    val subjectId = viewModel.yptState.value.subjects.first().id
    viewModel.toggleSubjectTimer(subjectId)
    runCurrent()

    advanceTimeBy(60_000L)
    runCurrent()

    assertNull(viewModel.yptState.value.activeAlarm)
  }

  @Test(timeout = 10_000L)
  fun formatDurationHelpers_formatCorrectly() {
    assertEquals("00:00:00", formatDurationHms(0L))
    assertEquals("01:05:09", formatDurationHms(3909L))
    assertEquals("25:00", formatDurationMmSs(1500L))
    assertEquals("01:02:03", formatDurationMmSs(3723L))
    assertEquals("45m", formatDurationShort(2700L))
    assertEquals("2h 15m", formatDurationShort(8100L))
  }

  @Test(timeout = 10_000L)
  fun toggleHeaderCollapsed_persistsAndTogglesState() = runTest {
    val repository = DefaultDataRepository(context = null)
    val viewModel = MainScreenViewModel(repository, coroutineScope = backgroundScope)
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.yptState.collect {} }
    runCurrent()

    assertFalse(viewModel.yptState.value.isHeaderCollapsed)

    viewModel.toggleHeaderCollapsed()
    runCurrent()

    assertTrue(viewModel.yptState.value.isHeaderCollapsed)
    assertTrue(repository.currentStudyState.isHeaderCollapsed)

    viewModel.toggleHeaderCollapsed()
    runCurrent()

    assertFalse(viewModel.yptState.value.isHeaderCollapsed)
    assertFalse(repository.currentStudyState.isHeaderCollapsed)
  }
}
