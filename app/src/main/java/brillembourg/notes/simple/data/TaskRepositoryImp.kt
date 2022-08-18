package brillembourg.notes.simple.data

import brillembourg.notes.simple.data.room.toData
import brillembourg.notes.simple.data.room.toDomain
import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.transform

class TaskRepositoryImp(
    private val database: TaskDatabase,
    private val dateProvider: DateProvider
) : TaskRepository {

    override suspend fun unArchiveTasks(params: UnArchiveTasksUseCase.Params): Resource<UnArchiveTasksUseCase.Result> {
        return safeCall {
            database.unArchiveTasks(params.ids)
            val message = if (params.ids.size > 1) UiText.NotesUnarchived else UiText.NoteUnarchived
            Resource.Success(UnArchiveTasksUseCase.Result(message))
        }
    }

    override suspend fun archiveTasks(params: ArchiveTasksUseCase.Params): Resource<ArchiveTasksUseCase.Result> {
        return safeCall {
            database.archiveTasks(params.ids)
            Resource.Success(
                ArchiveTasksUseCase.Result(
                    if (params.ids.size > 1) UiText.NotesArchived else UiText.NoteArchived
                )
            )
        }
    }


    override suspend fun createTask(params: CreateTaskUseCase.Params): Resource<CreateTaskUseCase.Result> {

        return safeCall {
            val dateCreated = dateProvider.getCurrentTime()
            val task = database.createTask(
                title = params.title,
                content = params.content,
                dateCreated = dateCreated
            ).toDomain()
            Resource.Success(CreateTaskUseCase.Result(task, UiText.NoteCreated))
        }
    }

    override suspend fun deleteTask(params: DeleteTasksUseCase.Params): Resource<DeleteTasksUseCase.Result> {
        return safeCall {
            database.deleteTasks(params.ids)
            val message = if (params.ids.size > 1) UiText.NotesDeleted else UiText.NoteDeleted
            Resource.Success(DeleteTasksUseCase.Result(message))
        }
    }

    override fun getArchivedTasks(params: GetArchivedTasksUseCase.Params): Flow<Resource<GetArchivedTasksUseCase.Result>> {
        return database.getArchivedTasks()
            .debounce(200)
            .transform {
                val result = GetArchivedTasksUseCase.Result(
                    it.map { taskEntity -> taskEntity.toDomain() }
                )
                emit(Resource.Success(result))
            }
    }

    override fun getTaskList(params: GetTaskListUseCase.Params): Flow<Resource<GetTaskListUseCase.Result>> {
        return database.getTaskList()
            .debounce(200)
            .transform {
                try {
                    val taskListDomain = it.map { taskEntity -> taskEntity.toDomain() }
                    val result = GetTaskListUseCase.Result(taskListDomain)
                    emit(Resource.Success(result))
                } catch (e: Exception) {
                    emit(Resource.Error(e))
                }
            }
    }

    override suspend fun saveTask(params: SaveTaskUseCase.Params): Resource<SaveTaskUseCase.Result> {
        return safeCall {
            database.saveTask(params.task.toData())
            Resource.Success(SaveTaskUseCase.Result(UiText.NoteUpdated))
        }
    }

    override suspend fun reorderTaskList(params: ReorderTaskListUseCase.Params): Resource<ReorderTaskListUseCase.Result> {
        return safeCall {
            database.saveTasksReordering(params.taskList.map { it.toData() })
            Resource.Success(ReorderTaskListUseCase.Result(UiText.NotesReordered))
        }
    }

}