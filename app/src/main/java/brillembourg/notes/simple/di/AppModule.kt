package brillembourg.notes.simple.di

import android.content.Context
import brillembourg.notes.simple.data.*
import brillembourg.notes.simple.data.room.AppDatabase
import brillembourg.notes.simple.data.room.BackupAndRestoreProvider
import brillembourg.notes.simple.data.room.RoomBackupLib
import brillembourg.notes.simple.domain.Schedulers
import brillembourg.notes.simple.domain.SchedulersImp
import brillembourg.notes.simple.domain.repositories.BackupAndRestoreRepository
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import brillembourg.notes.simple.presentation.trash.MessageManager
import brillembourg.notes.simple.presentation.trash.MessageManagerImp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun dateProvider(): DateProvider = DateProviderImp()

    @Singleton
    @Provides
    fun taskRepo(database: NoteDatabase, dateProvider: DateProvider): NotesRepository =
        NotesRepositoryImp(database, dateProvider)

    @Singleton
    @Provides
    fun dataRepo(backupAndRestoreProvider: BackupAndRestoreProvider): BackupAndRestoreRepository =
        BackupAndRestoreRepositoryImp(backupAndRestoreProvider)

    @Singleton
    @Provides
    fun backupAndRestore(@ApplicationContext context: Context): BackupAndRestoreProvider {
        return RoomBackupLib(context)
    }

    @Singleton
    @Provides
    fun userPrefRepo(@ApplicationContext context: Context): UserPrefRepository {
        return UserPreferencesRepositoryImp(context)
    }

    @Singleton
    @Provides
    fun taskDatabase(
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
    fun dispatchers(): Schedulers {
        return SchedulersImp()
    }

    @Singleton
    @Provides
    fun messageManager(): MessageManager = MessageManagerImp()

}