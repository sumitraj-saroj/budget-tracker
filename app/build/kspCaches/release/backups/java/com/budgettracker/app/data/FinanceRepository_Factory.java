package com.budgettracker.app.data;

import com.budgettracker.app.data.db.AccountDao;
import com.budgettracker.app.data.db.BudgetDao;
import com.budgettracker.app.data.db.CategoryDao;
import com.budgettracker.app.data.db.DebtDao;
import com.budgettracker.app.data.db.GoalDao;
import com.budgettracker.app.data.db.SubscriptionDao;
import com.budgettracker.app.data.db.TxDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class FinanceRepository_Factory implements Factory<FinanceRepository> {
  private final Provider<AccountDao> accountDaoProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  private final Provider<TxDao> txDaoProvider;

  private final Provider<BudgetDao> budgetDaoProvider;

  private final Provider<GoalDao> goalDaoProvider;

  private final Provider<DebtDao> debtDaoProvider;

  private final Provider<SubscriptionDao> subscriptionDaoProvider;

  private final Provider<PrefsRepository> prefsRepositoryProvider;

  private FinanceRepository_Factory(Provider<AccountDao> accountDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<TxDao> txDaoProvider,
      Provider<BudgetDao> budgetDaoProvider, Provider<GoalDao> goalDaoProvider,
      Provider<DebtDao> debtDaoProvider, Provider<SubscriptionDao> subscriptionDaoProvider,
      Provider<PrefsRepository> prefsRepositoryProvider) {
    this.accountDaoProvider = accountDaoProvider;
    this.categoryDaoProvider = categoryDaoProvider;
    this.txDaoProvider = txDaoProvider;
    this.budgetDaoProvider = budgetDaoProvider;
    this.goalDaoProvider = goalDaoProvider;
    this.debtDaoProvider = debtDaoProvider;
    this.subscriptionDaoProvider = subscriptionDaoProvider;
    this.prefsRepositoryProvider = prefsRepositoryProvider;
  }

  @Override
  public FinanceRepository get() {
    return newInstance(accountDaoProvider.get(), categoryDaoProvider.get(), txDaoProvider.get(), budgetDaoProvider.get(), goalDaoProvider.get(), debtDaoProvider.get(), subscriptionDaoProvider.get(), prefsRepositoryProvider.get());
  }

  public static FinanceRepository_Factory create(Provider<AccountDao> accountDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<TxDao> txDaoProvider,
      Provider<BudgetDao> budgetDaoProvider, Provider<GoalDao> goalDaoProvider,
      Provider<DebtDao> debtDaoProvider, Provider<SubscriptionDao> subscriptionDaoProvider,
      Provider<PrefsRepository> prefsRepositoryProvider) {
    return new FinanceRepository_Factory(accountDaoProvider, categoryDaoProvider, txDaoProvider, budgetDaoProvider, goalDaoProvider, debtDaoProvider, subscriptionDaoProvider, prefsRepositoryProvider);
  }

  public static FinanceRepository newInstance(AccountDao accountDao, CategoryDao categoryDao,
      TxDao txDao, BudgetDao budgetDao, GoalDao goalDao, DebtDao debtDao,
      SubscriptionDao subscriptionDao, PrefsRepository prefsRepository) {
    return new FinanceRepository(accountDao, categoryDao, txDao, budgetDao, goalDao, debtDao, subscriptionDao, prefsRepository);
  }
}
