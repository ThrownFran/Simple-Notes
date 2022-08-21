package brillembourg.notes.simple.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.use_cases.BackupAndRestoreNotesUseCase
import brillembourg.notes.simple.domain.use_cases.Screen
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

    init {
        observeUserMessages()
    }

    private fun observeUserMessages() {
        viewModelScope.launch {
            messageManager.message.collect { uiText ->
                _mainUiState.update {
                    it.copy(userMessage = uiText)
                }
            }
        }
    }

    fun prepareBackupNotes(screen: Screen) {
        viewModelScope.launch {
            val params = BackupAndRestoreNotesUseCase.PrepareBackupParams(screen)
            backupAndRestoreNotesUseCase.prepareBackup(params)
        }
    }

    fun onRestoreNotes() {
        viewModelScope.launch {
            when (val result = backupAndRestoreNotesUseCase.restore()) {
                is Resource.Success -> {
                    _mainUiState.value = _mainUiState.value.copy(
                        needsRestartApp = true,
                        userToastMessage = result.data.message
                    )
                }
                is Resource.Loading -> Unit
                is Resource.Error -> showToastErrorMessage(result.exception)
            }
        }
    }

    fun onBackupNotes() {
        viewModelScope.launch {
            when (val result = backupAndRestoreNotesUseCase.backup()) {
                is Resource.Success -> {
                    _mainUiState.value = _mainUiState.value.copy(
                        needsRestartApp = true,
                        userToastMessage = result.data.message
                    )
                }
                is Resource.Loading -> Unit
                is Resource.Error -> showToastErrorMessage(result.exception)
            }
        }
    }

    private fun showToastErrorMessage(exception: Exception) {
        _mainUiState.value =
            _mainUiState.value.copy(userToastMessage = getMessageFromError(exception))
    }

    fun onToastMessageShown() {
        _mainUiState.value = _mainUiState.value.copy(userToastMessage = null)
    }

    fun onUserMessageShown(uiText: UiText) {
        messageManager.onMessageShown(uiText)
    }


}