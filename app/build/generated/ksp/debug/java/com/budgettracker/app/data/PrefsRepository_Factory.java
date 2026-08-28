package com.budgettracker.app.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class PrefsRepository_Factory implements Factory<PrefsRepository> {
  private final Provider<Context> contextProvider;

  private PrefsRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PrefsRepository get() {
    return newInstance(contextProvider.get());
  }

  public static PrefsRepository_Factory create(Provider<Context> contextProvider) {
    return new PrefsRepository_Factory(contextProvider);
  }

  public static PrefsRepository newInstance(Context context) {
    return new PrefsRepository(context);
  }
}
