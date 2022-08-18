package brillembourg.notes.simple.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.DateProvider
import brillembourg.notes.simple.domain.use_cases.ArchiveTasksUseCase
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import brillembourg.notes.simple.domain.use_cases.ReorderTaskListUseCase
import brillembourg.notes.simple.presentation.models.TaskPresentationModel
import brillembourg.notes.simple.presentation.models.toDomain
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
class HomeViewModel @Inject constructor(
    private val getTaskListUseCase: GetTaskListUseCase,
    private val archiveTasksUseCase: ArchiveTasksUseCase,
    private val reorderTaskListUseCase: ReorderTaskListUseCase,
    private val dateProvider: DateProvider
) : ViewModel() {

    private val _taskListState: MutableStateFlow<List<TaskPresentationModel>> =
        MutableStateFlow(ArrayList())
    var taskListState = _taskListState.asStateFlow()

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    init {
        observeTaskList()
    }

    private fun observeTaskList() {
        viewModelScope.launch {
            getTaskListUseCase(GetTaskListUseCase.Params())
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _taskListState.value = result.data.taskList
                                .map { it.toPresentation(dateProvider) }
                                .onEach { //TODO navigation
                                }
                                .sortedBy { taskPresentationModel -> taskPresentationModel.order }
                                .asReversed()
                        }
                        is Resource.Error -> {
                            showMessage(UiText.DynamicString("Error loading tasks"))
                        }
                        is Resource.Loading -> Unit
                    }

                }
        }
    }

    fun onReorderedTaskList(reorderedTaskList: List<TaskPresentationModel>) {
        if (reorderedTaskList == taskListState.value) return

        viewModelScope.launch {
            val params = ReorderTaskListUseCase.Params(reorderedTaskList.map {
                it.toDomain(dateProvider)
            })

            when (val result = reorderTaskListUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    private fun showErrorMessage(exception: Exception) {
        _homeUiState.value = homeUiState.value.copy(userMessage = getMessageFromError(exception))
    }

    fun onTaskClick(it: TaskPresentationModel) {
        _homeUiState.value = _homeUiState.value.copy(
            navigateToDetail = NavigateToTaskDetailEvent(
                mustConsume = true,
                taskIndex = taskListState.value.indexOf(it),
                taskPresentationModel = it
            ),
            selectionMode = SelectionMode()
        )
    }

    private fun showMessage(message: UiText) {
        _homeUiState.value = homeUiState.value.copy(userMessage = message)
    }

    fun deleteTasks(tasksToDelete: List<TaskPresentationModel>) {
        viewModelScope.launch {
            val params = ArchiveTasksUseCase.Params(tasksToDelete.map { it.id })

            when (val result = archiveTasksUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }

        }
    }

    fun onMessageShown() {
        _homeUiState.value = _homeUiState.value.copy(userMessage = null)
    }

    fun onNavigateToDetailCompleted() {

        val navState =
            _homeUiState.value.navigateToDetail.copy(mustConsume = false)

        _homeUiState.value = _homeUiState.value.copy(
            navigateToDetail = navState,
        )
    }

    fun onSelection() {
        val sizeSelected = _taskListState.value.filter { it.isSelected }.size
        _homeUiState.value = _homeUiState.value.copy(
            selectionMode = SelectionMode(
                isActive = true, size = sizeSelected
            )
        )
    }

    fun onSelectionDismissed() {
        _homeUiState.value = _homeUiState.value.copy(
            selectionMode = SelectionMode()
        )

    }


}