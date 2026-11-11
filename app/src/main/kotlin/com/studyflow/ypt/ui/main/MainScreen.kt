package com.studyflow.ypt.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.studyflow.ypt.data.AppTheme
import com.studyflow.ypt.data.DefaultDataRepository
import com.studyflow.ypt.data.PomodoroConfig
import com.studyflow.ypt.data.PomodoroPhase
import com.studyflow.ypt.data.Subject
import com.studyflow.ypt.data.SubjectColorPalette
import com.studyflow.ypt.data.TimerMode
import com.studyflow.ypt.ui.theme.StudyFlowTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StudyCatPalette(
  val bg: Color,
  val cardBg: Color,
  val headerBg: Color,
  val accent: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val subtleSurface: Color,
)

internal fun paletteForTheme(theme: AppTheme): StudyCatPalette =
  when (theme) {
    AppTheme.DARK_YPT ->
      StudyCatPalette(
        bg = Color(0xFF0F1117),
        cardBg = Color(0xFF1A1D27),
        headerBg = Color(0xFF161923),
        accent = Color(0xFFFF6B4A),
        textPrimary = Color(0xFFF3F4F6),
        textSecondary = Color(0xFF9CA3AF),
        subtleSurface = Color.White.copy(alpha = 0.06f),
      )
    AppTheme.PASTEL_CAT ->
      StudyCatPalette(
        bg = Color(0xFFFFF7F9),
        cardBg = Color(0xFFFFFFFF),
        headerBg = Color(0xFFFFEAF0),
        accent = Color(0xFFEC4899),
        textPrimary = Color(0xFF3B232E),
        textSecondary = Color(0xFF85586F),
        subtleSurface = Color(0xFFEC4899).copy(alpha = 0.10f),
      )
    AppTheme.LIGHT_PAPER ->
      StudyCatPalette(
        bg = Color(0xFFF4F6FA),
        cardBg = Color(0xFFFFFFFF),
        headerBg = Color(0xFFE6ECF5),
        accent = Color(0xFF2563EB),
        textPrimary = Color(0xFF1E293B),
        textSecondary = Color(0xFF64748B),
        subtleSurface = Color(0xFF1E293B).copy(alpha = 0.07f),
      )
    AppTheme.MIDNIGHT_CYBER ->
      StudyCatPalette(
        bg = Color(0xFF070B14),
        cardBg = Color(0xFF111827),
        headerBg = Color(0xFF0D1322),
        accent = Color(0xFF06B6D4),
        textPrimary = Color(0xFFF1F5F9),
        textSecondary = Color(0xFF94A3B8),
        subtleSurface = Color.White.copy(alpha = 0.06f),
      )
    AppTheme.MATCHA_FOREST ->
      StudyCatPalette(
        bg = Color(0xFF0B1410),
        cardBg = Color(0xFF14241D),
        headerBg = Color(0xFF0F1C16),
        accent = Color(0xFF10B981),
        textPrimary = Color(0xFFECFDF5),
        textSecondary = Color(0xFFA7F3D0).copy(alpha = 0.72f),
        subtleSurface = Color.White.copy(alpha = 0.06f),
      )
  }

