package com.budgettracker.app.ui.transactions;

import androidx.lifecycle.SavedStateHandle;
import com.budgettracker.app.data.FinanceRepository;
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
public final class TransactionEditViewModel_Factory implements Factory<TransactionEditViewModel> {
  private final Provider<FinanceRepository> repoProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private TransactionEditViewModel_Factory(Provider<FinanceRepository> repoProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repoProvider = repoProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public TransactionEditViewModel get() {
    return newInstance(repoProvider.get(), savedStateHandleProvider.get());
  }

  public static TransactionEditViewModel_Factory create(Provider<FinanceRepository> repoProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new TransactionEditViewModel_Factory(repoProvider, savedStateHandleProvider);
  }

  public static TransactionEditViewModel newInstance(FinanceRepository repo,
      SavedStateHandle savedStateHandle) {
    return new TransactionEditViewModel(repo, savedStateHandle);
  }
}
