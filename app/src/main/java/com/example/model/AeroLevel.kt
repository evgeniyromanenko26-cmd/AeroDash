package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AeroAquaMint
import com.example.ui.theme.AeroCoralOrange
import com.example.ui.theme.AeroCyanGlow
import com.example.ui.theme.AeroEmeraldLime
import com.example.ui.theme.AeroLimeGlow
import com.example.ui.theme.AeroMagentaPulse
import com.example.ui.theme.AeroSkyBright
import com.example.ui.theme.AeroSkyDeep
import com.example.ui.theme.AeroSunAmber
import com.example.ui.theme.AeroVioletDream

enum class LevelDifficulty(
    val label: String,
    val stars: Int,
    val badgeStart: Color,
    val badgeEnd: Color,
) {
    BREEZE("Breeze", 2, AeroSkyBright, AeroAquaMint),
    SURGE("Surge", 4, AeroEmeraldLime, AeroLimeGlow),
    PRISM("Prism", 6, AeroSunAmber, AeroCoralOrange),
    VORTEX("Vortex", 8, AeroMagentaPulse, AeroVioletDream),
    SUPERNOVA("Supernova", 10, Color(0xFF0EA5E9), Color(0xFFEC4899)),
}

enum class CubeSkin(
    val id: String,
    val title: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
) {
    CRYSTAL_CYAN("crystal_cyan", "Crystal Cyan", AeroSkyDeep, AeroCyanGlow, Color(0xFF7DD3FC)),
    LIME_AQUA("lime_aqua", "Lime Aqua", AeroEmeraldLime, AeroLimeGlow, Color(0xFF86EFAC)),
    SOLAR_CORAL("solar_coral", "Solar Coral", AeroCoralOrange, AeroSunAmber, Color(0xFFFDE68A)),
    AURORA_PRISM("aurora_prism", "Aurora Prism", AeroVioletDream, AeroMagentaPulse, Color(0xFFF9A8D4)),
}

enum class ObstacleType {
    SPIKE,
    DOUBLE_SPIKE,
    BLOCK,
    JUMP_PAD,
    AERO_ORB,
}

data class LevelObstacle(
    val id: Int,
    val xUnits: Float,
    val yUnits: Float,
    val widthUnits: Float,
    val heightUnits: Float,
    val type: ObstacleType,
)

data class AeroLevel(
    val id: String,
    val number: Int,
    val title: String,
    val subtitle: String,
    val bpm: Int,
    val speedUnitsPerSec: Float,
    val lengthUnits: Float,
    val difficulty: LevelDifficulty,
    val accentStart: Color,
    val accentEnd: Color,
    val skyTintTop: Color,
    val skyTintBottom: Color,
    val bestProgress: Float = 0f,
    val attempts: Int = 0,
    val collectedOrbs: Set<Int> = emptySet(),
    val totalOrbs: Int = 3,
    val isCompleted: Boolean = false,
    val obstacles: List<LevelObstacle> = emptyList(),
) {
    val bestPercent: Int
        get() = (bestProgress.coerceIn(0f, 1f) * 100f).toInt()
}