private val LocalPalette = compositionLocalOf { paletteForTheme(AppTheme.DARK_YPT) }

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current.applicationContext
  val viewModel: MainScreenViewModel = viewModel {
    MainScreenViewModel(DefaultDataRepository(context))
  }
  val yptState by viewModel.yptState.collectAsStateWithLifecycle()

  // Obliga al celular a NO apagar la pantalla mientras el cronómetro o pomodoro esté activo
  KeepScreenOn(enabled = yptState.activeTimer.isRunning)

  if (yptState.activeTimer.isRunning && yptState.activeTimer.isOledFocusMode) {
    val activeSubject = yptState.subjects.find { it.id == yptState.activeTimer.subjectId }
    val isPomodoro = yptState.activeTimer.timerMode == TimerMode.POMODORO
    val displaySeconds =
      if (isPomodoro) {
        yptState.activeTimer.remainingPomodoroSeconds
      } else {
        yptState.activeTimer.elapsedSeconds
      }
    OledFocusScreen(
      subjectName =
        if (yptState.activeTimer.isBreakRunning) {
          "Descanso Pomodoro 🐾"
        } else {
          activeSubject?.name ?: "Estudiando"
        },
      subjectColor = Color(activeSubject?.colorHex ?: 0xFFFF6B4A),
      sessionSeconds = displaySeconds,
      totalTodaySeconds = yptState.totalSecondsToday,
      isPomodoro = isPomodoro,
      onExitOled = { viewModel.setOledFocusMode(false) },
      onPauseTimer = { viewModel.stopCurrentTimer() },
    )
  } else {
    YptScaffold(
      state = yptState,
      onToggleSubject = viewModel::toggleSubjectTimer,
      onAddSubject = viewModel::addSubject,
      onUpdateSubject = viewModel::updateSubject,
      onDeleteSubject = viewModel::deleteSubject,
      onEnterOledMode = { viewModel.setOledFocusMode(true) },
      onSelectTheme = viewModel::setAppTheme,
      onSelectTimerMode = viewModel::setTimerMode,
      onTogglePomodoroBreak = viewModel::togglePomodoroBreak,
      onSkipPomodoroPhase = viewModel::skipPomodoroPhase,
      onResetPomodoro = viewModel::resetPomodoro,
      onUpdatePomodoroConfig = viewModel::updatePomodoroConfig,
      modifier = modifier,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun YptScaffold(
  state: YptDashboardState,
  onToggleSubject: (String) -> Unit,
  onAddSubject: (String, Long, Int) -> Unit,
  onUpdateSubject: (String, String, Long, Int) -> Unit = { _, _, _, _ -> },
  onDeleteSubject: (String) -> Unit,
  onEnterOledMode: () -> Unit,
  onSelectTheme: (AppTheme) -> Unit = {},
  onSelectTimerMode: (TimerMode) -> Unit = {},
  onTogglePomodoroBreak: () -> Unit = {},
  onSkipPomodoroPhase: () -> Unit = {},
  onResetPomodoro: () -> Unit = {},
  onUpdatePomodoroConfig: (Int, Int, Int) -> Unit = { _, _, _ -> },
  modifier: Modifier = Modifier,
) {
  val palette = remember(state.selectedTheme) { paletteForTheme(state.selectedTheme) }
  var selectedTab by rememberSaveable { mutableIntStateOf(0) }
  var showAddDialog by rememberSaveable { mutableStateOf(false) }
  var editingSubjectId by rememberSaveable { mutableStateOf<String?>(null) }
  var showThemeDialog by rememberSaveable { mutableStateOf(false) }
  var showPomodoroConfigDialog by rememberSaveable { mutableStateOf(false) }

  CompositionLocalProvider(LocalPalette provides palette) {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      containerColor = palette.bg,
      floatingActionButton = {
        if (selectedTab == 0) {
          FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = palette.accent,
            contentColor = Color.White,
          ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir materia")
          }
        }
      },
      bottomBar = {
        NavigationBar(containerColor = palette.headerBg) {
          NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            icon = { Icon(Icons.Default.Timer, contentDescription = "Estudio") },
            label = { Text("Estudio") },
            colors =
              NavigationBarItemDefaults.colors(
                selectedIconColor = palette.accent,
                selectedTextColor = palette.accent,
                unselectedIconColor = palette.textSecondary,
                unselectedTextColor = palette.textSecondary,
                indicatorColor = palette.accent.copy(alpha = 0.15f),
              ),
          )
          NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Estadísticas") },
            label = { Text("Estadísticas") },
            colors =
              NavigationBarItemDefaults.colors(
                selectedIconColor = palette.accent,
                selectedTextColor = palette.accent,
                unselectedIconColor = palette.textSecondary,
                unselectedTextColor = palette.textSecondary,
                indicatorColor = palette.accent.copy(alpha = 0.15f),
              ),
          )
        }
      },
    ) { innerPadding ->
      if (selectedTab == 0) {
        TimerTabContent(
          state = state,
          onToggleSubject = onToggleSubject,
          onEditSubject = { id -> editingSubjectId = id },
          onDeleteSubject = onDeleteSubject,
          onEnterOledMode = onEnterOledMode,
          onOpenThemeSelector = { showThemeDialog = true },
          onSelectTimerMode = onSelectTimerMode,
          onTogglePomodoroBreak = onTogglePomodoroBreak,
          onSkipPomodoroPhase = onSkipPomodoroPhase,
          onResetPomodoro = onResetPomodoro,
          onOpenPomodoroConfig = { showPomodoroConfigDialog = true },
          modifier = Modifier.padding(innerPadding),
        )
      } else {
        StatsTabContent(
          state = state,
          onOpenThemeSelector = { showThemeDialog = true },
          modifier = Modifier.padding(innerPadding),
        )
      }
    }

    if (showAddDialog) {
      AddSubjectDialog(
        onDismiss = { showAddDialog = false },
        onConfirm = { name, colorHex, targetMins ->
          onAddSubject(name, colorHex, targetMins)
          showAddDialog = false
        },
      )
    }

    val subjectToEdit = state.subjects.find { it.id == editingSubjectId }
    if (subjectToEdit != null) {
      EditSubjectDialog(
        subject = subjectToEdit,
        onDismiss = { editingSubjectId = null },
        onConfirm = { name, colorHex, targetMins ->
          onUpdateSubject(subjectToEdit.id, name, colorHex, targetMins)
          editingSubjectId = null
        },
      )
    }

    if (showThemeDialog) {
      ThemeSelectorDialog(
        currentTheme = state.selectedTheme,
        onSelectTheme = { theme ->
          onSelectTheme(theme)
          showThemeDialog = false
        },
        onDismiss = { showThemeDialog = false },
      )
    }

    if (showPomodoroConfigDialog) {
      PomodoroConfigDialog(
        currentConfig = state.pomodoroConfig,
        onDismiss = { showPomodoroConfigDialog = false },
        onConfirm = { focus, shortBrk, longBrk ->
          onUpdatePomodoroConfig(focus, shortBrk, longBrk)
          showPomodoroConfigDialog = false
        },
      )
    }
  }
}

