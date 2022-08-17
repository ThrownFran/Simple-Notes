package brillembourg.notes.simple.data

import brillembourg.notes.simple.data.room.toData
import brillembourg.notes.simple.data.room.toDomain
import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform

class TaskRepositoryImp(
    val database: TaskDatabase,
    val dateProvider: DateProvider
) : TaskRepository {

    override suspend fun unArchiveTasks(params: UnArchiveTasksUseCase.Params): UnArchiveTasksUseCase.Result {
        database.unArchiveTasks(params.ids)
        return UnArchiveTasksUseCase.Result(
            if (params.ids.size > 1) "Notes unarchived" else "Note archived"
        )
    }

    override suspend fun archiveTasks(params: ArchiveTasksUseCase.Params): Resource<ArchiveTasksUseCase.Result> {
        return safeCall {
            database.archiveTasks(params.ids)
            Resource.Success(
                ArchiveTasksUseCase.Result(
                    if (params.ids.size > 1) "Notes archived" else "Note archived"
                )
            )
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> Resource<T>): Resource<T> {
        return try {
            block.invoke()
        } catch (e: Exception) {
            Resource.Error(e)
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
            Resource.Success(CreateTaskUseCase.Result(task, "Note created"))
        }
    }

    override fun deleteTask(params: DeleteTasksUseCase.Params): Flow<DeleteTasksUseCase.Result> {
        return flow {
            database.deleteTasks(params.ids)
            emit(
                DeleteTasksUseCase.Result(
                    if (params.ids.size > 1) "Notes deleted" else "Note deleted"
                )
            )
        }
    }

    override fun getArchivedTasks(params: GetArchivedTasksUseCase.Params): Flow<GetArchivedTasksUseCase.Result> {
        return database.getArchivedTasks()
            .debounce(200)
            .transform {
                emit(GetArchivedTasksUseCase.Result(
                    it.map { taskEntity -> taskEntity.toDomain() }
                ))
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
            Resource.Success(SaveTaskUseCase.Result("Note updated"))
        }
    }

    override suspend fun reorderTaskList(params: ReorderTaskListUseCase.Params): Resource<ReorderTaskListUseCase.Result> {
        return safeCall {
            database.saveTasksReordering(params.taskList.map { it.toData() })
            Resource.Success(ReorderTaskListUseCase.Result("Notes reordered"))
        }
    }

}