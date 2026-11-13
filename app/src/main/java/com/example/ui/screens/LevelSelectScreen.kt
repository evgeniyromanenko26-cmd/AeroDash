package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.AeroLevel
import com.example.model.CubeSkin
import com.example.model.LevelDifficulty
import com.example.model.ObstacleType
import com.example.ui.AeroDashUiState
import com.example.ui.components.AeroGlassCard
import com.example.ui.components.AeroLiquidProgressBar
import com.example.ui.components.AeroSkyBackdrop
import com.example.ui.components.GlossyAeroButton
import com.example.ui.theme.AeroAquaMint
import com.example.ui.theme.AeroCyanGlow
import com.example.ui.theme.AeroEmeraldLime
import com.example.ui.theme.AeroInkPrimary
import com.example.ui.theme.AeroInkSecondary
import com.example.ui.theme.AeroSkyBright
import com.example.ui.theme.AeroSkyDeep
import com.example.ui.theme.AeroSunAmber
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun LevelSelectScreen(
    uiState: AeroDashUiState,
    onSelectDifficulty: (LevelDifficulty?) -> Unit,
    onPlayLevel: (String) -> Unit,
    onTogglePracticeMode: () -> Unit,
    onToggleSound: () -> Unit,
    onOpenSkinPicker: () -> Unit,
    onDismissSkinPicker: () -> Unit,
    onSelectSkin: (CubeSkin) -> Unit,
    modifier: Modifier = Modifier,
) {
    val levels = uiState.filteredLevels
    val pagerState = rememberPagerState(pageCount = { levels.size.coerceAtLeast(1) })
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.selectedDifficulty, levels.size) {
        if (pagerState.currentPage >= levels.size && levels.isNotEmpty()) {
            pagerState.scrollToPage(0)
        }
    }

    val activePreviewLevel = levels.getOrNull(pagerState.currentPage)
    val backdropTop = activePreviewLevel?.skyTintTop ?: AeroSkyDeep
    val backdropBottom = activePreviewLevel?.skyTintBottom ?: AeroSkyBright

    AeroSkyBackdrop(
        topTint = backdropTop,
        bottomTint = backdropBottom,
        modifier = modifier,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .navigationBarsPadding(),
        ) {
            val isExpanded = maxWidth >= 720.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LevelSelectHeader(
                    uiState = uiState,
                    onToggleSound = onToggleSound,
                    onOpenSkinPicker = onOpenSkinPicker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 960.dp)
                        .align(Alignment.CenterHorizontally),
                )

                DifficultyFilterBar(
                    selectedDifficulty = uiState.selectedDifficulty,
                    onSelectDifficulty = onSelectDifficulty,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 960.dp)
                        .align(Alignment.CenterHorizontally),
                )

                if (levels.isEmpty()) {
                    EmptyFilterGlassState(
                        onResetFilter = { onSelectDifficulty(null) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .widthIn(max = 560.dp)
                            .align(Alignment.CenterHorizontally),
                    )
                } else if (isExpanded) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .widthIn(max = 1040.dp)
                            .align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                        ) {
                            items(levels, key = { it.id }) { level ->
                                val indexInFiltered = levels.indexOf(level)
                                val isSelected = pagerState.currentPage == indexInFiltered
                                CompactLevelRowCard(
                                    level = level,
                                    isSelected = isSelected,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(indexInFiltered)
                                        }
                                    },
                                )
                            }
                        }

                        val selectedLevel = levels[pagerState.currentPage.coerceIn(0, levels.lastIndex)]
                        AnimatedContent(
                            targetState = selectedLevel,
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight(),
                            label = "expanded_level_detail",
                        ) { targetLevel ->
                            FeaturedLevelGlassCard(
                                level = targetLevel,
                                selectedSkin = uiState.selectedSkin,
                                practiceMode = uiState.practiceMode,
                                onTogglePracticeMode = onTogglePracticeMode,
                                onPlayClick = { onPlayLevel(targetLevel.id) },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .widthIn(max = 560.dp)
                            .align(Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            pageSpacing = 14.dp,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("level_pager"),
                        ) { page ->
                            val level = levels[page]
                            FeaturedLevelGlassCard(
                                level = level,
                                selectedSkin = uiState.selectedSkin,
                                practiceMode = uiState.practiceMode,
                                onTogglePracticeMode = onTogglePracticeMode,
                                onPlayClick = { onPlayLevel(level.id) },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        LevelQuickDock(
                            levels = levels,
                            currentIndex = pagerState.currentPage,
                            onSelectIndex = { idx ->
                                scope.launch { pagerState.animateScrollToPage(idx) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        if (uiState.isSkinDialogVisible) {
            CubeSkinGlassDialog(
                selectedSkin = uiState.selectedSkin,
                onSelectSkin = onSelectSkin,
                onDismiss = onDismissSkinPicker,
            )
        }
    }
}

@Composable
private fun LevelSelectHeader(
    uiState: AeroDashUiState,
    onToggleSound: () -> Unit,
    onOpenSkinPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AeroGlassCard(
        modifier = modifier,
        cornerRadius = 24.dp,
        accentColor = AeroCyanGlow,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                AeroCubeMiniEmblem(
                    skin = uiState.selectedSkin,
                    modifier = Modifier.size(44.dp),
                )
                Column {
                    Text(
                        text = stringResource(R.string.level_select_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = AeroInkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.level_select_subtitle),
                        style = MaterialTheme.typography.labelMedium,
                        color = AeroInkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatGlassPill(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = AeroSunAmber,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    text = "${uiState.totalStars}/${uiState.maxStars}",
                    testTag = "total_stars_badge",
                )

                GlassCircleAction(
                    onClick = onOpenSkinPicker,
                    contentDescription = stringResource(R.string.cd_skin_selector),
                    accentColor = uiState.selectedSkin.primaryColor,
                    modifier = Modifier.testTag("skin_picker_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = stringResource(R.string.cd_skin_selector),
                        tint = AeroInkPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }

                GlassCircleAction(
                    onClick = onToggleSound,
                    contentDescription = stringResource(R.string.cd_sound_toggle),
                    accentColor = if (uiState.soundEnabled) AeroEmeraldLime else AeroInkSecondary,
                    modifier = Modifier.testTag("sound_toggle_button"),
                ) {
                    Icon(
                        imageVector = if (uiState.soundEnabled) {
                            Icons.AutoMirrored.Rounded.VolumeUp
                        } else {
                            Icons.AutoMirrored.Rounded.VolumeOff
                        },
                        contentDescription = stringResource(R.string.cd_sound_toggle),
                        tint = AeroInkPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatGlassPill(
    icon: @Composable () -> Unit,
    text: String,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .testTag(testTag)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.65f))
            .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        icon()
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = AeroInkPrimary,
        )
    }
}

@Composable
private fun GlassCircleAction(
    onClick: () -> Unit,
    contentDescription: String,
    accentColor: Color,
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        accentColor.copy(alpha = 0.28f),
                    )
                )
            )
            .border(1.2.dp, Color.White.copy(alpha = 0.92f), CircleShape)
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

@Composable
private fun DifficultyFilterBar(
    selectedDifficulty: LevelDifficulty?,
    onSelectDifficulty: (LevelDifficulty?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
    ) {
        item(key = "all") {
            DifficultyGlassChip(
                label = stringResource(R.string.filter_all),
                isSelected = selectedDifficulty == null,
                startColor = AeroSkyDeep,
                endColor = AeroCyanGlow,
                onClick = { onSelectDifficulty(null) },
                modifier = Modifier.testTag("filter_chip_all"),
            )
        }
        items(LevelDifficulty.entries, key = { it.name }) { difficulty ->
            DifficultyGlassChip(
                label = difficulty.label,
                isSelected = selectedDifficulty == difficulty,
                startColor = difficulty.badgeStart,
                endColor = difficulty.badgeEnd,
                onClick = { onSelectDifficulty(difficulty) },
                modifier = Modifier.testTag("filter_chip_${difficulty.name.lowercase()}"),
            )
        }
    }
}

@Composable
private fun DifficultyGlassChip(
    label: String,
    isSelected: Boolean,
    startColor: Color,
    endColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chip_scale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .sizeIn(minHeight = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                shape = CircleShape,
                ambientColor = startColor.copy(alpha = 0.35f),
            )
            .clip(CircleShape)
            .background(
                brush = if (isSelected) {
                    Brush.verticalGradient(listOf(endColor, startColor))
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.72f),
                            Color.White.copy(alpha = 0.42f),
                        )
                    )
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = Color.White.copy(alpha = if (isSelected) 0.95f else 0.72f),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Color.White else AeroInkPrimary,
            maxLines = 1,
        )
    }
}

@Composable
private fun FeaturedLevelGlassCard(
    level: AeroLevel,
    selectedSkin: CubeSkin,
    practiceMode: Boolean,
    onTogglePracticeMode: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AeroGlassCard(
        modifier = modifier.testTag("level_card_${level.id}"),
        cornerRadius = 30.dp,
        accentColor = level.accentEnd,
        isHighlighted = true,
        contentPadding = PaddingValues(18.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Card Top Meta Row
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(6.dp, CircleShape, ambientColor = level.accentStart)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(level.accentEnd, level.accentStart)
                                    )
                                )
                                .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                        ) {
                            Text(
                                text = level.number.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = level.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = AeroInkPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (level.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = AeroEmeraldLime,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Text(
                                text = level.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AeroInkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    // Difficulty + Stars glossy badge
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(level.difficulty.badgeStart, level.difficulty.badgeEnd)
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                        ) {
                            Text(
                                text = level.difficulty.label.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = AeroSunAmber,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "${level.difficulty.stars}",
                                style = MaterialTheme.typography.labelLarge,
                                color = AeroInkPrimary,
                            )
                        }
                    }
                }

                // Live animated Frutiger Aero mini course viewport
                LevelMiniHorizonViewport(
                    level = level,
                    skin = selectedSkin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(106.dp),
                )

                // BPM & Orbs & Attempts glass metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassMetricBox(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = level.accentStart,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        value = stringResource(R.string.label_bpm, level.bpm),
                        modifier = Modifier.weight(1f),
                    )
                    GlassMetricBox(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.WaterDrop,
                                contentDescription = null,
                                tint = AeroAquaMint,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        value = "${level.collectedOrbs.size}/${level.totalOrbs} ${stringResource(R.string.label_orbs)}",
                        modifier = Modifier.weight(1f),
                    )
                    GlassMetricBox(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = AeroSunAmber,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        value = "${level.attempts}",
                        modifier = Modifier.weight(0.65f),
                    )
                }

                // Best Progress Liquid Tube
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_best_progress),
                            style = MaterialTheme.typography.labelMedium,
                            color = AeroInkSecondary,
                        )
                        Text(
                            text = "${level.bestPercent}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = AeroInkPrimary,
                        )
                    }
                    AeroLiquidProgressBar(
                        progress = level.bestProgress,
                        gradientStart = level.accentStart,
                        gradientEnd = level.accentEnd,
                        height = 16.dp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Practice Toggle + Glossy PLAY CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val practiceInteraction = remember { MutableInteractionSource() }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .testTag("practice_toggle_${level.id}")
                        .sizeIn(minWidth = 48.dp, minHeight = 52.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (practiceMode) {
                                Brush.verticalGradient(
                                    listOf(AeroEmeraldLime, Color(0xFF15803D))
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.78f),
                                        Color.White.copy(alpha = 0.45f),
                                    )
                                )
                            }
                        )
                        .border(
                            width = 1.4.dp,
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(22.dp),
                        )
                        .clickable(
                            interactionSource = practiceInteraction,
                            indication = ripple(color = Color.White),
                            role = Role.Switch,
                            onClick = onTogglePracticeMode,
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = null,
                        tint = if (practiceMode) Color.White else AeroInkPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.action_practice_mode),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (practiceMode) Color.White else AeroInkPrimary,
                    )
                }

                GlossyAeroButton(
                    onClick = onPlayClick,
                    gradientStart = level.accentStart,
                    gradientEnd = level.accentEnd,
                    cornerRadius = 24.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("play_level_button_${level.id}"),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp),
                        )
                        Text(
                            text = stringResource(R.string.action_play_level),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassMetricBox(
    icon: @Composable () -> Unit,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.56f))
            .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        icon()
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = AeroInkPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LevelMiniHorizonViewport(
    level: AeroLevel,
    skin: CubeSkin,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "preview_${level.id}")
    val scrollPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "preview_scroll",
    )

    val previewDescription = stringResource(R.string.cd_level_preview, level.title)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .border(1.5.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(22.dp))
            .semantics { contentDescription = previewDescription },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Sky gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        level.skyTintTop.copy(alpha = 0.85f),
                        level.skyTintBottom.copy(alpha = 0.78f),
                    )
                )
            )

            // Soft aurora waves
            val wavePath = Path().apply {
                moveTo(0f, size.height * 0.55f)
                cubicTo(
                    size.width * 0.28f,
                    size.height * (0.30f + 0.1f * sin(scrollPhase * 2f * PI.toFloat())),
                    size.width * 0.72f,
                    size.height * (0.72f - 0.1f * sin(scrollPhase * 2f * PI.toFloat())),
                    size.width,
                    size.height * 0.45f,
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        level.accentEnd.copy(alpha = 0.35f),
                    )
                ),
            )

            val groundY = size.height * 0.76f

            // Glossy ground strip
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        level.accentStart.copy(alpha = 0.9f),
                        level.accentEnd.copy(alpha = 0.95f),
                    ),
                    startY = groundY,
                    endY = size.height,
                ),
                topLeft = Offset(0f, groundY),
                size = Size(size.width, size.height - groundY),
            )
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(0f, groundY),
                end = Offset(size.width, groundY),
                strokeWidth = 2.dp.toPx(),
            )

            // Scrolling obstacle silhouettes from the actual level
            val unitPx = 22.dp.toPx()
            val windowUnits = size.width / unitPx
            val scrollUnits = scrollPhase * 18f
            level.obstacles.take(8).forEach { obs ->
                val relX = ((obs.xUnits * 0.6f - scrollUnits) % (windowUnits + 6f) + (windowUnits + 6f)) % (windowUnits + 6f) - 2f
                val ox = relX * unitPx
                val oy = groundY - obs.yUnits * unitPx
                when (obs.type) {
                    ObstacleType.SPIKE, ObstacleType.DOUBLE_SPIKE -> {
                        val spikeW = if (obs.type == ObstacleType.DOUBLE_SPIKE) unitPx * 0.85f else unitPx
                        val count = if (obs.type == ObstacleType.DOUBLE_SPIKE) 2 else 1
                        repeat(count) { idx ->
                            val sx = ox + idx * spikeW
                            val spikePath = Path().apply {
                                moveTo(sx, oy)
                                lineTo(sx + spikeW * 0.5f, oy - unitPx * 0.95f)
                                lineTo(sx + spikeW, oy)
                                close()
                            }
                            drawPath(
                                path = spikePath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White, level.accentStart),
                                    startY = oy - unitPx,
                                    endY = oy,
                                ),
                            )
                            drawPath(
                                path = spikePath,
                                color = Color.White.copy(alpha = 0.9f),
                                style = Stroke(width = 1.4.dp.toPx()),
                            )
                        }
                    }

                    ObstacleType.BLOCK -> {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.85f), level.accentEnd.copy(alpha = 0.7f))
                            ),
                            topLeft = Offset(ox, oy - unitPx),
                            size = Size(obs.widthUnits * unitPx * 0.8f, unitPx),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        )
                    }

                    ObstacleType.AERO_ORB -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color.White, AeroAquaMint, level.accentEnd)
                            ),
                            radius = unitPx * 0.42f,
                            center = Offset(ox, oy - unitPx * 0.5f),
                        )
                    }

                    ObstacleType.JUMP_PAD -> {
                        drawRoundRect(
                            color = AeroSunAmber,
                            topLeft = Offset(ox, oy - 6.dp.toPx()),
                            size = Size(unitPx, 6.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                        )
                    }
                }
            }

            // Bouncing preview crystal cube
            val hop = abs(sin(scrollPhase * 4f * PI.toFloat()))
            val cubeSize = 26.dp.toPx()
            val cubeX = size.width * 0.22f
            val cubeY = groundY - cubeSize - hop * 28.dp.toPx()

            rotate(
                degrees = hop * 90f,
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
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.92f),
                    topLeft = Offset(cubeX, cubeY),
                    size = Size(cubeSize, cubeSize),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = 1.6.dp.toPx()),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = 4.dp.toPx(),
                    center = Offset(cubeX + cubeSize * 0.34f, cubeY + cubeSize * 0.34f),
                )
            }

            // Glass sheen across top of viewport
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.36f),
                        Color.Transparent,
                    ),
                    startY = 0f,
                    endY = size.height * 0.45f,
                )
            )
        }
    }
}

