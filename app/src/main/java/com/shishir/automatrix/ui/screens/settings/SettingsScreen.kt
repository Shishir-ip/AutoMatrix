package com.shishir.automatrix.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shishir.automatrix.automation.services.PhoneOrientation
import com.shishir.automatrix.domain.models.Mode
import com.shishir.automatrix.ui.components.FlowGlassCard
import com.shishir.automatrix.ui.components.FlowRowItem
import com.shishir.automatrix.ui.components.FlowSwitch
import com.shishir.automatrix.ui.components.NfcToolsDialog
import com.shishir.automatrix.ui.motion.FlowHaptics
import com.shishir.automatrix.ui.motion.FlowMotion
import com.shishir.automatrix.ui.motion.flowPress
import com.shishir.automatrix.ui.theme.LocalFlowGlassColors
import com.shishir.automatrix.permissions.PermissionManager

private const val DEVELOPER_GITHUB_URL = "https://github.com/Shishir-ip"

@Composable
fun SettingsScreen(
    themeMode: String,
    isDeveloperMode: Boolean,
    isFlipToShhhEnabled: Boolean,
    isShakeTriggerEnabled: Boolean,
    isCalendarTriggerEnabled: Boolean,
    phoneOrientation: PhoneOrientation,
    modes: List<Mode>,
    onThemeModeChange: (String) -> Unit,
    onDeveloperModeChange: (Boolean) -> Unit,
    onFlipToShhhChange: (Boolean) -> Unit,
    onShakeTriggerChange: (Boolean) -> Unit,
    onCalendarTriggerChange: (Boolean) -> Unit,
    onSimulateNfcTap: (String) -> Unit,
    onExportBackup: () -> String,
    onImportBackup: (String) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToDeveloper: () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showNfcDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var importInputText by remember { mutableStateOf("") }

    val hasBatteryExemption = remember {
        PermissionManager.checkBatteryOptimizationIgnored(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item(key = "header") {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = glassColors.textPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Personalize AutoMatrix and its triggers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassColors.textSecondary
                )
            }
        }

        // Section: Appearance
        item(key = "section_appearance") {
            SettingsSection(title = "Appearance") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (key, label) ->
                            val isSelected = themeMode == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .flowPress(level = FlowMotion.Level.SUBTLE)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) glassColors.accent else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSelected) glassColors.accent else glassColors.glassBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        FlowHaptics.selection(hapticFeedback, view)
                                        onThemeModeChange(key)
                                    }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        if (glassColors.accent == Color.White) Color.Black else Color.White
                                    } else glassColors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Triggers
        item(key = "section_triggers") {
            SettingsSection(title = "Triggers") {
                Column {
                    FlowRowItem(
                        title = "Flip to Shhh",
                        subtitle = "Face down silences notifications and starts Focus",
                        icon = Icons.Default.Smartphone,
                        trailing = {
                            FlowSwitch(
                                checked = isFlipToShhhEnabled,
                                onCheckedChange = onFlipToShhhChange
                            )
                        }
                    )
                    if (isFlipToShhhEnabled) {
                        Text(
                            text = "Live sensor: ${phoneOrientation.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (phoneOrientation == PhoneOrientation.FACE_DOWN) {
                                glassColors.accent
                            } else glassColors.textMuted,
                            modifier = Modifier.padding(start = 58.dp, bottom = 10.dp)
                        )
                    }

                    SettingsSeparator()

                    FlowRowItem(
                        title = "Shake Gesture",
                        subtitle = "Shake the device to run your top automation",
                        icon = Icons.Default.ScreenRotation,
                        trailing = {
                            FlowSwitch(
                                checked = isShakeTriggerEnabled,
                                onCheckedChange = onShakeTriggerChange
                            )
                        }
                    )

                    SettingsSeparator()

                    FlowRowItem(
                        title = "Calendar Events",
                        subtitle = "Start Focus automatically during busy events",
                        icon = Icons.Default.CalendarMonth,
                        trailing = {
                            FlowSwitch(
                                checked = isCalendarTriggerEnabled,
                                onCheckedChange = onCalendarTriggerChange
                            )
                        }
                    )

                    SettingsSeparator()

                    FlowRowItem(
                        title = "NFC Tags",
                        subtitle = "Write and scan NFC tags to switch modes",
                        icon = Icons.Default.Nfc,
                        onClick = { showNfcDialog = true }
                    )
                }
            }
        }

        // Section: Data
        item(key = "section_data") {
            SettingsSection(title = "Data") {
                Column {
                    FlowRowItem(
                        title = "Export Backup",
                        subtitle = "Save routines as JSON",
                        icon = Icons.Default.CloudDownload,
                        onClick = {
                            exportedJsonText = onExportBackup()
                            showExportDialog = true
                        }
                    )
                    SettingsSeparator()
                    FlowRowItem(
                        title = "Import Backup",
                        subtitle = "Restore routines from JSON",
                        icon = Icons.Default.CloudUpload,
                        onClick = {
                            importInputText = ""
                            showImportDialog = true
                        }
                    )
                }
            }
        }

        // Section: General
        item(key = "section_general") {
            SettingsSection(title = "General") {
                Column {
                    FlowRowItem(
                        title = "Permissions",
                        subtitle = "Manage system access",
                        icon = Icons.Default.Security,
                        onClick = onNavigateToPermissions
                    )
                    SettingsSeparator()
                    FlowRowItem(
                        title = "Execution Logs",
                        subtitle = "History of automation runs",
                        icon = Icons.Default.ListAlt,
                        onClick = onNavigateToLogs
                    )
                    SettingsSeparator()
                    FlowRowItem(
                        title = "Battery Optimization",
                        subtitle = if (hasBatteryExemption) "Unrestricted — triggers stay reliable" else "Restricted — triggers may be delayed",
                        icon = Icons.Default.BatteryAlert,
                        onClick = {
                            PermissionManager.requestIgnoreBatteryOptimizations(context)
                        }
                    )
                }
            }
        }

        // Section: About
        item(key = "section_about") {
            SettingsSection(title = "About") {
                Column {
                    FlowRowItem(
                        title = "Version",
                        subtitle = "AutoMatrix 1.0.0",
                        icon = Icons.Default.Info
                    )
                    SettingsSeparator()
                    FlowRowItem(
                        title = "Developer",
                        subtitle = "SHISHIR",
                        icon = Icons.Default.Person
                    )
                    SettingsSeparator()
                    FlowRowItem(
                        title = "GitHub",
                        subtitle = DEVELOPER_GITHUB_URL.removePrefix("https://"),
                        icon = Icons.Default.Code,
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(DEVELOPER_GITHUB_URL))
                            )
                        }
                    )
                    SettingsSeparator()
                    FlowRowItem(
                        title = "Developer Mode",
                        subtitle = "Diagnostics and test triggers",
                        icon = Icons.Default.BugReport,
                        trailing = {
                            FlowSwitch(
                                checked = isDeveloperMode,
                                onCheckedChange = onDeveloperModeChange
                            )
                        }
                    )
                    if (isDeveloperMode) {
                        SettingsSeparator()
                        FlowRowItem(
                            title = "Developer Tools",
                            icon = Icons.Default.BugReport,
                            onClick = onNavigateToDeveloper
                        )
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Backup Exported") },
            text = {
                Column {
                    Text("Your routines as JSON:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("AutoMatrix Backup", exportedJsonText))
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = glassColors.accent,
                        contentColor = if (glassColors.accent == Color.White) Color.Black else Color.White
                    )
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Backup") },
            text = {
                Column {
                    Text("Paste your exported AutoMatrix JSON:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        placeholder = { Text("Paste JSON here") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importInputText.isNotBlank()) {
                            onImportBackup(importInputText.trim())
                        }
                        showImportDialog = false
                    },
                    enabled = importInputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = glassColors.accent,
                        contentColor = if (glassColors.accent == Color.White) Color.Black else Color.White
                    )
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNfcDialog) {
        NfcToolsDialog(
            modes = modes,
            onDismiss = { showNfcDialog = false },
            onSimulateNfcTap = { modeId ->
                onSimulateNfcTap(modeId)
                showNfcDialog = false
            }
        )
    }
}

@Composable
private fun SettingsSeparator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(0.5.dp)
            .background(LocalFlowGlassColors.current.glassBorder)
    )
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current

    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = glassColors.textMuted,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        FlowGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                content()
            }
        }
    }
}
