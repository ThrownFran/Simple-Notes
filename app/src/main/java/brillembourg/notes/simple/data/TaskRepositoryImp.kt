package brillembourg.notes.simple.data

import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.domain.use_cases.CreateTaskUseCase
import brillembourg.notes.simple.domain.use_cases.DeleteTaskUseCase
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import brillembourg.notes.simple.domain.use_cases.SaveTaskUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TaskRepositoryImp: TaskRepository {

    val cacheList: MutableList<Task> = arrayListOf(
        Task(1L,"Andrea Perez",""),
        Task(2L,"I love you",""),
        Task(3L,"from here to the moon","")
    )

    var cacheIdCounter: Long = 1

    override fun createTask(params: CreateTaskUseCase.Params): Flow<CreateTaskUseCase.Result> {
        return flow {
            cacheList.add(Task(cacheIdCounter,params.content,params.date))
            cacheIdCounter += 1L
            emit(CreateTaskUseCase.Result("Task created"))
        }
    }

    override fun deleteTask(params: DeleteTaskUseCase.Params): Flow<DeleteTaskUseCase.Result> {
        return flow {
            val taskToDelete = findTaskById(params.id)
            cacheList.remove(taskToDelete)
            emit(DeleteTaskUseCase.Result("Task deleted"))
        }
    }

    private fun findTaskById(
        id: Long
    ): Task? {
        cacheList.forEach {
            if (it.id == id) return it
        }
        return null
    }

    override fun getTaskList(params: GetTaskListUseCase.Params): Flow<GetTaskListUseCase.Result> {
        return flow {
            val list: MutableList<Task> = ArrayList()

            delay(1000)

            list.add(cacheList[0])
            emit(GetTaskListUseCase.Result(list))

            delay(1000)

            list.add(cacheList[1])
            emit(GetTaskListUseCase.Result(list))

            delay(1000)

            list.add(cacheList[2])
            emit(GetTaskListUseCase.Result(list))
        }
    }

    override fun saveTask(params: SaveTaskUseCase.Params): Flow<SaveTaskUseCase.Result> {
        return flow {
            val taskToUpdate = findTaskById(params.id)
            if(taskToUpdate == null) {
                emit(SaveTaskUseCase.Result("Task not found"))
            } else {
                taskToUpdate.content = params.content
                emit(SaveTaskUseCase.Result("Task updated"))
            }
        }
    }
}