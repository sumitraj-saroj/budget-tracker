package com.budgettracker.app.ui.onboarding;

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
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<PrefsRepository> prefsRepositoryProvider;

  private final Provider<FinanceRepository> financeRepositoryProvider;

  private OnboardingViewModel_Factory(Provider<PrefsRepository> prefsRepositoryProvider,
      Provider<FinanceRepository> financeRepositoryProvider) {
    this.prefsRepositoryProvider = prefsRepositoryProvider;
    this.financeRepositoryProvider = financeRepositoryProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(prefsRepositoryProvider.get(), financeRepositoryProvider.get());
  }

  public static OnboardingViewModel_Factory create(
      Provider<PrefsRepository> prefsRepositoryProvider,
      Provider<FinanceRepository> financeRepositoryProvider) {
    return new OnboardingViewModel_Factory(prefsRepositoryProvider, financeRepositoryProvider);
  }

  public static OnboardingViewModel newInstance(PrefsRepository prefsRepository,
      FinanceRepository financeRepository) {
    return new OnboardingViewModel(prefsRepository, financeRepository);
  }
}
