package com.budgettracker.app.ui.settings;

import com.budgettracker.app.auth.GoogleAuthClient;
import com.budgettracker.app.data.PrefsRepository;
import com.budgettracker.app.data.backup.BackupManager;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<PrefsRepository> prefsRepositoryProvider;

  private final Provider<BackupManager> backupManagerProvider;

  private final Provider<GoogleAuthClient> googleAuthProvider;

  private SettingsViewModel_Factory(Provider<PrefsRepository> prefsRepositoryProvider,
      Provider<BackupManager> backupManagerProvider,
      Provider<GoogleAuthClient> googleAuthProvider) {
    this.prefsRepositoryProvider = prefsRepositoryProvider;
    this.backupManagerProvider = backupManagerProvider;
    this.googleAuthProvider = googleAuthProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(prefsRepositoryProvider.get(), backupManagerProvider.get(), googleAuthProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<PrefsRepository> prefsRepositoryProvider,
      Provider<BackupManager> backupManagerProvider,
      Provider<GoogleAuthClient> googleAuthProvider) {
    return new SettingsViewModel_Factory(prefsRepositoryProvider, backupManagerProvider, googleAuthProvider);
  }

  public static SettingsViewModel newInstance(PrefsRepository prefsRepository,
      BackupManager backupManager, GoogleAuthClient googleAuth) {
    return new SettingsViewModel(prefsRepository, backupManager, googleAuth);
  }
}
