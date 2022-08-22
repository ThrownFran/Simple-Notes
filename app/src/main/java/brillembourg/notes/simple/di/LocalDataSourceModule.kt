package brillembourg.notes.simple.di

import android.content.Context
import brillembourg.notes.simple.data.NoteDatabase
import brillembourg.notes.simple.data.room.AppDatabase
import brillembourg.notes.simple.data.room.RoomBackupHandler
import brillembourg.notes.simple.data.room.RoomBackupLib
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalDataSourceModule {

    @Singleton
    @Provides
    fun noteDatabase(
        roomDatabase: AppDatabase
    ): NoteDatabase =
        NoteDatabase(roomDatabase)

    @Singleton
    @Provides
    fun getAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.invoke(context)
    }

    @Singleton
    @Provides
    fun backupAndRestore(): RoomBackupHandler {
        return RoomBackupLib()
    }

}