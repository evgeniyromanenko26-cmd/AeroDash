package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.model.LevelDifficulty
import com.example.ui.AeroDashUiState
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.theme.AeroDashTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AeroDash", appName)
    }

    @Test
    fun `level select screen displays levels and launches selected level`() {
        var launchedLevelId: String? = null
        var selectedFilter: LevelDifficulty? = null

        composeRule.setContent {
            AeroDashTheme {
                LevelSelectScreen(
                    uiState = AeroDashUiState(),
                    onSelectDifficulty = { selectedFilter = it },
                    onPlayLevel = { launchedLevelId = it },
                    onTogglePracticeMode = {},
                    onToggleSound = {},
                    onOpenSkinPicker = {},
                    onDismissSkinPicker = {},
                    onSelectSkin = {},
                )
            }
        }

        composeRule.onNodeWithText("Azure Horizon").assertIsDisplayed()
        composeRule.onNodeWithTag("filter_chip_surge").performClick()
        assertEquals(LevelDifficulty.SURGE, selectedFilter)

        composeRule.onNodeWithTag("play_level_button_azure_horizon")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        assertEquals("azure_horizon", launchedLevelId)
    }
}
