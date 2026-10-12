package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)
    private val db = TaskEarnDatabase.getDatabase(application)

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity> = repository.userProfile
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserProfileEntity(
                balanceCoins = 150,
                streakCount = 3,
                lastCheckInDate = "",
                referralCode = "TEARN889",
                totalEarned = 350,
                tasksCompletedCount = 7
            )
        )

    init {
        viewModelScope.launch {
            // Check if tasks exist, if not seed initial tasks
            db.taskDao().getAllTasks().collect { taskList ->
                if (taskList.isEmpty()) {
                    val initialTasks = listOf(
                        TaskEntity(title = "PocketPay App Install", description = "Install, open for 30s & explore features", category = "Apps", rewardCoins = 100, timeMinutes = 3, iconName = "phone_android"),
                        TaskEntity(title = "Customer Feedback Survey", description = "Answer 5 quick questions about online shopping", category = "Surveys", rewardCoins = 50, timeMinutes = 2, iconName = "quiz"),
                        TaskEntity(title = "Watch Sponsored Ad Clip", description = "Watch full 60s video and earn instant coins", category = "Video", rewardCoins = 25, timeMinutes = 1, iconName = "play_circle"),
                        TaskEntity(title = "Math & Logic Puzzle Quiz", description = "Solve 4 basic math questions correctly", category = "Quiz", rewardCoins = 40, timeMinutes = 2, iconName = "calculate"),
                        TaskEntity(title = "AI Data Tagging Task", description = "Categorize 10 sample images accurately", category = "Apps", rewardCoins = 80, timeMinutes = 4, iconName = "label"),
                        TaskEntity(title = "Daily Shopping Habit Poll", description = "Quick 1 minute survey on grocery habits", category = "Surveys", rewardCoins = 35, timeMinutes = 1, iconName = "poll"),
                        TaskEntity(title = "Product Unboxing Video", description = "Watch 45s tech review clip", category = "Video", rewardCoins = 30, timeMinutes = 1, iconName = "smart_display"),
                        TaskEntity(title = "General Trivia Challenge", description = "Test your general knowledge with 5 questions", category = "Quiz", rewardCoins = 45, timeMinutes = 2, iconName = "psychology")
                    )
                    db.taskDao().insertAll(initialTasks)
                }
            }
        }

        viewModelScope.launch {
            // Check if user profile exists
            db.userDao().getUserProfile().collect { profile ->
                if (profile == null) {
                    db.userDao().upsertUser(UserProfileEntity())
                    // Add initial welcome transaction
                    repository.addTransaction("Welcome Bonus", "+150 Coins", "EARNED", "Success")
                }
            }
        }
    }

    fun claimDailyBonus() {
        viewModelScope.launch {
            val current = userProfile.value
            val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            if (current.lastCheckInDate == todayStr) return@launch // Already claimed today

            val bonusCoins = 50
            val newBalance = current.balanceCoins + bonusCoins
            val newStreak = current.streakCount + 1
            val newTotalEarned = current.totalEarned + bonusCoins

            db.userDao().upsertUser(
                current.copy(
                    balanceCoins = newBalance,
                    streakCount = newStreak,
                    lastCheckInDate = todayStr,
                    totalEarned = newTotalEarned
                )
            )

            repository.addTransaction("Daily Check-in Bonus", "+$bonusCoins Coins", "EARNED", "Success")
        }
    }

    fun completeTask(task: TaskEntity) {
        viewModelScope.launch {
            if (task.isCompleted) return@launch
            db.taskDao().markTaskCompleted(task.id)

            val current = userProfile.value
            val newBalance = current.balanceCoins + task.rewardCoins
            val newTotalEarned = current.totalEarned + task.rewardCoins
            val newCompletedCount = current.tasksCompletedCount + 1

            db.userDao().upsertUser(
                current.copy(
                    balanceCoins = newBalance,
                    totalEarned = newTotalEarned,
                    tasksCompletedCount = newCompletedCount
                )
            )

            repository.addTransaction(task.title, "+${task.rewardCoins} Coins", "EARNED", "Success")
        }
    }

    fun requestWithdrawal(method: String, destination: String, coins: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val current = userProfile.value
            if (current.balanceCoins < coins) {
                onResult(false, "Insufficient coins balance!")
                return@launch
            }

            val rupees = coins / 10 // Conversion rate: 10 coins = ₹1
            val newBalance = current.balanceCoins - coins

            db.userDao().upsertUser(current.copy(balanceCoins = newBalance))
            repository.addTransaction("Withdrawal via $method ($destination)", "-₹$rupees", "WITHDRAWN", "Pending")
            onResult(true, "Withdrawal request of ₹$rupees submitted successfully!")
        }
    }
}
