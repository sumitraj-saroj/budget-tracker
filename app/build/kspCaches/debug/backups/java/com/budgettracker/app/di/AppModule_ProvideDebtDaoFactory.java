package com.budgettracker.app.di;

import com.budgettracker.app.data.db.AppDatabase;
import com.budgettracker.app.data.db.DebtDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideDebtDaoFactory implements Factory<DebtDao> {
  private final Provider<AppDatabase> dbProvider;

  private AppModule_ProvideDebtDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public DebtDao get() {
    return provideDebtDao(dbProvider.get());
  }

  public static AppModule_ProvideDebtDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideDebtDaoFactory(dbProvider);
  }

  public static DebtDao provideDebtDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDebtDao(db));
  }
}
