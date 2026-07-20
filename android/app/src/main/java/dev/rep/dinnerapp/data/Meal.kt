package dev.rep.dinnerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single dinner option. Ports the JSON shape used by pick-dinner.py
 * ({ "name", "season" }) and leaves room for the optional recipe url the
 * script's docstring already anticipates.
 */
@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val season: Season = Season.ANY,
    val recipeUrl: String? = null,
)
