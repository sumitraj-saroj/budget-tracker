package com.budgettracker.app.ui.home;

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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<FinanceRepository> repoProvider;

  private final Provider<PrefsRepository> prefsRepositoryProvider;

  private HomeViewModel_Factory(Provider<FinanceRepository> repoProvider,
      Provider<PrefsRepository> prefsRepositoryProvider) {
    this.repoProvider = repoProvider;
    this.prefsRepositoryProvider = prefsRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(repoProvider.get(), prefsRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<FinanceRepository> repoProvider,
      Provider<PrefsRepository> prefsRepositoryProvider) {
    return new HomeViewModel_Factory(repoProvider, prefsRepositoryProvider);
  }

  public static HomeViewModel newInstance(FinanceRepository repo, PrefsRepository prefsRepository) {
    return new HomeViewModel(repo, prefsRepository);
  }
}
