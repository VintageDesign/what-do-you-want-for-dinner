package dev.rep.dinnerapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.rep.dinnerapp.data.Meal
import dev.rep.dinnerapp.data.Season

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickScreen(
    onOpenMeals: () -> Unit,
    viewModel: PickViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val mealCount by viewModel.mealCount.collectAsState()
    val maxDays = minOf(PickUiState.MAX_DAYS, mealCount).coerceAtLeast(1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("What's For Dinner?") },
                actions = {
                    IconButton(onClick = onOpenMeals) {
                        Icon(Icons.Filled.RestaurantMenu, contentDescription = "Edit meals")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = seasonLabel(viewModel.currentSeason),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(24.dp))

            DaySelector(
                days = state.days.coerceAtMost(maxDays),
                maxDays = maxDays,
                onChange = viewModel::setDays,
            )

            // One button: rerolls just the un-kept meals once some are kept,
            // otherwise does a fresh pick.
            val anyKept = state.keptIds.isNotEmpty()
            val rerollMode = state.hasPicked && anyKept && state.canReroll
            val label = when {
                !state.hasPicked -> "Pick dinners"
                rerollMode -> "Reroll the rest"
                else -> "Pick again"
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { if (rerollMode) viewModel.reroll() else viewModel.pick() },
                enabled = mealCount > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Casino, contentDescription = null)
                Text(text = "  $label")
            }

            Spacer(Modifier.height(16.dp))

            if (mealCount == 0) {
                Text(
                    text = "No meals yet. Tap the menu icon to add some.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 32.dp),
                )
            } else if (state.hasPicked) {
                Text(
                    text = "Tap a meal to keep it, then reroll the rest.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(state.picks) { index, meal ->
                    PickCard(
                        dayNumber = index + 1,
                        meal = meal,
                        kept = meal.id in state.keptIds,
                        onToggleKeep = { viewModel.toggleKeep(meal) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PickCard(
    dayNumber: Int,
    meal: Meal,
    kept: Boolean,
    onToggleKeep: () -> Unit,
) {
    val colors = if (kept) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleKeep),
        colors = colors,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Day $dayNumber",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(56.dp),
            )
            Text(
                text = meal.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onToggleKeep) {
                Icon(
                    imageVector = if (kept) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (kept) "Kept — tap to release" else "Tap to keep",
                    tint = if (kept) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DaySelector(days: Int, maxDays: Int, onChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        FilledIconButton(
            onClick = { onChange((days - 1).coerceAtLeast(1)) },
            enabled = days > 1,
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Fewer days")
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = days.toString(),
                style = MaterialTheme.typography.displayMedium,
            )
            Text(
                text = if (days == 1) "day" else "days",
                style = MaterialTheme.typography.labelMedium,
            )
        }
        FilledIconButton(
            onClick = { onChange((days + 1).coerceAtMost(maxDays)) },
            enabled = days < maxDays,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "More days")
        }
    }
}

private fun seasonLabel(season: Season): String = when (season) {
    Season.SUMMER -> "☀  Summer weighting"
    Season.WINTER -> "❄  Winter weighting"
    Season.ANY -> "Seasonal weighting: off"
}
