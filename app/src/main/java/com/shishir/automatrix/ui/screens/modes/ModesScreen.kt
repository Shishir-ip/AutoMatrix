package com.shishir.automatrix.ui.screens.modes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shishir.automatrix.domain.models.Mode
import com.shishir.automatrix.ui.components.IconHelper
import com.shishir.automatrix.ui.motion.FlowHaptics
import com.shishir.automatrix.ui.motion.FlowMotion
import com.shishir.automatrix.ui.theme.LocalFlowGlassColors
import com.shishir.automatrix.ui.theme.StatusSuccess

@Composable
fun ModesScreen(
    modes: List<Mode>,
    onToggleMode: (Mode) -> Unit,
    onNavigateToModeDetail: (String) -> Unit = {}
) {
    val glassColors = LocalFlowGlassColors.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item(span = { GridItemSpan(2) }) {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    text = "Modes",
                    style = MaterialTheme.typography.headlineLarge,
                    color = glassColors.textPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Tap a mode to inspect its actions, or toggle it on or off",
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassColors.textSecondary
                )
            }
        }

        // Clean 2-column mode cards with spring physics and tactile activation
        items(modes, key = { it.id }) { mode ->
            ModeCardItem(
                mode = mode,
                onToggleMode = onToggleMode,
                onClick = { onNavigateToModeDetail(mode.id) }
            )
        }
    }
}

@Composable
private fun ModeCardItem(
    mode: Mode,
    onToggleMode: (Mode) -> Unit,
    onClick: () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val onAccent = if (glassColors.accent == Color.White) Color.Black else Color.White

    val isSelected = mode.isActive
    val cardBg = if (isSelected) glassColors.accent else glassColors.glassSurface
    val cardBorder = if (isSelected) glassColors.accent else glassColors.glassBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .clickable(
                role = Role.Button,
                onClick = {
                    FlowHaptics.impact(hapticFeedback, view)
                    onClick()
                }
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) onAccent.copy(alpha = 0.12f) else glassColors.accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconHelper.getIconByName(mode.iconName),
                        contentDescription = null,
                        tint = if (isSelected) onAccent else glassColors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Quick toggle chip inside card
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) onAccent.copy(alpha = 0.14f) else glassColors.glassBorder.copy(alpha = 0.5f))
                        .clickable {
                            FlowHaptics.modeToggle(hapticFeedback, view, !isSelected)
                            onToggleMode(mode)
                        }
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) onAccent else glassColors.textMuted)
                        )
                        Text(
                            text = if (isSelected) "On" else "Off",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) onAccent else glassColors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = mode.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = if (isSelected) onAccent else glassColors.textPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${mode.actions.size} actions · View details",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = if (isSelected) onAccent.copy(alpha = 0.7f) else glassColors.textSecondary,
                maxLines = 1
            )
        }
    }
}
