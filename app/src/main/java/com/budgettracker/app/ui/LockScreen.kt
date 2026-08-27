package com.budgettracker.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.budgettracker.app.security.BiometricLock
import com.budgettracker.app.util.findActivity

@Composable
fun LockScreen(onUnlock: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity() as? FragmentActivity
    var error by remember { mutableStateOf<String?>(null) }

    fun prompt() {
        val fragmentActivity = activity ?: run {
            onUnlock()
            return
        }
        BiometricLock.authenticate(
            activity = fragmentActivity,
            title = "Unlock Budget Tracker",
            subtitle = "Use your fingerprint, face, or screen lock",
            onSuccess = onUnlock,
            onError = { message ->
                if (message != "cancelled") error = message
            },
        )
    }

    LaunchedEffect(Unit) { prompt() }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Fingerprint,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(88.dp),
            )
            Spacer(Modifier.size(24.dp))
            Text("Budget Tracker is locked", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.size(8.dp))
            Text(
                text = error ?: "Use your fingerprint, face, or screen lock to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(32.dp))
            Button(onClick = { prompt() }) { Text("Unlock") }
        }
    }
}