@Composable
private fun TimerTabContent(
  state: YptDashboardState,
  onToggleSubject: (String) -> Unit,
  onEditSubject: (String) -> Unit,
  onDeleteSubject: (String) -> Unit,
  onEnterOledMode: () -> Unit,
  onOpenThemeSelector: () -> Unit,
  onSelectTimerMode: (TimerMode) -> Unit,
  onTogglePomodoroBreak: () -> Unit,
  onSkipPomodoroPhase: () -> Unit,
  onResetPomodoro: () -> Unit,
  onOpenPomodoroConfig: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalPalette.current
  val activeSubject = state.subjects.find { it.id == state.activeTimer.subjectId }
  val isPomodoro = state.timerMode == TimerMode.POMODORO
  val dateFormatted = remember {
    SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"))
      .format(Date())
      .replaceFirstChar { it.uppercase() }
  }

  Column(modifier = modifier.fillMaxSize()) {
    // Cabecera superior estilo StudyCat / YPT
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors =
                if (state.activeTimer.isRunning) {
                  listOf(
                    Color(activeSubject?.colorHex ?: 0xFFFF6B4A).copy(alpha = 0.25f),
                    palette.headerBg,
                  )
                } else {
                  listOf(palette.headerBg, palette.bg)
                }
            )
          )
          .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        // Fila superior: Identidad StudyCat + Botón Tema + Estado de Pantalla Activa
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              color = palette.accent.copy(alpha = 0.16f),
              shape = CircleShape,
            ) {
              Text(
                text = if (state.activeTimer.isRunning) "🐱" else "🐾",
                fontSize = 16.sp,
                modifier = Modifier.padding(6.dp),
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "StudyCat",
                color = palette.textPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
              )
              Text(
                text = dateFormatted,
                color = palette.textSecondary,
                fontSize = 11.sp,
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            // Botón para cambiar el Tema
            Surface(
              color = palette.subtleSurface,
              shape = RoundedCornerShape(50),
              modifier =
                Modifier.clip(RoundedCornerShape(50))
                  .clickable { onOpenThemeSelector() }
                  .semantics { contentDescription = "Cambiar tema" },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = null,
                  tint = palette.accent,
                  modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Tema",
                  color = palette.textPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }

            // Indicador de pantalla siempre encendida
            Surface(
              color =
                if (state.activeTimer.isRunning) {
                  Color(0xFF10B981).copy(alpha = 0.2f)
                } else {
                  palette.subtleSurface
                },
              shape = RoundedCornerShape(50),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Default.LightMode,
                  contentDescription = null,
                  tint = if (state.activeTimer.isRunning) Color(0xFF10B981) else palette.textSecondary,
                  modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text =
                    if (state.activeTimer.isRunning) {
                      "Pantalla siempre activa"
                    } else {
                      "En espera"
                    },
                  color = if (state.activeTimer.isRunning) Color(0xFF10B981) else palette.textSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selector de Modo: Cronómetro vs Pomodoro
        Row(
          modifier =
            Modifier.clip(RoundedCornerShape(50))
              .background(palette.cardBg)
              .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          TimerModeChip(
            label = "⏱️ Cronómetro",
            selected = state.timerMode == TimerMode.STOPWATCH,
            onClick = { onSelectTimerMode(TimerMode.STOPWATCH) },
          )
          TimerModeChip(
            label = "🍅 Pomodoro",
            selected = state.timerMode == TimerMode.POMODORO,
            onClick = { onSelectTimerMode(TimerMode.POMODORO) },
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!isPomodoro) {
          // MODO CRONÓMETRO (Clásico YPT)
          Text(
            text = formatDurationHms(state.totalSecondsToday),
            color = palette.textPrimary,
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
          )
        } else {
          // MODO POMODORO
          val phase = state.activeTimer.pomodoroPhase
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(
              color = palette.accent.copy(alpha = 0.16f),
              shape = RoundedCornerShape(50),
            ) {
              Text(
                text =
                  when (phase) {
                    PomodoroPhase.FOCUS -> "🍅 Fase: Enfoque"
                    PomodoroPhase.SHORT_BREAK -> "☕ Descanso Corto"
                    PomodoroPhase.LONG_BREAK -> "🌙 Descanso Largo"
                  },
                color = palette.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
              )
            }

            Surface(
              color = palette.subtleSurface,
              shape = RoundedCornerShape(50),
              modifier =
                Modifier.clip(RoundedCornerShape(50))
                  .clickable { onOpenPomodoroConfig() }
                  .semantics { contentDescription = "Configurar Pomodoro" },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Default.Settings,
                  contentDescription = null,
                  tint = palette.textSecondary,
                  modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${state.pomodoroConfig.focusMinutes}/${state.pomodoroConfig.shortBreakMinutes}m",
                  color = palette.textSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Reloj regresivo del Pomodoro
          Text(
            text = formatDurationMmSs(state.activeTimer.remainingPomodoroSeconds),
            color = palette.textPrimary,
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
          )

          Text(
            text = "Total hoy: ${formatDurationHms(state.totalSecondsToday)} · 🐾 ${state.completedPomodorosToday} pomodoros",
            color = palette.textSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Controles rápidos de fase Pomodoro
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (phase != PomodoroPhase.FOCUS) {
              Button(
                onClick = onTogglePomodoroBreak,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
              ) {
                Icon(
                  imageVector =
                    if (state.activeTimer.isBreakRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text =
                    if (state.activeTimer.isBreakRunning) "Pausar descanso" else "Iniciar descanso",
                  fontSize = 12.sp,
                )
              }
            }

            OutlinedButton(
              onClick = onSkipPomodoroPhase,
              shape = RoundedCornerShape(50),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
              Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = null,
                tint = palette.textPrimary,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (phase == PomodoroPhase.FOCUS) "Saltar a descanso" else "Volver a enfoque",
                color = palette.textPrimary,
                fontSize = 11.sp,
              )
            }

            IconButton(onClick = onResetPomodoro, modifier = Modifier.size(32.dp)) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reiniciar Pomodoro",
                tint = palette.textSecondary,
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }

        // Materia activa + botón Modo OLED
        AnimatedVisibility(visible = state.activeTimer.isRunning) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(6.dp))
            if (state.activeTimer.isStudying) {
              Text(
                text =
                  "🐱 Estudiando: ${activeSubject?.name ?: ""} (${formatDurationHms(state.activeTimer.elapsedSeconds)})",
                color = Color(activeSubject?.colorHex ?: 0xFFFF6B4A),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
              )
            } else {
              Text(
                text = "😸 Gatito tomando un descanso...",
                color = palette.accent,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
              onClick = onEnterOledMode,
              shape = RoundedCornerShape(50),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            ) {
              Icon(
                Icons.Default.Bedtime,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = palette.textPrimary,
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Modo Pantalla Negra (OLED)", color = palette.textPrimary, fontSize = 12.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Barra de métricas rápidas estilo YPT
        val goalSeconds = (state.dailyGoalMinutes * 60L).coerceAtLeast(1L)
        val progressPercent = ((state.totalSecondsToday * 100L) / goalSeconds).coerceAtMost(100L)

        Row(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(palette.cardBg)
              .padding(vertical = 10.dp, horizontal = 8.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
          QuickMetricItem(label = "Inicio", value = state.firstStartTimeFormatted)
          QuickMetricItem(label = "Fin", value = state.lastEndTimeFormatted)
          QuickMetricItem(
            label = "Máx. Enfoque",
            value = formatDurationShort(state.maxContinuousSecondsToday),
          )
          QuickMetricItem(label = "Meta (${state.dailyGoalMinutes / 60}h)", value = "$progressPercent%")
        }
      }
    }

    // Lista de Materias
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "MATERIAS Y TAREAS",
            color = palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text =
              if (isPomodoro) {
                "Toca ▶ para iniciar Pomodoro en una materia"
              } else {
                "Toca ▶ para iniciar cronómetro"
              },
            color = palette.textSecondary,
            fontSize = 11.sp,
          )
        }
      }

      items(state.subjects, key = { it.id }) { subject ->
        val isRunningThis = state.activeTimer.subjectId == subject.id
        val secondsStudied = state.subjectSecondsToday[subject.id] ?: 0L
        SubjectRowCard(
          subject = subject,
          secondsStudied = secondsStudied,
          isRunning = isRunningThis,
          onTogglePlay = { onToggleSubject(subject.id) },
          onEdit = { onEditSubject(subject.id) },
          onDelete = { onDeleteSubject(subject.id) },
        )
      }

      item { Spacer(modifier = Modifier.height(72.dp)) }
    }
  }
}

@Composable
private fun TimerModeChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val palette = LocalPalette.current
  Box(
    modifier =
      Modifier.clip(RoundedCornerShape(50))
        .background(if (selected) palette.accent else Color.Transparent)
        .clickable { onClick() }
        .padding(horizontal = 16.dp, vertical = 7.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      color = if (selected) Color.White else palette.textSecondary,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
    )
  }
}

