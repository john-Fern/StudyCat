package com.studyflow.ypt

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.studyflow.ypt.ui.main.MainScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MainNavigation(
  authCallbackUri: StateFlow<String?> = MutableStateFlow(null),
  onAuthCallbackHandled: () -> Unit = {},
) {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(
            onItemClick = { navKey -> backStack.add(navKey) },
            authCallbackUri = authCallbackUri,
            onAuthCallbackHandled = onAuthCallbackHandled,
          )
        }
      },
  )
}
