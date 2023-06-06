package brillembourg.notes.simple.presentation.home.delete

import brillembourg.notes.simple.domain.use_cases.notes.ArchiveNotesUseCase
import brillembourg.notes.simple.domain.use_cases.notes.DeleteNotesUseCase
import brillembourg.notes.simple.domain.use_cases.notes.UnArchiveNotesUseCase
import brillembourg.notes.simple.presentation.base.MessageManager
import brillembourg.notes.simple.presentation.home.NoteList
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteDeletionManager constructor(
    private val archiveNotesUseCase: ArchiveNotesUseCase,
    private val unArchiveNotesUseCase: UnArchiveNotesUseCase,
    private val deleteNotesUseCase: DeleteNotesUseCase,
    private val messageManager: MessageManager,
    private val noteList: StateFlow<NoteList>,
    private val coroutineScope: CoroutineScope,
    private val onDismissSelectionMode: () -> Unit
) {

    private val _dialogs: MutableStateFlow<NoteDeletionState> =
        MutableStateFlow(NoteDeletionState.Idle)
    val dialogs = _dialogs.asStateFlow()

    private fun getSelectedTasks() = noteList.value.notes.filter { it.isSelected }

    private fun showErrorMessage(exception: Exception) {
        messageManager.showMessage(getMessageFromError(exception))
    }

    private fun showMessage(message: UiText) {
        messageManager.showMessage(message)
    }

    private fun deleteNotes(tasksToDeleteIds: List<Long>) {
        coroutineScope.launch {
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
            NoteDeletionState.ConfirmDeleteDialog(tasksToDeleteSize = getSelectedTasks().size)
        }
    }

    fun onDismissConfirmDeleteShown() {
        _dialogs.update { NoteDeletionState.Idle }
    }

    fun onDeleteNotes() {
        val tasksToDeleteIds = getSelectedTasks().map { it.id }
        deleteNotes(tasksToDeleteIds)

        _dialogs.update { NoteDeletionState.Idle }
        onDismissSelectionMode()
    }

    //endregion

    //region Archive
    fun onArchiveNotes() {

        val tasksToDeleteIds = getSelectedTasks().map { it.id }
        archiveNotes(tasksToDeleteIds)

        _dialogs.update { NoteDeletionState.Idle }

        onDismissSelectionMode()
    }

    private fun archiveNotes(tasksToDeleteIds: List<Long>) {
        coroutineScope.launch {
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
            NoteDeletionState.ConfirmArchiveDialog(
                tasksToArchiveSize = getSelectedTasks().size
            )
        }
    }

    fun onDismissConfirm() {
        _dialogs.update { NoteDeletionState.Idle }
    }

    //endregion

    //region Unarchive

    fun onUnarchiveTasks() {
        val tasksSelectedIds = getSelectedTasks().map { it.id }

        onDismissSelectionMode()
        unarchiveTasks(tasksSelectedIds)
    }

    private fun unarchiveTasks(taskToUnarchiveIds: List<Long>) {
        coroutineScope.launch {
            val params = UnArchiveNotesUseCase.Params(taskToUnarchiveIds)
            when (val result = unArchiveNotesUseCase(params)) {
                is Resource.Success -> showMessage(result.data.message)
                is Resource.Error -> showErrorMessage(result.exception)
                is Resource.Loading -> Unit
            }
        }
    }

    //endregion
}