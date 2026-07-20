package dev.rep.dinnerapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.rep.dinnerapp.data.AppDatabase
import dev.rep.dinnerapp.data.Meal
import dev.rep.dinnerapp.data.Season
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MealListViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).mealDao()

    val meals: StateFlow<List<Meal>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(existing: Meal?, name: String, season: Season, recipeUrl: String?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val url = recipeUrl?.trim()?.ifBlank { null }
        viewModelScope.launch {
            if (existing == null) {
                dao.insert(Meal(name = trimmed, season = season, recipeUrl = url))
            } else {
                dao.update(existing.copy(name = trimmed, season = season, recipeUrl = url))
            }
        }
    }

    fun delete(meal: Meal) {
        viewModelScope.launch { dao.delete(meal) }
    }
}
