package brillembourg.notes.simple.di

import android.content.Context
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.data.database.NoteDatabase
import brillembourg.notes.simple.data.database.RoomBackupHandler
import brillembourg.notes.simple.data.repositories.BackupAndRestoreRepositoryImp
import brillembourg.notes.simple.data.repositories.NotesRepositoryImp
import brillembourg.notes.simple.data.repositories.UserPreferencesRepositoryImp
import brillembourg.notes.simple.domain.repositories.BackupAndRestoreRepository
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.domain.repositories.UserPrefRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun taskRepo(database: NoteDatabase, dateProvider: DateProvider): NotesRepository =
        NotesRepositoryImp(database, dateProvider)

    @Singleton
    @Provides
    fun userPrefRepo(@ApplicationContext context: Context): UserPrefRepository {
        return UserPreferencesRepositoryImp(context)
    }

    @Singleton
    @Provides
    fun dataRepo(roomBackupHandler: RoomBackupHandler): BackupAndRestoreRepository =
        BackupAndRestoreRepositoryImp(roomBackupHandler)
}