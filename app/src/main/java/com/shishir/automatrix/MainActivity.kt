package com.shishir.automatrix

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.shishir.automatrix.ui.navigation.FlowAppContent
import com.shishir.automatrix.ui.theme.AutoMatrixTheme
import com.shishir.automatrix.ui.viewmodel.FlowViewModel
import com.shishir.automatrix.util.DisplayRefreshRateHelper
import com.shishir.automatrix.util.NfcManager

class MainActivity : ComponentActivity() {

    private val viewModel: FlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DisplayRefreshRateHelper.optimizeWindowForHighRefreshRate(this)
        handleNfcIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            AutoMatrixTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FlowAppContent(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: android.content.Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        if (action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_TECH_DISCOVERED
        ) {
            val tagId = NfcManager.parseTagId(intent)
            val payload = NfcManager.parseNdefPayload(intent)
            viewModel.handleNfcTagScanned(tagId, payload)
        }
    }
}
