package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TaskEntity
import com.example.ui.screens.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Tasks : Screen("tasks", "Tasks", Icons.Default.Assignment)
    object Wallet : Screen("wallet", "Wallet", Icons.Default.AccountBalanceWallet)
    object Refer : Screen("refer", "Refer", Icons.Default.Share)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEarnApp(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var taskCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedTaskForDetail by remember { mutableStateOf<TaskEntity?>(null) }

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    val screens = listOf(Screen.Home, Screen.Tasks, Screen.Wallet, Screen.Refer, Screen.Profile)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = {
                            if (screen == Screen.Tasks) {
                                taskCategoryFilter = null
                            }
                            currentScreen = screen
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Home -> HomeScreen(
                    userProfile = userProfile,
                    tasks = tasks,
                    onClaimDaily = { viewModel.claimDailyBonus() },
                    onTaskClick = { task -> selectedTaskForDetail = task },
                    onNavigateToTasks = { category ->
                        taskCategoryFilter = category
                        currentScreen = Screen.Tasks
                    },
                    onNavigateToWallet = { currentScreen = Screen.Wallet }
                )
                is Screen.Tasks -> TaskCenterScreen(
                    tasks = tasks,
                    initialCategoryFilter = taskCategoryFilter,
                    onTaskClick = { task -> selectedTaskForDetail = task }
                )
                is Screen.Wallet -> WalletScreen(
                    userProfile = userProfile,
                    transactions = transactions,
                    onRequestWithdrawal = { method, dest, coins, callback ->
                        viewModel.requestWithdrawal(method, dest, coins, callback)
                    }
                )
                is Screen.Refer -> ReferralScreen(userProfile = userProfile)
                is Screen.Profile -> ProfileScreen(userProfile = userProfile)
            }
        }
    }

    // Task Detail Dialog
    if (selectedTaskForDetail != null) {
        TaskDetailDialog(
            task = selectedTaskForDetail!!,
            onDismiss = { selectedTaskForDetail = null },
            onComplete = { task ->
                viewModel.completeTask(task)
            }
        )
    }
}
