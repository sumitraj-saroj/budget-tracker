package com.budgettracker.app.ui.budgets;

import androidx.lifecycle.SavedStateHandle;
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
public final class BudgetEditViewModel_Factory implements Factory<BudgetEditViewModel> {
  private final Provider<FinanceRepository> repoProvider;

  private final Provider<PrefsRepository> prefsRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private BudgetEditViewModel_Factory(Provider<FinanceRepository> repoProvider,
      Provider<PrefsRepository> prefsRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repoProvider = repoProvider;
    this.prefsRepositoryProvider = prefsRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public BudgetEditViewModel get() {
    return newInstance(repoProvider.get(), prefsRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static BudgetEditViewModel_Factory create(Provider<FinanceRepository> repoProvider,
      Provider<PrefsRepository> prefsRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new BudgetEditViewModel_Factory(repoProvider, prefsRepositoryProvider, savedStateHandleProvider);
  }

  public static BudgetEditViewModel newInstance(FinanceRepository repo,
      PrefsRepository prefsRepository, SavedStateHandle savedStateHandle) {
    return new BudgetEditViewModel(repo, prefsRepository, savedStateHandle);
  }
}