@Composable
private fun QuickMetricItem(label: String, value: String) {
  val palette = LocalPalette.current
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, color = palette.textSecondary, fontSize = 11.sp)
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = value,
      color = palette.textPrimary,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
    )
  }
}

@Composable
private fun SubjectRowCard(
  subject: Subject,
  secondsStudied: Long,
  isRunning: Boolean,
  onTogglePlay: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val palette = LocalPalette.current
  val subjectColor = Color(subject.colorHex)
  val targetSeconds = (subject.targetMinutesPerDay * 60L).coerceAtLeast(1L)
  val progress = (secondsStudied.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)

  Card(
    modifier =
      Modifier.fillMaxWidth()
        .then(
          if (isRunning) {
            Modifier.border(1.5.dp, subjectColor, RoundedCornerShape(16.dp))
          } else {
            Modifier
          }
        ),
    shape = RoundedCornerShape(16.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = if (isRunning) subjectColor.copy(alpha = 0.14f) else palette.cardBg
      ),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Botón Play / Pause con el color de la materia (estilo YPT)
        IconButton(
          onClick = onTogglePlay,
          modifier =
            Modifier.size(44.dp)
              .clip(CircleShape)
              .background(if (isRunning) subjectColor else subjectColor.copy(alpha = 0.2f)),
        ) {
          Icon(
            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isRunning) "Pausar ${subject.name}" else "Iniciar ${subject.name}",
            tint = if (isRunning) Color.White else subjectColor,
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
          modifier =
            Modifier.weight(1f)
              .clickable { onEdit() },
        ) {
          Text(
            text = subject.name,
            color = palette.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Meta: ${subject.targetMinutesPerDay} min",
            color = palette.textSecondary,
            fontSize = 12.sp,
          )
        }

        Text(
          text = formatDurationHms(secondsStudied),
          color = if (isRunning) subjectColor else palette.textPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
        )

        Spacer(modifier = Modifier.width(2.dp))

        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Editar ${subject.name}",
            tint = palette.textSecondary.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp),
          )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Eliminar ${subject.name}",
            tint = palette.textSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)),
        color = subjectColor,
        trackColor = palette.subtleSurface,
      )
    }
  }
}