@Composable
private fun LevelQuickDock(
    levels: List<AeroLevel>,
    currentIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AeroGlassCard(
        modifier = modifier,
        cornerRadius = 24.dp,
        accentColor = AeroCyanGlow,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            levels.forEachIndexed { index, level ->
                val isSelected = index == currentIndex
                val interactionSource = remember { MutableInteractionSource() }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .testTag("dock_level_${level.id}")
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                Brush.verticalGradient(listOf(level.accentEnd, level.accentStart))
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.55f),
                                        Color.White.copy(alpha = 0.25f),
                                    )
                                )
                            }
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = Color.White.copy(alpha = if (isSelected) 0.95f else 0.7f),
                            shape = CircleShape,
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(color = Color.White),
                            role = Role.Tab,
                            onClick = { onSelectIndex(index) },
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = level.number.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) Color.White else AeroInkPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactLevelRowCard(
    level: AeroLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    AeroGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compact_level_${level.id}")
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                role = Role.Button,
                onClick = onClick,
            ),
        cornerRadius = 22.dp,
        accentColor = level.accentEnd,
        isHighlighted = isSelected,
        contentPadding = PaddingValues(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(level.accentEnd, level.accentStart)))
                    .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape),
            ) {
                Text(
                    text = level.number.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AeroInkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${level.difficulty.label} • ${level.bestPercent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AeroInkSecondary,
                )
            }
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = AeroSunAmber,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "${level.difficulty.stars}",
                style = MaterialTheme.typography.labelLarge,
                color = AeroInkPrimary,
            )
        }
    }
}

