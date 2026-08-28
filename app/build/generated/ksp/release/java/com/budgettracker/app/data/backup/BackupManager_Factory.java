package com.budgettracker.app.data.backup;

import com.budgettracker.app.data.PrefsRepository;
import com.budgettracker.app.data.db.AppDatabase;
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
public final class BackupManager_Factory implements Factory<BackupManager> {
  private final Provider<AppDatabase> dbProvider;

  private final Provider<PrefsRepository> prefsRepositoryProvider;

  private BackupManager_Factory(Provider<AppDatabase> dbProvider,
      Provider<PrefsRepository> prefsRepositoryProvider) {
    this.dbProvider = dbProvider;
    this.prefsRepositoryProvider = prefsRepositoryProvider;
  }

  @Override
  public BackupManager get() {
    return newInstance(dbProvider.get(), prefsRepositoryProvider.get());
  }

  public static BackupManager_Factory create(Provider<AppDatabase> dbProvider,
      Provider<PrefsRepository> prefsRepositoryProvider) {
    return new BackupManager_Factory(dbProvider, prefsRepositoryProvider);
  }

  public static BackupManager newInstance(AppDatabase db, PrefsRepository prefsRepository) {
    return new BackupManager(db, prefsRepository);
  }
}
