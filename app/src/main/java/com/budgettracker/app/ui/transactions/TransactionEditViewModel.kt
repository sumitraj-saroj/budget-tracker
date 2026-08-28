package com.budgettracker.app.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.db.AccountEntity
import com.budgettracker.app.data.db.CategoryEntity
import com.budgettracker.app.data.db.CategoryType
import com.budgettracker.app.data.db.TxEntity
import com.budgettracker.app.data.db.TxType
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.convertMinor
import com.budgettracker.app.util.parseAmountMinor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class TxEditState(
    val isNew: Boolean = true,
    val loaded: Boolean = false,
    val type: TxType = TxType.EXPENSE,
    val amountText: String = "",
    val categoryId: Long? = null,
    val accountId: Long = 0,
    val toAccountId: Long? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val amountError: Boolean = false,
) {
    val fromAccount: AccountEntity? get() = accounts.firstOrNull { it.id == accountId }
    val toAccount: AccountEntity? get() = toAccountId?.let { id -> accounts.firstOrNull { it.id == id } }
    val currency get() = fromAccount?.currency ?: Currencies.DEFAULT
}

@HiltViewModel
class TransactionEditViewModel @Inject constructor(
    private val repo: FinanceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val txId: Long = savedStateHandle.get<String>("txId")?.toLongOrNull() ?: -1L
    private val quickCategoryId: Long = savedStateHandle.get<Long>("categoryId") ?: -1L
    private val presetType: TxType? = savedStateHandle.get<String>("type")?.let { raw ->
        TxType.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    }

    private val _state = MutableStateFlow(TxEditState())
    val state: StateFlow<TxEditState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repo.accounts, repo.categories) { accounts, categories -> accounts to categories }
                .collect { (accounts, categories) ->
                    _state.update { s ->
                        var next = s.copy(
                            accounts = accounts.filter { !it.isArchived || it.id == s.accountId || it.id == s.toAccountId },
                            categories = categories,
                        )
                        if (next.accountId == 0L) {
                            next = next.copy(accountId = next.accounts.firstOrNull()?.id ?: 0L)
                        }
                        if (next.type == TxType.TRANSFER && next.toAccountId == null) {
                            next = next.copy(toAccountId = next.accounts.firstOrNull { it.id != next.accountId }?.id)
                        }
                        if (txId <= 0) {
                            // Apply FAB long-press preset type
                            if (presetType != null && next.type != presetType) {
                                next = applyType(next, presetType)
                            }
                            // Apply quick-add category prefill (from Home tiles)
                            if (quickCategoryId > 0 && next.categoryId == null) {
                                next = next.copy(categoryId = categories.firstOrNull { it.id == quickCategoryId }?.id)
                            }
                        }
                        next
                    }
                }
        }

        if (txId > 0) {
            viewModelScope.launch {
                val tx = repo.txById(txId) ?: return@launch
                val account = repo.accountById(tx.accountId)
                _state.update { s ->
                    s.copy(
                        isNew = false,
                        loaded = true,
                        type = tx.type,
                        amountText = amountToText(tx.amountMinor, account?.currency?.minorDigits ?: 2),
                        categoryId = tx.categoryId,
                        accountId = tx.accountId,
                        toAccountId = tx.toAccountId,
                        dateMillis = tx.date,
                        note = tx.note,
                    )
                }
            }
        } else {
            _state.update { it.copy(loaded = true) }
        }
    }

    private fun amountToText(amountMinor: Long, digits: Int): String {
        val major = BigDecimal(amountMinor).movePointLeft(digits)
        return major.stripTrailingZeros().toPlainString()
    }

    fun setType(type: TxType) {
        _state.update { applyType(it, type) }
    }

    private fun applyType(s: TxEditState, type: TxType): TxEditState {
        var next = s.copy(type = type, amountError = false)
        if (type != TxType.TRANSFER) {
            val wanted = if (type == TxType.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME
            val currentValid = next.categoryId?.let { id -> next.categories.firstOrNull { it.id == id && it.type == wanted } }
            if (currentValid == null) {
                next = next.copy(
                    categoryId = next.categories.firstOrNull { it.type == wanted && it.name == "Other" }?.id
                        ?: next.categories.firstOrNull { it.type == wanted }?.id,
                )
            }
        }
        if (type == TxType.TRANSFER && next.toAccountId == null) {
            next = next.copy(toAccountId = next.accounts.firstOrNull { it.id != next.accountId }?.id)
        }
        return next
    }

    fun setAmount(text: String) {
        _state.update { it.copy(amountText = text.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(12), amountError = false) }
    }

    /** Handles the custom keypad: digits, decimal separator, "BACK". */
    fun onKeypad(key: String) {
        _state.update { s ->
            val maxFrac = s.currency.minorDigits
            var t = s.amountText
            when {
                key == "BACK" -> t = t.dropLast(1)
                key == "." -> if (maxFrac > 0 && !t.contains('.')) {
                    t = if (t.isEmpty()) "0." else "$t."
                }
                else -> {
                    if (t == "0") t = ""
                    val dot = t.indexOf('.')
                    val fracLen = if (dot >= 0) t.length - dot - 1 else 0
                    val wholeLen = if (dot >= 0) dot else t.length
                    if (wholeLen < 12 && fracLen < maxFrac) t += key
                }
            }
            s.copy(amountText = t, amountError = false)
        }
    }

    fun setCategory(categoryId: Long?) {
        _state.update { it.copy(categoryId = categoryId) }
    }

    fun setAccount(accountId: Long) {
        _state.update { s ->
            var next = s.copy(accountId = accountId)
            if (next.toAccountId == accountId) {
                next = next.copy(toAccountId = next.accounts.firstOrNull { it.id != accountId }?.id)
            }
            next
        }
    }

    fun setToAccount(accountId: Long) {
        _state.update { s ->
            var next = s.copy(toAccountId = accountId)
            if (next.accountId == accountId) {
                next = next.copy(accountId = next.accounts.firstOrNull { it.id != accountId }?.id ?: accountId)
            }
            next
        }
    }

    fun setDate(millis: Long) {
        _state.update { it.copy(dateMillis = millis) }
    }

    fun setNote(text: String) {
        _state.update { it.copy(note = text.take(200)) }
    }

    fun createCategory(name: String, emoji: String, colorArgb: Long) {
        viewModelScope.launch {
            val type = if (_state.value.type == TxType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
            val id = repo.saveCategory(
                CategoryEntity(name = name.trim(), emoji = emoji, colorArgb = colorArgb, type = type, isCustom = true),
            )
            _state.update { it.copy(categoryId = id) }
        }
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        val account = s.fromAccount ?: return
        val amount = parseAmountMinor(s.amountText, account.currency)
        if (amount == null) {
            _state.update { it.copy(amountError = true) }
            return
        }
        if (s.type == TxType.TRANSFER) {
            val toAccount = s.toAccount ?: return
            val toAmount = if (toAccount.currencyCode == account.currencyCode) {
                amount
            } else {
                convertMinor(amount, account.currency, toAccount.currency)
            }
            _state.update { it.copy(amountError = false) }
            viewModelScope.launch {
                repo.saveTx(
                    TxEntity(
                        id = if (txId > 0) txId else 0,
                        amountMinor = amount,
                        type = TxType.TRANSFER,
                        accountId = account.id,
                        toAccountId = toAccount.id,
                        toAmountMinor = toAmount,
                        categoryId = null,
                        date = s.dateMillis,
                        note = s.note.trim(),
                    ),
                )
                onDone()
            }
        } else {
            if (s.categoryId == null) {
                _state.update { it.copy(amountError = false) }
                return
            }
            viewModelScope.launch {
                repo.saveTx(
                    TxEntity(
                        id = if (txId > 0) txId else 0,
                        amountMinor = amount,
                        type = s.type,
                        accountId = account.id,
                        categoryId = s.categoryId,
                        date = s.dateMillis,
                        note = s.note.trim(),
                    ),
                )
                onDone()
            }
        }
    }

    fun delete(onDone: () -> Unit) {
        if (txId <= 0) {
            onDone()
            return
        }
        viewModelScope.launch {
            repo.txById(txId)?.let { repo.deleteTx(it) }
            onDone()
        }
    }
}
