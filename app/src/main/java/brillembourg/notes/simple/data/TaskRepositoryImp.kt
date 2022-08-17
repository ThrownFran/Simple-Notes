package brillembourg.notes.simple.data

import brillembourg.notes.simple.data.room.toData
import brillembourg.notes.simple.data.room.toDomain
import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.domain.use_cases.*
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

    override suspend fun archiveTasks(params: ArchiveTasksUseCase.Params): ArchiveTasksUseCase.Result {
        database.archiveTasks(params.ids)
        return ArchiveTasksUseCase.Result(
            if (params.ids.size > 1) "Notes archived" else "Note archived"
        )
    }

    override suspend fun createTask(params: CreateTaskUseCase.Params): CreateTaskUseCase.Result {

        val dateCreated = dateProvider.getCurrentTime()
        val task = database.createTask(
            title = params.title,
            content = params.content,
            dateCreated = dateCreated
        ).toDomain()
        return CreateTaskUseCase.Result(task, "Note created")

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

    override fun getTaskList(params: GetTaskListUseCase.Params): Flow<GetTaskListUseCase.Result> {
        return database.getTaskList()
            .debounce(200)
            .transform {
                emit(GetTaskListUseCase.Result(
                    it.map { taskEntity -> taskEntity.toDomain() }
                ))
            }
    }

    override fun saveTask(params: SaveTaskUseCase.Params): Flow<SaveTaskUseCase.Result> {
        return flow {
            database.saveTask(params.task.toData())
            emit(SaveTaskUseCase.Result("Note updated"))
        }
    }

    override suspend fun reorderTaskList(params: ReorderTaskListUseCase.Params): ReorderTaskListUseCase.Result {
        database.saveTasksReordering(params.taskList.map { it.toData() })
        return ReorderTaskListUseCase.Result("Notes reordered")
    }

}