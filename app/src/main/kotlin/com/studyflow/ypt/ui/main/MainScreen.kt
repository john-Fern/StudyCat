package com.studyflow.ypt.ui.main

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.studyflow.ypt.data.StudySession
import com.studyflow.ypt.data.Subject
import com.studyflow.ypt.data.SubjectColorPalette
import com.studyflow.ypt.data.TimerMode
import com.studyflow.ypt.data.cloud.CloudAccountUiState
import com.studyflow.ypt.data.cloud.CloudSyncStatus
import com.studyflow.ypt.data.cloud.SupabaseCloudSyncService
import com.studyflow.ypt.ui.theme.StudyFlowTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
    AppTheme.LAVENDER_DREAM ->
      StudyCatPalette(
        bg = Color(0xFFF8F5FE),
        cardBg = Color(0xFFFFFFFF),
        headerBg = Color(0xFFEEE8FC),
        accent = Color(0xFF8B5CF6),
        textPrimary = Color(0xFF2E1065),
        textSecondary = Color(0xFF6D28D9),
        subtleSurface = Color(0xFF8B5CF6).copy(alpha = 0.10f),
      )
    AppTheme.AMBER_SUNSET ->
      StudyCatPalette(
        bg = Color(0xFF15100B),
        cardBg = Color(0xFF211A13),
        headerBg = Color(0xFF1B140E),
        accent = Color(0xFFF59E0B),
        textPrimary = Color(0xFFFEF3C7),
        textSecondary = Color(0xFFD97706),
        subtleSurface = Color.White.copy(alpha = 0.06f),
      )
    AppTheme.NORDIC_FROST ->
      StudyCatPalette(
        bg = Color(0xFF0C141F),
        cardBg = Color(0xFF142030),
        headerBg = Color(0xFF0F1927),
        accent = Color(0xFF38BDF8),
        textPrimary = Color(0xFFF0F9FF),
        textSecondary = Color(0xFF7DD3FC),
        subtleSurface = Color.White.copy(alpha = 0.06f),
      )
    AppTheme.COFFEE_LATTE ->
      StudyCatPalette(
        bg = Color(0xFFFAF6F0),
        cardBg = Color(0xFFFFFFFF),
        headerBg = Color(0xFFF0E7DB),
        accent = Color(0xFF8D5B4C),
        textPrimary = Color(0xFF3B271E),
        textSecondary = Color(0xFF7C5949),
        subtleSurface = Color(0xFF8D5B4C).copy(alpha = 0.10f),
      )
    AppTheme.NEO_VIOLET ->
      StudyCatPalette(
        bg = Color(0xFF0E071A),
        cardBg = Color(0xFF1A102E),
        headerBg = Color(0xFF150C25),
        accent = Color(0xFFD946EF),
        textPrimary = Color(0xFFFAF5FF),
        textSecondary = Color(0xFFC084FC),
        subtleSurface = Color.White.copy(alpha = 0.06f),
      )
    AppTheme.MINT_BREEZE ->
      StudyCatPalette(
        bg = Color(0xFFF0FDF4),
        cardBg = Color(0xFFFFFFFF),
        headerBg = Color(0xFFDCFCE7),
        accent = Color(0xFF059669),
        textPrimary = Color(0xFF064E3B),
        textSecondary = Color(0xFF047857),
        subtleSurface = Color(0xFF059669).copy(alpha = 0.10f),
      )
    AppTheme.CRIMSON_NIGHT ->
      StudyCatPalette(
        bg = Color(0xFF140A0D),
        cardBg = Color(0xFF221117),
        headerBg = Color(0xFF1B0D12),
        accent = Color(0xFFF43F5E),
        textPrimary = Color(0xFFFFF1F2),
        textSecondary = Color(0xFFFDA4AF),
        subtleSurface = Color.White.copy(alpha = 0.06f),
      )
  }

