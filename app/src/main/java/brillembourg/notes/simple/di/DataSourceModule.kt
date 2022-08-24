package brillembourg.notes.simple.di

import android.content.Context
import brillembourg.notes.simple.data.database.AppDatabase
import brillembourg.notes.simple.data.database.RoomBackupHandler
import brillembourg.notes.simple.data.database.RoomBackupLib
import brillembourg.notes.simple.data.database.categories.CategoriesDatabase
import brillembourg.notes.simple.data.database.notes.NoteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataSourceModule {

    @Singleton
    @Provides
    fun getAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.invoke(context)
    }

    @Singleton
    @Provides
    fun noteDatabase(
        roomDatabase: AppDatabase
    ): NoteDatabase =
        NoteDatabase(roomDatabase)

    @Singleton
    @Provides
    fun categoryDatabase(
        roomDatabase: AppDatabase
    ): CategoriesDatabase =
        CategoriesDatabase(roomDatabase)


    @Singleton
    @Provides
    fun backupAndRestore(): RoomBackupHandler {
        return RoomBackupLib()
    }
}