@Composable
private fun EmptyFilterGlassState(
    onResetFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        AeroGlassCard(
            cornerRadius = 28.dp,
            accentColor = AeroCyanGlow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = AeroSkyDeep,
                    modifier = Modifier.size(44.dp),
                )
                Text(
                    text = stringResource(R.string.empty_filter_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = AeroInkPrimary,
                )
                Text(
                    text = stringResource(R.string.empty_filter_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AeroInkSecondary,
                )
                GlossyAeroButton(
                    onClick = onResetFilter,
                    modifier = Modifier.testTag("reset_filter_button"),
                ) {
                    Text(
                        text = stringResource(R.string.filter_all),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun AeroCubeMiniEmblem(
    skin: CubeSkin,
    modifier: Modifier = Modifier,
) {
    val emblemDesc = stringResource(R.string.cd_aero_emblem)
    Canvas(
        modifier = modifier.semantics { contentDescription = emblemDesc }
    ) {
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(skin.secondaryColor, skin.primaryColor),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.95f),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 1.8.dp.toPx()),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = size.minDimension * 0.14f,
            center = Offset(size.width * 0.34f, size.height * 0.34f),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = size.minDimension * 0.14f,
            center = Offset(size.width * 0.66f, size.height * 0.34f),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(size.width * 0.28f, size.height * 0.62f),
            size = Size(size.width * 0.44f, size.height * 0.12f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        )
    }
}

@Composable
private fun CubeSkinGlassDialog(
    selectedSkin: CubeSkin,
    onSelectSkin: (CubeSkin) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        AeroGlassCard(
            cornerRadius = 28.dp,
            accentColor = selectedSkin.primaryColor,
            isHighlighted = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("skin_picker_dialog"),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.skin_picker_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = AeroInkPrimary,
                    )
                    GlassCircleAction(
                        onClick = onDismiss,
                        contentDescription = "Close",
                        accentColor = AeroCyanGlow,
                        modifier = Modifier.testTag("close_skin_dialog"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = AeroInkPrimary,
                        )
                    }
                }

                CubeSkin.entries.forEach { skin ->
                    val isSelected = skin == selectedSkin
                    val interactionSource = remember { MutableInteractionSource() }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("skin_option_${skin.id}")
                            .sizeIn(minHeight = 56.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(
                                        listOf(
                                            skin.secondaryColor.copy(alpha = 0.45f),
                                            Color.White.copy(alpha = 0.8f),
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.6f),
                                            Color.White.copy(alpha = 0.4f),
                                        )
                                    )
                                }
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) skin.primaryColor else Color.White.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(color = skin.primaryColor),
                                role = Role.RadioButton,
                                onClick = { onSelectSkin(skin) },
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        AeroCubeMiniEmblem(skin = skin, modifier = Modifier.size(36.dp))
                        Text(
                            text = skin.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = AeroInkPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Selected",
                                tint = skin.primaryColor,
                            )
                        }
                    }
                }
            }
        }
    }
}