private val LocalPalette = compositionLocalOf { paletteForTheme(AppTheme.DARK_YPT) }

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  authCallbackUri: StateFlow<String?> = MutableStateFlow(null),
  onAuthCallbackHandled: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current.applicationContext
  val viewModel: MainScreenViewModel = viewModel {
    val cloudService = SupabaseCloudSyncService(context = context)
    MainScreenViewModel(DefaultDataRepository(context), cloudSyncService = cloudService)
  }
  val yptState by viewModel.yptState.collectAsStateWithLifecycle()
  val cloudAccount by viewModel.cloudAccountState.collectAsStateWithLifecycle()
  val incomingCallback by authCallbackUri.collectAsStateWithLifecycle()

  LaunchedEffect(incomingCallback) {
    incomingCallback?.let { uri ->
      viewModel.completeGoogleSignIn(uri)
      onAuthCallbackHandled()
    }
  }

  // Obliga al celular a NO apagar la pantalla mientras el cronómetro, pomodoro o alarma estén activos
  KeepScreenOn(enabled = yptState.activeTimer.isRunning || yptState.activeAlarm != null)
  PomodoroAlarmEffect(alarm = yptState.activeAlarm)

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
      cloudAccount = cloudAccount,
      onToggleSubject = viewModel::toggleSubjectTimer,
      onAddSubject = viewModel::addSubject,
      onUpdateSubject = viewModel::updateSubject,
      onDeleteSubject = viewModel::deleteSubject,
      onUpdateSessionEndTime = viewModel::updateSessionEndTime,
      onDeleteSession = viewModel::deleteSession,
      onSelectTimelineBlock = viewModel::selectSessionForTenMinuteBlock,
      onEnterOledMode = { viewModel.setOledFocusMode(true) },
      onToggleHeaderCollapsed = viewModel::toggleHeaderCollapsed,
      onSelectTheme = viewModel::setAppTheme,
      onSelectTimerMode = viewModel::setTimerMode,
      onTogglePomodoroBreak = viewModel::togglePomodoroBreak,
      onSkipPomodoroPhase = viewModel::skipPomodoroPhase,
      onResetPomodoro = viewModel::resetPomodoro,
      onUpdatePomodoroConfig = viewModel::updatePomodoroConfig,
      onDismissAlarm = viewModel::dismissAlarm,
      onSignInWithEmail = viewModel::signInWithEmail,
      onSignUpWithEmail = viewModel::signUpWithEmail,
      onStartGoogleSignIn = viewModel::startGoogleSignIn,
      onSyncNow = viewModel::syncNow,
      onSignOut = viewModel::signOut,
      onDeleteCloudData = viewModel::deleteCloudData,
      onClearCloudMessages = viewModel::clearCloudMessages,
      modifier = modifier,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun YptScaffold(
  state: YptDashboardState,
  cloudAccount: CloudAccountUiState = CloudAccountUiState(),
  onToggleSubject: (String) -> Unit,
  onAddSubject: (String, Long, Int) -> Unit,
  onUpdateSubject: (String, String, Long, Int) -> Unit = { _, _, _, _ -> },
  onDeleteSubject: (String) -> Unit,
  onUpdateSessionEndTime: (String, Long) -> Unit = { _, _ -> },
  onDeleteSession: (String) -> Unit = {},
  onSelectTimelineBlock: (Int, Int) -> String? = { hour, blockIdx ->
    findSessionForTenMinuteBlock(state.todaySessions, hour, blockIdx)?.id
  },
  onEnterOledMode: () -> Unit,
  onToggleHeaderCollapsed: () -> Unit = {},
  onSelectTheme: (AppTheme) -> Unit = {},
  onSelectTimerMode: (TimerMode) -> Unit = {},
  onTogglePomodoroBreak: () -> Unit = {},
  onSkipPomodoroPhase: () -> Unit = {},
  onResetPomodoro: () -> Unit = {},
  onUpdatePomodoroConfig: (Int, Int, Int, Boolean) -> Unit = { _, _, _, _ -> },
  onDismissAlarm: () -> Unit = {},
  onSignInWithEmail: (String, String) -> Unit = { _, _ -> },
  onSignUpWithEmail: (String, String) -> Unit = { _, _ -> },
  onStartGoogleSignIn: () -> String? = { null },
  onSyncNow: () -> Unit = {},
  onSignOut: () -> Unit = {},
  onDeleteCloudData: () -> Unit = {},
  onClearCloudMessages: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val palette = remember(state.selectedTheme) { paletteForTheme(state.selectedTheme) }
  var selectedTab by rememberSaveable { mutableIntStateOf(0) }
  var showAddDialog by rememberSaveable { mutableStateOf(false) }
  var editingSubjectId by rememberSaveable { mutableStateOf<String?>(null) }
  var editingSessionId by rememberSaveable { mutableStateOf<String?>(null) }
  var showThemeDialog by rememberSaveable { mutableStateOf(false) }
  var showPomodoroConfigDialog by rememberSaveable { mutableStateOf(false) }
  var showCloudDialog by rememberSaveable { mutableStateOf(false) }

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
          cloudAccount = cloudAccount,
          onOpenCloudAccount = { showCloudDialog = true },
          onToggleSubject = onToggleSubject,
          onEditSubject = { id -> editingSubjectId = id },
          onDeleteSubject = onDeleteSubject,
          onEditSession = { id -> editingSessionId = id },
          onEnterOledMode = onEnterOledMode,
          onToggleHeaderCollapsed = onToggleHeaderCollapsed,
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
          cloudAccount = cloudAccount,
          onOpenCloudAccount = { showCloudDialog = true },
          onOpenThemeSelector = { showThemeDialog = true },
          onEditSession = { id -> editingSessionId = id },
          onSelectTimelineBlock = { hour, blockIdx ->
            onSelectTimelineBlock(hour, blockIdx)?.let { id -> editingSessionId = id }
          },
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

    val sessionToEdit = state.todaySessions.find { it.id == editingSessionId }
    if (sessionToEdit != null) {
      val sessionSubject = state.subjects.find { it.id == sessionToEdit.subjectId }
      EditSessionDialog(
        session = sessionToEdit,
        subjectName = sessionSubject?.name ?: "Materia",
        subjectColor = Color(sessionSubject?.colorHex ?: 0xFFFF6B4A),
        onDismiss = { editingSessionId = null },
        onUpdateEndTime = { newEndMillis ->
          onUpdateSessionEndTime(sessionToEdit.id, newEndMillis)
          editingSessionId = null
        },
        onDelete = {
          onDeleteSession(sessionToEdit.id)
          editingSessionId = null
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
        onConfirm = { focus, shortBrk, longBrk, alarmEnabled ->
          onUpdatePomodoroConfig(focus, shortBrk, longBrk, alarmEnabled)
          showPomodoroConfigDialog = false
        },
      )
    }

    if (showCloudDialog) {
      CloudAccountDialog(
        account = cloudAccount,
        onDismiss = {
          onClearCloudMessages()
          showCloudDialog = false
        },
        onSignInWithEmail = onSignInWithEmail,
        onSignUpWithEmail = onSignUpWithEmail,
        onStartGoogleSignIn = {
          val url = onStartGoogleSignIn()
          if (url != null) {
            try {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
              context.startActivity(intent)
            } catch (_: Exception) {
              Toast.makeText(context, "No se encontró un navegador web para abrir Google.", Toast.LENGTH_SHORT).show()
            }
          }
        },
        onSyncNow = onSyncNow,
        onSignOut = onSignOut,
        onDeleteCloudData = onDeleteCloudData,
        onClearMessages = onClearCloudMessages,
      )
    }

    val activeAlarm = state.activeAlarm
    if (activeAlarm != null) {
      val alarmSubject = state.subjects.find { it.id == activeAlarm.subjectId }
      PomodoroAlarmDialog(
        alarm = activeAlarm,
        subjectName = alarmSubject?.name,
        pomodoroConfig = state.pomodoroConfig,
        onDismiss = onDismissAlarm,
        onStartBreakNow = {
          onDismissAlarm()
          onTogglePomodoroBreak()
        },
      )
    }
  }
}

