package brillembourg.notes.simple.presentation.trash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.use_cases.DeleteTasksUseCase
import brillembourg.notes.simple.domain.use_cases.GetArchivedTasksUseCase
import brillembourg.notes.simple.domain.use_cases.UnArchiveTasksUseCase
import brillembourg.notes.simple.presentation.extras.SingleLiveEvent
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.presentation.models.toPresentation
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val getArchivedTasksUseCase: GetArchivedTasksUseCase,
    private val unArchiveTasksUseCase: UnArchiveTasksUseCase,
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val dateProvider: DateProvider
) : ViewModel() {

    private val _state: MutableLiveData<TrashState> = MutableLiveData()
    private val _navigateToDetailEvent: SingleLiveEvent<TaskPresentationModel> = SingleLiveEvent()
    private val _messageEvent: SingleLiveEvent<UiText> = SingleLiveEvent()

    //Observables
    val state: LiveData<TrashState> get() = _state
    val navigateToDetailEvent: LiveData<TaskPresentationModel> get() = _navigateToDetailEvent
    val messageEvent: LiveData<UiText> get() = _messageEvent

    private val _taskList: MutableStateFlow<List<TaskPresentationModel>> =
        MutableStateFlow(ArrayList())
    val taskList = _taskList.asStateFlow()

    init {
        getArchivedTasksAndObserve()
    }

    private fun getArchivedTasksAndObserve() {
        viewModelScope.launch {
            getArchivedTasksUseCase(GetArchivedTasksUseCase.Params())
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _taskList.value =
                                result.data.taskList.map { it.toPresentation(dateProvider) }
                                    .sortedBy { taskPresentationModel -> taskPresentationModel.order }
                                    .asReversed()
                        }
                        is Resource.Error -> _state.value =
                            TrashState.ShowError(UiText.DynamicString("Error loading tasks"))
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    fun clickItem(it: TaskPresentationModel) {
        _navigateToDetailEvent.value = it
    }

    private fun showMessage(message: UiText) {
        _messageEvent.value = message
    }

    fun unarchiveTasks(taskToUnarchive: List<TaskPresentationModel>) {
        viewModelScope.launch {
            val params = UnArchiveTasksUseCase.Params(taskToUnarchive.map { it.id })
            when (val result = unArchiveTasksUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    private fun showErrorMessage(exception: Exception) {
        _messageEvent.value = getMessageFromError(exception)
    }

    fun clickDeleteTasks(tasksToDelete: List<TaskPresentationModel>) {
        viewModelScope.launch {
            val params = DeleteTasksUseCase.Params(tasksToDelete.map { it.id })
            when (val result = deleteTasksUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }


}