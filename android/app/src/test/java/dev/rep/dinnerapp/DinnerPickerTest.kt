package dev.rep.dinnerapp

import dev.rep.dinnerapp.data.DinnerPicker
import dev.rep.dinnerapp.data.Meal
import dev.rep.dinnerapp.data.Season
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class DinnerPickerTest {

    private fun meals(n: Int, season: Season = Season.ANY): List<Meal> =
        (1..n).map { Meal(id = it.toLong(), name = "Meal $it", season = season) }

    @Test
    fun `returns exactly the requested number of distinct meals`() {
        val result = DinnerPicker.pick(meals(20), days = 5, rng = Random(42))
        assertEquals(5, result.size)
        assertEquals(5, result.map { it.id }.toSet().size)
    }

    @Test
    fun `clamps days to the number of available meals`() {
        val result = DinnerPicker.pick(meals(3), days = 7, rng = Random(1))
        assertEquals(3, result.size)
    }

    @Test
    fun `clamps days up to at least one`() {
        val result = DinnerPicker.pick(meals(5), days = 0, rng = Random(1))
        assertEquals(1, result.size)
    }

    @Test
    fun `empty meal list yields empty result`() {
        assertTrue(DinnerPicker.pick(emptyList(), days = 5).isEmpty())
    }

    @Test
    fun `excluded meals never appear in the result`() {
        val pool = meals(20)
        val exclude = setOf(1L, 2L, 3L, 4L, 5L)
        repeat(200) { seed ->
            val result = DinnerPicker.pick(pool, days = 5, exclude = exclude, rng = Random(seed.toLong()))
            assertEquals(5, result.size)
            assertTrue(result.none { it.id in exclude })
        }
    }

    @Test
    fun `days clamps to the excluded pool size`() {
        // 5 meals, exclude 3 -> at most 2 can be returned.
        val result = DinnerPicker.pick(meals(5), days = 5, exclude = setOf(1L, 2L, 3L), rng = Random(7))
        assertEquals(2, result.size)
    }

    @Test
    fun `winter-only meals are down-weighted in summer`() {
        // July -> summer. One winter meal competing against many "any" meals.
        val july = LocalDate.of(2026, 7, 20)
        val pool = meals(9, Season.ANY) + Meal(id = 99, name = "Stew", season = Season.WINTER)

        var winterPickedFirstSlot = 0
        val trials = 4000
        repeat(trials) { seed ->
            val picks = DinnerPicker.pick(pool, days = 1, today = july, rng = Random(seed.toLong()))
            if (picks.single().id == 99L) winterPickedFirstSlot++
        }

        // Unweighted, the winter meal would be chosen ~1/10 of the time. With a 0.25
        // weight against nine 1.0 meals it should land well under that.
        val rate = winterPickedFirstSlot.toDouble() / trials
        assertTrue("winter pick rate was $rate, expected < 0.06", rate < 0.06)
    }

    @Test
    fun `season detection matches the script month buckets`() {
        assertEquals(Season.WINTER, Season.current(LocalDate.of(2026, 1, 15)))
        assertEquals(Season.WINTER, Season.current(LocalDate.of(2026, 12, 15)))
        assertEquals(Season.SUMMER, Season.current(LocalDate.of(2026, 7, 15)))
        assertEquals(Season.ANY, Season.current(LocalDate.of(2026, 4, 15)))
    }
}
