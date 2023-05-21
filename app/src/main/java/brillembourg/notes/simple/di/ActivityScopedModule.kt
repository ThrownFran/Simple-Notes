package brillembourg.notes.simple.di

import android.content.Context
import brillembourg.notes.simple.data.database.RoomBackupBuilder
import brillembourg.notes.simple.data.database.backup.RoomBackupBuilderImp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
class ActivityScopedModule {


    /*Room backup needs the Activity Context. Inject this only in Activity*/
    @ActivityScoped
    @Provides
    fun backupPrepare(@ActivityContext context: Context): RoomBackupBuilder =
        RoomBackupBuilderImp(context)

}