package brillembourg.notes.simple.data

import brillembourg.notes.simple.data.room.toData
import brillembourg.notes.simple.data.room.toDomain
import brillembourg.notes.simple.domain.repositories.NotesRepository
import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.util.GetTaskException
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform

class NotesRepositoryImp(
    private val database: NoteDatabase,
    private val dateProvider: DateProvider
) : NotesRepository {

    override suspend fun unArchiveTasks(params: UnArchiveNotesUseCase.Params): Resource<UnArchiveNotesUseCase.Result> {
        return safeCall {
            database.unArchiveTasks(params.ids)
            val message = if (params.ids.size > 1) UiText.NotesUnarchived else UiText.NoteUnarchived
            Resource.Success(UnArchiveNotesUseCase.Result(message))
        }
    }

    override suspend fun archiveTasks(params: ArchiveNotesUseCase.Params): Resource<ArchiveNotesUseCase.Result> {
        return safeCall {
            database.archiveTasks(params.ids)
            Resource.Success(
                ArchiveNotesUseCase.Result(
                    if (params.ids.size > 1) UiText.NotesArchived else UiText.NoteArchived
                )
            )
        }
    }


    override suspend fun createTask(params: CreateNoteUseCase.Params): Resource<CreateNoteUseCase.Result> {

        return safeCall {
            val dateCreated = dateProvider.getCurrentTime()
            val task = database.createTask(
                title = params.title,
                content = params.content,
                dateCreated = dateCreated
            ).toDomain()
            Resource.Success(CreateNoteUseCase.Result(task, UiText.NoteCreated))
        }
    }

    override suspend fun deleteTask(params: DeleteNotesUseCase.Params): Resource<DeleteNotesUseCase.Result> {
        return safeCall {
            database.deleteTasks(params.ids)
            val message = if (params.ids.size > 1) UiText.NotesDeleted else UiText.NoteDeleted
            Resource.Success(DeleteNotesUseCase.Result(message))
        }
    }

    override fun getArchivedTasks(params: GetArchivedNotesUseCase.Params): Flow<Resource<GetArchivedNotesUseCase.Result>> {
        return database.getArchivedTasks()
            .debounce(200)
            .distinctUntilChanged()
            .transform {
                try {
                    val result = GetArchivedNotesUseCase.Result(
                        it.map { taskEntity -> taskEntity.toDomain() }
                    )
                    emit(Resource.Success(result))
                } catch (e: Exception) {
                    emit(Resource.Error(GetTaskException(e.message)))
                }
            }
    }

    override fun getTaskList(params: GetNotesUseCase.Params): Flow<Resource<GetNotesUseCase.Result>> {
        return database.getTaskList()
            .debounce(200)
            .distinctUntilChanged()
            .transform {
                try {
                    val taskListDomain = it.map { taskEntity -> taskEntity.toDomain() }
                    val result = GetNotesUseCase.Result(taskListDomain)
                    emit(Resource.Success(result))
                } catch (e: Exception) {
                    emit(Resource.Error(GetTaskException(e.message)))
                }
            }
    }

    override suspend fun saveTask(params: SaveNoteUseCase.Params): Resource<SaveNoteUseCase.Result> {
        return safeCall {
            database.saveTask(params.note.toData())
            Resource.Success(SaveNoteUseCase.Result(UiText.NoteUpdated))
        }
    }

    override suspend fun reorderTaskList(params: ReorderNotesUseCase.Params): Resource<ReorderNotesUseCase.Result> {
        return safeCall {
            database.saveTasksReordering(params.noteList.map { it.toData() })
            Resource.Success(ReorderNotesUseCase.Result(UiText.NotesReordered))
        }
    }

}