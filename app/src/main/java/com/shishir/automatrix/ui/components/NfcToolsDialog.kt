package com.shishir.automatrix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shishir.automatrix.domain.models.Mode
import com.shishir.automatrix.ui.theme.LocalFlowGlassColors
import com.shishir.automatrix.ui.theme.StatusSuccess
import com.shishir.automatrix.util.NfcManager

@Composable
fun NfcToolsDialog(
    modes: List<Mode>,
    onDismiss: () -> Unit,
    onSimulateNfcTap: (String) -> Unit
) {
    val context = LocalContext.current
    val glassColors = LocalFlowGlassColors.current

    val isSupported = remember { NfcManager.isNfcSupported(context) }
    val isEnabled = remember { NfcManager.isNfcEnabled(context) }

    var selectedModeId by remember {
        mutableStateOf(modes.firstOrNull()?.id ?: "mode_work")
    }
    var showWriteGuidance by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = glassColors.glassSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LocalFlowGlassColors.current.accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "NFC",
                        tint = LocalFlowGlassColors.current.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "NFC Tag Triggers",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = glassColors.textPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status chip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(glassColors.accent.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSupported && isEnabled) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isSupported && isEnabled) StatusSuccess else glassColors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSupported && isEnabled) "Hardware NFC Active"
                        else if (isSupported) "NFC is turned OFF in System Settings"
                        else "NFC Simulation Mode (Emulated/Ready)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = glassColors.textPrimary
                    )
                }

                Text(
                    text = "Select Focus Mode to link with an NFC sticker:",
                    style = MaterialTheme.typography.bodySmall,
                    color = glassColors.textSecondary
                )

                // Mode Chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    modes.forEach { mode ->
                        val isSelected = mode.id == selectedModeId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) LocalFlowGlassColors.current.accent.copy(alpha = 0.2f)
                                    else glassColors.glassBorder.copy(alpha = 0.3f)
                                )
                                .clickable { selectedModeId = mode.id }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = mode.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) LocalFlowGlassColors.current.accent else glassColors.textPrimary
                                )
                                Text(
                                    text = "Tag URI: automatrix://mode/${mode.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glassColors.textSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = LocalFlowGlassColors.current.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (showWriteGuidance) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(LocalFlowGlassColors.current.accent.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🏷️ Ready! Hold any standard physical NFC sticker/tag against the back of your device. AutoMatrix will automatically detect and write the configuration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = glassColors.textPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSimulateNfcTap(selectedModeId)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LocalFlowGlassColors.current.accent)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Test Tap Now",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = { showWriteGuidance = !showWriteGuidance }
                ) {
                    Text(
                        text = if (showWriteGuidance) "Hide Guide" else "Write Tag",
                        color = glassColors.textPrimary
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = glassColors.textSecondary)
                }
            }
        }
    )
}
