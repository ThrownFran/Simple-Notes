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
import kotlinx.coroutines.flow.StateFlow
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
    var taskListState: StateFlow<List<TaskPresentationModel>> = _taskListState.asStateFlow()

    private val _homeUiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

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

    fun reorderList(reorderedTaskList: List<TaskPresentationModel>) {
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

    fun clickItem(it: TaskPresentationModel) {
        _homeUiState.value = _homeUiState.value.copy(
            navigateToDetail = NavigateToDetailEvent(
                mustConsume = true,
                taskIndex = taskListState.value.indexOf(it),
                taskPresentationModel = it
            )
        )
//        _navigateToDetailEvent.value = it
    }

    private fun showMessage(message: UiText) {
        _homeUiState.value = homeUiState.value.copy(userMessage = message)
    }

    fun clickDeleteTasks(tasksToDelete: List<TaskPresentationModel>) {
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
        _homeUiState.value.navigateToDetail =
            _homeUiState.value.navigateToDetail.copy(
                mustConsume = false,
                isCurrentlyInDetailScreen = true
            )
    }

    fun onPopFromDetailScreen() {
        _homeUiState.value.navigateToDetail = _homeUiState.value.navigateToDetail.copy(
            isCurrentlyInDetailScreen = false
        )
    }


}