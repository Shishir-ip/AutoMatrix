package com.shishir.automatrix.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shishir.automatrix.domain.models.ConditionLogic
import com.shishir.automatrix.domain.models.RoutinePriority
import com.shishir.automatrix.ui.motion.FlowHaptics
import com.shishir.automatrix.ui.motion.FlowMotion
import com.shishir.automatrix.ui.motion.flowPress
import com.shishir.automatrix.ui.theme.FlowGlassColors
import com.shishir.automatrix.ui.theme.LocalFlowGlassColors
import com.shishir.automatrix.ui.theme.StatusError
import com.shishir.automatrix.ui.theme.StatusSuccess

/**
 * iOS-style grouped card surface: solid fill, hairline separator, tactile press.
 */
@Composable
fun FlowGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    accentGlowColor: Color? = null, // retained for API compatibility; unused in B&W design
    motionLevel: FlowMotion.Level = FlowMotion.Level.MEDIUM,
    enablePressAnimation: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val surfaceColor = backgroundColor ?: glassColors.glassSurface
    val borderCol = borderColor ?: glassColors.glassBorder
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    val baseModifier = modifier
        .clip(shape)
        .background(surfaceColor)
        .border(width = 1.dp, color = borderCol, shape = shape)

    val finalModifier = if (onClick != null) {
        if (enablePressAnimation) {
            val interactionSource = remember { MutableInteractionSource() }
            baseModifier
                .flowPress(level = motionLevel, interactionSource = interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    role = Role.Button,
                    onClick = {
                        FlowHaptics.tick(hapticFeedback, view)
                        onClick()
                    }
                )
        } else {
            baseModifier.clickable(
                role = Role.Button,
                onClick = {
                    FlowHaptics.tick(hapticFeedback, view)
                    onClick()
                }
            )
        }
    } else {
        baseModifier
    }

    Box(modifier = finalModifier) {
        content()
    }
}

/**
 * iOS grouped-list row: squircle icon container, 17pt title, chevron.
 */
@Composable
fun FlowRowItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = LocalFlowGlassColors.current.accent,
    iconBackgroundColor: Color = iconTint.copy(alpha = 0.12f),
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    val rowModifier = modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) {
                Modifier
                    .clickable(
                        role = Role.Button,
                        onClick = {
                            FlowHaptics.tick(hapticFeedback, view)
                            onClick()
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            } else {
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            }
        )

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = glassColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = glassColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        } else if (onClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = glassColors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Primary action button: full-radius pill in the theme accent (white/black).
 */
@Composable
fun FlowPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val glassColors = LocalFlowGlassColors.current
    val onAccent = if (glassColors.accent == Color.White) Color.Black else Color.White

    Button(
        onClick = {
            FlowHaptics.impact(hapticFeedback, view)
            onClick()
        },
        modifier = modifier
            .height(50.dp)
            .flowPress(level = FlowMotion.Level.MEDIUM, enabled = enabled, interactionSource = interactionSource),
        shape = RoundedCornerShape(25.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = glassColors.accent,
            contentColor = onAccent,
            disabledContainerColor = glassColors.accent.copy(alpha = 0.3f),
            disabledContentColor = onAccent.copy(alpha = 0.5f)
        ),
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

/**
 * Backwards-compatible alias for existing screens. Uses the clean primary button styling.
 */
@Composable
fun FlowGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradientColors: List<Color> = emptyList(),
    enabled: Boolean = true
) {
    FlowPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        enabled = enabled
    )
}

@Composable
fun FlowOutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedButton(
        onClick = {
            FlowHaptics.tick(hapticFeedback, view)
            onClick()
        },
        modifier = modifier
            .height(44.dp)
            .flowPress(level = FlowMotion.Level.SUBTLE, interactionSource = interactionSource),
        shape = RoundedCornerShape(22.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = glassColors.textPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassColors.glassBorder)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = glassColors.textPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * iOS segmented control for the condition logic (All / Any).
 */
@Composable
fun FlowLogicToggle(
    currentLogic: ConditionLogic,
    onLogicChanged: (ConditionLogic) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(glassColors.glassSurface)
            .border(1.dp, glassColors.glassBorder, RoundedCornerShape(9.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlowLogicSegment(
            label = "All",
            selected = currentLogic == ConditionLogic.ALL,
            glassColors = glassColors,
            hapticFeedback = hapticFeedback,
            view = view
        ) {
            onLogicChanged(ConditionLogic.ALL)
        }

        Spacer(modifier = Modifier.width(2.dp))

        FlowLogicSegment(
            label = "Any",
            selected = currentLogic == ConditionLogic.ANY,
            glassColors = glassColors,
            hapticFeedback = hapticFeedback,
            view = view
        ) {
            onLogicChanged(ConditionLogic.ANY)
        }
    }
}

@Composable
private fun FlowLogicSegment(
    label: String,
    selected: Boolean,
    glassColors: FlowGlassColors,
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback,
    view: android.view.View,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (selected) glassColors.accent else Color.Transparent)
            .clickable {
                if (!selected) {
                    FlowHaptics.tick(hapticFeedback, view)
                    onClick()
                }
            }
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) {
                if (glassColors.accent == Color.White) Color.Black else Color.White
            } else {
                glassColors.textSecondary
            }
        )
    }
}

