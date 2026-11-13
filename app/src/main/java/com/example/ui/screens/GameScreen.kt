package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.audio.AeroSoundEngine
import com.example.model.AeroLevel
import com.example.model.CubeSkin
import com.example.model.ObstacleType
import com.example.ui.components.AeroGlassCard
import com.example.ui.components.AeroLiquidProgressBar
import com.example.ui.components.AeroSkyBackdrop
import com.example.ui.components.GlossyAeroButton
import com.example.ui.theme.AeroAquaMint
import com.example.ui.theme.AeroCoralOrange
import com.example.ui.theme.AeroCyanGlow
import com.example.ui.theme.AeroEmeraldLime
import com.example.ui.theme.AeroInkPrimary
import com.example.ui.theme.AeroInkSecondary
import com.example.ui.theme.AeroSkyDeep
import com.example.ui.theme.AeroSunAmber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class RunPhase {
    RUNNING,
    PAUSED,
    CRASHED,
    VICTORY,
}

private data class AeroParticle(
    var xUnits: Float,
    var yUnits: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val color: Color,
    val radiusDp: Float,
)

@Composable
fun GameScreen(
    level: AeroLevel,
    skin: CubeSkin,
    soundEnabled: Boolean,
    practiceMode: Boolean,
    soundEngine: AeroSoundEngine,
    onToggleSound: () -> Unit,
    onAttemptFinished: (progress: Float, orbs: Set<Int>, completed: Boolean) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        onExit()
    }

    var phase by remember(level.id) { mutableStateOf(RunPhase.RUNNING) }
    var attemptNumber by remember(level.id) { mutableIntStateOf(level.attempts + 1) }
    var playerX by remember(level.id) { mutableFloatStateOf(0f) }
    var playerY by remember(level.id) { mutableFloatStateOf(0f) }
    var playerVy by remember(level.id) { mutableFloatStateOf(0f) }
    var playerRotation by remember(level.id) { mutableFloatStateOf(0f) }
    var isGrounded by remember(level.id) { mutableStateOf(true) }
    var isJumpHeld by remember(level.id) { mutableStateOf(false) }
    var jumpBuffered by remember(level.id) { mutableStateOf(false) }
    var checkpointX by remember(level.id) { mutableStateOf<Float?>(null) }
    var collectedOrbsThisRun by remember(level.id) { mutableStateOf(emptySet<Int>()) }
    val particles = remember(level.id) { mutableStateListOf<AeroParticle>() }

    fun spawnSplash(x: Float, y: Float, color: Color, count: Int = 10) {
        repeat(count) {
            val angle = Random.nextFloat() * (2f * PI.toFloat())
            val speed = 2.5f + Random.nextFloat() * 5.5f
            particles.add(
                AeroParticle(
                    xUnits = x,
                    yUnits = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed + 2f,
                    life = 1f,
                    color = color,
                    radiusDp = 3f + Random.nextFloat() * 4f,
                )
            )
        }
    }

    fun restartRun(fromCheckpoint: Boolean) {
        val spawnX = if (fromCheckpoint && practiceMode) checkpointX ?: 0f else 0f
        playerX = spawnX
        playerY = 0f
        playerVy = 0f
        playerRotation = 0f
        isGrounded = true
        jumpBuffered = false
        particles.clear()
        if (!fromCheckpoint) {
            collectedOrbsThisRun = emptySet()
        }
        attemptNumber += 1
        phase = RunPhase.RUNNING
    }

    LaunchedEffect(level.id, phase) {
        if (phase != RunPhase.RUNNING) return@LaunchedEffect

        var lastTimeNanos = 0L
        val gravity = -35f
        val jumpVelocity = 13.4f
        val padVelocity = 18.2f

        while (phase == RunPhase.RUNNING) {
            withFrameNanos { frameNanos ->
                if (lastTimeNanos == 0L) {
                    lastTimeNanos = frameNanos
                    return@withFrameNanos
                }
                val dt = ((frameNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0f, 0.04f)
                lastTimeNanos = frameNanos

                // Move horizontally
                playerX += level.speedUnitsPerSec * dt

                // Handle jump input
                if ((isJumpHeld || jumpBuffered) && isGrounded) {
                    playerVy = jumpVelocity
                    isGrounded = false
                    jumpBuffered = false
                    soundEngine.playJump(soundEnabled)
                    spawnSplash(playerX + 0.4f, playerY, skin.glowColor, count = 6)
                }

                // Vertical physics
                playerVy += gravity * dt
                val nextY = playerY + playerVy * dt
                var landedOnSurface = false
                var surfaceY = 0f

                if (nextY <= 0f) {
                    surfaceY = 0f
                    landedOnSurface = true
                }

                // Check obstacle collisions
                val cubeLeft = playerX + 0.16f
                val cubeRight = playerX + 0.84f
                val cubeBottom = nextY
                val cubeTop = nextY + 0.88f

                var crashed = false
                for (obs in level.obstacles) {
                    val obsLeft = obs.xUnits
                    val obsRight = obs.xUnits + obs.widthUnits
                    val obsBottom = obs.yUnits
                    val obsTop = obs.yUnits + obs.heightUnits

                    when (obs.type) {
                        ObstacleType.SPIKE, ObstacleType.DOUBLE_SPIKE -> {
                            // Slightly forgiving spike triangle hitbox
                            val hitLeft = obsLeft + 0.24f
                            val hitRight = obsRight - 0.24f
                            val hitTop = obsTop - 0.22f
                            if (cubeRight > hitLeft && cubeLeft < hitRight && cubeBottom < hitTop && cubeTop > obsBottom + 0.08f) {
                                crashed = true
                                break
                            }
                        }

                        ObstacleType.BLOCK -> {
                            val horizOverlap = cubeRight > obsLeft + 0.08f && cubeLeft < obsRight - 0.08f
                            if (horizOverlap) {
                                val wasAbove = playerY >= obsTop - 0.25f
                                if (wasAbove && playerVy <= 0f && nextY <= obsTop && nextY >= obsTop - 0.45f) {
                                    if (obsTop > surfaceY) {
                                        surfaceY = obsTop
                                        landedOnSurface = true
                                    }
                                } else if (cubeBottom < obsTop - 0.22f && cubeTop > obsBottom + 0.12f) {
                                    crashed = true
                                    break
                                }
                            }
                        }

                        ObstacleType.JUMP_PAD -> {
                            if (cubeRight > obsLeft && cubeLeft < obsRight && cubeBottom <= obsTop + 0.25f && cubeTop >= obsBottom) {
                                playerVy = padVelocity
                                isGrounded = false
                                landedOnSurface = false
                                soundEngine.playPad(soundEnabled)
                                spawnSplash(obsLeft + 0.5f, obsTop, AeroSunAmber, count = 10)
                            }
                        }

                        ObstacleType.AERO_ORB -> {
                            if (!collectedOrbsThisRun.contains(obs.id)) {
                                val cx = obsLeft + 0.5f
                                val cy = obsBottom + 0.5f
                                val px = playerX + 0.5f
                                val py = nextY + 0.5f
                                val dx = cx - px
                                val dy = cy - py
                                if (dx * dx + dy * dy < 0.95f) {
                                    collectedOrbsThisRun = collectedOrbsThisRun + obs.id
                                    soundEngine.playOrb(soundEnabled)
                                    spawnSplash(cx, cy, AeroAquaMint, count = 14)
                                }
                            }
                        }
                    }
                }

                if (crashed) {
                    soundEngine.playCrash(soundEnabled)
                    spawnSplash(playerX + 0.5f, nextY + 0.5f, AeroCoralOrange, count = 22)
                    val progress = (playerX / level.lengthUnits).coerceIn(0f, 1f)
                    onAttemptFinished(progress, collectedOrbsThisRun, false)
                    phase = RunPhase.CRASHED
                    return@withFrameNanos
                }

                if (landedOnSurface) {
                    playerY = surfaceY
                    playerVy = 0f
                    isGrounded = true
                    // Snap rotation to nearest 90 degrees smoothly
                    val snapped = ((playerRotation + 45f) / 90f).toInt() * 90f
                    playerRotation += (snapped - playerRotation) * 0.45f
                } else {
                    playerY = nextY
                    isGrounded = false
                    playerRotation += 340f * dt
                }

                // Trail particle when moving on ground
                if (isGrounded && Random.nextFloat() < 0.35f) {
                    particles.add(
                        AeroParticle(
                            xUnits = playerX,
                            yUnits = playerY + 0.1f,
                            vx = -2.2f,
                            vy = Random.nextFloat() * 1.4f,
                            life = 0.65f,
                            color = skin.glowColor,
                            radiusDp = 3f,
                        )
                    )
                }

                // Update particles
                val iterator = particles.listIterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.xUnits += p.vx * dt
                    p.yUnits += p.vy * dt
                    p.life -= dt * 2.2f
                    if (p.life <= 0f) {
                        iterator.remove()
                    }
                }

                // Check victory
                if (playerX >= level.lengthUnits) {
                    soundEngine.playVictory(soundEnabled)
                    onAttemptFinished(1f, collectedOrbsThisRun, true)
                    phase = RunPhase.VICTORY
                }
            }
        }
    }

    val progress = (playerX / level.lengthUnits).coerceIn(0f, 1f)

    AeroSkyBackdrop(
        topTint = level.skyTintTop,
        bottomTint = level.skyTintBottom,
        modifier = modifier,
    ) {
        // Playfield Gesture Layer + Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_playfield")
                .pointerInput(level.id) {
                    detectTapGestures(
                        onPress = {
                            if (phase == RunPhase.RUNNING) {
                                isJumpHeld = true
                                jumpBuffered = true
                                tryAwaitRelease()
                                isJumpHeld = false
                            }
                        }
                    )
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val unitPx = (size.minDimension / 11f).coerceIn(28.dp.toPx(), 48.dp.toPx())
                val groundY = size.height * 0.74f
                val cameraX = playerX - 3.2f

                // Distant translucent hills
                val hillOffset = -(cameraX * unitPx * 0.18f) % size.width
                val hillPath = Path().apply {
                    moveTo(0f, groundY)
                    for (i in -1..2) {
                        val base = hillOffset + i * size.width
                        cubicTo(
                            base + size.width * 0.25f,
                            groundY - 90.dp.toPx(),
                            base + size.width * 0.75f,
                            groundY - 35.dp.toPx(),
                            base + size.width,
                            groundY,
                        )
                    }
                    lineTo(size.width, groundY)
                    close()
                }
                drawPath(
                    path = hillPath,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.26f), level.accentEnd.copy(alpha = 0.22f))
                    ),
                )

                // Glossy ground surface
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(level.accentStart, level.accentEnd.copy(alpha = 0.92f)),
                        startY = groundY,
                        endY = size.height,
                    ),
                    topLeft = Offset(0f, groundY),
                    size = Size(size.width, size.height - groundY),
                )

                // Glossy floor grid lines
                val gridStep = unitPx * 1.4f
                val firstGrid = -((cameraX * unitPx) % gridStep)
                var gx = firstGrid
                while (gx < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.22f),
                        start = Offset(gx, groundY),
                        end = Offset(gx, size.height),
                        strokeWidth = 1.2.dp.toPx(),
                    )
                    gx += gridStep
                }
                drawLine(
                    color = Color.White.copy(alpha = 0.95f),
                    start = Offset(0f, groundY),
                    end = Offset(size.width, groundY),
                    strokeWidth = 3.dp.toPx(),
                )

                // Practice checkpoint marker
                checkpointX?.let { cpX ->
                    val screenCpX = (cpX - cameraX) * unitPx
                    if (screenCpX in -unitPx..size.width + unitPx) {
                        drawLine(
                            color = AeroEmeraldLime,
                            start = Offset(screenCpX, groundY),
                            end = Offset(screenCpX, groundY - unitPx * 2.2f),
                            strokeWidth = 3.dp.toPx(),
                        )
                        drawCircle(
                            color = AeroEmeraldLime,
                            radius = 8.dp.toPx(),
                            center = Offset(screenCpX, groundY - unitPx * 2.2f),
                        )
                    }
                }

                // Finish Portal
                val portalScreenX = (level.lengthUnits - cameraX) * unitPx
                if (portalScreenX in -unitPx * 2..size.width + unitPx * 2) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.White, AeroAquaMint, level.accentStart)
                        ),
                        topLeft = Offset(portalScreenX, groundY - unitPx * 4.5f),
                        size = Size(unitPx * 0.9f, unitPx * 4.5f),
                        cornerRadius = CornerRadius(unitPx * 0.45f, unitPx * 0.45f),
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(portalScreenX, groundY - unitPx * 4.5f),
                        size = Size(unitPx * 0.9f, unitPx * 4.5f),
                        cornerRadius = CornerRadius(unitPx * 0.45f, unitPx * 0.45f),
                        style = Stroke(width = 2.5.dp.toPx()),
                    )
                }

                // Obstacles
                level.obstacles.forEach { obs ->
                    val ox = (obs.xUnits - cameraX) * unitPx
                    val ow = obs.widthUnits * unitPx
                    if (ox + ow < -unitPx || ox > size.width + unitPx) return@forEach
                    val oyBottom = groundY - obs.yUnits * unitPx
                    val oh = obs.heightUnits * unitPx

                    when (obs.type) {
                        ObstacleType.SPIKE, ObstacleType.DOUBLE_SPIKE -> {
                            val count = if (obs.type == ObstacleType.DOUBLE_SPIKE) 2 else 1
                            val singleW = ow / count
                            repeat(count) { idx ->
                                val sx = ox + idx * singleW
                                val path = Path().apply {
                                    moveTo(sx, oyBottom)
                                    lineTo(sx + singleW * 0.5f, oyBottom - oh)
                                    lineTo(sx + singleW, oyBottom)
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White, level.accentStart),
                                        startY = oyBottom - oh,
                                        endY = oyBottom,
                                    ),
                                )
                                drawPath(
                                    path = path,
                                    color = Color.White.copy(alpha = 0.95f),
                                    style = Stroke(width = 1.8.dp.toPx()),
                                )
                            }
                        }

                        ObstacleType.BLOCK -> {
                            val topLeft = Offset(ox, oyBottom - oh)
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.88f),
                                        level.accentEnd.copy(alpha = 0.82f),
                                    ),
                                    startY = topLeft.y,
                                    endY = oyBottom,
                                ),
                                topLeft = topLeft,
                                size = Size(ow, oh),
                                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                            )
                            drawRoundRect(
                                color = Color.White,
                                topLeft = topLeft,
                                size = Size(ow, oh),
                                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }

                        ObstacleType.JUMP_PAD -> {
                            val padTop = oyBottom - oh
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    listOf(AeroSunAmber, Color.White, AeroSunAmber)
                                ),
                                topLeft = Offset(ox, padTop),
                                size = Size(ow, oh),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            )
                        }

                        ObstacleType.AERO_ORB -> {
                            if (!collectedOrbsThisRun.contains(obs.id)) {
                                val center = Offset(ox + ow * 0.5f, oyBottom - oh * 0.5f)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(Color.White, AeroAquaMint, AeroCyanGlow),
                                        center = center,
                                        radius = unitPx * 0.55f,
                                    ),
                                    radius = unitPx * 0.44f,
                                    center = center,
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = unitPx * 0.44f,
                                    center = center,
                                    style = Stroke(width = 2.dp.toPx()),
                                )
                            }
                        }
                    }
                }

                // Particles
                particles.forEach { p ->
                    val px = (p.xUnits - cameraX) * unitPx
                    val py = groundY - p.yUnits * unitPx
                    drawCircle(
                        color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                        radius = p.radiusDp.dp.toPx() * p.life.coerceIn(0.2f, 1f),
                        center = Offset(px, py),
                    )
                }

                // Player Crystal Cube
                if (phase != RunPhase.CRASHED) {
                    val cubeX = (playerX - cameraX) * unitPx
                    val cubeY = groundY - (playerY + 1f) * unitPx
                    val cubeSize = unitPx
                    rotate(
                        degrees = playerRotation,
                        pivot = Offset(cubeX + cubeSize * 0.5f, cubeY + cubeSize * 0.5f),
                    ) {
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(skin.secondaryColor, skin.primaryColor),
                                start = Offset(cubeX, cubeY),
                                end = Offset(cubeX + cubeSize, cubeY + cubeSize),
                            ),
                            topLeft = Offset(cubeX, cubeY),
                            size = Size(cubeSize, cubeSize),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.96f),
                            topLeft = Offset(cubeX, cubeY),
                            size = Size(cubeSize, cubeSize),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                            style = Stroke(width = 2.2.dp.toPx()),
                        )
                        // Specular shine
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = cubeSize * 0.13f,
                            center = Offset(cubeX + cubeSize * 0.34f, cubeY + cubeSize * 0.34f),
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = cubeSize * 0.13f,
                            center = Offset(cubeX + cubeSize * 0.66f, cubeY + cubeSize * 0.34f),
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.85f),
                            topLeft = Offset(cubeX + cubeSize * 0.26f, cubeY + cubeSize * 0.62f),
                            size = Size(cubeSize * 0.48f, cubeSize * 0.12f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        )
                    }
                }
            }

            // Top Glassmorphic HUD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AeroGlassCard(
                    cornerRadius = 22.dp,
                    accentColor = level.accentEnd,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        HudIconButton(
                            onClick = onExit,
                            contentDescription = stringResource(R.string.cd_back_to_levels),
                            modifier = Modifier.testTag("game_back_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back_to_levels),
                                tint = AeroInkPrimary,
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = level.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AeroInkPrimary,
                                )
                                Text(
                                    text = "${(progress * 100f).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AeroInkPrimary,
                                )
                            }
                            AeroLiquidProgressBar(
                                progress = progress,
                                gradientStart = level.accentStart,
                                gradientEnd = level.accentEnd,
                                height = 10.dp,
                            )
                        }

                        HudIconButton(
                            onClick = onToggleSound,
                            contentDescription = stringResource(R.string.cd_sound_toggle),
                            modifier = Modifier.testTag("game_sound_button"),
                        ) {
                            Icon(
                                imageVector = if (soundEnabled) {
                                    Icons.AutoMirrored.Rounded.VolumeUp
                                } else {
                                    Icons.AutoMirrored.Rounded.VolumeOff
                                },
                                contentDescription = stringResource(R.string.cd_sound_toggle),
                                tint = AeroInkPrimary,
                            )
                        }

                        HudIconButton(
                            onClick = {
                                phase = if (phase == RunPhase.PAUSED) RunPhase.RUNNING else RunPhase.PAUSED
                            },
                            contentDescription = stringResource(R.string.cd_pause_game),
                            modifier = Modifier.testTag("game_pause_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Pause,
                                contentDescription = stringResource(R.string.cd_pause_game),
                                tint = AeroInkPrimary,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp)
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.65f))
                            .border(1.dp, Color.White, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = "${stringResource(R.string.label_attempts)} #$attemptNumber",
                            style = MaterialTheme.typography.labelMedium,
                            color = AeroInkPrimary,
                        )
                    }

                    if (practiceMode) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AeroEmeraldLime.copy(alpha = 0.85f))
                                .border(1.dp, Color.White, CircleShape)
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.game_practice_badge),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                            )
                        }
                    }
                }
            }

            // Bottom hint & Practice checkpoint button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(18.dp),
            ) {
                Text(
                    text = stringResource(R.string.game_tap_to_jump),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.Center),
                )

                if (practiceMode && phase == RunPhase.RUNNING) {
                    GlossyAeroButton(
                        onClick = { checkpointX = (playerX - 1.5f).coerceAtLeast(0f) },
                        gradientStart = Color(0xFF15803D),
                        gradientEnd = AeroEmeraldLime,
                        cornerRadius = 22.dp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .testTag("place_checkpoint_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = stringResource(R.string.cd_place_checkpoint),
                            tint = Color.White,
                        )
                    }
                }
            }

            // Modals for Paused / Crashed / Victory
            if (phase != RunPhase.RUNNING) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x66041C2C))
                        .padding(24.dp),
                ) {
                    AeroGlassCard(
                        cornerRadius = 30.dp,
                        accentColor = level.accentEnd,
                        isHighlighted = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 420.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            when (phase) {
                                RunPhase.PAUSED -> {
                                    Text(
                                        text = stringResource(R.string.game_paused_title),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = AeroInkPrimary,
                                    )
                                    Text(
                                        text = "${level.title} • ${(progress * 100f).toInt()}%",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AeroInkSecondary,
                                    )
                                    GlossyAeroButton(
                                        onClick = { phase = RunPhase.RUNNING },
                                        gradientStart = level.accentStart,
                                        gradientEnd = level.accentEnd,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("resume_button"),
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(Icons.Rounded.PlayArrow, null, tint = Color.White)
                                            Text(
                                                text = stringResource(R.string.action_resume),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                            )
                                        }
                                    }
                                    GlossyAeroButton(
                                        onClick = { restartRun(fromCheckpoint = false) },
                                        gradientStart = AeroSkyDeep,
                                        gradientEnd = AeroCyanGlow,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("restart_from_pause_button"),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.action_retry),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                        )
                                    }
                                    GlossyAeroButton(
                                        onClick = onExit,
                                        gradientStart = Color(0xFF475569),
                                        gradientEnd = Color(0xFF94A3B8),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("exit_to_levels_button"),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.action_levels),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                        )
                                    }
                                }

                                RunPhase.CRASHED -> {
                                    Text(
                                        text = stringResource(R.string.game_crashed_title),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = AeroInkPrimary,
                                    )
                                    Text(
                                        text = "${(progress * 100f).toInt()}% • ${collectedOrbsThisRun.size}/${level.totalOrbs} Orbs",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AeroInkSecondary,
                                    )
                                    if (practiceMode && checkpointX != null) {
                                        GlossyAeroButton(
                                            onClick = { restartRun(fromCheckpoint = true) },
                                            gradientStart = Color(0xFF15803D),
                                            gradientEnd = AeroEmeraldLime,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("respawn_checkpoint_button"),
                                        ) {
                                            Text(
                                                text = "CHECKPOINT",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                            )
                                        }
                                    }
                                    GlossyAeroButton(
                                        onClick = { restartRun(fromCheckpoint = false) },
                                        gradientStart = level.accentStart,
                                        gradientEnd = level.accentEnd,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("retry_button"),
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Icon(Icons.Rounded.Refresh, null, tint = Color.White)
                                            Text(
                                                text = stringResource(R.string.action_retry),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                            )
                                        }
                                    }
                                    GlossyAeroButton(
                                        onClick = onExit,
                                        gradientStart = Color(0xFF475569),
                                        gradientEnd = Color(0xFF94A3B8),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("crash_levels_button"),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.action_levels),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                        )
                                    }
                                }

                                RunPhase.VICTORY -> {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Rounded.Star, null, tint = AeroSunAmber, modifier = Modifier.size(32.dp))
                                        Icon(Icons.Rounded.WaterDrop, null, tint = AeroAquaMint, modifier = Modifier.size(32.dp))
                                    }
                                    Text(
                                        text = stringResource(R.string.game_victory_title),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AeroInkPrimary,
                                    )
                                    Text(
                                        text = "+${level.difficulty.stars} Stars • ${collectedOrbsThisRun.size}/${level.totalOrbs} Orbs",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AeroInkSecondary,
                                    )
                                    GlossyAeroButton(
                                        onClick = onExit,
                                        gradientStart = AeroEmeraldLime,
                                        gradientEnd = AeroAquaMint,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("victory_levels_button"),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.action_levels),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    GlossyAeroButton(
                                        onClick = { restartRun(fromCheckpoint = false) },
                                        gradientStart = level.accentStart,
                                        gradientEnd = level.accentEnd,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("victory_replay_button"),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.action_retry),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                        )
                                    }
                                }

                                RunPhase.RUNNING -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HudIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.7f))
            .border(1.dp, Color.White, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick,
            ),
    ) {
        content()
    }
}
