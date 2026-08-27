package com.budgettracker.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY isArchived, sortOrder, id")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun byId(id: Long): AccountEntity?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int

    @Query("SELECT * FROM accounts")
    suspend fun snapshot(): List<AccountEntity>

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Insert
    suspend fun insertAll(accounts: List<AccountEntity>)

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY id")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun byId(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT * FROM categories")
    suspend fun snapshot(): List<CategoryEntity>

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}

@Dao
interface TxDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<TxEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun byId(id: Long): TxEntity?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId OR toAccountId = :accountId")
    suspend fun countForAccount(accountId: Long): Int

    @Query("SELECT * FROM transactions")
    suspend fun snapshot(): List<TxEntity>

    @Insert
    suspend fun insert(tx: TxEntity): Long

    @Insert
    suspend fun insertAll(txs: List<TxEntity>)

    @Update
    suspend fun update(tx: TxEntity)

    @Delete
    suspend fun delete(tx: TxEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY id")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun byId(id: Long): BudgetEntity?

    @Query("SELECT * FROM budgets")
    suspend fun snapshot(): List<BudgetEntity>

    @Insert
    suspend fun insert(budget: BudgetEntity): Long

    @Insert
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY id")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun byId(id: Long): GoalEntity?

    @Query("SELECT * FROM goals")
    suspend fun snapshot(): List<GoalEntity>

    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Insert
    suspend fun insertAll(goals: List<GoalEntity>)

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("DELETE FROM goals")
    suspend fun deleteAll()
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY isSettled, dueDate, id")
    fun observeAll(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun byId(id: Long): DebtEntity?

    @Query("SELECT * FROM debts")
    suspend fun snapshot(): List<DebtEntity>

    @Insert
    suspend fun insert(debt: DebtEntity): Long

    @Insert
    suspend fun insertAll(debts: List<DebtEntity>)

    @Update
    suspend fun update(debt: DebtEntity)

    @Delete
    suspend fun delete(debt: DebtEntity)

    @Query("DELETE FROM debts")
    suspend fun deleteAll()
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY isActive DESC, nextDue, id")
    fun observeAll(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun byId(id: Long): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions")
    suspend fun snapshot(): List<SubscriptionEntity>

    @Insert
    suspend fun insert(sub: SubscriptionEntity): Long

    @Insert
    suspend fun insertAll(subs: List<SubscriptionEntity>)

    @Update
    suspend fun update(sub: SubscriptionEntity)

    @Delete
    suspend fun delete(sub: SubscriptionEntity)

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAll()
}
