package com.quantumslate.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Navigation and scrolling — the two things device testing caught that no unit test could.
 *
 * Both regressions this guards against actually shipped: swipe navigation was wired to a
 * gesture detector missing its required callback, and three of four dashboards had no scroll
 * container at all, making content below the fold unreachable.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DashboardNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun modeDot(index: Int) = "Dashboard mode ${index + 1} of 4"

    @Test
    fun launchesShowingTheFirstMode() {
        composeRule.onNodeWithContentDescription("${modeDot(0)}, selected").assertIsDisplayed()
    }

    @Test
    fun allFourModesAreReachableBySwiping() {
        // Regression guard: the swipe handler once had no onHorizontalDrag callback, so the
        // app was a static single screen.
        repeat(3) {
            composeRule.onRoot().performTouchInput { swipeLeft() }
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithContentDescription("${modeDot(3)}, selected").assertIsDisplayed()
    }

    @Test
    fun swipingBackReturnsToTheFirstMode() {
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onRoot().performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("${modeDot(0)}, selected").assertIsDisplayed()
    }

    @Test
    fun tappingAModeDotJumpsDirectlyToThatMode() {
        composeRule.onNodeWithContentDescription(modeDot(2)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("${modeDot(2)}, selected").assertIsDisplayed()
    }

    @Test
    fun everyModeExposesASettingsGear() {
        repeat(4) { index ->
            composeRule.onNodeWithContentDescription(modeDot(index)).performClick()
            composeRule.waitForIdle()
            composeRule.onAllNodesWithContentDescription("Settings").onFirst().assertIsDisplayed()
        }
    }

    @Test
    fun settingsOpensAndBackReturnsToTheSameMode() {
        composeRule.onNodeWithContentDescription(modeDot(2)).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithContentDescription("Settings").onFirst().performClick()
        composeRule.waitForIdle()

        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
            .uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
        composeRule.waitForIdle()

        // The mode must survive the round trip, not reset to Minimalist.
        composeRule.onNodeWithContentDescription("${modeDot(2)}, selected").assertIsDisplayed()
    }

    @Test
    fun dashboardContentScrolls() {
        // Regression guard: Minimalist, Data-Dense and Retro shipped with no verticalScroll,
        // so anything below the fold could not be reached at all.
        composeRule.onNodeWithContentDescription(modeDot(1)).performClick()
        composeRule.waitForIdle()

        // Scrolling to the mode indicator only succeeds inside a real scroll container.
        composeRule.onNode(hasContentDescription(modeDot(3))).performScrollTo().assertIsDisplayed()
    }
}
