package dev.rep.dinnerapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

@Database(entities = [Meal::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext).also { INSTANCE = it }
            }

        private fun build(context: Context): AppDatabase {
            lateinit var database: AppDatabase
            database = Room.databaseBuilder(context, AppDatabase::class.java, "dinners.db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed from bundled assets/list.json on first creation.
                        CoroutineScope(Dispatchers.IO).launch {
                            val meals = readSeed(context)
                            if (meals.isNotEmpty()) database.mealDao().insertAll(meals)
                        }
                    }
                })
                .build()
            return database
        }

        /** Parse assets/list.json into [Meal] rows. Mirrors the { name, season } shape. */
        private fun readSeed(context: Context): List<Meal> {
            val text = context.assets.open("list.json").bufferedReader().use { it.readText() }
            val array = JSONArray(text)
            return buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val name = obj.optString("name").trim()
                    if (name.isEmpty()) continue
                    add(
                        Meal(
                            name = name,
                            season = Season.fromJson(obj.optString("season")),
                            recipeUrl = obj.optString("recipe").ifBlank { null },
                        )
                    )
                }
            }
        }
    }
}