@Composable
private fun StatsTabContent(
  state: YptDashboardState,
  onOpenThemeSelector: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalPalette.current
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Estadísticas del Día 🐾",
          color = palette.textPrimary,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
        )
        TextButton(onClick = onOpenThemeSelector) {
          Icon(
            Icons.Default.Palette,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(state.selectedTheme.displayName, color = palette.accent, fontSize = 12.sp)
        }
      }
    }

    // Resumen numérico
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBg),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceAround,
        ) {
          QuickMetricItem(
            label = "Total Hoy",
            value = formatDurationShort(state.totalSecondsToday),
          )
          QuickMetricItem(
            label = "Sesiones",
            value = "${state.todaySessions.size}",
          )
          QuickMetricItem(
            label = "Pomodoros",
            value = "🍅 ${state.completedPomodorosToday}",
          )
          QuickMetricItem(
            label = "Racha Máx.",
            value = formatDurationShort(state.maxContinuousSecondsToday),
          )
        }
      }
    }

    // Distribución por Materia
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBg),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "DISTRIBUCIÓN POR MATERIA",
            color = palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(12.dp))

          val total = state.totalSecondsToday.coerceAtLeast(1L)
          state.subjects.forEach { subject ->
            val sec = state.subjectSecondsToday[subject.id] ?: 0L
            val ratio = (sec.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            val percent = (ratio * 100).toInt()
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Box(
                modifier =
                  Modifier.size(12.dp)
                    .clip(CircleShape)
                    .background(Color(subject.colorHex))
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = subject.name,
                color = palette.textPrimary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "${formatDurationHms(sec)} ($percent%)",
                color = palette.textSecondary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
              )
            }
          }
        }
      }
    }

    // Cuadrícula de Bloques de 10 Minutos (Estilo YPT)
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBg),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "TIMELINE DE 10 MINUTOS (ESTILO YPT)",
            color = palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Cada celda representa 10 minutos de concentración en su hora correspondiente.",
            color = palette.textSecondary,
            fontSize = 11.sp,
          )
          Spacer(modifier = Modifier.height(12.dp))

          // Mostramos desde las 06:00 hasta las 23:00 + 00:00..05:00 como YPT
          val orderedHours = (6..23) + (0..5)
          orderedHours.forEach { hour ->
            val blocks = state.tenMinuteBlocks[hour] ?: List(6) { null }
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = String.format(Locale.getDefault(), "%02d:00", hour),
                color = palette.textSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(44.dp),
              )
              Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                blocks.forEach { colorHex ->
                  Box(
                    modifier =
                      Modifier.weight(1f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                          if (colorHex != null) {
                            Color(colorHex)
                          } else {
                            palette.subtleSurface
                          }
                        )
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun OledFocusScreen(
  subjectName: String,
  subjectColor: Color,
  sessionSeconds: Long,
  totalTodaySeconds: Long,
  isPomodoro: Boolean,
  onExitOled: () -> Unit,
  onPauseTimer: () -> Unit,
) {
  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Color.Black)
        .clickable { onExitOled() }
        .padding(32.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "🐱 PANTALLA SIEMPRE ENCENDIDA · MODO OLED",
        color = subjectColor.copy(alpha = 0.8f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(24.dp))
      Text(
        text = subjectName,
        color = Color.White.copy(alpha = 0.85f),
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text =
          if (isPomodoro) {
            formatDurationMmSs(sessionSeconds)
          } else {
            formatDurationHms(sessionSeconds)
          },
        color = subjectColor,
        fontSize = 64.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Total hoy: ${formatDurationHms(totalTodaySeconds)}",
        color = Color.White.copy(alpha = 0.5f),
        fontSize = 15.sp,
        fontFamily = FontFamily.Monospace,
      )
      Spacer(modifier = Modifier.height(48.dp))
      Button(
        onClick = onPauseTimer,
        colors = ButtonDefaults.buttonColors(containerColor = subjectColor),
      ) {
        Icon(Icons.Default.Pause, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Pausar estudio")
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Toca cualquier parte para volver al panel",
        color = Color.White.copy(alpha = 0.35f),
        fontSize = 12.sp,
      )
    }
  }
}

@Composable
private fun ThemeSelectorDialog(
  currentTheme: AppTheme,
  onSelectTheme: (AppTheme) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Temas de StudyCat 🐾") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppTheme.entries.forEach { theme ->
          val previewPalette = paletteForTheme(theme)
          val isSelected = theme == currentTheme
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(previewPalette.cardBg)
                .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) previewPalette.accent else previewPalette.textSecondary.copy(alpha = 0.25f),
                  shape = RoundedCornerShape(12.dp),
                )
                .clickable { onSelectTheme(theme) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier =
                Modifier.size(28.dp)
                  .clip(CircleShape)
                  .background(previewPalette.accent)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = theme.displayName,
                color = previewPalette.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
              )
              Text(
                text = theme.description,
                color = previewPalette.textSecondary,
                fontSize = 11.sp,
              )
            }
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionado",
                tint = previewPalette.accent,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Cerrar") }
    },
  )
}

