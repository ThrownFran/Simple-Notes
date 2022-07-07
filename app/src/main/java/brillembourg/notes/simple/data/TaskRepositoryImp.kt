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

class TaskRepositoryImp : TaskRepository {

    val cacheList: MutableList<Task> = arrayListOf(
        Task(1L, "Andrea Perez", ""),
        Task(2L, "I love you", ""),
        Task(3L, "from here to the moon", "")
    )


//    val cacheList: MutableList<Task> = arrayListOf(
//        Task(1L,"Andrea Perez",""),
//        Task(2L,"I love you",""),
//        Task(3L,"from here to the moon",""),
//        Task(4L,"Andrea Perez",""),
//        Task(5L,"I love you",""),
//        Task(6L,"from here to the moon",""),
//        Task(7L,"Andrea Perez",""),
//        Task(8L,"I love you",""),
//        Task(9L,"from here to the moon",""),
//        Task(10L,"Andrea Perez",""),
//        Task(11L,"I love you",""),
//        Task(12L,"from here to the moon",""),
//        Task(13L,"Andrea Perez",""),
//        Task(14L,"I love you",""),
//        Task(15L,"from here to the moon",""),
//        Task(16L,"I love you",""),
//        Task(17L,"from here to the moon",""),
//        Task(18L,"Andrea Perez",""),
//        Task(19L,"I love you",""),
//        Task(20L,"from here to the moon",""),
//        Task(21L,"Andrea Perez",""),
//        Task(22L,"I love you",""),
//        Task(23L,"I love you",""),
//        Task(24L,"from here to the moon",""),
//        Task(25L,"Andrea Perez",""),
//        Task(26L,"I love you",""),
//        Task(27L,"from here to the moon",""),
//        Task(28L,"Andrea Perez",""),
//        Task(29L,"I love you",""),
//    )

    var cacheIdCounter: Long = 4L

    override fun createTask(params: CreateTaskUseCase.Params): Flow<CreateTaskUseCase.Result> {
        return flow {
            cacheList.add(Task(cacheIdCounter, params.content, params.date))
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

            emit(GetTaskListUseCase.Result(cacheList))

//            val list: MutableList<Task> = ArrayList()
//
//            delay(1000)
//
//            list.add(cacheList[0])
//            emit(GetTaskListUseCase.Result(list))
//
//            delay(1000)
//
//            list.add(cacheList[1])
//            emit(GetTaskListUseCase.Result(list))
//
//            delay(1000)
//
//            list.add(cacheList[2])
//            emit(GetTaskListUseCase.Result(list))
        }
    }

    override fun saveTask(params: SaveTaskUseCase.Params): Flow<SaveTaskUseCase.Result> {

        //Update Task
        val taskToUpdate: Task? = params.id?.let { findTaskById(it) }
        taskToUpdate?.let {
            return flow {
                it.content = params.content
                emit(SaveTaskUseCase.Result("Task updated"))
            }
        }

        //Create Task
        return flow {
            cacheList.add(Task(cacheIdCounter, params.content, ""))
            cacheIdCounter += 1L
            emit(SaveTaskUseCase.Result("Task saved"))
        }
    }
}