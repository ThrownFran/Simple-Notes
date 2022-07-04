package brillembourg.notes.simple.di

import brillembourg.notes.simple.data.TaskRepositoryImp
import brillembourg.notes.simple.domain.repositories.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Singleton
    @Provides
    fun taskRepo () : TaskRepository = TaskRepositoryImp()

}