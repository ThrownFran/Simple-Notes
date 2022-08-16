package brillembourg.notes.simple.ui.home

import androidx.lifecycle.*
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.use_cases.ArchiveTasksUseCase
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import brillembourg.notes.simple.domain.use_cases.ReorderTaskListUseCase
import brillembourg.notes.simple.ui.extras.SingleLiveEvent
import brillembourg.notes.simple.ui.models.TaskPresentationModel
import brillembourg.notes.simple.ui.models.toDomain
import brillembourg.notes.simple.ui.models.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTaskListUseCase: GetTaskListUseCase,
    private val archiveTasksUseCase: ArchiveTasksUseCase,
    private val reorderTaskListUseCase: ReorderTaskListUseCase,
    private val dateProvider: DateProvider
) : ViewModel() {

    private val _state: MutableLiveData<HomeState> = MutableLiveData()
    private val _navigateToDetailEvent: SingleLiveEvent<TaskPresentationModel> = SingleLiveEvent()
    private val _messageEvent: SingleLiveEvent<String> = SingleLiveEvent()

    //Observables
    val state: LiveData<HomeState> get() = _state
    val navigateToDetailEvent: LiveData<TaskPresentationModel> get() = _navigateToDetailEvent
    val messageEvent: LiveData<String> get() = _messageEvent

    fun observeTaskList(): LiveData<List<TaskPresentationModel>> = handleTaskListObservable()

    private fun handleTaskListObservable() = getTaskListUseCase
        .execute(GetTaskListUseCase.Params())
        .map {
            it.taskList.map { it.toPresentation(dateProvider) }
                .sortedBy { taskPresentationModel -> taskPresentationModel.order }
                .asReversed()
        }
        .catch {
            it.stackTrace
            _state.value = HomeState.ShowError("Error loading tasks")
        }.asLiveData(viewModelScope.coroutineContext)

    fun reorderList(it: List<TaskPresentationModel>) {
        reorderTaskListUseCase.execute(
            ReorderTaskListUseCase.Params(
                it.map { taskPresentationModel -> taskPresentationModel.toDomain(dateProvider) }
            )
        ).onEach {
            showMessage(it.message)
        }
            .launchIn(viewModelScope)
    }

    fun clickItem(it: TaskPresentationModel) {
        _navigateToDetailEvent.value = it
    }

    private fun showMessage(message: String) {
        _messageEvent.value = message
    }

    fun clickDeleteTasks(tasksToDelete: List<TaskPresentationModel>) {
        viewModelScope.launch {
            try {
                val result = archiveTasksUseCase.execute(
                    ArchiveTasksUseCase.Params(tasksToDelete.map { it.id })
                )
                showMessage(result.message)
            } catch (e: Exception) {
                _messageEvent.value = "Error deleting tasks"
            }
        }
    }


}