package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.*
import brillembourg.notes.simple.util.Resource
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun createTask(params: CreateTaskUseCase.Params): Resource<CreateTaskUseCase.Result>
    suspend fun saveTask(params: SaveTaskUseCase.Params): Resource<SaveTaskUseCase.Result>
    suspend fun reorderTaskList(params: ReorderTaskListUseCase.Params): Resource<ReorderTaskListUseCase.Result>
    suspend fun archiveTasks(params: ArchiveTasksUseCase.Params): Resource<ArchiveTasksUseCase.Result>
    suspend fun unArchiveTasks(params: UnArchiveTasksUseCase.Params): UnArchiveTasksUseCase.Result
    fun deleteTask(params: DeleteTasksUseCase.Params): Flow<DeleteTasksUseCase.Result>
    fun getTaskList(params: GetTaskListUseCase.Params): Flow<Resource<GetTaskListUseCase.Result>>
    fun getArchivedTasks(params: GetArchivedTasksUseCase.Params): Flow<GetArchivedTasksUseCase.Result>
}