object AeroLevelCatalog {
    fun createInitialLevels(): List<AeroLevel> = listOf(
        AeroLevel(
            id = "azure_horizon",
            number = 1,
            title = "Azure Horizon",
            subtitle = "Sunlit clouds & smooth glass hops",
            bpm = 118,
            speedUnitsPerSec = 7.4f,
            lengthUnits = 110f,
            difficulty = LevelDifficulty.BREEZE,
            accentStart = AeroSkyDeep,
            accentEnd = AeroAquaMint,
            skyTintTop = Color(0xFF0284C7),
            skyTintBottom = Color(0xFF67E8F9),
            obstacles = buildAzureHorizonObstacles(),
        ),
        AeroLevel(
            id = "emerald_canopy",
            number = 2,
            title = "Emerald Canopy",
            subtitle = "Lush lime terraces & crystal pads",
            bpm = 126,
            speedUnitsPerSec = 8.1f,
            lengthUnits = 126f,
            difficulty = LevelDifficulty.SURGE,
            accentStart = AeroEmeraldLime,
            accentEnd = AeroCyanGlow,
            skyTintTop = Color(0xFF0D9488),
            skyTintBottom = Color(0xFF86EFAC),
            obstacles = buildEmeraldCanopyObstacles(),
        ),
        AeroLevel(
            id = "solar_prism",
            number = 3,
            title = "Solar Prism",
            subtitle = "Amber refraction & aerial step-ups",
            bpm = 134,
            speedUnitsPerSec = 8.8f,
            lengthUnits = 140f,
            difficulty = LevelDifficulty.PRISM,
            accentStart = AeroCoralOrange,
            accentEnd = AeroSunAmber,
            skyTintTop = Color(0xFF0369A1),
            skyTintBottom = Color(0xFFFDE047),
            obstacles = buildSolarPrismObstacles(),
        ),
        AeroLevel(
            id = "crystal_vortex",
            number = 4,
            title = "Crystal Vortex",
            subtitle = "Iridescent double spikes & rapid leaps",
            bpm = 144,
            speedUnitsPerSec = 9.5f,
            lengthUnits = 152f,
            difficulty = LevelDifficulty.VORTEX,
            accentStart = AeroVioletDream,
            accentEnd = AeroMagentaPulse,
            skyTintTop = Color(0xFF4C1D95),
            skyTintBottom = Color(0xFF38BDF8),
            obstacles = buildCrystalVortexObstacles(),
        ),
        AeroLevel(
            id = "aurora_supernova",
            number = 5,
            title = "Aurora Supernova",
            subtitle = "Hyper-gloss finale through sky rings",
            bpm = 154,
            speedUnitsPerSec = 10.2f,
            lengthUnits = 166f,
            difficulty = LevelDifficulty.SUPERNOVA,
            accentStart = Color(0xFF0EA5E9),
            accentEnd = AeroMagentaPulse,
            skyTintTop = Color(0xFF0F172A),
            skyTintBottom = Color(0xFF2DD4BF),
            obstacles = buildAuroraSupernovaObstacles(),
        ),
    )

