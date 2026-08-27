package com.budgettracker.app.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.data.db.GoalEntity
import com.budgettracker.app.util.parseAmountMinor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalsUiState(
    val loading: Boolean = true,
    val baseCurrency: String = "USD",
    val goals: List<GoalEntity> = emptyList(),
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repo: FinanceRepository,
    prefsRepository: PrefsRepository,
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = combine(
        repo.goals,
        prefsRepository.prefs,
    ) { goals, prefs ->
        GoalsUiState(
            loading = false,
            baseCurrency = prefs.baseCurrency,
            goals = goals.sortedWith(compareBy({ it.savedMinor >= it.targetMinor }, { it.id })),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    fun saveGoal(
        id: Long,
        name: String,
        emoji: String,
        colorArgb: Long,
        targetText: String,
        targetDate: Long?,
        note: String,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            val currency = com.budgettracker.app.util.Currencies.byCode(uiState.value.baseCurrency)
            val target = parseAmountMinor(targetText, currency) ?: return@launch
            val existing = repo.goalById(id)
            repo.saveGoal(
                GoalEntity(
                    id = id,
                    name = name.trim(),
                    emoji = emoji,
                    colorArgb = colorArgb,
                    targetMinor = target,
                    savedMinor = existing?.savedMinor ?: 0,
                    targetDate = targetDate,
                    note = note.trim(),
                ),
            )
            onSaved()
        }
    }

    fun contribute(goal: GoalEntity, amountText: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val currency = com.budgettracker.app.util.Currencies.byCode(uiState.value.baseCurrency)
            val amount = parseAmountMinor(amountText, currency) ?: return@launch
            repo.contributeToGoal(goal, amount)
            onDone()
        }
    }

    fun delete(goal: GoalEntity) {
        viewModelScope.launch { repo.deleteGoal(goal) }
    }
}
