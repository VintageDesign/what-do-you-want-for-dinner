package dev.rep.dinnerapp.data

import java.time.LocalDate

/**
 * Seasonal categorization of a meal, mirroring the `season` field in the original
 * `pick-dinner.py` list.json ("any" / "summer" / "winter").
 */
enum class Season(val jsonValue: String) {
    ANY("any"),
    SUMMER("summer"),
    WINTER("winter");

    companion object {
        fun fromJson(value: String?): Season =
            entries.firstOrNull { it.jsonValue.equals(value?.trim(), ignoreCase = true) } ?: ANY

        /**
         * The active season for a given date, matching the month buckets in pick-dinner.py:
         * winter = Jan, Feb, Oct, Nov, Dec; summer = Jun, Jul, Aug; everything else = any.
         */
        fun current(today: LocalDate = LocalDate.now()): Season = when (today.monthValue) {
            1, 2, 10, 11, 12 -> WINTER
            6, 7, 8 -> SUMMER
            else -> ANY
        }
    }
}