@Composable
private fun PomodoroConfigDialog(
  currentConfig: PomodoroConfig,
  onDismiss: () -> Unit,
  onConfirm: (Int, Int, Int) -> Unit,
) {
  var focusText by remember { mutableStateOf(currentConfig.focusMinutes.toString()) }
  var shortBreakText by remember { mutableStateOf(currentConfig.shortBreakMinutes.toString()) }
  var longBreakText by remember { mutableStateOf(currentConfig.longBreakMinutes.toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Configurar Pomodoro 🍅") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Preajustes rápidos:", style = MaterialTheme.typography.bodySmall)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          OutlinedButton(
            onClick = {
              focusText = "25"
              shortBreakText = "5"
              longBreakText = "15"
            },
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp),
          ) {
            Text("25 / 5", fontSize = 12.sp)
          }
          OutlinedButton(
            onClick = {
              focusText = "50"
              shortBreakText = "10"
              longBreakText = "20"
            },
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp),
          ) {
            Text("50 / 10", fontSize = 12.sp)
          }
          OutlinedButton(
            onClick = {
              focusText = "45"
              shortBreakText = "15"
              longBreakText = "25"
            },
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp),
          ) {
            Text("45 / 15", fontSize = 12.sp)
          }
        }

        OutlinedTextField(
          value = focusText,
          onValueChange = { focusText = it.filter { ch -> ch.isDigit() } },
          label = { Text("Minutos de Enfoque") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = shortBreakText,
          onValueChange = { shortBreakText = it.filter { ch -> ch.isDigit() } },
          label = { Text("Descanso Corto (minutos)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = longBreakText,
          onValueChange = { longBreakText = it.filter { ch -> ch.isDigit() } },
          label = { Text("Descanso Largo (minutos)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val focus = focusText.toIntOrNull()?.coerceIn(1, 180) ?: 25
          val shortBrk = shortBreakText.toIntOrNull()?.coerceIn(1, 60) ?: 5
          val longBrk = longBreakText.toIntOrNull()?.coerceIn(1, 90) ?: 15
          onConfirm(focus, shortBrk, longBrk)
        }
      ) {
        Text("Guardar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    },
  )
}

@Composable
private fun AddSubjectDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, Long, Int) -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var targetMinutesText by remember { mutableStateOf("60") }
  var selectedColor by remember { mutableLongStateOf(SubjectColorPalette.first()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Nueva Materia") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nombre (ej. Cálculo, Física)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = targetMinutesText,
          onValueChange = { targetMinutesText = it.filter { ch -> ch.isDigit() } },
          label = { Text("Meta diaria (minutos)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Text("Color distintivo:", style = MaterialTheme.typography.bodySmall)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          SubjectColorPalette.forEach { colorHex ->
            val isSelected = selectedColor == colorHex
            Box(
              modifier =
                Modifier.size(32.dp)
                  .clip(CircleShape)
                  .background(Color(colorHex))
                  .then(
                    if (isSelected) {
                      Modifier.border(2.5.dp, Color.White, CircleShape)
                    } else {
                      Modifier
                    }
                  )
                  .clickable { selectedColor = colorHex }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val mins = targetMinutesText.toIntOrNull()?.coerceIn(10, 720) ?: 60
          if (name.isNotBlank()) {
            onConfirm(name, selectedColor, mins)
          }
        }
      ) {
        Text("Guardar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    },
  )
}

@Composable
private fun EditSubjectDialog(
  subject: Subject,
  onDismiss: () -> Unit,
  onConfirm: (String, Long, Int) -> Unit,
) {
  var name by remember(subject.id) { mutableStateOf(subject.name) }
  var targetMinutesText by remember(subject.id) { mutableStateOf(subject.targetMinutesPerDay.toString()) }
  var selectedColor by remember(subject.id) { mutableLongStateOf(subject.colorHex) }
  val availableColors = remember(subject.colorHex) {
    if (subject.colorHex in SubjectColorPalette) SubjectColorPalette else listOf(subject.colorHex) + SubjectColorPalette
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Editar Materia") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nombre de la materia") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = targetMinutesText,
          onValueChange = { targetMinutesText = it.filter { ch -> ch.isDigit() } },
          label = { Text("Meta diaria (minutos)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Text("Color distintivo:", style = MaterialTheme.typography.bodySmall)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          availableColors.forEach { colorHex ->
            val isSelected = selectedColor == colorHex
            Box(
              modifier =
                Modifier.size(32.dp)
                  .clip(CircleShape)
                  .background(Color(colorHex))
                  .then(
                    if (isSelected) {
                      Modifier.border(2.5.dp, Color.White, CircleShape)
                    } else {
                      Modifier
                    }
                  )
                  .clickable { selectedColor = colorHex }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val mins = targetMinutesText.toIntOrNull()?.coerceIn(5, 720) ?: subject.targetMinutesPerDay
          if (name.isNotBlank()) {
            onConfirm(name.trim(), selectedColor, mins)
          }
        }
      ) {
        Text("Guardar")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    },
  )
}

internal fun formatDurationHms(totalSeconds: Long): String {
  val hours = totalSeconds / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60
  return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}

internal fun formatDurationMmSs(totalSeconds: Long): String {
  val hours = totalSeconds / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60
  return if (hours > 0) {
    String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
  } else {
    String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
  }
}

internal fun formatDurationShort(totalSeconds: Long): String {
  val hours = totalSeconds / 3600
  val minutes = (totalSeconds % 3600) / 60
  return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

@Composable
internal fun MainScreen(data: List<String>, modifier: Modifier = Modifier) {
  YptScaffold(
    state =
      YptDashboardState(
        subjects =
          data.mapIndexed { idx, name ->
            Subject(id = "id_$idx", name = name, colorHex = SubjectColorPalette[idx % SubjectColorPalette.size])
          }
      ),
    onToggleSubject = {},
    onAddSubject = { _, _, _ -> },
    onDeleteSubject = {},
    onEnterOledMode = {},
    modifier = modifier,
  )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
  StudyFlowTheme {
    YptScaffold(
      state = YptDashboardState(),
      onToggleSubject = {},
      onAddSubject = { _, _, _ -> },
      onDeleteSubject = {},
      onEnterOledMode = {},
    )
  }
}
