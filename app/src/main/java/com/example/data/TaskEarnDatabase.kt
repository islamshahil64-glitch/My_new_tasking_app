package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "Apps", "Surveys", "Video", "Quiz"
    val rewardCoins: Int,
    val timeMinutes: Int,
    val isCompleted: Boolean = false,
    val iconName: String = "task"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: String, // e.g. "+50 Coins" or "-₹50"
    val date: String,
    val type: String, // "EARNED", "WITHDRAWN"
    val status: String // "Success", "Pending"
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val balanceCoins: Int = 150,
    val streakCount: Int = 3,
    val lastCheckInDate: String = "",
    val referralCode: String = "TEARN889",
    val totalEarned: Int = 350,
    val tasksCompletedCount: Int = 7
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Query("UPDATE tasks SET isCompleted = 1 WHERE id = :taskId")
    suspend fun markTaskCompleted(taskId: Long)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserProfileEntity)
}

@Database(entities = [TaskEntity::class, TransactionEntity::class, UserProfileEntity::class], version = 1, exportSchema = false)
abstract class TaskEarnDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: TaskEarnDatabase? = null

        fun getDatabase(context: Context): TaskEarnDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskEarnDatabase::class.java,
                    "task_earn_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
