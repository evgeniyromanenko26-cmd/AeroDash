package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.model.AeroLevel
import com.example.model.AeroLevelCatalog
import com.example.model.CubeSkin
import com.example.model.LevelDifficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AeroDashUiState(
    val levels: List<AeroLevel> = AeroLevelCatalog.createInitialLevels(),
    val selectedDifficulty: LevelDifficulty? = null,
    val selectedSkin: CubeSkin = CubeSkin.CRYSTAL_CYAN,
    val soundEnabled: Boolean = true,
    val practiceMode: Boolean = false,
    val activeLevelId: String? = null,
    val isSkinDialogVisible: Boolean = false,
) {
    val filteredLevels: List<AeroLevel>
        get() = if (selectedDifficulty == null) {
            levels
        } else {
            levels.filter { it.difficulty == selectedDifficulty }
        }

    val totalStars: Int
        get() = levels.filter { it.isCompleted }.sumOf { it.difficulty.stars }

    val maxStars: Int
        get() = levels.sumOf { it.difficulty.stars }

    val totalOrbs: Int
        get() = levels.sumOf { it.collectedOrbs.size }

    val maxOrbs: Int
        get() = levels.sumOf { it.totalOrbs }

    val activeLevel: AeroLevel?
        get() = activeLevelId?.let { id -> levels.firstOrNull { it.id == id } }
}

class AeroDashViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AeroDashUiState())
    val uiState: StateFlow<AeroDashUiState> = _uiState.asStateFlow()

    fun selectDifficultyFilter(difficulty: LevelDifficulty?) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun togglePracticeMode() {
        _uiState.update { it.copy(practiceMode = !it.practiceMode) }
    }

    fun setSkinDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isSkinDialogVisible = visible) }
    }

    fun selectSkin(skin: CubeSkin) {
        _uiState.update { it.copy(selectedSkin = skin, isSkinDialogVisible = false) }
    }

    fun launchLevel(levelId: String) {
        _uiState.update { it.copy(activeLevelId = levelId) }
    }

    fun exitLevel() {
        _uiState.update { it.copy(activeLevelId = null) }
    }

    fun recordAttempt(levelId: String, progress: Float, newlyCollectedOrbs: Set<Int>, completed: Boolean) {
        _uiState.update { state ->
            val updatedLevels = state.levels.map { level ->
                if (level.id == levelId) {
                    val mergedOrbs = level.collectedOrbs + newlyCollectedOrbs
                    val newBest = if (completed) 1f else maxOf(level.bestProgress, progress.coerceIn(0f, 1f))
                    level.copy(
                        attempts = level.attempts + 1,
                        bestProgress = newBest,
                        collectedOrbs = mergedOrbs,
                        isCompleted = level.isCompleted || completed,
                    )
                } else {
                    level
                }
            }
            state.copy(levels = updatedLevels)
        }
    }
}
