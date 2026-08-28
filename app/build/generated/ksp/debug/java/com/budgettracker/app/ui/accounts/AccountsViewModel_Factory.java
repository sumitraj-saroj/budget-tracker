package com.budgettracker.app.ui.accounts;

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
public final class AccountsViewModel_Factory implements Factory<AccountsViewModel> {
  private final Provider<FinanceRepository> repoProvider;

  private final Provider<PrefsRepository> prefsRepositoryProvider;

  private AccountsViewModel_Factory(Provider<FinanceRepository> repoProvider,
      Provider<PrefsRepository> prefsRepositoryProvider) {
    this.repoProvider = repoProvider;
    this.prefsRepositoryProvider = prefsRepositoryProvider;
  }

  @Override
  public AccountsViewModel get() {
    return newInstance(repoProvider.get(), prefsRepositoryProvider.get());
  }

  public static AccountsViewModel_Factory create(Provider<FinanceRepository> repoProvider,
      Provider<PrefsRepository> prefsRepositoryProvider) {
    return new AccountsViewModel_Factory(repoProvider, prefsRepositoryProvider);
  }

  public static AccountsViewModel newInstance(FinanceRepository repo,
      PrefsRepository prefsRepository) {
    return new AccountsViewModel(repo, prefsRepository);
  }
}