    private fun buildAzureHorizonObstacles(): List<LevelObstacle> = listOf(
        LevelObstacle(1, 14f, 0f, 1f, 1f, ObstacleType.SPIKE),
        LevelObstacle(2, 23f, 0f, 1f, 1f, ObstacleType.SPIKE),
        LevelObstacle(3, 30f, 1.8f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(4, 34f, 0f, 2f, 1f, ObstacleType.BLOCK),
        LevelObstacle(5, 36f, 1f, 1f, 1f, ObstacleType.SPIKE),
        LevelObstacle(6, 46f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(7, 55f, 0f, 1.1f, 0.35f, ObstacleType.JUMP_PAD),
        LevelObstacle(8, 60f, 2.2f, 3.2f, 1f, ObstacleType.BLOCK),
        LevelObstacle(9, 61.5f, 3.4f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(10, 70f, 0f, 1f, 1f, ObstacleType.SPIKE),
        LevelObstacle(11, 79f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(12, 88f, 0f, 2.5f, 1f, ObstacleType.BLOCK),
        LevelObstacle(13, 93f, 2.5f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(14, 98f, 0f, 1f, 1f, ObstacleType.SPIKE),
    )

    private fun buildEmeraldCanopyObstacles(): List<LevelObstacle> = listOf(
        LevelObstacle(1, 13f, 0f, 1f, 1f, ObstacleType.SPIKE),
        LevelObstacle(2, 20f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(3, 28f, 0f, 2.2f, 1f, ObstacleType.BLOCK),
        LevelObstacle(4, 31f, 1f, 2.2f, 1f, ObstacleType.BLOCK),
        LevelObstacle(5, 32f, 2.5f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(6, 42f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(7, 50f, 0f, 1.1f, 0.35f, ObstacleType.JUMP_PAD),
        LevelObstacle(8, 55f, 2.4f, 3.5f, 1f, ObstacleType.BLOCK),
        LevelObstacle(9, 63f, 0f, 1f, 1f, ObstacleType.SPIKE),
        LevelObstacle(10, 70f, 2.0f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(11, 74f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(12, 84f, 0f, 2.6f, 1.1f, ObstacleType.BLOCK),
        LevelObstacle(13, 88f, 1.1f, 2.6f, 1f, ObstacleType.BLOCK),
        LevelObstacle(14, 95f, 3.1f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(15, 102f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(16, 113f, 0f, 1f, 1f, ObstacleType.SPIKE),
    )

    private fun buildSolarPrismObstacles(): List<LevelObstacle> = listOf(
        LevelObstacle(1, 12f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(2, 21f, 0f, 1.1f, 0.35f, ObstacleType.JUMP_PAD),
        LevelObstacle(3, 26f, 2.2f, 3.0f, 1f, ObstacleType.BLOCK),
        LevelObstacle(4, 27.5f, 3.6f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(5, 35f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(6, 44f, 0f, 2.2f, 1f, ObstacleType.BLOCK),
        LevelObstacle(7, 48f, 1.2f, 2.2f, 1f, ObstacleType.BLOCK),
        LevelObstacle(8, 52f, 2.2f, 2.4f, 1f, ObstacleType.BLOCK),
        LevelObstacle(9, 61f, 0f, 1f, 1f, ObstacleType.SPIKE),
        LevelObstacle(10, 68f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(11, 75f, 2.1f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(12, 79f, 0f, 1.1f, 0.35f, ObstacleType.JUMP_PAD),
        LevelObstacle(13, 86f, 2.4f, 3.4f, 1f, ObstacleType.BLOCK),
        LevelObstacle(14, 95f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(15, 105f, 0f, 1f, 1f, ObstacleType.SPIKE),
        LevelObstacle(16, 113f, 2.4f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(17, 120f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(18, 129f, 0f, 1f, 1f, ObstacleType.SPIKE),
    )

    private fun buildCrystalVortexObstacles(): List<LevelObstacle> = listOf(
        LevelObstacle(1, 12f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(2, 20f, 0f, 1f, 1f, ObstacleType.SPIKE),
        LevelObstacle(3, 27f, 0f, 2.2f, 1f, ObstacleType.BLOCK),
        LevelObstacle(4, 30.5f, 1.3f, 2.5f, 1f, ObstacleType.BLOCK),
        LevelObstacle(5, 32f, 2.9f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(6, 40f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(7, 48f, 0f, 1.1f, 0.35f, ObstacleType.JUMP_PAD),
        LevelObstacle(8, 54f, 2.3f, 2.6f, 1f, ObstacleType.BLOCK),
        LevelObstacle(9, 61f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(10, 69f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(11, 77f, 2.3f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(12, 82f, 0f, 2.4f, 1f, ObstacleType.BLOCK),
        LevelObstacle(13, 86f, 1.4f, 2.4f, 1f, ObstacleType.BLOCK),
        LevelObstacle(14, 94f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(15, 102f, 0f, 1.1f, 0.35f, ObstacleType.JUMP_PAD),
        LevelObstacle(16, 108f, 2.6f, 3.0f, 1f, ObstacleType.BLOCK),
        LevelObstacle(17, 110f, 4.0f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(18, 120f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(19, 130f, 0f, 1f, 1f, ObstacleType.SPIKE),
        LevelObstacle(20, 139f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
    )

    private fun buildAuroraSupernovaObstacles(): List<LevelObstacle> = listOf(
        LevelObstacle(1, 12f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(2, 20f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(3, 28f, 0f, 1.1f, 0.35f, ObstacleType.JUMP_PAD),
        LevelObstacle(4, 34f, 2.3f, 2.8f, 1f, ObstacleType.BLOCK),
        LevelObstacle(5, 36f, 3.8f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(6, 44f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(7, 52f, 0f, 2.2f, 1f, ObstacleType.BLOCK),
        LevelObstacle(8, 56f, 1.3f, 2.2f, 1f, ObstacleType.BLOCK),
        LevelObstacle(9, 60f, 2.4f, 2.4f, 1f, ObstacleType.BLOCK),
        LevelObstacle(10, 69f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(11, 77f, 0f, 1f, 1f, ObstacleType.SPIKE),
        LevelObstacle(12, 84f, 2.1f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(13, 88f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(14, 96f, 0f, 1.1f, 0.35f, ObstacleType.JUMP_PAD),
        LevelObstacle(15, 102f, 2.5f, 3.2f, 1f, ObstacleType.BLOCK),
        LevelObstacle(16, 112f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(17, 121f, 0f, 2.4f, 1f, ObstacleType.BLOCK),
        LevelObstacle(18, 126f, 2.4f, 1f, 1f, ObstacleType.AERO_ORB),
        LevelObstacle(19, 132f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(20, 141f, 0f, 1.8f, 1f, ObstacleType.DOUBLE_SPIKE),
        LevelObstacle(21, 152f, 0f, 1f, 1f, ObstacleType.SPIKE),
    )
}
