package brillembourg.notes.simple.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.use_cases.DeleteTaskUseCase
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import brillembourg.notes.simple.ui.SingleLiveEvent
import brillembourg.notes.simple.ui.TaskPresentationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTaskListUseCase: GetTaskListUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    var state: MutableLiveData<HomeState> = MutableLiveData()
    val navigateToDetail: SingleLiveEvent<TaskPresentationModel> = SingleLiveEvent()
    val navigateToCreateTask: SingleLiveEvent<Any> = SingleLiveEvent()

    fun getTaskList() {
        getTaskListUseCase.execute(GetTaskListUseCase.Params())
            .onEach {
                state.value = HomeState.TaskListSuccess(it.taskList.map { taskModel ->
                    TaskPresentationModel(
                        taskModel.id,
                        taskModel.content,
                        taskModel.date
                    )
                })
            }
            .launchIn(viewModelScope)
    }

    fun clickItem(it: TaskPresentationModel) {
        navigateToDetail.value = it
    }

    fun createTask() {
        navigateToCreateTask.value = Any()
    }

    fun longClick(it: TaskPresentationModel) {
        deleteTask(it)
    }

    private fun deleteTask(it: TaskPresentationModel) {
        deleteTaskUseCase.execute(DeleteTaskUseCase.Params(it.id))
            .onEach {
                getTaskList()
            }
            .launchIn(viewModelScope)
    }

}