package com.budgettracker.app.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.AccountWithBalance
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.data.db.AccountEntity
import com.budgettracker.app.data.db.AccountType
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.convertMinor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val loading: Boolean = true,
    val baseCurrency: String = "USD",
    val accounts: List<AccountWithBalance> = emptyList(),
    val totalBaseMinor: Long = 0,
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val repo: FinanceRepository,
    prefsRepository: PrefsRepository,
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = combine(
        repo.accountBalances(),
        prefsRepository.prefs,
    ) { balances, prefs ->
        val base = Currencies.byCode(prefs.baseCurrency)
        AccountsUiState(
            loading = false,
            baseCurrency = prefs.baseCurrency,
            accounts = balances,
            totalBaseMinor = balances
                .filter { !it.account.isArchived && it.account.includeInTotals }
                .sumOf { convertMinor(it.balanceMinor, it.account.currency, base) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsUiState())

    /** Insert or update; returns the saved id. */
    fun saveAccount(
        id: Long,
        name: String,
        emoji: String,
        colorArgb: Long,
        type: AccountType,
        currencyCode: String,
        startingBalanceText: String,
        isArchived: Boolean,
        includeInTotals: Boolean,
        onSaved: (Boolean) -> Unit,
    ) {
        viewModelScope.launch {
            val currency = Currencies.byCode(currencyCode)
            val starting = com.budgettracker.app.util.parseAmountMinor(startingBalanceText, currency) ?: 0L
            val result = repo.saveAccount(
                AccountEntity(
                    id = id,
                    name = name.trim(),
                    emoji = emoji,
                    colorArgb = colorArgb,
                    type = type,
                    currencyCode = currencyCode,
                    startingBalanceMinor = starting,
                    isArchived = isArchived,
                    includeInTotals = includeInTotals,
                ),
            )
            onSaved(true)
        }
    }

    fun deleteAccount(account: AccountEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(repo.deleteAccount(account))
        }
    }
}
