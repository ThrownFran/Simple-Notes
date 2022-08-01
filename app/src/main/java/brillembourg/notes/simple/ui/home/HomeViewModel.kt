package brillembourg.notes.simple.ui.home

import android.util.Log
import androidx.lifecycle.*
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.use_cases.DeleteTaskUseCase
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import brillembourg.notes.simple.domain.use_cases.SaveTaskListUseCase
import brillembourg.notes.simple.ui.extras.SingleLiveEvent
import brillembourg.notes.simple.ui.models.TaskPresentationModel
import brillembourg.notes.simple.ui.models.toDomain
import brillembourg.notes.simple.ui.models.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTaskListUseCase: GetTaskListUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val saveTaskListUseCase: SaveTaskListUseCase,
    private val dateProvider: DateProvider
) : ViewModel() {

    private val _state: MutableLiveData<HomeState> = MutableLiveData()
    private val _navigateToDetailEvent: SingleLiveEvent<TaskPresentationModel> = SingleLiveEvent()
    private val _navigateToCreateEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    private val _messageEvent: SingleLiveEvent<String> = SingleLiveEvent()

    //Observables
    val state: LiveData<HomeState> get() = _state
    val navigateToDetailEvent: LiveData<TaskPresentationModel> get() = _navigateToDetailEvent
    val navigateToCreateEvent: LiveData<Any> get() = _navigateToCreateEvent
    val messageEvent: LiveData<String> get() = _messageEvent

    fun observeTaskList(): LiveData<List<TaskPresentationModel>> = handleTaskListObservable()

    private fun handleTaskListObservable() = getTaskListUseCase
        .execute(GetTaskListUseCase.Params())
        .map {
            Log.e("HomeViewModel updated list", it.taskList.toString())
            it.taskList.map { it.toPresentation(dateProvider) }
                .sortedBy { taskPresentationModel -> taskPresentationModel.order }
            //                .asReversed()
        }
        .catch {
            it.stackTrace
            _state.value = HomeState.ShowError("Error loading tasks")
        }.asLiveData(viewModelScope.coroutineContext)

    fun reorderList(it: List<TaskPresentationModel>) {
        saveTaskListUseCase.execute(
            SaveTaskListUseCase.Params(
                it.map { taskPresentationModel -> taskPresentationModel.toDomain(dateProvider) }
            )
        )
            .launchIn(viewModelScope)
    }

    fun clickItem(it: TaskPresentationModel) {
        _navigateToDetailEvent.value = it
    }

    fun createTask() {
        _navigateToCreateEvent.value = Any()
    }

    fun clickDeleteTask(it: TaskPresentationModel) {
        deleteTask(it)
    }

    private fun deleteTask(it: TaskPresentationModel) {
        deleteTaskUseCase.execute(DeleteTaskUseCase.Params(it.id))
            .debounce(400)
            .onEach {
                showMessage(it.message)
            }
            .launchIn(viewModelScope)
    }

    private fun showMessage(message: String) {
        _messageEvent.value = message
    }


}