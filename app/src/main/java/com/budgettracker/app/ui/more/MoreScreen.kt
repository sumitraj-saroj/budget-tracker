package com.budgettracker.app.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MoreScreen(
    onAccounts: () -> Unit,
    onGoals: () -> Unit,
    onDebts: () -> Unit,
    onSubscriptions: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "More",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        MoreItem(
            icon = Icons.Rounded.AccountBalanceWallet,
            title = "Accounts",
            subtitle = "Cash, bank, cards & multi-currency balances",
            onClick = onAccounts,
        )
        MoreItem(
            icon = Icons.Rounded.Flag,
            title = "Savings goals",
            subtitle = "Set targets and track progress",
            onClick = onGoals,
        )
        MoreItem(
            icon = Icons.Rounded.Handshake,
            title = "Debts & loans",
            subtitle = "Money lent out or borrowed",
            onClick = onDebts,
        )
        MoreItem(
            icon = Icons.Rounded.Subscriptions,
            title = "Subscriptions",
            subtitle = "Recurring payments & upcoming bills",
            onClick = onSubscriptions,
        )
        MoreItem(
            icon = Icons.Rounded.Settings,
            title = "Settings",
            subtitle = "Theme, accent color, security & backups",
            onClick = onSettings,
        )
        Text(
            "Budget Tracker v1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp, bottom = 32.dp),
        )
    }
}

@Composable
private fun MoreItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.size(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
