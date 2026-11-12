package com.studyflow.ypt.data.cloud

import com.studyflow.ypt.data.PersistedStudyState
import com.studyflow.ypt.data.StudySession
import com.studyflow.ypt.data.Subject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class CloudSyncLogicTest {

  private fun session(id: String, start: Long, end: Long, subjectId: String = "subj_1") =
    StudySession(
      id = id,
      subjectId = subjectId,
      startTimeMillis = start,
      endTimeMillis = end,
      durationSeconds = (end - start) / 1000L,
      dateKey = "2026-11-11",
    )

  @Test
  fun mergeStudyStates_unitesSessionsAndSubjectsFromBothDevices() {
    val local =
      PersistedStudyState(
        subjects = listOf(Subject("subj_1", "Cálculo", 0xFF3B82F6), Subject("subj_local", "Física", 0xFF10B981)),
        sessions = listOf(session("a", 1_000L, 61_000L)),
        completedPomodorosByDate = mapOf("2026-11-11" to 2),
      )
    val remote =
      PersistedStudyState(
        subjects = listOf(Subject("subj_1", "Cálculo I", 0xFF3B82F6)),
        sessions = listOf(session("b", 100_000L, 160_000L)),
        completedPomodorosByDate = mapOf("2026-11-11" to 3),
      )

    val merged = mergeStudyStates(local, remote)

    assertEquals(listOf("a", "b"), merged.sessions.map { it.id })
    assertEquals(setOf("subj_1", "subj_local"), merged.subjects.map { it.id }.toSet())
    assertEquals("Cálculo I", merged.subjects.first { it.id == "subj_1" }.name)
    assertEquals(3, merged.completedPomodorosByDate["2026-11-11"])
  }

  @Test
  fun sanitizeStudyState_dropsCorruptedSessionsAndTrimsLongNames() {
    val dirty =
      PersistedStudyState(
        subjects = listOf(Subject("subj_1", "x".repeat(500), 0xFF3B82F6)),
        sessions =
          listOf(
            session("ok", 1_000L, 61_000L),
            session("negative", 61_000L, 1_000L),
            session("orphan", 1_000L, 61_000L, subjectId = "no_existe"),
          ),
        dailyGoalMinutes = 99_999,
      )

    val clean = sanitizeStudyState(dirty)

    assertEquals(listOf("ok"), clean.sessions.map { it.id })
    assertEquals(MAX_SUBJECT_NAME_LENGTH, clean.subjects.first().name.length)
    assertEquals(16 * 60, clean.dailyGoalMinutes)
  }

  @Test
  fun isValidEmailInput_acceptsOnlyWellFormedEmails() {
    assertTrue(isValidEmailInput("gato@studycat.app"))
    assertFalse(isValidEmailInput("sin-arroba.com"))
    assertFalse(isValidEmailInput("a@b"))
    assertFalse(isValidEmailInput("dos@@arrobas.com"))
    assertFalse(isValidEmailInput(""))
  }

  @Test
  fun pkceCodeChallenge_matchesSha256Base64UrlWithoutPadding() {
    val verifier = "dBjftJeZ4CVP-mJ92K9mWYS0OaOyYc1gE1u4w5lWV3Wl"
    assertEquals("pX2ik5LSmG-h0oQW69PXGEP1Aq7GSM5rwX40CXIuNI0", pkceCodeChallenge(verifier))
  }

  @Test
  fun generatePkceVerifier_isLongRandomAndUrlSafe() {
    val first = generatePkceVerifier()
    val second = generatePkceVerifier()
    assertTrue(first.length >= 43)
    assertTrue(first.all { it.isLetterOrDigit() || it == '-' || it == '_' })
    assertFalse(first == second)
  }

  @Test
  fun parseCallbackParams_readsQueryAndFragmentParameters() {
    val fromQuery = parseCallbackParams("studycat://auth-callback?code=abc123&state=x")
    assertEquals("abc123", fromQuery["code"])

    val fromFragment = parseCallbackParams("studycat://auth-callback#error=access_denied&error_description=Cancelado%20por%20el%20usuario")
    assertEquals("access_denied", fromFragment["error"])
    assertEquals("Cancelado por el usuario", fromFragment["error_description"])
  }
}
