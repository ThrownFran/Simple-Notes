package brillembourg.notes.simple.ui.trash

import androidx.lifecycle.*
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.use_cases.DeleteTasksUseCase
import brillembourg.notes.simple.domain.use_cases.GetArchivedTasksUseCase
import brillembourg.notes.simple.ui.extras.SingleLiveEvent
import brillembourg.notes.simple.ui.models.TaskPresentationModel
import brillembourg.notes.simple.ui.models.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val getArchivedTasksUseCase: GetArchivedTasksUseCase,
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val dateProvider: DateProvider
) : ViewModel() {

    private val _state: MutableLiveData<TrashState> = MutableLiveData()
    private val _navigateToDetailEvent: SingleLiveEvent<TaskPresentationModel> = SingleLiveEvent()
    private val _messageEvent: SingleLiveEvent<String> = SingleLiveEvent()

    //Observables
    val state: LiveData<TrashState> get() = _state
    val navigateToDetailEvent: LiveData<TaskPresentationModel> get() = _navigateToDetailEvent
    val messageEvent: LiveData<String> get() = _messageEvent

    fun observeTaskList(): LiveData<List<TaskPresentationModel>> = handleTaskListObservable()

    fun totalNotes() = 14

    private fun handleTaskListObservable() = getArchivedTasksUseCase
        .execute(GetArchivedTasksUseCase.Params())
        .map {
            it.taskList.map { it.toPresentation(dateProvider) }
                .sortedBy { taskPresentationModel -> taskPresentationModel.order }
                .asReversed()
        }
        .catch {
            it.stackTrace
            _state.value = TrashState.ShowError("Error loading tasks")
        }.asLiveData(viewModelScope.coroutineContext)


    fun clickItem(it: TaskPresentationModel) {
        _navigateToDetailEvent.value = it
    }

    private fun showMessage(message: String) {
        _messageEvent.value = message
    }

    fun clickDeleteTasks(tasksToDelete: List<TaskPresentationModel>) {
        deleteTasksUseCase.execute(DeleteTasksUseCase.Params(tasksToDelete.map { it.id }))
            .debounce(400)
            .onEach {
                showMessage(it.message)
            }
            .launchIn(viewModelScope)
//        tasksToDelete.forEach { clickDeleteTask(it) }
    }


}