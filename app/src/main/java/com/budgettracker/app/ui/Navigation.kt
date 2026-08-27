package com.budgettracker.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.budgettracker.app.ui.accounts.AccountsScreen
import com.budgettracker.app.ui.budgets.BudgetEditScreen
import com.budgettracker.app.ui.budgets.BudgetsScreen
import com.budgettracker.app.ui.debts.DebtsScreen
import com.budgettracker.app.ui.goals.GoalsScreen
import com.budgettracker.app.ui.home.HomeScreen
import com.budgettracker.app.ui.more.MoreScreen
import com.budgettracker.app.ui.settings.SettingsScreen
import com.budgettracker.app.ui.stats.StatsScreen
import com.budgettracker.app.ui.subscriptions.SubscriptionsScreen
import com.budgettracker.app.ui.transactions.TransactionEditScreen
import com.budgettracker.app.ui.transactions.TransactionsScreen

object Routes {
    const val HOME = "home"
    const val TXNS = "transactions"
    const val STATS = "stats"
    const val BUDGETS = "budgets"
    const val MORE = "more"
    const val ACCOUNTS = "accounts"
    const val GOALS = "goals"
    const val DEBTS = "debts"
    const val SUBSCRIPTIONS = "subscriptions"
    const val SETTINGS = "settings"
    const val TX_EDIT = "txedit/{txId}"
    const val BUDGET_EDIT = "budgetedit/{budgetId}"

    fun txEdit(id: Long) = "txedit/$id"
    fun budgetEdit(id: Long) = "budgetedit/$id"
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.HOME, "Home", Icons.Rounded.Home),
    TopLevelDestination(Routes.TXNS, "Records", Icons.Rounded.ReceiptLong),
    TopLevelDestination(Routes.STATS, "Stats", Icons.Rounded.PieChart),
    TopLevelDestination(Routes.BUDGETS, "Budgets", Icons.Rounded.AccountBalanceWallet),
    TopLevelDestination(Routes.MORE, "More", Icons.Rounded.MoreHoriz),
)

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showChrome = currentRoute in topLevelDestinations.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showChrome) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navController.navigateTopLevel(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showChrome) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.txEdit(-1)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add transaction")
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onTxClick = { id -> navController.navigate(Routes.txEdit(id)) },
                    onAddTx = { navController.navigate(Routes.txEdit(-1)) },
                    onOpenBudgets = { navController.navigateTopLevel(Routes.BUDGETS) },
                    onOpenTransactions = { navController.navigateTopLevel(Routes.TXNS) },
                    onOpenSubscriptions = { navController.navigate(Routes.SUBSCRIPTIONS) },
                    onOpenGoals = { navController.navigate(Routes.GOALS) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.TXNS) {
                TransactionsScreen(
                    onTxClick = { id -> navController.navigate(Routes.txEdit(id)) },
                    onAddTx = { navController.navigate(Routes.txEdit(-1)) },
                )
            }
            composable(Routes.STATS) {
                StatsScreen()
            }
            composable(Routes.BUDGETS) {
                BudgetsScreen(
                    onEditBudget = { id -> navController.navigate(Routes.budgetEdit(id)) },
                    onNewBudget = { navController.navigate(Routes.budgetEdit(-1)) },
                )
            }
            composable(Routes.MORE) {
                MoreScreen(
                    onAccounts = { navController.navigate(Routes.ACCOUNTS) },
                    onGoals = { navController.navigate(Routes.GOALS) },
                    onDebts = { navController.navigate(Routes.DEBTS) },
                    onSubscriptions = { navController.navigate(Routes.SUBSCRIPTIONS) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.ACCOUNTS) { AccountsScreen() }
            composable(Routes.GOALS) { GoalsScreen() }
            composable(Routes.DEBTS) { DebtsScreen() }
            composable(Routes.SUBSCRIPTIONS) { SubscriptionsScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(Routes.TX_EDIT) { entry ->
                val id = entry.arguments?.getString("txId")?.toLongOrNull() ?: -1L
                TransactionEditScreen(txId = id, onDone = { navController.popBackStack() })
            }
            composable(Routes.BUDGET_EDIT) { entry ->
                val id = entry.arguments?.getString("budgetId")?.toLongOrNull() ?: -1L
                BudgetEditScreen(budgetId = id, onDone = { navController.popBackStack() })
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
