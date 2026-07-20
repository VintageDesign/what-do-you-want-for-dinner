package dev.rep.dinnerapp.data

import java.time.LocalDate
import kotlin.math.pow
import kotlin.random.Random

/**
 * Pure port of `pick-dinner.py`: seasonally-weighted random selection of distinct meals.
 *
 * The original script hardcoded 5 meals for the week; here the caller chooses how many
 * days to pick. The three steps mirror the script exactly:
 *   1. Detect the current season from the month (see [Season.current]).
 *   2. Weight each meal by how well it fits the season (see [weightFor]).
 *   3. Draw `days` distinct meals via weighted sampling without replacement.
 *
 * Kept free of Android dependencies so it can be unit-tested on the JVM by injecting
 * a fixed [today] and a seeded [rng].
 */
object DinnerPicker {

    /**
     * Per-meal weight, matching pick-dinner.py lines 54-58:
     *   - a winter meal in summer is unlikely  -> 0.25
     *   - a summer meal in winter is possible   -> 0.5 (more likely to grill in winter
     *     than to have soup in summer)
     *   - everything else                       -> 1.0
     */
    fun weightFor(meal: Meal, season: Season): Double = when {
        meal.season == Season.WINTER && season == Season.SUMMER -> 0.25
        meal.season == Season.SUMMER && season == Season.WINTER -> 0.5
        else -> 1.0
    }

    /**
     * Pick [days] distinct meals, seasonally weighted. Any meal whose id is in [exclude]
     * is dropped from the pool first (used by "keep + reroll" so a reroll never returns a
     * meal the user is keeping). [days] is clamped to `[1, pool.size]` so asking for more
     * days than there are meals returns them all (the numpy version would instead raise
     * "cannot take a larger sample than population"). Returns an empty list when the pool
     * is empty.
     *
     * Uses Efraimidis-Spirakis weighted reservoir sampling: each meal gets a key of
     * `u^(1/weight)` for `u` uniform in (0,1], and the highest keys win. This is the
     * standard correct method for weighted sampling without replacement and reproduces
     * the semantics of `np.random.choice(..., replace=False, p=...)`.
     */
    fun pick(
        meals: List<Meal>,
        days: Int,
        exclude: Set<Long> = emptySet(),
        today: LocalDate = LocalDate.now(),
        rng: Random = Random.Default,
    ): List<Meal> {
        val pool = if (exclude.isEmpty()) meals else meals.filter { it.id !in exclude }
        if (pool.isEmpty()) return emptyList()
        val n = days.coerceIn(1, pool.size)
        val season = Season.current(today)

        return pool
            .map { meal ->
                // u in (0, 1] avoids log/pow issues at exactly 0.
                val u = 1.0 - rng.nextDouble()
                val key = u.pow(1.0 / weightFor(meal, season))
                meal to key
            }
            .sortedByDescending { it.second }
            .take(n)
            .map { it.first }
    }
}