/**
 * Priority badge in strict grayscale — only weight and fill differentiate levels.
 */
@Composable
fun FlowPriorityBadge(
    priority: RoutinePriority,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalFlowGlassColors.current
    val (bgColor, textColor, weight) = when (priority) {
        RoutinePriority.CRITICAL -> Triple(glassColors.textPrimary, glassColors.glassSurface, FontWeight.Bold)
        RoutinePriority.HIGH -> Triple(glassColors.accent.copy(alpha = 0.14f), glassColors.textPrimary, FontWeight.SemiBold)
        RoutinePriority.NORMAL -> Triple(glassColors.accent.copy(alpha = 0.08f), glassColors.textSecondary, FontWeight.Medium)
        RoutinePriority.LOW -> Triple(Color.Transparent, glassColors.textMuted, FontWeight.Normal)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priority.label,
            fontSize = 10.sp,
            fontWeight = weight,
            color = textColor
        )
    }
}

/**
 * iOS-style switch: 51x31 track, 27dp thumb, monochrome accent.
 */
@Composable
fun FlowSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val isDark = glassColors.accent == Color.White

    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> glassColors.textMuted.copy(alpha = 0.3f)
            checked -> glassColors.accent
            else -> if (isDark) Color(0xFF39393D) else Color(0xFFE9E9EA)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "switchTrack"
    )
    val thumbColor = if (checked && isDark) Color.Black else Color.White

    // Travel: width 51 - 2*2 padding - thumb 27 = 20dp
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = FlowMotion.ToggleDp,
        label = "switchThumbOffset"
    )

    Box(
        modifier = modifier
            .size(width = 51.dp, height = 31.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .then(
                if (!checked) {
                    Modifier.border(
                        0.5.dp,
                        if (isDark) Color(0x14FFFFFF) else Color(0x143C3C43),
                        RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            )
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                indication = null,
                interactionSource = null,
                onValueChange = { newValue ->
                    FlowHaptics.tick(hapticFeedback, view)
                    onCheckedChange(newValue)
                }
            )
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(27.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

/**
 * Tactile monochrome slider with zero-latency finger tracking.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FlowSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            FlowHaptics.tick(hapticFeedback, view)
        }
    }

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        steps = steps,
        enabled = enabled,
        interactionSource = interactionSource,
        thumb = {
            Box(
                modifier = Modifier
                    .size(if (isPressed) 24.dp else 22.dp)
                    .clip(CircleShape)
                    .background(glassColors.accent)
            )
        },
        colors = SliderDefaults.colors(
            thumbColor = glassColors.accent,
            activeTrackColor = glassColors.accent,
            inactiveTrackColor = glassColors.glassBorder
        )
    )
}

/**
 * Execution button: monochrome primary, semantic green only on success.
 */
@Composable
fun FlowRunButton(
    text: String = "Test Run Routine",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRunning: Boolean = false,
    isSuccess: Boolean = false
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val glassColors = LocalFlowGlassColors.current
    val onAccent = if (glassColors.accent == Color.White) Color.Black else Color.White

    val checkScale by animateFloatAsState(
        targetValue = if (isSuccess) 1.0f else 0.6f,
        animationSpec = FlowMotion.Success,
        label = "checkScale"
    )

    Button(
        onClick = {
            FlowHaptics.impact(hapticFeedback, view)
            onClick()
        },
        modifier = modifier
            .height(50.dp)
            .flowPress(level = FlowMotion.Level.MEDIUM, interactionSource = interactionSource),
        shape = RoundedCornerShape(25.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSuccess) StatusSuccess else glassColors.accent,
            contentColor = if (isSuccess) Color.Black else onAccent
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = onAccent,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Running…",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            } else if (isSuccess) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = checkScale
                            scaleY = checkScale
                        }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Executed Successfully",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun FlowEmptyState(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    icon: ImageVector = Icons.Default.AutoAwesome,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalFlowGlassColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(glassColors.accent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = glassColors.accent,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = glassColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = glassColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        FlowPrimaryButton(
            text = buttonText,
            onClick = onButtonClick,
            icon = Icons.Default.Add,
            modifier = Modifier.width(200.dp)
        )
    }
}
