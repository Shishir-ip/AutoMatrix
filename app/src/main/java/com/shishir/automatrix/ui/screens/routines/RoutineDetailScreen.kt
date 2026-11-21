package com.shishir.automatrix.ui.screens.routines

import androidx.compose.material3.ExperimentalMaterial3Api
import com.shishir.automatrix.ui.components.FlowOutlinedActionButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shishir.automatrix.domain.models.Routine
import com.shishir.automatrix.ui.components.FlowGlassCard
import com.shishir.automatrix.ui.components.FlowPrimaryButton
import com.shishir.automatrix.ui.components.FlowPriorityBadge
import com.shishir.automatrix.ui.components.FlowSwitch
import com.shishir.automatrix.ui.components.IconHelper
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextAlign
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.shishir.automatrix.ui.components.QrCodeImage
import com.shishir.automatrix.ui.components.sheetScrollWithoutDismiss
import com.shishir.automatrix.util.ShareCodec
import com.shishir.automatrix.ui.theme.LocalFlowGlassColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    routine: Routine?,
    onNavigateBack: () -> Unit,
    onEditRoutine: () -> Unit,
    onDeleteRoutine: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onRunNow: () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val context = LocalContext.current
    var showShareSheet by remember { mutableStateOf(false) }

    if (routine == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Routine not found", color = glassColors.textSecondary)
        }
        return
    }

    val accentColor = LocalFlowGlassColors.current.accent

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = glassColors.textPrimary
                    )
                }
                Row {
                    IconButton(onClick = onEditRoutine) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = LocalFlowGlassColors.current.accent
                        )
                    }
                    IconButton(onClick = onDeleteRoutine) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = LocalFlowGlassColors.current.accent
                        )
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                FlowPrimaryButton(
                    text = "Test Run Routine",
                    onClick = onRunNow,
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            item(key = "header_card") {
                FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(accentColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconByName(routine.iconName),
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = routine.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = glassColors.textPrimary
                            )
                            if (routine.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = routine.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FlowPriorityBadge(priority = routine.priority)
                                Text(
                                    text = "· Cooldown ${routine.cooldownSeconds}s",
                                    fontSize = 11.sp,
                                    color = glassColors.textMuted
                                )
                            }
                        }

                        FlowSwitch(
                            checked = routine.isEnabled,
                            onCheckedChange = onToggleEnabled
                        )
                    }
                }
            }

            // Stats summary row
            item(key = "stats_card") {
                FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Times Triggered",
                                fontSize = 12.sp,
                                color = glassColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${routine.triggerCount}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalFlowGlassColors.current.accent
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Last Executed",
                                fontSize = 12.sp,
                                color = glassColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val lastTime = routine.lastTriggeredTime
                            Text(
                                text = if (lastTime != null) SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(lastTime)) else "Never",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = glassColors.textPrimary
                            )
                        }
                    }
                }
            }

            // Share routine
            item(key = "share_card") {
                FlowGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showShareSheet = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = LocalFlowGlassColors.current.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Share this Routine",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = glassColors.textPrimary
                            )
                            Text(
                                text = "Show a QR code or send it to another device",
                                fontSize = 12.sp,
                                color = glassColors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = glassColors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // WHEN Conditions in single clean card
            item(key = "when_section") {
                Column {
                    Text(
                        text = "WHEN (${routine.conditionLogic.name})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = glassColors.textSecondary,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )

                    FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            if (routine.conditions.isEmpty()) {
                                Text(
                                    text = "No conditions (Runs manually or on all events)",
                                    fontSize = 13.sp,
                                    color = glassColors.textMuted,
                                    modifier = Modifier.padding(14.dp)
                                )
                            } else {
                                routine.conditions.forEachIndexed { index, cond ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(LocalFlowGlassColors.current.accent.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = IconHelper.getIconByName(cond.type.iconName),
                                                contentDescription = null,
                                                tint = LocalFlowGlassColors.current.accent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cond.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = glassColors.textPrimary
                                            )
                                            Text(
                                                text = cond.summary,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = glassColors.textSecondary
                                            )
                                        }
                                    }

                                    if (index < routine.conditions.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp)
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

            // THEN Actions in single clean card
            item(key = "then_section") {
                Column {
                    Text(
                        text = "THEN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = glassColors.textSecondary,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )

                    FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            if (routine.actions.isEmpty()) {
                                Text(
                                    text = "No actions configured",
                                    fontSize = 13.sp,
                                    color = glassColors.textMuted,
                                    modifier = Modifier.padding(14.dp)
                                )
                            } else {
                                routine.actions.forEachIndexed { index, act ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(LocalFlowGlassColors.current.accent.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = IconHelper.getIconByName(act.type.iconName),
                                                contentDescription = null,
                                                tint = LocalFlowGlassColors.current.accent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = act.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = glassColors.textPrimary
                                            )
                                            Text(
                                                text = act.summary,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = glassColors.textSecondary
                                            )
                                        }
                                    }

                                    if (index < routine.actions.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp)
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

    if (showShareSheet && routine != null) {
        val shareCode = remember(routine.id) { ShareCodec.encode(routine) }
        val qrFriendly = ShareCodec.isQrFriendly(shareCode)

        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = glassColors.glassSurface,
            tonalElevation = 0.dp,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .sheetScrollWithoutDismiss()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Share " + '"' + routine.name + '"',
                    style = MaterialTheme.typography.titleLarge,
                    color = glassColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "The routine imports disabled — review it before enabling.",
                    style = MaterialTheme.typography.bodySmall,
                    color = glassColors.textSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (qrFriendly) {
                    QrCodeImage(content = shareCode, size = 240.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Scan with any QR scanner, or open it on a device with AutoMatrix installed",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textMuted,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "This routine is too large for a QR code — use the buttons below instead.",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                FlowOutlinedActionButton(
                    text = "Copy share code",
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("AutoMatrix Routine", shareCode))
                        showShareSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowOutlinedActionButton(
                    text = "Share via apps",
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareCode)
                        }
                        context.startActivity(Intent.createChooser(send, "Share routine"))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}