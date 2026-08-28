package com.budgettracker.app;

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
public final class AppViewModel_Factory implements Factory<AppViewModel> {
  private final Provider<PrefsRepository> prefsRepositoryProvider;

  private final Provider<FinanceRepository> financeRepositoryProvider;

  private AppViewModel_Factory(Provider<PrefsRepository> prefsRepositoryProvider,
      Provider<FinanceRepository> financeRepositoryProvider) {
    this.prefsRepositoryProvider = prefsRepositoryProvider;
    this.financeRepositoryProvider = financeRepositoryProvider;
  }

  @Override
  public AppViewModel get() {
    return newInstance(prefsRepositoryProvider.get(), financeRepositoryProvider.get());
  }

  public static AppViewModel_Factory create(Provider<PrefsRepository> prefsRepositoryProvider,
      Provider<FinanceRepository> financeRepositoryProvider) {
    return new AppViewModel_Factory(prefsRepositoryProvider, financeRepositoryProvider);
  }

  public static AppViewModel newInstance(PrefsRepository prefsRepository,
      FinanceRepository financeRepository) {
    return new AppViewModel(prefsRepository, financeRepository);
  }
}
