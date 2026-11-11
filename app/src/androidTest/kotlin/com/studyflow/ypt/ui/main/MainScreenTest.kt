package com.studyflow.ypt.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for StudyCat [com.studyflow.ypt.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { MainScreen(FAKE_DATA) }
  }

  @Test
  fun studyCatHeaderAndSubjects_exist() {
    composeTestRule.onNodeWithText("StudyCat").assertExists()
    FAKE_DATA.forEach { composeTestRule.onNodeWithText(it).assertExists() }
  }

  @Test
  fun timerModesAndThemeSelector_exist() {
    composeTestRule.onNodeWithText("⏱️ Cronómetro").assertExists()
    composeTestRule.onNodeWithText("🍅 Pomodoro").assertExists()
    composeTestRule.onNodeWithText("Tema").performClick()
    composeTestRule.onNodeWithText("Temas de StudyCat 🐾").assertExists()
  }
}

private val FAKE_DATA = listOf("Ingeniería de Software", "Algoritmos y Estructuras", "Kotlin & Android")
