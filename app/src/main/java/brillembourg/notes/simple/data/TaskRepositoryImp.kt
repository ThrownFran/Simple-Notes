package brillembourg.notes.simple.data

import brillembourg.notes.simple.data.room.toDomain
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.domain.use_cases.CreateTaskUseCase
import brillembourg.notes.simple.domain.use_cases.DeleteTaskUseCase
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import brillembourg.notes.simple.domain.use_cases.SaveTaskUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TaskRepositoryImp(
    val cache: TaskCache,
    val database: TaskDatabase
) : TaskRepository {

    override fun createTask(params: CreateTaskUseCase.Params): Flow<CreateTaskUseCase.Result> {
        return flow {
            val task = database.createTask(params.content).toDomain()
            emit(CreateTaskUseCase.Result(task, "Task created"))
        }
    }

    override fun deleteTask(params: DeleteTaskUseCase.Params): Flow<DeleteTaskUseCase.Result> {
        return flow {
            database.roomDatabase.taskDao().delete(params.id)
            emit(DeleteTaskUseCase.Result("task deleted"))
        }
    }

    override fun getTaskList(params: GetTaskListUseCase.Params): Flow<GetTaskListUseCase.Result> {
        return flow {
            val taskList = database.getTaskList().map { it.toDomain() }
            emit(GetTaskListUseCase.Result(taskList))
        }
    }

    override fun saveTask(params: SaveTaskUseCase.Params): Flow<SaveTaskUseCase.Result> {
        return flow {
            database.saveTask(params.task)
            emit(SaveTaskUseCase.Result("task updated"))
        }
    }

}