package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.*
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun createTask(params: CreateTaskUseCase.Params): CreateTaskUseCase.Result
    suspend fun archiveTasks(params: ArchiveTasksUseCase.Params): ArchiveTasksUseCase.Result
    suspend fun unArchiveTasks(params: UnArchiveTasksUseCase.Params): UnArchiveTasksUseCase.Result
    suspend fun reorderTaskList(params: ReorderTaskListUseCase.Params): ReorderTaskListUseCase.Result

    fun deleteTask(params: DeleteTasksUseCase.Params): Flow<DeleteTasksUseCase.Result>
    fun getTaskList(params: GetTaskListUseCase.Params): Flow<GetTaskListUseCase.Result>
    fun getArchivedTasks(params: GetArchivedTasksUseCase.Params): Flow<GetArchivedTasksUseCase.Result>
    fun saveTask(params: SaveTaskUseCase.Params): Flow<SaveTaskUseCase.Result>
}