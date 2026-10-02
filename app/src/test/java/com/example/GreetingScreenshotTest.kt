package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.EventEntity
import com.example.model.EventStatus
import com.example.model.OdStatus
import com.example.model.SourceType
import com.example.model.VerificationStatus
import com.example.ui.components.EventCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun eventCard_screenshot() {
        val sampleEvent = EventEntity(
            eventId = "EVT-TEST",
            title = "HackVerse 2026: 36-Hour National Hackathon",
            description = "The premier annual 36-hour collegiate hackathon with AI, Web3, and Mobile tracks.",
            category = "Technical",
            startDate = "2026-09-24",
            startTime = "09:00 AM",
            endTime = "09:00 PM",
            venue = "Academic Block 2 - Tech Auditorium",
            organizer = "Coding Club",
            organizerType = "CLUB",
            department = "Computer Science & Engineering",
            odStatus = OdStatus.AVAILABLE.name,
            registrationRequired = true,
            registrationUrl = "https://hackverse2026.college.edu",
            contactInformation = "hackverse@college.edu",
            posterUrl = "poster_hackathon",
            source = "Authorized College Event API v2",
            sourceType = SourceType.COLLEGE_API.name,
            createdBy = "College Central API Sync",
            updatedBy = "Coding Club Lead",
            verifiedBy = "Dr. Ramanathan (Faculty Coordinator)",
            verificationStatus = VerificationStatus.VERIFIED.name,
            eventStatus = EventStatus.PUBLISHED.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                EventCard(
                    event = sampleEvent,
                    onClick = {},
                    onSaveToggle = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
