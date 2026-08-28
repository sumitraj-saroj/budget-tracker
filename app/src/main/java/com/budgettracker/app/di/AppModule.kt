package com.budgettracker.app.di

import android.content.Context
import com.budgettracker.app.data.db.AccountDao
import com.budgettracker.app.data.db.AppDatabase
import com.budgettracker.app.data.db.BudgetDao
import com.budgettracker.app.data.db.CategoryDao
import com.budgettracker.app.data.db.DebtDao
import com.budgettracker.app.data.db.GoalDao
import com.budgettracker.app.data.db.SubscriptionDao
import com.budgettracker.app.data.db.TxDao
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.build(context)

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTxDao(db: AppDatabase): TxDao = db.txDao()

    @Provides
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideDebtDao(db: AppDatabase): DebtDao = db.debtDao()

    @Provides
    fun provideSubscriptionDao(db: AppDatabase): SubscriptionDao = db.subscriptionDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
