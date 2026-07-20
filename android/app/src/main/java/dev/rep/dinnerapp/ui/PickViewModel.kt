package dev.rep.dinnerapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.rep.dinnerapp.data.AppDatabase
import dev.rep.dinnerapp.data.DinnerPicker
import dev.rep.dinnerapp.data.Meal
import dev.rep.dinnerapp.data.Season
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PickUiState(
    val days: Int = DEFAULT_DAYS,
    val picks: List<Meal> = emptyList(),
    val keptIds: Set<Long> = emptySet(),
    val hasPicked: Boolean = false,
) {
    /** True when at least one picked meal is not being kept, so a reroll would do something. */
    val canReroll: Boolean get() = picks.any { it.id !in keptIds }

    companion object {
        const val DEFAULT_DAYS = 5
        const val MAX_DAYS = 7
    }
}

class PickViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).mealDao()

    /** Total meals available; the day selector is capped at this (or MAX_DAYS). */
    val mealCount: StateFlow<Int> = dao.observeAll()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _uiState = MutableStateFlow(PickUiState())
    val uiState: StateFlow<PickUiState> = _uiState.asStateFlow()

    val currentSeason: Season = Season.current()

    fun setDays(days: Int) {
        _uiState.value = _uiState.value.copy(days = days)
    }

    fun pick() {
        viewModelScope.launch {
            val meals = dao.getAll()
            val picks = DinnerPicker.pick(meals, _uiState.value.days)
            _uiState.value = _uiState.value.copy(picks = picks, keptIds = emptySet(), hasPicked = true)
        }
    }

    /** Toggle whether a picked meal is kept (protected) across the next reroll. */
    fun toggleKeep(meal: Meal) {
        val kept = _uiState.value.keptIds
        _uiState.value = _uiState.value.copy(
            keptIds = if (meal.id in kept) kept - meal.id else kept + meal.id,
        )
    }

    /** Re-pick only the non-kept slots, leaving kept meals in place and avoiding duplicates. */
    fun reroll() {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.canReroll) return@launch
            val meals = dao.getAll()
            val needed = state.picks.count { it.id !in state.keptIds }
            val fresh = DinnerPicker.pick(meals, needed, exclude = state.keptIds).iterator()
            val newPicks = state.picks.map { meal ->
                if (meal.id in state.keptIds) meal
                else if (fresh.hasNext()) fresh.next() else meal
            }
            _uiState.value = state.copy(picks = newPicks)
        }
    }
}