@Composable
private fun TimerTabContent(
  state: YptDashboardState,
  cloudAccount: CloudAccountUiState = CloudAccountUiState(),
  onOpenCloudAccount: () -> Unit = {},
  onToggleSubject: (String) -> Unit,
  onEditSubject: (String) -> Unit,
  onDeleteSubject: (String) -> Unit,
  onEditSession: (String) -> Unit = {},
  onEnterOledMode: () -> Unit,
  onOpenThemeSelector: () -> Unit,
  onSelectTimerMode: (TimerMode) -> Unit,
  onTogglePomodoroBreak: () -> Unit,
  onSkipPomodoroPhase: () -> Unit,
  onResetPomodoro: () -> Unit,
  onOpenPomodoroConfig: () -> Unit,
  onToggleHeaderCollapsed: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val palette = LocalPalette.current
  val context = LocalContext.current
  val activeSubject = state.subjects.find { it.id == state.activeTimer.subjectId }
  val isPomodoro = state.timerMode == TimerMode.POMODORO
  val dateFormatted = remember {
    SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"))
      .format(Date())
      .replaceFirstChar { it.uppercase() }
  }

  Column(modifier = modifier.fillMaxSize()) {
    // Cabecera superior de StudyCat
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
          .padding(horizontal = 18.dp, vertical = if (state.isHeaderCollapsed) 10.dp else 14.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        // Fila superior: Identidad StudyCat + Acciones (Nube/Cuenta, Tema, Pantalla Activa)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false),
          ) {
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
                fontSize = 15.sp,
                maxLines = 1,
              )
              Text(
                text = dateFormatted,
                color = palette.textSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Spacer(modifier = Modifier.width(6.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            // Botón Nube / Cuenta
            Surface(
              color = palette.subtleSurface,
              shape = RoundedCornerShape(50),
              modifier =
                Modifier.clip(RoundedCornerShape(50))
                  .clickable { onOpenCloudAccount() }
                  .semantics { contentDescription = "Cuenta y respaldo en la nube" },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector =
                    if (cloudAccount.isSignedIn) {
                      if (cloudAccount.syncStatus == CloudSyncStatus.SYNCING) {
                        Icons.Default.CloudSync
                      } else {
                        Icons.Default.CloudDone
                      }
                    } else {
                      Icons.Default.CloudQueue
                    },
                  contentDescription = null,
                  tint =
                    if (cloudAccount.isSignedIn) {
                      if (cloudAccount.syncStatus == CloudSyncStatus.ERROR) Color(0xFFEF4444) else Color(0xFF10B981)
                    } else {
                      palette.accent
                    },
                  modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = if (cloudAccount.isSignedIn) "Nube" else "Cuenta",
                  color = palette.textPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  softWrap = false,
                )
              }
            }

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
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = null,
                  tint = palette.accent,
                  modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "Tema",
                  color = palette.textPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  softWrap = false,
                )
              }
            }

            // Indicador de pantalla siempre encendida (compacto: nunca desborda en vertical)
            val isScreenActive = state.activeTimer.isRunning
            Surface(
              color =
                if (isScreenActive) {
                  Color(0xFF10B981).copy(alpha = 0.2f)
                } else {
                  palette.subtleSurface
                },
              shape = RoundedCornerShape(50),
              modifier =
                Modifier.clip(RoundedCornerShape(50))
                  .clickable {
                    Toast.makeText(
                      context,
                      if (isScreenActive) {
                        "Pantalla activa: no se apagará mientras estudies ☀️"
                      } else {
                        "Modo en espera: la pantalla no se bloqueará al activar el temporizador ☀️"
                      },
                      Toast.LENGTH_SHORT,
                    ).show()
                  }
                  .semantics {
                    contentDescription = if (isScreenActive) "Pantalla activa" else "En espera"
                  },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Default.LightMode,
                  contentDescription = null,
                  tint = if (isScreenActive) Color(0xFF10B981) else palette.textSecondary,
                  modifier = Modifier.size(13.dp),
                )
                if (isScreenActive) {
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "Activa",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                  )
                }
              }
            }
            // Botón Flecha para Comprimir / Expandir
            Surface(
              color =
                if (state.isHeaderCollapsed) {
                  palette.accent.copy(alpha = 0.18f)
                } else {
                  palette.subtleSurface
                },
              shape = RoundedCornerShape(50),
              modifier =
                Modifier.clip(RoundedCornerShape(50))
                  .clickable { onToggleHeaderCollapsed() }
                  .semantics {
                    contentDescription =
                      if (state.isHeaderCollapsed) "Expandir panel de tiempo" else "Comprimir panel de tiempo"
                  },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector =
                    if (state.isHeaderCollapsed) {
                      Icons.Default.KeyboardArrowDown
                    } else {
                      Icons.Default.KeyboardArrowUp
                    },
                  contentDescription = null,
                  tint = if (state.isHeaderCollapsed) palette.accent else palette.textSecondary,
                  modifier = Modifier.size(13.dp),
                )
              }
            }
          }
        }

        // VISTA COMPRIMIDA (Limpia y minimalista: foco directo en estudiar)
        AnimatedVisibility(visible = state.isHeaderCollapsed) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
          ) {
            if (state.activeTimer.isRunning) {
              Row(
                modifier =
                  Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(activeSubject?.colorHex ?: 0xFFFF6B4A).copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f, fill = false),
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(activeSubject?.colorHex ?: 0xFFFF6B4A),
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (state.activeTimer.isBreakRunning) "Descanso" else (activeSubject?.name ?: "Estudiando"),
                    color = Color(activeSubject?.colorHex ?: 0xFFFF6B4A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text =
                      if (isPomodoro) {
                        formatDurationMmSs(state.activeTimer.remainingPomodoroSeconds)
                      } else {
                        formatDurationHms(state.activeTimer.elapsedSeconds)
                      },
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                  )
                }
                OutlinedButton(
                  onClick = onEnterOledMode,
                  shape = RoundedCornerShape(50),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.height(28.dp),
                ) {
                  Icon(
                    Icons.Default.Bedtime,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = palette.textPrimary,
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("OLED", color = palette.textPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            } else {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "⏱️ Hoy: ${formatDurationHms(state.totalSecondsToday)} · Meta: ${state.dailyGoalMinutes / 60}h",
                  color = palette.textSecondary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  maxLines = 1,
                )
                Surface(
                  color = palette.subtleSurface,
                  shape = RoundedCornerShape(50),
                  modifier =
                    Modifier.clip(RoundedCornerShape(50))
                      .clickable { onToggleHeaderCollapsed() },
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Text("Expandir", fontSize = 11.sp, color = palette.accent, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                      Icons.Default.KeyboardArrowDown,
                      contentDescription = null,
                      tint = palette.accent,
                      modifier = Modifier.size(13.dp),
                    )
                  }
                }
              }
            }
          }
        }

        // VISTA EXPANDIDA (Completa: selector de modo, reloj gigante y métricas)
        AnimatedVisibility(visible = !state.isHeaderCollapsed) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
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
              // MODO CRONÓMETRO
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

            // Barra de métricas rápidas
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

            Spacer(modifier = Modifier.height(10.dp))

            // Opción para comprimir el panel al pie de la cabecera
            Surface(
              color = palette.subtleSurface,
              shape = RoundedCornerShape(50),
              modifier =
                Modifier.clip(RoundedCornerShape(50))
                  .clickable { onToggleHeaderCollapsed() }
                  .semantics { contentDescription = "Comprimir panel de tiempo" },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Comprimir panel",
                  color = palette.textSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.KeyboardArrowUp,
                  contentDescription = null,
                  tint = palette.textSecondary,
                  modifier = Modifier.size(14.dp),
                )
              }
            }
          }
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
        val subjectSessions = state.todaySessions.filter { it.subjectId == subject.id }
        SubjectRowCard(
          subject = subject,
          secondsStudied = secondsStudied,
          subjectSessions = subjectSessions,
          isRunning = isRunningThis,
          onTogglePlay = { onToggleSubject(subject.id) },
          onEdit = { onEditSubject(subject.id) },
          onDelete = { onDeleteSubject(subject.id) },
          onEditSession = onEditSession,
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
  subjectSessions: List<StudySession> = emptyList(),
  isRunning: Boolean,
  onTogglePlay: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onEditSession: (String) -> Unit = {},
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
        // Botón Play / Pause con el color de la materia
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

      if (subjectSessions.isNotEmpty()) {
        val timeFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Bloques estudiados hoy (toca para ajustar término o borrar):",
          color = palette.textSecondary,
          fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          subjectSessions.forEach { session ->
            val startStr = timeFmt.format(Date(session.startTimeMillis))
            val endStr = timeFmt.format(Date(session.endTimeMillis))
            Surface(
              color = palette.subtleSurface,
              shape = RoundedCornerShape(10.dp),
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { onEditSession(session.id) },
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier =
                      Modifier.size(8.dp)
                        .clip(CircleShape)
                        .background(subjectColor)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "$startStr → $endStr",
                    color = palette.textPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                  )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = formatDurationHms(session.durationSeconds),
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar bloque $startStr",
                    tint = palette.accent,
                    modifier = Modifier.size(14.dp),
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
private fun StatsTabContent(
  state: YptDashboardState,
  cloudAccount: CloudAccountUiState = CloudAccountUiState(),
  onOpenCloudAccount: () -> Unit = {},
  onOpenThemeSelector: () -> Unit,
  onEditSession: (String) -> Unit = {},
  onSelectTimelineBlock: (Int, Int) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier,
) {
  val palette = LocalPalette.current
  val subjectMap = remember(state.subjects) { state.subjects.associateBy { it.id } }
  val sessionTimeFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

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
        Row(verticalAlignment = Alignment.CenterVertically) {
          TextButton(onClick = onOpenCloudAccount) {
            Icon(
              imageVector =
                if (cloudAccount.isSignedIn) {
                  if (cloudAccount.syncStatus == CloudSyncStatus.SYNCING) {
                    Icons.Default.CloudSync
                  } else {
                    Icons.Default.CloudDone
                  }
                } else {
                  Icons.Default.CloudQueue
                },
              contentDescription = null,
              tint =
                if (cloudAccount.isSignedIn) {
                  if (cloudAccount.syncStatus == CloudSyncStatus.ERROR) Color(0xFFEF4444) else Color(0xFF10B981)
                } else {
                  palette.accent
                },
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (cloudAccount.isSignedIn) "Nube" else "Cuenta",
              color =
                if (cloudAccount.isSignedIn) {
                  if (cloudAccount.syncStatus == CloudSyncStatus.ERROR) Color(0xFFEF4444) else Color(0xFF10B981)
                } else {
                  palette.accent
                },
              fontSize = 12.sp,
              maxLines = 1,
              softWrap = false,
            )
          }
          TextButton(onClick = onOpenThemeSelector) {
            Icon(
              Icons.Default.Palette,
              contentDescription = null,
              tint = palette.accent,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = state.selectedTheme.displayName,
              color = palette.accent,
              fontSize = 12.sp,
              maxLines = 1,
              softWrap = false,
            )
          }
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

    // Bloques registrados hoy (editables para borrar o ajustar hora de término)
    if (state.todaySessions.isNotEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = palette.cardBg),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "BLOQUES ESTUDIADOS HOY",
              color = palette.textSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Toca cualquier bloque para ajustar su hora de término o eliminarlo si fue un error.",
              color = palette.textSecondary,
              fontSize = 11.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              state.todaySessions.forEach { session ->
                val subject = subjectMap[session.subjectId]
                val subjColor = Color(subject?.colorHex ?: 0xFFFF6B4A)
                val startStr = sessionTimeFmt.format(Date(session.startTimeMillis))
                val endStr = sessionTimeFmt.format(Date(session.endTimeMillis))
                Surface(
                  color = palette.subtleSurface,
                  shape = RoundedCornerShape(12.dp),
                  modifier =
                    Modifier.fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .clickable { onEditSession(session.id) },
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.weight(1f),
                    ) {
                      Box(
                        modifier =
                          Modifier.size(10.dp)
                            .clip(CircleShape)
                            .background(subjColor)
                      )
                      Spacer(modifier = Modifier.width(10.dp))
                      Column {
                        Text(
                          text = subject?.name ?: "Materia",
                          color = palette.textPrimary,
                          fontSize = 13.sp,
                          fontWeight = FontWeight.SemiBold,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                          text = "$startStr → $endStr",
                          color = palette.textSecondary,
                          fontSize = 11.sp,
                          fontFamily = FontFamily.Monospace,
                        )
                      }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = formatDurationHms(session.durationSeconds),
                        color = palette.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar bloque",
                        tint = palette.accent,
                        modifier = Modifier.size(16.dp),
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

    // Cuadrícula de Bloques de 10 Minutos
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBg),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "TIMELINE DE 10 MINUTOS",
            color = palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Cada celda representa 10 minutos de concentración. Toca un bloque pintado para editar su término o borrarlo.",
            color = palette.textSecondary,
            fontSize = 11.sp,
          )
          Spacer(modifier = Modifier.height(12.dp))

          // Mostramos desde las 06:00 hasta las 23:00 + 00:00..05:00
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
                blocks.forEachIndexed { blockIdx, colorHex ->
                  Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                      Modifier.weight(1f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                          if (colorHex != null) {
                            Color(colorHex)
                          } else {
                            palette.subtleSurface
                          }
                        )
                        .then(
                          if (colorHex != null) {
                            Modifier.border(
                                1.dp,
                                Color.White.copy(alpha = 0.45f),
                                RoundedCornerShape(5.dp),
                              )
                              .clickable { onSelectTimelineBlock(hour, blockIdx) }
                          } else {
                            Modifier
                          }
                        ),
                  ) {
                    if (colorHex != null) {
                      Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar bloque de las $hour:${blockIdx * 10}",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(11.dp),
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
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
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
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = theme.displayName,
                  color = previewPalette.textPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (theme.isDark) "🌙 Oscuro" else "☀️ Claro",
                  color = previewPalette.textSecondary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                )
              }
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
  onConfirm: (Int, Int, Int, Boolean) -> Unit,
) {
  var focusText by remember { mutableStateOf(currentConfig.focusMinutes.toString()) }
  var shortBreakText by remember { mutableStateOf(currentConfig.shortBreakMinutes.toString()) }
  var longBreakText by remember { mutableStateOf(currentConfig.longBreakMinutes.toString()) }
  var alarmEnabled by remember { mutableStateOf(currentConfig.alarmEnabled) }

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

        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Alarma al terminar Pomodoro y descanso",
              style = MaterialTheme.typography.bodySmall,
            )
          }
          Switch(
            checked = alarmEnabled,
            onCheckedChange = { alarmEnabled = it },
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val focus = focusText.toIntOrNull()?.coerceIn(1, 180) ?: 25
          val shortBrk = shortBreakText.toIntOrNull()?.coerceIn(1, 60) ?: 5
          val longBrk = longBreakText.toIntOrNull()?.coerceIn(1, 90) ?: 15
          onConfirm(focus, shortBrk, longBrk, alarmEnabled)
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
private fun PomodoroAlarmDialog(
  alarm: PomodoroAlarmState,
  subjectName: String?,
  pomodoroConfig: PomodoroConfig,
  onDismiss: () -> Unit,
  onStartBreakNow: () -> Unit,
) {
  val isFocusCompleted = alarm.type == PomodoroAlarmType.FOCUS_COMPLETED
  val breakMinutes =
    if (alarm.nextPhase == PomodoroPhase.LONG_BREAK) {
      pomodoroConfig.longBreakMinutes
    } else {
      pomodoroConfig.shortBreakMinutes
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.NotificationsActive,
          contentDescription = null,
          tint = LocalPalette.current.accent,
          modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text =
            if (isFocusCompleted) {
              "¡Pomodoro Completado! 🍅"
            } else {
              "¡Terminó el Descanso! 🐾"
            },
          fontWeight = FontWeight.Bold,
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isFocusCompleted) {
          Text(
            text =
              if (!subjectName.isNullOrBlank()) {
                "Completaste tu sesión de enfoque en \"$subjectName\"."
              } else {
                "Completaste tu sesión de enfoque."
              },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text =
              "Siguiente fase: ${alarm.nextPhase.displayName} ($breakMinutes min). Toca abajo para iniciar tu descanso o detener la alarma.",
            style = MaterialTheme.typography.bodySmall,
          )
        } else {
          Text(
            text = "Tu ${alarm.completedPhase.displayName.lowercase()} ha finalizado.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text =
              "¡Es hora de volver a concentrarse! Selecciona ▶ en tu materia para iniciar el siguiente Pomodoro de ${pomodoroConfig.focusMinutes} min.",
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    },
    confirmButton = {
      if (isFocusCompleted) {
        Button(onClick = onStartBreakNow) {
          Text("Iniciar descanso ($breakMinutes min) ☕")
        }
      } else {
        Button(onClick = onDismiss) {
          Text("¡Volver a estudiar! 🐱")
        }
      }
    },
    dismissButton = {
      if (isFocusCompleted) {
        TextButton(onClick = onDismiss) {
          Text("Detener alarma")
        }
      }
    },
  )
}

@Composable
private fun SubjectColorGrid(
  selectedColor: Long,
  onColorSelect: (Long) -> Unit,
  availableColors: List<Long>,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    availableColors.chunked(6).forEach { rowColors ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        rowColors.forEach { colorHex ->
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
                .clickable { onColorSelect(colorHex) },
            contentAlignment = Alignment.Center,
          ) {
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionado",
                tint = Color.White,
                modifier = Modifier.size(16.dp),
              )
            }
          }
        }
        repeat(6 - rowColors.size) {
          Spacer(modifier = Modifier.size(32.dp))
        }
      }
    }
  }
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
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Vista previa en vivo del nombre y color seleccionado
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(selectedColor).copy(alpha = 0.16f))
              .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier.size(14.dp)
                .clip(CircleShape)
                .background(Color(selectedColor))
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = name.ifBlank { "Vista previa de la materia" },
            color = Color(selectedColor),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

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
        SubjectColorGrid(
          selectedColor = selectedColor,
          onColorSelect = { selectedColor = it },
          availableColors = SubjectColorPalette,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val mins = targetMinutesText.toIntOrNull()?.coerceIn(10, 720) ?: 60
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
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Vista previa en vivo del nombre y color seleccionado
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(selectedColor).copy(alpha = 0.16f))
              .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier.size(14.dp)
                .clip(CircleShape)
                .background(Color(selectedColor))
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = name.ifBlank { "Vista previa de la materia" },
            color = Color(selectedColor),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

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
        SubjectColorGrid(
          selectedColor = selectedColor,
          onColorSelect = { selectedColor = it },
          availableColors = availableColors,
        )
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

