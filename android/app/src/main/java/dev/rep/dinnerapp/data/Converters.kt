package dev.rep.dinnerapp.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun seasonToString(season: Season): String = season.jsonValue

    @TypeConverter
    fun stringToSeason(value: String?): Season = Season.fromJson(value)
}
