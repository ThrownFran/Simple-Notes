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

    override fun createTask(params: CreateTaskUseCase.Params): Flow<CreateTaskUseCase.Result> {
        return flow {
            val dateCreated = dateProvider.getCurrentTime()
            val task = database.createTask(
                title = params.title,
                content = params.content,
                dateCreated = dateCreated
            ).toDomain()
            emit(CreateTaskUseCase.Result(task, "Task created"))
        }
    }

    override fun deleteTask(params: DeleteTaskUseCase.Params): Flow<DeleteTaskUseCase.Result> {
        return flow {
            database.deleteTask(params.id)
            emit(DeleteTaskUseCase.Result("Task deleted"))
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
//        return flow {
//            val taskList = database.getTaskList().map { it.toDomain() }
//            emit(GetTaskListUseCase.Result(taskList))
//        }
    }

    override fun saveTask(params: SaveTaskUseCase.Params): Flow<SaveTaskUseCase.Result> {
        return flow {
            database.saveTask(params.task.toData())
            emit(SaveTaskUseCase.Result("Task updated"))
        }
    }

    override fun saveTaskList(params: SaveTaskListUseCase.Params): Flow<SaveTaskListUseCase.Result> {
        return flow {
            database.saveTasks(params.taskList.map { it.toData() })
            emit(SaveTaskListUseCase.Result("Tasks saved"))
        }
    }
}