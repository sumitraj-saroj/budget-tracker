package com.budgettracker.app.ui.transactions;

import com.budgettracker.app.data.FinanceRepository;
import com.budgettracker.app.data.PrefsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class TransactionsViewModel_Factory implements Factory<TransactionsViewModel> {
  private final Provider<FinanceRepository> repoProvider;

  private final Provider<PrefsRepository> prefsRepositoryProvider;

  private TransactionsViewModel_Factory(Provider<FinanceRepository> repoProvider,
      Provider<PrefsRepository> prefsRepositoryProvider) {
    this.repoProvider = repoProvider;
    this.prefsRepositoryProvider = prefsRepositoryProvider;
  }

  @Override
  public TransactionsViewModel get() {
    return newInstance(repoProvider.get(), prefsRepositoryProvider.get());
  }

  public static TransactionsViewModel_Factory create(Provider<FinanceRepository> repoProvider,
      Provider<PrefsRepository> prefsRepositoryProvider) {
    return new TransactionsViewModel_Factory(repoProvider, prefsRepositoryProvider);
  }

  public static TransactionsViewModel newInstance(FinanceRepository repo,
      PrefsRepository prefsRepository) {
    return new TransactionsViewModel(repo, prefsRepository);
  }
}
