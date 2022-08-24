package brillembourg.notes.simple.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.use_cases.BackupAndRestoreNotesUseCase
import brillembourg.notes.simple.domain.use_cases.BackupModel
import brillembourg.notes.simple.presentation.trash.MessageManager
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
class MainViewModel @Inject constructor(
    private val backupAndRestoreNotesUseCase: BackupAndRestoreNotesUseCase,
    private val messageManager: MessageManager
) : ViewModel() {

    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState = _mainUiState.asStateFlow()

    private val _incomingContentFromExternalApp: MutableStateFlow<String?> = MutableStateFlow(null)
    val incomingContentFromExternalApp = _incomingContentFromExternalApp.asStateFlow()

    init {
        observeAndNotifyUserMessages()
    }

    fun onIncomingContentFromExternalApp(content: String) {
        _incomingContentFromExternalApp.update { content }
    }

    private fun observeAndNotifyUserMessages() {
        viewModelScope.launch {
            messageManager.message.collect { uiText ->
                _mainUiState.update {
                    it.copy(userMessage = uiText)
                }
            }
        }
    }

    fun onRestoreNotes(backupModel: BackupModel) {
        viewModelScope.launch {
            when (val result =
                backupAndRestoreNotesUseCase.restore(BackupAndRestoreNotesUseCase.Params(backupModel))) {
                is Resource.Success -> {
                    _mainUiState.update {
                        it.copy(
                            needsRestartApp = true,
                            userToastMessage = result.data.message
                        )
                    }
                }
                is Resource.Loading -> Unit
                is Resource.Error -> showToastErrorMessage(result.exception)
            }
        }
    }

    fun onBackupNotes(backupModel: BackupModel) {
        viewModelScope.launch {
            when (val result =
                backupAndRestoreNotesUseCase.backup(BackupAndRestoreNotesUseCase.Params(backupModel))) {
                is Resource.Success -> {
                    _mainUiState.update {
                        it.copy(
                            needsRestartApp = true,
                            userToastMessage = result.data.message
                        )
                    }
                }
                is Resource.Loading -> Unit
                is Resource.Error -> showToastErrorMessage(result.exception)
            }
        }
    }

    private fun showToastErrorMessage(exception: Exception) {
        _mainUiState.update { it.copy(userToastMessage = getMessageFromError(exception)) }
    }

    fun onToastMessageShown() {
        _mainUiState.update { it.copy(userToastMessage = null) }
    }

    fun onUserMessageShown(uiText: UiText) {
        messageManager.onMessageShown(uiText)
    }

    fun onIncommingContentProcessed() {
        _incomingContentFromExternalApp.update { null }
    }


}