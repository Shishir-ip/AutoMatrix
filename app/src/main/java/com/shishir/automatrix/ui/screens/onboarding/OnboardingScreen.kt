package com.shishir.automatrix.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shishir.automatrix.ui.components.FlowPrimaryButton
import com.shishir.automatrix.ui.motion.FlowHaptics
import com.shishir.automatrix.ui.theme.LocalFlowGlassColors
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val glassColors = LocalFlowGlassColors.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val pagerState = rememberPagerState(pageCount = { 3 })

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result does not block entry; the app asks contextually later
        onFinished()
    }

    fun finish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onFinished()
        }
    }

    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.Shield,
            title = "Welcome to AutoMatrix",
            body = "Automate your phone and protect your focus. Everything runs on your device — no account, no cloud, no tracking."
        ),
        OnboardingPage(
            icon = Icons.Default.Bolt,
            title = "Routines & Modes",
            body = "Routines react to triggers like time, Wi-Fi, charging or notifications. Modes reconfigure your phone in one tap.",
            bullets = listOf(
                Triple(Icons.Default.Bolt, "Routines", "Trigger → condition → action"),
                Triple(Icons.Default.Tune, "Modes", "One-tap focus profiles"),
                Triple(Icons.Default.Nfc, "NFC & gestures", "Tap a sticker or flip your phone")
            )
        ),
        OnboardingPage(
            icon = Icons.Default.Notifications,
            title = "You're in control",
            body = "Permissions are requested only as you use features — manage everything anytime in Settings → Permissions. Notifications permission is optional but recommended.",
            bullets = null
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            val page = pages[index]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(glassColors.accent.copy(alpha = 0.1f))
                        .border(1.dp, glassColors.glassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = glassColors.accent,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = glassColors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = page.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = glassColors.textSecondary,
                    textAlign = TextAlign.Center
                )

                if (page.bullets != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    page.bullets.forEach { (icon, title, subtitle) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(glassColors.accent.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = glassColors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = glassColors.textPrimary
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Page indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (selected) 8.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) glassColors.accent else glassColors.glassBorder
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        FlowPrimaryButton(
            text = if (pagerState.currentPage < 2) "Continue" else "Get Started",
            onClick = {
                FlowHaptics.impact(hapticFeedback, view)
                if (pagerState.currentPage < 2) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    finish()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
    val bullets: List<Triple<ImageVector, String, String>>? = null
)
