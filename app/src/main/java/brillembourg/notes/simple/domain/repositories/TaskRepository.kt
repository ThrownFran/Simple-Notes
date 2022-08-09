package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.*
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun createTask(params: CreateTaskUseCase.Params): Flow<CreateTaskUseCase.Result>
    fun deleteTask(params: DeleteTasksUseCase.Params): Flow<DeleteTasksUseCase.Result>
    fun getTaskList(params: GetTaskListUseCase.Params): Flow<GetTaskListUseCase.Result>
    fun saveTask(params: SaveTaskUseCase.Params): Flow<SaveTaskUseCase.Result>
    fun reorderTaskList(params: ReorderTaskListUseCase.Params): Flow<ReorderTaskListUseCase.Result>
}