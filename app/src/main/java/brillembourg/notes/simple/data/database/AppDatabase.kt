package brillembourg.notes.simple.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import brillembourg.notes.simple.data.database.categories.CategoryDao
import brillembourg.notes.simple.data.database.categories.CategoryEntity
import brillembourg.notes.simple.data.database.notes.TaskDao
import brillembourg.notes.simple.data.database.notes.TaskEntity


@Database(
    entities = [TaskEntity::class, CategoryEntity::class], version = 7
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance
            ?: synchronized(LOCK) {
                instance
                    ?: buildDatabase(
                        context
                    ).also {
                        instance = it
                    }
            }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "task_database"
        )
            .addMigrations(MIGRATION_5_to_6)
            .addMigrations(MIGRATION_6_to_7)
            .build()


        private val MIGRATION_5_to_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE taskentity"
                            + " ADD COLUMN is_archived INTEGER NOT NULL DEFAULT '0'"
                )
            }
        }

        private val MIGRATION_6_to_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `categoryentity` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `order` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
    }


}