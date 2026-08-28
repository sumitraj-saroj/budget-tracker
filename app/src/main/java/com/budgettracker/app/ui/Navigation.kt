package com.budgettracker.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.budgettracker.app.AppViewModel
import com.budgettracker.app.util.hapticKey
import com.budgettracker.app.util.hapticTick
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    const val TXNS = "transactions?categoryId={categoryId}"
    const val TXNS_PLAIN = "transactions"
    const val STATS = "stats"
    const val BUDGETS = "budgets"
    const val MORE = "more"
    const val ACCOUNTS = "accounts"
    const val GOALS = "goals"
    const val DEBTS = "debts"
    const val SUBSCRIPTIONS = "subscriptions"
    const val SETTINGS = "settings"
    const val TX_EDIT = "txedit/{txId}?categoryId={categoryId}&type={type}"
    const val BUDGET_EDIT = "budgetedit/{budgetId}"

    fun txEdit(id: Long) = "txedit/$id"
    fun txEditQuick(categoryId: Long, type: String = "EXPENSE") = "txedit/-1?categoryId=$categoryId&type=$type"
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
fun MainNavHost(appViewModel: AppViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showChrome = currentRoute in topLevelDestinations.map { it.route }

    // Backup badge on the More tab when there is data and the last backup is stale.
    val txCount by appViewModel.txCount.collectAsStateWithLifecycle()
    val prefs by appViewModel.prefs.collectAsStateWithLifecycle()
    val backupStale = txCount > 15 &&
        (prefs?.lastBackupAt == null || System.currentTimeMillis() - (prefs?.lastBackupAt ?: 0L) > 14L * 24 * 3600 * 1000)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showChrome) {
                FloatingBottomBar(
                    currentRoute = currentRoute,
                    destinations = topLevelDestinations,
                    showBackupBadge = backupStale,
                    onSelect = { navController.navigateTopLevel(it) },
                )
            }
        },
        floatingActionButton = {
            if (showChrome) {
                QuickAddFab(
                    onQuickAdd = { type ->
                        navController.navigate("txedit/-1?type=$type")
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
            enterTransition = {
                androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220)) +
                    androidx.compose.animation.slideInVertically(
                        androidx.compose.animation.core.tween(220),
                    ) { it / 28 }
            },
            exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(150)) },
            popEnterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220)) },
            popExitTransition = {
                androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(150)) +
                    androidx.compose.animation.slideOutVertically(
                        androidx.compose.animation.core.tween(220),
                    ) { it / 28 }
            },
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onTxClick = { id -> navController.navigate(Routes.txEdit(id)) },
                    onAddTx = { navController.navigate(Routes.txEdit(-1)) },
                    onQuickAdd = { categoryId ->
                        navController.navigate(Routes.txEditQuick(categoryId))
                    },
                    onOpenBudgets = { navController.navigateTopLevel(Routes.BUDGETS) },
                    onOpenTransactions = { navController.navigateTopLevel(Routes.TXNS_PLAIN) },
                    onOpenSubscriptions = { navController.navigate(Routes.SUBSCRIPTIONS) },
                    onOpenGoals = { navController.navigate(Routes.GOALS) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(
                route = Routes.TXNS,
                arguments = listOf(
                    androidx.navigation.navArgument("categoryId") {
                        type = androidx.navigation.NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                TransactionsScreen(
                    onTxClick = { id -> navController.navigate(Routes.txEdit(id)) },
                    onAddTx = { navController.navigate(Routes.txEdit(-1)) },
                )
            }
            composable(Routes.STATS) {
                StatsScreen(
                    onOpenCategory = { categoryId ->
                        navController.navigate("${Routes.TXNS_PLAIN}?categoryId=$categoryId")
                    },
                )
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
            composable(
                route = Routes.TX_EDIT,
                arguments = listOf(
                    androidx.navigation.navArgument("categoryId") {
                        type = androidx.navigation.NavType.LongType
                        defaultValue = -1L
                    },
                    androidx.navigation.navArgument("type") {
                        type = androidx.navigation.NavType.StringType
                        defaultValue = "EXPENSE"
                    },
                ),
            ) { entry ->
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

/** FAB: tap = quick expense, long-press = pick expense / income / transfer. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun QuickAddFab(onQuickAdd: (String) -> Unit) {
    val view = androidx.compose.ui.platform.LocalView.current
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = androidx.compose.material3.FloatingActionButtonDefaults.shape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(56.dp)
                .clip(androidx.compose.material3.FloatingActionButtonDefaults.shape)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = androidx.compose.material3.ripple(),
                    onClick = {
                        view.hapticKey()
                        onQuickAdd("EXPENSE")
                    },
                    onLongClick = {
                        view.hapticTick()
                        menuOpen = true
                    },
                ),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Rounded.Add, contentDescription = "Add transaction", modifier = Modifier.size(26.dp))
            }
        }
        androidx.compose.material3.DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
        ) {
            listOf(
                "Expense" to "EXPENSE",
                "Income" to "INCOME",
                "Transfer" to "TRANSFER",
            ).forEach { (label, type) ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        menuOpen = false
                        view.hapticKey()
                        onQuickAdd(type)
                    },
                )
            }
        }
    }
}

/**
 * Floating pill navigation bar: a detached rounded surface with icon-only
 * destinations; the selected tab expands into a pill with its label.
 */
@Composable
private fun FloatingBottomBar(
    currentRoute: String?,
    destinations: List<TopLevelDestination>,
    showBackupBadge: Boolean,
    onSelect: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 2.dp,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEach { destination ->
                    val view = androidx.compose.ui.platform.LocalView.current
                    val selected = currentRoute == destination.route
                    val circleColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            Color.Transparent
                        },
                        animationSpec = tween(250),
                        label = "navCircleColor",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                view.hapticTick()
                                onSelect(destination.route)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(50))
                                .background(circleColor)
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = if (selected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(24.dp),
                            )
                            if (destination.route == Routes.MORE && showBackupBadge) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(10.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
