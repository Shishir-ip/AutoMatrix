package com.shishir.automatrix.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shishir.automatrix.domain.models.ExecutionLog
import com.shishir.automatrix.domain.models.Mode
import com.shishir.automatrix.domain.models.Routine
import com.shishir.automatrix.ui.components.FlowEmptyState
import com.shishir.automatrix.ui.components.FlowGlassCard
import com.shishir.automatrix.ui.components.FlowRowItem
import com.shishir.automatrix.ui.components.FlowSwitch
import com.shishir.automatrix.ui.components.IconHelper
import com.shishir.automatrix.ui.motion.FlowHaptics
import com.shishir.automatrix.ui.motion.FlowMotion
import com.shishir.automatrix.ui.motion.flowPress
import com.shishir.automatrix.ui.theme.LocalFlowGlassColors
import com.shishir.automatrix.ui.theme.StatusError
import com.shishir.automatrix.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    routines: List<Routine>,
    modes: List<Mode>,
    logs: List<ExecutionLog>,
    onNavigateToCreateRoutine: () -> Unit,
    onNavigateToRoutineDetail: (String) -> Unit,
    onToggleRoutine: (Routine) -> Unit,
    onToggleMode: (Mode) -> Unit,
    onNavigateToModeDetail: (String) -> Unit = {}
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val onAccent = if (glassColors.accent == Color.White) Color.Black else Color.White
    val enabledCount = remember(routines) { routines.count { it.isEnabled } }
    val activeMode = remember(modes) { modes.firstOrNull { it.isActive } }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val recentRoutines = remember(routines) { routines.take(5) }
    val recentLogs = remember(logs) { logs.take(3) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Large title header
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AutoMatrix",
                        style = MaterialTheme.typography.headlineLarge,
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    val statusText = when {
                        activeMode != null -> "${activeMode.name} mode is on"
                        enabledCount > 0 -> "$enabledCount active ${if (enabledCount == 1) "automation" else "automations"}"
                        else -> "No automations running"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = glassColors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .flowPress(level = FlowMotion.Level.MEDIUM)
                        .clip(CircleShape)
                        .background(glassColors.accent)
                        .clickable {
                            FlowHaptics.tick(hapticFeedback, view)
                            onNavigateToCreateRoutine()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Routine",
                        tint = onAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 2. Modes (monochrome chips)
        if (modes.isNotEmpty()) {
            item(key = "modes_section") {
                Column {
                    SectionHeader(title = "Modes")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(modes, key = { it.id }) { mode ->
                            val isSelected = mode.isActive
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) glassColors.accent else glassColors.glassSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) glassColors.accent else glassColors.glassBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        FlowHaptics.tick(hapticFeedback, view)
                                        onNavigateToModeDetail(mode.id)
                                    }
                                    .padding(horizontal = 13.dp, vertical = 9.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(mode.iconName),
                                        contentDescription = null,
                                        tint = if (isSelected) onAccent else glassColors.textSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )

                                    Text(
                                        text = mode.name,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) onAccent else glassColors.textSecondary
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable {
                                                FlowHaptics.modeToggle(hapticFeedback, view, !isSelected)
                                                onToggleMode(mode)
                                            }
                                            .padding(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) onAccent else glassColors.textMuted)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Routines
        item(key = "routines_section") {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "Routines")
                    Text(
                        text = "${routines.size} total",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textSecondary
                    )
                }

                if (routines.isEmpty()) {
                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToCreateRoutine
                    ) {
                        FlowEmptyState(
                            title = "No routines yet",
                            description = "Create your first routine to automate daily tasks",
                            buttonText = "Create Routine",
                            onButtonClick = onNavigateToCreateRoutine
                        )
                    }
                } else {
                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column {
                            recentRoutines.forEachIndexed { index, routine ->
                                val triggerSummary = remember(routine.conditions) {
                                    routine.conditions.firstOrNull()?.summary
                                        ?: if (routine.conditions.isEmpty()) "Manual" else "${routine.conditions.size} triggers"
                                }

                                FlowRowItem(
                                    title = routine.name,
                                    subtitle = triggerSummary,
                                    icon = IconHelper.getIconByName(routine.iconName),
                                    onClick = { onNavigateToRoutineDetail(routine.id) },
                                    trailing = {
                                        FlowSwitch(
                                            checked = routine.isEnabled,
                                            onCheckedChange = { onToggleRoutine(routine) }
                                        )
                                    }
                                )

                                if (index < recentRoutines.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .height(0.5.dp)
                                            .background(glassColors.glassBorder)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Recent activity
        if (recentLogs.isNotEmpty()) {
            item(key = "activity_section") {
                Column {
                    SectionHeader(title = "Recent Activity")

                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column {
                            recentLogs.forEachIndexed { index, log ->
                                val timeStr = remember(log.timestamp) { timeFormat.format(Date(log.timestamp)) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (log.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (log.isSuccess) StatusSuccess else StatusError,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = log.routineName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = glassColors.textPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (log.isSuccess) "Completed" else (log.errorMessage ?: "Failed"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (log.isSuccess) glassColors.textSecondary else StatusError,
                                            maxLines = 1
                                        )
                                    }

                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = glassColors.textMuted
                                    )
                                }

                                if (index < recentLogs.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .height(0.5.dp)
                                            .background(glassColors.glassBorder)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = LocalFlowGlassColors.current.textMuted,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}
