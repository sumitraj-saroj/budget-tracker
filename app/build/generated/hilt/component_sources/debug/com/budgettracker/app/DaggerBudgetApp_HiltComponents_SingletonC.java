package com.budgettracker.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.budgettracker.app.auth.GoogleAuthClient;
import com.budgettracker.app.data.FinanceRepository;
import com.budgettracker.app.data.PrefsRepository;
import com.budgettracker.app.data.backup.BackupManager;
import com.budgettracker.app.data.db.AccountDao;
import com.budgettracker.app.data.db.AppDatabase;
import com.budgettracker.app.data.db.BudgetDao;
import com.budgettracker.app.data.db.CategoryDao;
import com.budgettracker.app.data.db.DebtDao;
import com.budgettracker.app.data.db.GoalDao;
import com.budgettracker.app.data.db.SubscriptionDao;
import com.budgettracker.app.data.db.TxDao;
import com.budgettracker.app.di.AppModule_ProvideAccountDaoFactory;
import com.budgettracker.app.di.AppModule_ProvideBudgetDaoFactory;
import com.budgettracker.app.di.AppModule_ProvideCategoryDaoFactory;
import com.budgettracker.app.di.AppModule_ProvideDatabaseFactory;
import com.budgettracker.app.di.AppModule_ProvideDebtDaoFactory;
import com.budgettracker.app.di.AppModule_ProvideGoalDaoFactory;
import com.budgettracker.app.di.AppModule_ProvideSubscriptionDaoFactory;
import com.budgettracker.app.di.AppModule_ProvideTxDaoFactory;
import com.budgettracker.app.ui.accounts.AccountsViewModel;
import com.budgettracker.app.ui.accounts.AccountsViewModel_HiltModules;
import com.budgettracker.app.ui.accounts.AccountsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.accounts.AccountsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.budgettracker.app.ui.budgets.BudgetEditViewModel;
import com.budgettracker.app.ui.budgets.BudgetEditViewModel_HiltModules;
import com.budgettracker.app.ui.budgets.BudgetEditViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.budgets.BudgetEditViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.budgettracker.app.ui.budgets.BudgetsViewModel;
import com.budgettracker.app.ui.budgets.BudgetsViewModel_HiltModules;
import com.budgettracker.app.ui.budgets.BudgetsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.budgets.BudgetsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.budgettracker.app.ui.debts.DebtsViewModel;
import com.budgettracker.app.ui.debts.DebtsViewModel_HiltModules;
import com.budgettracker.app.ui.debts.DebtsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.debts.DebtsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.budgettracker.app.ui.goals.GoalsViewModel;
import com.budgettracker.app.ui.goals.GoalsViewModel_HiltModules;
import com.budgettracker.app.ui.goals.GoalsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.goals.GoalsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.budgettracker.app.ui.home.HomeViewModel;
import com.budgettracker.app.ui.home.HomeViewModel_HiltModules;
import com.budgettracker.app.ui.home.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.home.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.budgettracker.app.ui.onboarding.OnboardingViewModel;
import com.budgettracker.app.ui.onboarding.OnboardingViewModel_HiltModules;
import com.budgettracker.app.ui.onboarding.OnboardingViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.onboarding.OnboardingViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.budgettracker.app.ui.settings.SettingsViewModel;
import com.budgettracker.app.ui.settings.SettingsViewModel_HiltModules;
import com.budgettracker.app.ui.settings.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.settings.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.budgettracker.app.ui.stats.StatsViewModel;
import com.budgettracker.app.ui.stats.StatsViewModel_HiltModules;
import com.budgettracker.app.ui.stats.StatsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.stats.StatsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.budgettracker.app.ui.subscriptions.SubscriptionsViewModel;
import com.budgettracker.app.ui.subscriptions.SubscriptionsViewModel_HiltModules;
import com.budgettracker.app.ui.subscriptions.SubscriptionsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.subscriptions.SubscriptionsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.budgettracker.app.ui.transactions.TransactionEditViewModel;
import com.budgettracker.app.ui.transactions.TransactionEditViewModel_HiltModules;
import com.budgettracker.app.ui.transactions.TransactionEditViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.transactions.TransactionEditViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.budgettracker.app.ui.transactions.TransactionsViewModel;
import com.budgettracker.app.ui.transactions.TransactionsViewModel_HiltModules;
import com.budgettracker.app.ui.transactions.TransactionsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.budgettracker.app.ui.transactions.TransactionsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerBudgetApp_HiltComponents_SingletonC {
  private DaggerBudgetApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public BudgetApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements BudgetApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public BudgetApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements BudgetApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public BudgetApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements BudgetApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public BudgetApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements BudgetApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public BudgetApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements BudgetApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public BudgetApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements BudgetApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public BudgetApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements BudgetApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public BudgetApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends BudgetApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends BudgetApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends BudgetApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends BudgetApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    Map keySetMapOfClassOfObjectAndBooleanBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, Boolean>newMapBuilder(13);
      mapBuilder.put(AccountsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AccountsViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(AppViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AppViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(BudgetEditViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, BudgetEditViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(BudgetsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, BudgetsViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(DebtsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DebtsViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(GoalsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, GoalsViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(OnboardingViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, OnboardingViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(StatsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, StatsViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(SubscriptionsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SubscriptionsViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(TransactionEditViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TransactionEditViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(TransactionsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TransactionsViewModel_HiltModules.KeyModule.provide());
      return mapBuilder.build();
    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(keySetMapOfClassOfObjectAndBooleanBuilder());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends BudgetApp_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<AccountsViewModel> accountsViewModelProvider;

    Provider<AppViewModel> appViewModelProvider;

    Provider<BudgetEditViewModel> budgetEditViewModelProvider;

    Provider<BudgetsViewModel> budgetsViewModelProvider;

    Provider<DebtsViewModel> debtsViewModelProvider;

    Provider<GoalsViewModel> goalsViewModelProvider;

    Provider<HomeViewModel> homeViewModelProvider;

    Provider<OnboardingViewModel> onboardingViewModelProvider;

    Provider<SettingsViewModel> settingsViewModelProvider;

    Provider<StatsViewModel> statsViewModelProvider;

    Provider<SubscriptionsViewModel> subscriptionsViewModelProvider;

    Provider<TransactionEditViewModel> transactionEditViewModelProvider;

    Provider<TransactionsViewModel> transactionsViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    Map hiltViewModelMapMapOfClassOfObjectAndProviderOfViewModelBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(13);
      mapBuilder.put(AccountsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (accountsViewModelProvider)));
      mapBuilder.put(AppViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (appViewModelProvider)));
      mapBuilder.put(BudgetEditViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (budgetEditViewModelProvider)));
      mapBuilder.put(BudgetsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (budgetsViewModelProvider)));
      mapBuilder.put(DebtsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (debtsViewModelProvider)));
      mapBuilder.put(GoalsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (goalsViewModelProvider)));
      mapBuilder.put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (homeViewModelProvider)));
      mapBuilder.put(OnboardingViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (onboardingViewModelProvider)));
      mapBuilder.put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (settingsViewModelProvider)));
      mapBuilder.put(StatsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (statsViewModelProvider)));
      mapBuilder.put(SubscriptionsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (subscriptionsViewModelProvider)));
      mapBuilder.put(TransactionEditViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (transactionEditViewModelProvider)));
      mapBuilder.put(TransactionsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (transactionsViewModelProvider)));
      return mapBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.accountsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.appViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.budgetEditViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.budgetsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.debtsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.goalsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.onboardingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.statsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.subscriptionsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.transactionEditViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.transactionsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(hiltViewModelMapMapOfClassOfObjectAndProviderOfViewModelBuilder());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.budgettracker.app.ui.accounts.AccountsViewModel
          return (T) new AccountsViewModel(singletonCImpl.financeRepositoryProvider.get(), singletonCImpl.prefsRepositoryProvider.get());

          case 1: // com.budgettracker.app.AppViewModel
          return (T) new AppViewModel(singletonCImpl.prefsRepositoryProvider.get(), singletonCImpl.financeRepositoryProvider.get());

          case 2: // com.budgettracker.app.ui.budgets.BudgetEditViewModel
          return (T) new BudgetEditViewModel(singletonCImpl.financeRepositoryProvider.get(), singletonCImpl.prefsRepositoryProvider.get(), viewModelCImpl.savedStateHandle);

          case 3: // com.budgettracker.app.ui.budgets.BudgetsViewModel
          return (T) new BudgetsViewModel(singletonCImpl.financeRepositoryProvider.get(), singletonCImpl.prefsRepositoryProvider.get());

          case 4: // com.budgettracker.app.ui.debts.DebtsViewModel
          return (T) new DebtsViewModel(singletonCImpl.financeRepositoryProvider.get(), singletonCImpl.prefsRepositoryProvider.get());

          case 5: // com.budgettracker.app.ui.goals.GoalsViewModel
          return (T) new GoalsViewModel(singletonCImpl.financeRepositoryProvider.get(), singletonCImpl.prefsRepositoryProvider.get());

          case 6: // com.budgettracker.app.ui.home.HomeViewModel
          return (T) new HomeViewModel(singletonCImpl.financeRepositoryProvider.get(), singletonCImpl.prefsRepositoryProvider.get());

          case 7: // com.budgettracker.app.ui.onboarding.OnboardingViewModel
          return (T) new OnboardingViewModel(singletonCImpl.prefsRepositoryProvider.get(), singletonCImpl.financeRepositoryProvider.get());

          case 8: // com.budgettracker.app.ui.settings.SettingsViewModel
          return (T) new SettingsViewModel(singletonCImpl.prefsRepositoryProvider.get(), singletonCImpl.backupManagerProvider.get(), singletonCImpl.googleAuthClientProvider.get());

          case 9: // com.budgettracker.app.ui.stats.StatsViewModel
          return (T) new StatsViewModel(singletonCImpl.financeRepositoryProvider.get(), singletonCImpl.prefsRepositoryProvider.get());

          case 10: // com.budgettracker.app.ui.subscriptions.SubscriptionsViewModel
          return (T) new SubscriptionsViewModel(singletonCImpl.financeRepositoryProvider.get(), singletonCImpl.prefsRepositoryProvider.get());

          case 11: // com.budgettracker.app.ui.transactions.TransactionEditViewModel
          return (T) new TransactionEditViewModel(singletonCImpl.financeRepositoryProvider.get(), viewModelCImpl.savedStateHandle);

          case 12: // com.budgettracker.app.ui.transactions.TransactionsViewModel
          return (T) new TransactionsViewModel(singletonCImpl.financeRepositoryProvider.get(), singletonCImpl.prefsRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends BudgetApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends BudgetApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends BudgetApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<AppDatabase> provideDatabaseProvider;

    Provider<PrefsRepository> prefsRepositoryProvider;

    Provider<FinanceRepository> financeRepositoryProvider;

    Provider<BackupManager> backupManagerProvider;

    Provider<GoogleAuthClient> googleAuthClientProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    AccountDao accountDao() {
      return AppModule_ProvideAccountDaoFactory.provideAccountDao(provideDatabaseProvider.get());
    }

    CategoryDao categoryDao() {
      return AppModule_ProvideCategoryDaoFactory.provideCategoryDao(provideDatabaseProvider.get());
    }

    TxDao txDao() {
      return AppModule_ProvideTxDaoFactory.provideTxDao(provideDatabaseProvider.get());
    }

    BudgetDao budgetDao() {
      return AppModule_ProvideBudgetDaoFactory.provideBudgetDao(provideDatabaseProvider.get());
    }

    GoalDao goalDao() {
      return AppModule_ProvideGoalDaoFactory.provideGoalDao(provideDatabaseProvider.get());
    }

    DebtDao debtDao() {
      return AppModule_ProvideDebtDaoFactory.provideDebtDao(provideDatabaseProvider.get());
    }

    SubscriptionDao subscriptionDao() {
      return AppModule_ProvideSubscriptionDaoFactory.provideSubscriptionDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 1));
      this.prefsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PrefsRepository>(singletonCImpl, 2));
      this.financeRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FinanceRepository>(singletonCImpl, 0));
      this.backupManagerProvider = DoubleCheck.provider(new SwitchingProvider<BackupManager>(singletonCImpl, 3));
      this.googleAuthClientProvider = DoubleCheck.provider(new SwitchingProvider<GoogleAuthClient>(singletonCImpl, 4));
    }

    @Override
    public void injectBudgetApp(BudgetApp budgetApp) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.budgettracker.app.data.FinanceRepository
          return (T) new FinanceRepository(singletonCImpl.accountDao(), singletonCImpl.categoryDao(), singletonCImpl.txDao(), singletonCImpl.budgetDao(), singletonCImpl.goalDao(), singletonCImpl.debtDao(), singletonCImpl.subscriptionDao(), singletonCImpl.prefsRepositoryProvider.get());

          case 1: // com.budgettracker.app.data.db.AppDatabase
          return (T) AppModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.budgettracker.app.data.PrefsRepository
          return (T) new PrefsRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.budgettracker.app.data.backup.BackupManager
          return (T) new BackupManager(singletonCImpl.provideDatabaseProvider.get(), singletonCImpl.prefsRepositoryProvider.get());

          case 4: // com.budgettracker.app.auth.GoogleAuthClient
          return (T) new GoogleAuthClient();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
