package com.budgettracker.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun txTypeToString(value: TxType): String = value.name

    @TypeConverter
    fun stringToTxType(value: String): TxType = TxType.valueOf(value)

    @TypeConverter
    fun categoryTypeToString(value: CategoryType): String = value.name

    @TypeConverter
    fun stringToCategoryType(value: String): CategoryType = CategoryType.valueOf(value)

    @TypeConverter
    fun accountTypeToString(value: AccountType): String = value.name

    @TypeConverter
    fun stringToAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun debtDirectionToString(value: DebtDirection): String = value.name

    @TypeConverter
    fun stringToDebtDirection(value: String): DebtDirection = DebtDirection.valueOf(value)

    @TypeConverter
    fun cycleToString(value: Cycle): String = value.name

    @TypeConverter
    fun stringToCycle(value: String): Cycle = Cycle.valueOf(value)

    @TypeConverter
    fun budgetPeriodTypeToString(value: com.budgettracker.app.util.BudgetPeriodType): String = value.name

    @TypeConverter
    fun stringToBudgetPeriodType(value: String): com.budgettracker.app.util.BudgetPeriodType =
        com.budgettracker.app.util.BudgetPeriodType.valueOf(value)

    @TypeConverter
    fun longListToString(value: List<Long>): String = value.joinToString(",")

    @TypeConverter
    fun stringToLongList(value: String): List<Long> =
        if (value.isBlank()) emptyList() else value.split(",").mapNotNull { it.toLongOrNull() }
}

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TxEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        DebtEntity::class,
        SubscriptionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun txDao(): TxDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun debtDao(): DebtDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "budget_tracker.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
