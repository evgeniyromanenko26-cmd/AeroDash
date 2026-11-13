package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.AeroSoundEngine
import com.example.ui.AeroDashViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.theme.AeroDashTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AeroDashTheme {
                val viewModel: AeroDashViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val soundEngine = remember { AeroSoundEngine() }

                Crossfade(
                    targetState = uiState.activeLevel,
                    animationSpec = tween(durationMillis = 260),
                    label = "screen_crossfade",
                ) { activeLevel ->
                    if (activeLevel == null) {
                        LevelSelectScreen(
                            uiState = uiState,
                            onSelectDifficulty = viewModel::selectDifficultyFilter,
                            onPlayLevel = viewModel::launchLevel,
                            onTogglePracticeMode = viewModel::togglePracticeMode,
                            onToggleSound = viewModel::toggleSound,
                            onOpenSkinPicker = { viewModel.setSkinDialogVisible(true) },
                            onDismissSkinPicker = { viewModel.setSkinDialogVisible(false) },
                            onSelectSkin = viewModel::selectSkin,
                        )
                    } else {
                        GameScreen(
                            level = activeLevel,
                            skin = uiState.selectedSkin,
                            soundEnabled = uiState.soundEnabled,
                            practiceMode = uiState.practiceMode,
                            soundEngine = soundEngine,
                            onToggleSound = viewModel::toggleSound,
                            onAttemptFinished = { progress, orbs, completed ->
                                viewModel.recordAttempt(
                                    levelId = activeLevel.id,
                                    progress = progress,
                                    newlyCollectedOrbs = orbs,
                                    completed = completed,
                                )
                            },
                            onExit = viewModel::exitLevel,
                        )
                    }
                }
            }
        }
    }
}
