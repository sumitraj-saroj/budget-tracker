package com.budgettracker.app.auth;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GoogleAuthClient_Factory implements Factory<GoogleAuthClient> {
  @Override
  public GoogleAuthClient get() {
    return newInstance();
  }

  public static GoogleAuthClient_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GoogleAuthClient newInstance() {
    return new GoogleAuthClient();
  }

  private static final class InstanceHolder {
    static final GoogleAuthClient_Factory INSTANCE = new GoogleAuthClient_Factory();
  }
}
