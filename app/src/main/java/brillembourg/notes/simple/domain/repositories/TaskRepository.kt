package brillembourg.notes.simple.domain.repositories

import brillembourg.notes.simple.domain.use_cases.CreateTaskUseCase
import brillembourg.notes.simple.domain.use_cases.DeleteTaskUseCase
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import brillembourg.notes.simple.domain.use_cases.SaveTaskUseCase
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun createTask(params: CreateTaskUseCase.Params): Flow<CreateTaskUseCase.Result>
    fun deleteTask(params: DeleteTaskUseCase.Params): Flow<DeleteTaskUseCase.Result>
    fun getTaskList(params: GetTaskListUseCase.Params): Flow<GetTaskListUseCase.Result>
    fun saveTask(params: SaveTaskUseCase.Params): Flow<SaveTaskUseCase.Result>
}