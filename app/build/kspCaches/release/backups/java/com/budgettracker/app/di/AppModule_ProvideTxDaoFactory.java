package com.budgettracker.app.di;

import com.budgettracker.app.data.db.AppDatabase;
import com.budgettracker.app.data.db.TxDao;
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
public final class AppModule_ProvideTxDaoFactory implements Factory<TxDao> {
  private final Provider<AppDatabase> dbProvider;

  private AppModule_ProvideTxDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TxDao get() {
    return provideTxDao(dbProvider.get());
  }

  public static AppModule_ProvideTxDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideTxDaoFactory(dbProvider);
  }

  public static TxDao provideTxDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTxDao(db));
  }
}
