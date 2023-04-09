package brillembourg.notes.simple.presentation.home.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.use_cases.notes.ArchiveNotesUseCase
import brillembourg.notes.simple.domain.use_cases.notes.DeleteNotesUseCase
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteNotesViewModel @Inject constructor(
    private val archiveNotesUseCase: ArchiveNotesUseCase,
    private val deleteNotesUseCase: DeleteNotesUseCase,
    private val messageManager: MessageManager,
    uiState: UiState
) : ViewModel() {

    private val _dialogs: MutableStateFlow<DeleteNoteState> = MutableStateFlow(DeleteNoteState.Idle)
    val dialogs = _dialogs.asStateFlow()
    private val _homeUiState = uiState.homeUiState

    private fun getSelectedTasks() = _homeUiState.value.noteList.notes.filter { it.isSelected }

    private fun showErrorMessage(exception: Exception) {
        messageManager.showMessage(getMessageFromError(exception))
    }

    private fun showMessage(message: UiText) {
        messageManager.showMessage(message)
    }

    private fun deleteNotes(tasksToDeleteIds: List<Long>) {
        viewModelScope.launch {
            val params = DeleteNotesUseCase.Params(tasksToDeleteIds)
            when (val result = deleteNotesUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    fun onDeleteConfirm() {
        _dialogs.update {
            DeleteNoteState.DeleteCategoriesConfirmation(tasksToDeleteSize = getSelectedTasks().size)
        }
    }

    fun onDismissConfirmDeleteShown() {
        _dialogs.update { DeleteNoteState.Idle }
    }

    fun onDeleteNotes() {
        val tasksToDeleteIds = getSelectedTasks().map { it.id }
        deleteNotes(tasksToDeleteIds)

        _dialogs.update { DeleteNoteState.Idle }
        _homeUiState.update { it.copy(selectionModeActive = null) }
    }

    //endregion

    //region Archive
    fun onArchiveNotes() {

        val tasksToDeleteIds = getSelectedTasks().map { it.id }
        archiveNotes(tasksToDeleteIds)

        _dialogs.update { DeleteNoteState.Idle }

        _homeUiState.update { it.copy(selectionModeActive = null) }

    }

    private fun archiveNotes(tasksToDeleteIds: List<Long>) {
        viewModelScope.launch {
            val params = ArchiveNotesUseCase.Params(tasksToDeleteIds)
            when (val result = archiveNotesUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    fun onArchiveConfirmNotes() {
        _dialogs.update {
            DeleteNoteState.ShowArchiveNotesConfirmationState(
                tasksToArchiveSize = getSelectedTasks().size
            )
        }
    }

    fun onDismissConfirmArchiveShown() {
        _dialogs.update { DeleteNoteState.Idle }
    }
}