package brillembourg.notes.simple.di

import brillembourg.notes.simple.presentation.home.delete.UiState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SharedStateModule {

    @Singleton
    @Provides
    fun uiState(): UiState = UiState()

}