@Composable
private fun EditSessionDialog(
  session: StudySession,
  subjectName: String,
  subjectColor: Color,
  onDismiss: () -> Unit,
  onUpdateEndTime: (Long) -> Unit,
  onDelete: () -> Unit,
) {
  val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
  val startFormatted =
    remember(session.startTimeMillis) { timeFormat.format(Date(session.startTimeMillis)) }

  var editedEndMillis by remember(session.id, session.endTimeMillis) {
    mutableLongStateOf(session.endTimeMillis)
  }

  val initialCal =
    remember(session.id, session.endTimeMillis) {
      Calendar.getInstance().apply { timeInMillis = session.endTimeMillis }
    }
  var endHourText by remember(session.id, session.endTimeMillis) {
    mutableStateOf(
      String.format(Locale.getDefault(), "%02d", initialCal.get(Calendar.HOUR_OF_DAY))
    )
  }
  var endMinuteText by remember(session.id, session.endTimeMillis) {
    mutableStateOf(String.format(Locale.getDefault(), "%02d", initialCal.get(Calendar.MINUTE)))
  }
  var endSecondText by remember(session.id, session.endTimeMillis) {
    mutableStateOf(String.format(Locale.getDefault(), "%02d", initialCal.get(Calendar.SECOND)))
  }

  val initialDurationSec =
    ((session.endTimeMillis - session.startTimeMillis) / 1_000L).coerceAtLeast(1L)
  var durationMinText by remember(session.id, session.endTimeMillis) {
    mutableStateOf((initialDurationSec / 60L).toString())
  }
  var durationSecText by remember(session.id, session.endTimeMillis) {
    mutableStateOf((initialDurationSec % 60L).toString())
  }

  fun syncFieldsFromMillis(millis: Long) {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    endHourText = String.format(Locale.getDefault(), "%02d", cal.get(Calendar.HOUR_OF_DAY))
    endMinuteText = String.format(Locale.getDefault(), "%02d", cal.get(Calendar.MINUTE))
    endSecondText = String.format(Locale.getDefault(), "%02d", cal.get(Calendar.SECOND))
    val durSec = ((millis - session.startTimeMillis) / 1_000L).coerceAtLeast(1L)
    durationMinText = (durSec / 60L).toString()
    durationSecText = (durSec % 60L).toString()
  }

  fun shiftEndMillis(deltaMillis: Long) {
    val updated = (editedEndMillis + deltaMillis).coerceAtLeast(session.startTimeMillis + 1_000L)
    editedEndMillis = updated
    syncFieldsFromMillis(updated)
  }

  fun updateMillisFromText(hStr: String, mStr: String, sStr: String) {
    val h = hStr.toIntOrNull() ?: return
    val m = mStr.toIntOrNull() ?: return
    val s = sStr.toIntOrNull() ?: return
    if (h in 0..23 && m in 0..59 && s in 0..59) {
      val cal =
        Calendar.getInstance().apply {
          timeInMillis = session.startTimeMillis
          set(Calendar.HOUR_OF_DAY, h)
          set(Calendar.MINUTE, m)
          set(Calendar.SECOND, s)
        }
      val updated = cal.timeInMillis.coerceAtLeast(session.startTimeMillis + 1_000L)
      editedEndMillis = updated
      val durSec = ((updated - session.startTimeMillis) / 1_000L).coerceAtLeast(1L)
      durationMinText = (durSec / 60L).toString()
      durationSecText = (durSec % 60L).toString()
    }
  }

  fun updateMillisFromDuration(minStr: String, secStr: String) {
    val mins = minStr.toLongOrNull() ?: 0L
    val secs = secStr.toLongOrNull() ?: 0L
    val totalSec = (mins * 60L + secs).coerceAtLeast(1L)
    val updated = session.startTimeMillis + totalSec * 1_000L
    editedEndMillis = updated
    val cal = Calendar.getInstance().apply { timeInMillis = updated }
    endHourText = String.format(Locale.getDefault(), "%02d", cal.get(Calendar.HOUR_OF_DAY))
    endMinuteText = String.format(Locale.getDefault(), "%02d", cal.get(Calendar.MINUTE))
    endSecondText = String.format(Locale.getDefault(), "%02d", cal.get(Calendar.SECOND))
  }

  val resultingDurationSeconds =
    ((editedEndMillis - session.startTimeMillis) / 1_000L).coerceAtLeast(1L)
  val endFormatted = timeFormat.format(Date(editedEndMillis))

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Editar Bloque de Estudio") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier =
              Modifier.size(12.dp)
                .clip(CircleShape)
                .background(subjectColor)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = subjectName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = "Hora de inicio (fija): $startFormatted",
            style = MaterialTheme.typography.bodySmall,
          )
          Text(
            text = "Hora de término: $endFormatted",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = "Duración resultante: ${formatDurationHms(resultingDurationSeconds)}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
          )
        }

        Text(
          text = "Ajustar tiempo estudiado (ajusta el término):",
          style = MaterialTheme.typography.bodySmall,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = durationMinText,
            onValueChange = {
              val filtered = it.filter { ch -> ch.isDigit() }.take(3)
              durationMinText = filtered
              updateMillisFromDuration(filtered, durationSecText)
            },
            label = { Text("Minutos") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = durationSecText,
            onValueChange = {
              val filtered = it.filter { ch -> ch.isDigit() }.take(2)
              durationSecText = filtered
              updateMillisFromDuration(durationMinText, filtered)
            },
            label = { Text("Segundos") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }

        Text(
          text = "O indicar hora exacta de término (HH : MM : SS):",
          style = MaterialTheme.typography.bodySmall,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = endHourText,
            onValueChange = {
              val filtered = it.filter { ch -> ch.isDigit() }.take(2)
              endHourText = filtered
              updateMillisFromText(filtered, endMinuteText, endSecondText)
            },
            label = { Text("Hora") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = endMinuteText,
            onValueChange = {
              val filtered = it.filter { ch -> ch.isDigit() }.take(2)
              endMinuteText = filtered
              updateMillisFromText(endHourText, filtered, endSecondText)
            },
            label = { Text("Min") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = endSecondText,
            onValueChange = {
              val filtered = it.filter { ch -> ch.isDigit() }.take(2)
              endSecondText = filtered
              updateMillisFromText(endHourText, endMinuteText, filtered)
            },
            label = { Text("Seg") },
            singleLine = true,
            modifier = Modifier.weight(1f),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          TextButton(onClick = { shiftEndMillis(-5 * 60_000L) }) { Text("-5m", fontSize = 12.sp) }
          TextButton(onClick = { shiftEndMillis(-60_000L) }) { Text("-1m", fontSize = 12.sp) }
          TextButton(onClick = { shiftEndMillis(-10_000L) }) { Text("-10s", fontSize = 12.sp) }
          TextButton(onClick = { shiftEndMillis(10_000L) }) { Text("+10s", fontSize = 12.sp) }
          TextButton(onClick = { shiftEndMillis(60_000L) }) { Text("+1m", fontSize = 12.sp) }
        }

        OutlinedButton(
          onClick = onDelete,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Borrar bloque de estudio")
        }
      }
    },
    confirmButton = {
      Button(onClick = { onUpdateEndTime(editedEndMillis) }) {
        Text("Guardar término")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancelar") }
    },
  )
}

@Composable
private fun CloudAccountDialog(
  account: CloudAccountUiState,
  onDismiss: () -> Unit,
  onSignInWithEmail: (String, String) -> Unit,
  onSignUpWithEmail: (String, String) -> Unit,
  onStartGoogleSignIn: () -> Unit,
  onSyncNow: () -> Unit,
  onSignOut: () -> Unit,
  onDeleteCloudData: () -> Unit,
  onClearMessages: () -> Unit,
) {
  val palette = LocalPalette.current
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var showDeleteConfirmation by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (account.isSignedIn) Icons.Default.CloudDone else Icons.Default.Cloud,
          contentDescription = null,
          tint = if (account.isSignedIn) Color(0xFF10B981) else palette.accent,
          modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (account.isSignedIn) "Respaldo en la Nube 🐾" else "Iniciar Sesión en StudyCat 🐱",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
        )
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        if (account.errorMessage != null) {
          Surface(
            color = Color(0xFFEF4444).copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = account.errorMessage,
              color = Color(0xFFEF4444),
              fontSize = 12.sp,
              modifier = Modifier.padding(10.dp),
            )
          }
        }

        if (account.infoMessage != null) {
          Surface(
            color = Color(0xFF10B981).copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = account.infoMessage,
              color = Color(0xFF10B981),
              fontSize = 12.sp,
              modifier = Modifier.padding(10.dp),
            )
          }
        }

        if (account.isSignedIn) {
          Text(
            text = "Conectado como: ${account.userEmail ?: ""}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
          )

          Surface(
            color = palette.subtleSurface,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              when (account.syncStatus) {
                CloudSyncStatus.SYNCING -> {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                  Text("Sincronizando con la nube...", fontSize = 12.sp)
                }
                CloudSyncStatus.SYNCED -> {
                  Icon(
                    Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(16.dp),
                  )
                  Text(
                    text = "Sincronizado ${account.lastSyncedAtFormatted?.let { "a las $it" } ?: "correctamente"}",
                    fontSize = 12.sp,
                    color = Color(0xFF10B981),
                  )
                }
                CloudSyncStatus.ERROR -> {
                  Icon(
                    Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp),
                  )
                  Text("Error al sincronizar.", fontSize = 12.sp, color = Color(0xFFEF4444))
                }
                CloudSyncStatus.OFFLINE_LOCAL_ONLY -> {
                  Icon(
                    Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = palette.textSecondary,
                    modifier = Modifier.size(16.dp),
                  )
                  Text("Modo local.", fontSize = 12.sp, color = palette.textSecondary)
                }
              }
            }
          }

          Button(
            onClick = onSyncNow,
            modifier = Modifier.fillMaxWidth(),
            enabled = !account.isLoading && account.syncStatus != CloudSyncStatus.SYNCING,
          ) {
            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sincronizar ahora")
          }

          OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cerrar sesión")
          }

          if (!showDeleteConfirmation) {
            TextButton(
              onClick = { showDeleteConfirmation = true },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = "Eliminar mi respaldo de la nube",
                color = Color(0xFFEF4444),
                fontSize = 12.sp,
              )
            }
          } else {
            Column(
              verticalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
              Text(
                text = "¿Confirmas borrar tu respaldo de la nube? Tus datos locales se conservarán.",
                fontSize = 11.sp,
                color = Color(0xFFEF4444),
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
              ) {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                  Text("Cancelar", fontSize = 12.sp)
                }
                Button(
                  onClick = {
                    showDeleteConfirmation = false
                    onDeleteCloudData()
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                ) {
                  Text("Sí, eliminar", fontSize = 12.sp)
                }
              }
            }
          }

          Text(
            text = "🔒 Tus datos se respaldan en Supabase mediante PostgreSQL con Row Level Security (RLS) y tus credenciales locales se cifran por hardware con AndroidKeyStore (AES-256-GCM).",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
            fontSize = 10.sp,
          )
        } else {
          Text(
            text = "Inicia sesión para respaldar tus materias y sesiones en la nube. ¡100% gratuito y privado!",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
          )

          Button(
            onClick = onStartGoogleSignIn,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
          ) {
            Text("Continuar con Google 🌐", fontWeight = FontWeight.Bold)
          }

          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = palette.subtleSurface)
            Text(
              text = " O usa tu correo ",
              color = palette.textSecondary,
              fontSize = 11.sp,
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = palette.subtleSurface)
          }

          PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = palette.subtleSurface,
            contentColor = palette.accent,
          ) {
            Tab(
              selected = selectedTabIndex == 0,
              onClick = {
                selectedTabIndex = 0
                onClearMessages()
              },
              text = { Text("Iniciar sesión", fontSize = 12.sp) },
            )
            Tab(
              selected = selectedTabIndex == 1,
              onClick = {
                selectedTabIndex = 1
                onClearMessages()
              },
              text = { Text("Crear cuenta", fontSize = 12.sp) },
            )
          }

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )

          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña (mín. 8 caracteres)") },
            singleLine = true,
            visualTransformation =
              if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector =
                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                  modifier = Modifier.size(18.dp),
                )
              }
            },
            modifier = Modifier.fillMaxWidth(),
          )

          if (account.isLoading) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
            ) {
              CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
          } else {
            Button(
              onClick = {
                if (selectedTabIndex == 0) {
                  onSignInWithEmail(email, password)
                } else {
                  onSignUpWithEmail(email, password)
                }
              },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(if (selectedTabIndex == 0) "Entrar" else "Registrarme")
            }
          }

          Text(
            text = "🔒 Tu información local nunca se pierde. Cada usuario accede únicamente a sus datos mediante políticas de Row Level Security (RLS) en Supabase.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
            fontSize = 10.sp,
          )
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Cerrar") }
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
