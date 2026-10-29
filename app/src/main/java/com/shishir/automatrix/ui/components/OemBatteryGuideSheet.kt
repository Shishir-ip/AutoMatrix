package com.shishir.automatrix.ui.components

import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shishir.automatrix.permissions.PermissionManager
import com.shishir.automatrix.ui.theme.LocalFlowGlassColors

private data class OemGuide(
    val title: String,
    val steps: List<String>
)

/**
 * Step-by-step guide for the aggressive battery managers that ship on many
 * Android devices and silently kill background automation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OemBatteryGuideSheet(onDismiss: () -> Unit) {
    val glassColors = LocalFlowGlassColors.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val manufacturer = Build.MANUFACTURER.lowercase()
    val guides = listOf(
        OemGuide(
            title = "Xiaomi · Redmi · POCO (MIUI/HyperOS)",
            steps = listOf(
                "Open the Security app → Autostart → enable AutoMatrix",
                "Settings → Battery → tap AutoMatrix → No restrictions",
                "In recents, long-press the AutoMatrix card → tap the lock icon",
                "Notifications: allow Autostart and Background popup"
            )
        ),
        OemGuide(
            title = "Samsung (One UI)",
            steps = listOf(
                "Settings → Battery → Background usage limits → Never sleeping apps → add AutoMatrix",
                "Settings → Apps → AutoMatrix → Battery → Unrestricted",
                "Settings → Device care → Battery → turn off 'Adaptive battery' if triggers delay"
            )
        ),
        OemGuide(
            title = "Huawei · Honor (EMUI/Magic OS)",
            steps = listOf(
                "Settings → Battery → App launch → AutoMatrix → Manage manually",
                "Enable all three: Auto-launch, Secondary launch, Run in background",
                "Settings → Battery → More battery settings → turn off 'Sleep mode' optimization"
            )
        ),
        OemGuide(
            title = "OnePlus · Oppo · Realme (ColorOS)",
            steps = listOf(
                "Settings → Battery → More settings → Optimize battery use → AutoMatrix → Don't optimize",
                "Settings → Apps → AutoMatrix → Battery usage → Allow background activity",
                "In recents, pull the AutoMatrix card down to lock it"
            )
        ),
        OemGuide(
            title = "Vivo · iQOO (OriginOS/Funtouch)",
            steps = listOf(
                "Settings → Battery → Background power consumption → AutoMatrix → Allow high background power",
                "i Manager → App manager → Autostart → enable AutoMatrix"
            )
        ),
        OemGuide(
            title = "Pixel · Motorola · Stock Android",
            steps = listOf(
                "Settings → Apps → AutoMatrix → Battery → Unrestricted",
                "Most stock devices run automation reliably after this one step"
            )
        )
    )

    val manufacturerGuide = guides.firstOrNull { guide ->
        guide.title.split('·').any { brand ->
            manufacturer.contains(brand.trim().lowercase().split(' ').first())
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = glassColors.glassSurface,
        tonalElevation = 0.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = 24.dp, topEnd = 24.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Battery & Startup Guide",
                style = MaterialTheme.typography.titleLarge,
                color = glassColors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Many phones quietly stop automation apps to save battery. These steps keep AutoMatrix running reliably. Recommended steps for your device (${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }}) are listed first.",
                style = MaterialTheme.typography.bodySmall,
                color = glassColors.textSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            val orderedGuides = if (manufacturerGuide != null) {
                listOf(manufacturerGuide) + guides.filter { it != manufacturerGuide }
            } else {
                guides
            }

            orderedGuides.forEach { guide ->
                val isRecommended = guide == manufacturerGuide
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                        .background(glassColors.accent.copy(alpha = if (isRecommended) 0.07f else 0f))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = guide.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = glassColors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isRecommended) {
                            Text(
                                text = "Your device",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = glassColors.textSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    guide.steps.forEachIndexed { index, step ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(glassColors.accent.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = glassColors.accent
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.textSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            FlowPrimaryButton(
                text = "Disable Battery Optimization",
                onClick = {
                    PermissionManager.requestIgnoreBatteryOptimizations(context)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
