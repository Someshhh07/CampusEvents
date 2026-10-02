package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.OdStatus
import com.example.model.UserRole
import com.example.service.AiExtractionService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Campus Events", appName)
    }

    @Test
    fun `ai extraction service correctly parses circular announcement`() {
        val announcement = """
            CAMPUS CIRCULAR:
            Event: Quantum Computing Workshop
            Organized By: Physics & Tech Society
            Date: 2026-11-05
            Time: 10:00 AM - 01:00 PM
            Venue: Science Block Auditorium 3
            OD Available for all registered attendees.
            Registration Link: https://forms.gle/quantum2026
            Contact: quantum@college.edu
        """.trimIndent()

        val draft = AiExtractionService.extractEventFromText(announcement)
        assertEquals("Technical", draft.category)
        assertTrue(draft.title.contains("Quantum", ignoreCase = true))
        assertEquals("Science Block Auditorium 3", draft.venue)
        assertEquals("2026-11-05", draft.startDate)
        assertEquals("10:00 AM", draft.startTime)
        assertEquals(OdStatus.AVAILABLE.name, draft.odStatus)
        assertEquals("https://forms.gle/quantum2026", draft.registrationUrl)

        val entity = AiExtractionService.createDraftEntity(draft)
        assertEquals("DRAFT", entity.eventStatus)
        assertEquals("PENDING", entity.verificationStatus)
        assertEquals("AI_EXTRACTION", entity.sourceType)
    }

    @Test
    fun `user roles have correct display names`() {
        assertEquals("Student", UserRole.STUDENT.displayName)
        assertEquals("Faculty Coordinator", UserRole.FACULTY.displayName)
        assertEquals("Club President", UserRole.CLUB_PRESIDENT.displayName)
        assertEquals("Platform Administrator", UserRole.PLATFORM_ADMIN.displayName)
    }

    @Test
    fun `theme mode toggle and preferences persistence`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.viewmodel.CollegeEventsViewModel(app)

        // Switch to LIGHT
        vm.setThemeMode(com.example.ui.theme.ThemeMode.LIGHT)
        assertEquals(com.example.ui.theme.ThemeMode.LIGHT, vm.themeMode.value)

        // Check SharedPreferences persistence
        val prefs = app.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        assertEquals("LIGHT", prefs.getString("theme_mode", null))

        // Switch to DARK
        vm.setThemeMode(com.example.ui.theme.ThemeMode.DARK)
        assertEquals(com.example.ui.theme.ThemeMode.DARK, vm.themeMode.value)
        assertEquals("DARK", prefs.getString("theme_mode", null))

        // Switch to SYSTEM
        vm.setThemeMode(com.example.ui.theme.ThemeMode.SYSTEM)
        assertEquals(com.example.ui.theme.ThemeMode.SYSTEM, vm.themeMode.value)
        assertEquals("SYSTEM", prefs.getString("theme_mode", null))
    }

    @Test
    fun `main activity launches successfully`() {
        androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
            }
        }
    }
}
