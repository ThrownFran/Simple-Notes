package brillembourg.notes.simple.di

import android.content.Context
import brillembourg.notes.simple.data.*
import brillembourg.notes.simple.data.room.AppDatabase
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
    fun dateProvider(): DateProvider = DateProviderImp()

    @Singleton
    @Provides
    fun taskRepo(database: TaskDatabase, dateProvider: DateProvider): TaskRepository =
        TaskRepositoryImp(database,dateProvider)

    @Singleton
    @Provides
    fun taskDatabase(
        @ApplicationContext appContext: Context,
        roomDatabase: AppDatabase
    ): TaskDatabase =
        TaskDatabase(appContext,roomDatabase)

    @Singleton
    @Provides
    fun getAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.invoke(context)
    }

}