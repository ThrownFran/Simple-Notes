package brillembourg.notes.simple.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import brillembourg.notes.simple.data.database.categories.CategoryDao
import brillembourg.notes.simple.data.database.categories.CategoryEntity
import brillembourg.notes.simple.data.database.notes.NoteEntity
import brillembourg.notes.simple.data.database.notes.TaskDao


@Database(
    entities = [NoteEntity::class, CategoryEntity::class, CategoryNoteCrossRef::class], version = 10
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
            .addMigrations(MIGRATION_7_to_8)
            .addMigrations(MIGRATION_8_to_9)
            .addMigrations(MIGRATION_9_to_10)
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
                database.execSQL("CREATE TABLE IF NOT EXISTS `categoryentity` (`id` INTEGER, `name` TEXT NOT NULL, `order` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        private val MIGRATION_7_to_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE categoryentity RENAME COLUMN id TO category_id")
                database.execSQL("ALTER TABLE taskentity RENAME COLUMN id TO note_id")
            }
        }

        private val MIGRATION_8_to_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `category_note_cross_ref` (`category_id` INTEGER NOT NULL, `note_id` INTEGER NOT NULL, PRIMARY KEY(`category_id`, `note_id`))")
            }
        }

        private val MIGRATION_9_to_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("CREATE TABLE IF NOT EXISTS `category_note_cross_ref_copy` (`category_id` INTEGER NOT NULL, `note_id` INTEGER NOT NULL, PRIMARY KEY(`category_id`, `note_id`), FOREIGN KEY(`category_id`) REFERENCES `categoryentity`(`category_id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`note_id`) REFERENCES `taskentity`(`note_id`) ON UPDATE NO ACTION ON DELETE CASCADE )")


                // Copy the data
                database.execSQL(
                    "INSERT INTO `category_note_cross_ref_copy` (category_id, note_id, category_id,note_id) " +
                            "SELECT category_id, note_id, category_id, note_id " +
                            "FROM category_note_cross_ref"
                )
                // Remove old table
                database.execSQL("DROP TABLE category_note_cross_ref")
                // Change name of table to correct one
                database.execSQL("ALTER TABLE category_note_cross_ref_copy RENAME TO category_note_cross_ref")
            }
        }


    }


}