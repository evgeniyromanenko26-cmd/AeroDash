package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AeroCyanGlow
import com.example.ui.theme.AeroSkyBright
import com.example.ui.theme.AeroSkyDeep
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class FloatingBubbleSpec(
    val xFraction: Float,
    val yBase: Float,
    val radiusDp: Float,
    val phaseOffset: Float,
    val driftSpeed: Float,
)

private val DefaultBubbles = listOf(
    FloatingBubbleSpec(0.12f, 0.18f, 34f, 0.0f, 1.0f),
    FloatingBubbleSpec(0.84f, 0.14f, 44f, 1.1f, 0.8f),
    FloatingBubbleSpec(0.28f, 0.42f, 22f, 2.3f, 1.2f),
    FloatingBubbleSpec(0.72f, 0.56f, 30f, 0.7f, 0.9f),
    FloatingBubbleSpec(0.16f, 0.78f, 38f, 3.1f, 1.1f),
    FloatingBubbleSpec(0.89f, 0.82f, 26f, 1.9f, 1.3f),
)

@Composable
fun AeroSkyBackdrop(
    modifier: Modifier = Modifier,
    topTint: Color = AeroSkyDeep,
    bottomTint: Color = AeroSkyBright,
    content: @Composable BoxScope.() -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aero_bubbles")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "bubble_phase",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_aero_sky_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            topTint.copy(alpha = 0.34f),
                            AeroCyanGlow.copy(alpha = 0.16f),
                            bottomTint.copy(alpha = 0.28f),
                        )
                    )
                )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            DefaultBubbles.forEach { bubble ->
                val radiusPx = bubble.radiusDp.dp.toPx()
                val angle = animPhase * bubble.driftSpeed + bubble.phaseOffset
                val cx = size.width * bubble.xFraction + cos(angle) * 14.dp.toPx()
                val cy = size.height * bubble.yBase + sin(angle) * 20.dp.toPx()

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.22f),
                            AeroCyanGlow.copy(alpha = 0.34f),
                        ),
                        center = Offset(cx - radiusPx * 0.2f, cy - radiusPx * 0.2f),
                        radius = radiusPx,
                    ),
                    radius = radiusPx,
                    center = Offset(cx, cy),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f),
                    radius = radiusPx,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.2.dp.toPx()),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.72f),
                    radius = radiusPx * 0.22f,
                    center = Offset(cx - radiusPx * 0.34f, cy - radiusPx * 0.34f),
                )
            }
        }

        content()
    }
}

@Composable
fun AeroGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    accentColor: Color = AeroCyanGlow,
    isHighlighted: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderColors = if (isHighlighted) {
        listOf(
            Color.White.copy(alpha = 0.96f),
            accentColor.copy(alpha = 0.75f),
            Color.White.copy(alpha = 0.70f),
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.88f),
            Color.White.copy(alpha = 0.36f),
            accentColor.copy(alpha = 0.38f),
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isHighlighted) 18.dp else 10.dp,
                shape = shape,
                ambientColor = accentColor.copy(alpha = 0.35f),
                spotColor = accentColor.copy(alpha = 0.45f),
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isHighlighted) 0.78f else 0.66f),
                        Color.White.copy(alpha = if (isHighlighted) 0.50f else 0.40f),
                        accentColor.copy(alpha = if (isHighlighted) 0.24f else 0.16f),
                    )
                )
            )
            .drawBehind {
                val glossHeight = size.height * 0.46f
                val glossPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, glossHeight * 0.68f)
                    quadraticTo(
                        size.width * 0.5f,
                        glossHeight * 1.15f,
                        0f,
                        glossHeight * 0.78f,
                    )
                    close()
                }
                drawPath(
                    path = glossPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.48f),
                            Color.White.copy(alpha = 0.04f),
                        ),
                        startY = 0f,
                        endY = glossHeight,
                    ),
                )
            }
            .border(
                width = if (isHighlighted) 2.dp else 1.3.dp,
                brush = Brush.verticalGradient(colors = borderColors),
                shape = shape,
            )
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun GlossyAeroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientStart: Color = AeroSkyDeep,
    gradientEnd: Color = AeroCyanGlow,
    cornerRadius: Dp = 24.dp,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "aero_btn_scale",
    )
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.5f
            }
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = gradientStart.copy(alpha = 0.45f),
                spotColor = gradientEnd.copy(alpha = 0.55f),
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        gradientEnd,
                        gradientStart,
                    )
                )
            )
            .drawBehind {
                // Classic Frutiger Aero top gel shine
                val pad = 4.dp.toPx()
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.65f),
                            Color.White.copy(alpha = 0.08f),
                        ),
                        startY = pad,
                        endY = size.height * 0.52f,
                    ),
                    topLeft = Offset(pad, pad * 0.7f),
                    size = Size(
                        width = (size.width - pad * 2f).coerceAtLeast(0f),
                        height = (size.height * 0.46f).coerceAtLeast(0f),
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                )
            }
            .border(
                width = 1.4.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.92f),
                        Color.White.copy(alpha = 0.35f),
                    )
                ),
                shape = shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun AeroLiquidProgressBar(
    progress: Float,
    gradientStart: Color,
    gradientEnd: Color,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "liquid_progress",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.38f))
            .border(1.dp, Color.White.copy(alpha = 0.85f), CircleShape)
            .padding(2.dp),
    ) {
        if (animatedProgress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(gradientStart, gradientEnd)
                        )
                    )
                    .drawBehind {
                        drawLine(
                            color = Color.White.copy(alpha = 0.65f),
                            start = Offset(4.dp.toPx(), size.height * 0.28f),
                            end = Offset((size.width - 4.dp.toPx()).coerceAtLeast(4.dp.toPx()), size.height * 0.28f),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    },
            )
        }
    }
}
