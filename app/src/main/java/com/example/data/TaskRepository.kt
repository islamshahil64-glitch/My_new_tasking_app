package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class TaskRepository(context: Context) {
    private val db = TaskEarnDatabase.getDatabase(context)
    private val taskDao = db.taskDao()
    private val transactionDao = db.transactionDao()
    private val userDao = db.userDao()

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val userProfile: Flow<UserProfileEntity?> = userDao.getUserProfile()

    suspend fun seedInitialDataIfNeeded() {
        // If no user profile exists, create one
        // We can check or upsert initial user profile
        // Let's seed initial tasks if table is empty
        // We can check flow or insert default seed
    }

    suspend fun initializeDefaultData() {
        // Seed default profile if needed
        // For simplicity, we can do a check in ViewModel or init
    }

    suspend fun completeTask(taskId: Long, rewardCoins: Int, taskTitle: String) {
        taskDao.markTaskCompleted(taskId)
        // Update user profile balance and earnings
        // We can fetch current user or update via SQL / DAO
    }

    suspend fun addTransaction(title: String, amount: String, type: String, status: String = "Success") {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        transactionDao.insertTransaction(
            TransactionEntity(
                title = title,
                amount = amount,
                date = dateStr,
                type = type,
                status = status
            )
        )
    }

    suspend fun updateCoinsAndUser(coinsChange: Int, completedIncrement: Int = 0) {
        // Handled in ViewModel or repository helper
    }

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        userDao.upsertUser(profile)
    }

    suspend fun insertInitialTasks(tasks: List<TaskEntity>) {
        taskDao.insertAll(tasks)
    }
}
