package brillembourg.notes.simple.data

import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.use_cases.CreateTaskUseCase
import brillembourg.notes.simple.domain.use_cases.DeleteTaskUseCase
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import brillembourg.notes.simple.domain.use_cases.SaveTaskUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TaskCache {

    val cacheList: MutableList<Task> = arrayListOf(
        Task(1L, content = "Andrea Perez", date = ""),
        Task(2L, content = "I love you", date = ""),
        Task(3L, content = "from here to the moon", date = "")
    )

    var cacheIdCounter: Long = 4L

    fun createTask(params: CreateTaskUseCase.Params): Flow<CreateTaskUseCase.Result> {
        return flow {
            val task = Task(cacheIdCounter, content = params.content, date = "")
            cacheList.add(task)
            cacheIdCounter += 1L
            emit(CreateTaskUseCase.Result(task,"Task created"))
        }
    }

    fun deleteTask(params: DeleteTaskUseCase.Params): Flow<DeleteTaskUseCase.Result> {
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

    fun getTaskList(params: GetTaskListUseCase.Params): Flow<GetTaskListUseCase.Result> {
        return flow {
            emit(GetTaskListUseCase.Result(cacheList))
        }
    }

    fun saveTask(params: SaveTaskUseCase.Params): Flow<SaveTaskUseCase.Result> {

        //Update Task
        val taskToUpdate: Task? = params.task.id?.let { findTaskById(it) }
        taskToUpdate?.let {
            return flow {
                it.content = params.task.content
                emit(SaveTaskUseCase.Result("Task updated"))
            }
        }

        //Create Task
        return flow {
            cacheList.add(Task(cacheIdCounter, content = params.task.content, date = ""))
            cacheIdCounter += 1L
            emit(SaveTaskUseCase.Result("Task saved"))
        }
    }

}