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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.shishir.automatrix.ui.components.FlowPrimaryButton
import com.shishir.automatrix.ui.components.sheetScrollWithoutDismiss
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
import androidx.compose.runtime.setValue
import com.shishir.automatrix.ui.theme.LocalFlowGlassColors
import com.shishir.automatrix.ui.theme.StatusSuccess

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ModesScreen(
    modes: List<Mode>,
    onToggleMode: (Mode) -> Unit,
    onNavigateToModeDetail: (String) -> Unit = {},
    onCreateMode: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    var showCreateSheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

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

        // New mode card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(glassColors.accent.copy(alpha = 0.06f))
                    .border(1.dp, glassColors.glassBorder, RoundedCornerShape(18.dp))
                    .clickable(
                        role = Role.Button,
                        onClick = {
                            FlowHaptics.tick(hapticFeedback, view)
                            showCreateSheet = true
                        }
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(glassColors.accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = glassColors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "New Mode",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = glassColors.textPrimary
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateModeSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { name, icon, description ->
                onCreateMode(name, icon, description)
                showCreateSheet = false
            }
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CreateModeSheet(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    var modeName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var modeDescription by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var selectedIcon by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("AutoAwesome") }

    val iconOptions = IconHelper.PRESET_ICONS

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = glassColors.glassSurface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .sheetScrollWithoutDismiss()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "New Mode",
                style = MaterialTheme.typography.titleLarge,
                color = glassColors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "A mode is a one-tap profile that applies a set of device actions.",
                style = MaterialTheme.typography.bodySmall,
                color = glassColors.textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = modeName,
                onValueChange = { modeName = it },
                label = { Text("Mode Name") },
                placeholder = { Text("e.g. Reading, Gym, Focus Time") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = glassColors.accent,
                    unfocusedBorderColor = glassColors.glassBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = modeDescription,
                onValueChange = { modeDescription = it },
                label = { Text("Description (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = glassColors.accent,
                    unfocusedBorderColor = glassColors.glassBorder
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Icon",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = glassColors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(iconOptions) { iconName ->
                    val isSelected = iconName == selectedIcon
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (isSelected) glassColors.accent else glassColors.glassSurface)
                            .border(
                                1.dp,
                                if (isSelected) glassColors.accent else glassColors.glassBorder,
                                RoundedCornerShape(11.dp)
                            )
                            .clickable {
                                FlowHaptics.selection(hapticFeedback, view)
                                selectedIcon = iconName
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(iconName),
                            contentDescription = null,
                            tint = if (isSelected) {
                                if (glassColors.accent == Color.White) Color.Black else Color.White
                            } else glassColors.textSecondary,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            FlowPrimaryButton(
                text = "Create Mode",
                enabled = modeName.isNotBlank(),
                onClick = { onCreate(modeName, selectedIcon, modeDescription) },
                modifier = Modifier.fillMaxWidth()
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
                text = "${mode.actions.size} ${if (mode.actions.size == 1) "action" else "actions"} · View details",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = if (isSelected) onAccent.copy(alpha = 0.7f) else glassColors.textSecondary,
                maxLines = 1
            )
        }
    }
}
