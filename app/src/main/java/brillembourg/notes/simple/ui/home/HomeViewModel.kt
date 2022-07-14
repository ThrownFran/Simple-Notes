package brillembourg.notes.simple.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.use_cases.DeleteTaskUseCase
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import brillembourg.notes.simple.ui.extras.SingleLiveEvent
import brillembourg.notes.simple.ui.models.TaskPresentationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTaskListUseCase: GetTaskListUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val dateProvider: DateProvider
) : ViewModel() {

    private val _state: MutableLiveData<HomeState> = MutableLiveData()
    private val _navigateToDetailEvent: SingleLiveEvent<TaskPresentationModel> = SingleLiveEvent()
    private val _navigateToCreateEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    private val _messageEvent: SingleLiveEvent<String> = SingleLiveEvent()

    //Observables
    val state : LiveData<HomeState> = _state
    val navigateToDetailEvent: LiveData<TaskPresentationModel> = _navigateToDetailEvent
    val navigateToCreateEvent: LiveData<Any> = _navigateToCreateEvent
    val messageEvent: LiveData<String> = _messageEvent

    fun getTaskList() {
        getTaskListUseCase.execute(GetTaskListUseCase.Params())
            .onEach {
                _state.value = HomeState.TaskListSuccess(it.taskList.map { taskModel ->
                    TaskPresentationModel(
                        taskModel.id,
                        taskModel.content,
                        dateProvider.formatTimeToLocalDate(taskModel.date)
                    )
                })
            }
            .catch {
                it.stackTrace
                _state.value = HomeState.TaskListError("Error loading tasks")
            }
            .launchIn(viewModelScope)
    }

    fun clickItem(it: TaskPresentationModel) {
        _navigateToDetailEvent.value = it
    }

    fun createTask() {
        _navigateToCreateEvent.value = Any()
    }

    fun longClick(it: TaskPresentationModel) {
        deleteTask(it)
    }

    private fun deleteTask(it: TaskPresentationModel) {
        deleteTaskUseCase.execute(DeleteTaskUseCase.Params(it.id))
            .onEach {
                getTaskList()
                showMessage(it.message)
            }
            .launchIn(viewModelScope)
    }

    private fun showMessage(message: String) {
        _messageEvent.value = message
    }

}