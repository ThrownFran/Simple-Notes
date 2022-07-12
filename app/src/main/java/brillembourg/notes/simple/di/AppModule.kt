package brillembourg.notes.simple.di

import android.content.Context
import brillembourg.notes.simple.data.TaskCache
import brillembourg.notes.simple.data.TaskDatabase
import brillembourg.notes.simple.data.TaskRepositoryImp
import brillembourg.notes.simple.data.room.RoomDatabase
import brillembourg.notes.simple.domain.repositories.TaskRepository
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
    fun taskCache(): TaskCache = TaskCache()

    @Singleton
    @Provides
    fun taskRepo(cache: TaskCache, database: TaskDatabase): TaskRepository =
        TaskRepositoryImp(cache, database)

    @Singleton
    @Provides
    fun taskDatabase(
        @ApplicationContext appContext: Context,
        roomDatabase: RoomDatabase
    ): TaskDatabase =
        TaskDatabase(appContext,roomDatabase)

}