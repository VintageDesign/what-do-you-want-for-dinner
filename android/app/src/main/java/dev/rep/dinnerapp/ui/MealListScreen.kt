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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.rep.dinnerapp.data.Meal
import dev.rep.dinnerapp.data.Season

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealListScreen(
    onBack: () -> Unit,
    viewModel: MealListViewModel = viewModel(),
) {
    val meals by viewModel.meals.collectAsState()
    var editing by remember { mutableStateOf<EditTarget?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meals (${meals.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = EditTarget(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add meal")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(meals, key = { it.id }) { meal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editing = EditTarget(meal) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(meal.name, style = MaterialTheme.typography.titleMedium)
                            if (meal.season != Season.ANY) {
                                Text(
                                    seasonName(meal.season),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.delete(meal) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete ${meal.name}")
                        }
                    }
                }
            }
        }
    }

    editing?.let { target ->
        MealEditDialog(
            target = target,
            onDismiss = { editing = null },
            onSave = { name, season, url ->
                viewModel.save(target.meal, name, season, url)
                editing = null
            },
        )
    }
}

private data class EditTarget(val meal: Meal?)

@Composable
private fun MealEditDialog(
    target: EditTarget,
    onDismiss: () -> Unit,
    onSave: (String, Season, String?) -> Unit,
) {
    var name by remember { mutableStateOf(target.meal?.name ?: "") }
    var season by remember { mutableStateOf(target.meal?.season ?: Season.ANY) }
    var url by remember { mutableStateOf(target.meal?.recipeUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (target.meal == null) "Add meal" else "Edit meal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Season", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Season.entries.forEach { option ->
                        FilterChip(
                            selected = season == option,
                            onClick = { season = option },
                            label = { Text(seasonName(option)) },
                            modifier = Modifier.selectable(selected = season == option) {},
                        )
                    }
                }
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Recipe URL (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(0.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, season, url) },
                enabled = name.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun seasonName(season: Season): String = when (season) {
    Season.ANY -> "Any"
    Season.SUMMER -> "Summer"
    Season.WINTER -> "Winter"
}
