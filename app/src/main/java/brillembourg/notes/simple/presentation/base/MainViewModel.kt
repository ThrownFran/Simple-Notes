package brillembourg.notes.simple.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.Screen
import brillembourg.notes.simple.domain.use_cases.BackupNotesUseCase
import brillembourg.notes.simple.presentation.extras.SingleLiveEvent
import brillembourg.notes.simple.util.Resource
import brillembourg.notes.simple.util.getMessageFromError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val backupNotesUseCase: BackupNotesUseCase
) : ViewModel() {

    private val _messageEvent: SingleLiveEvent<String> = SingleLiveEvent()

    private val _restoreSuccessEvent: SingleLiveEvent<String> = SingleLiveEvent()
    val restoreSuccessEvent: LiveData<String> get() = _restoreSuccessEvent

    private val _backupSuccessEvent: SingleLiveEvent<String> = SingleLiveEvent()
    val backupSuccessEvent: LiveData<String> get() = _backupSuccessEvent

    private val _createTaskEvent: SingleLiveEvent<Any> = SingleLiveEvent()
    val createTaskEvent: LiveData<Any> get() = _createTaskEvent

    //Observables
    val messageEvent: LiveData<String> get() = _messageEvent

    fun createTask() {
        _createTaskEvent.call()
    }

    fun prepareBackupNotes(screen: Screen) {
        viewModelScope.launch {
            val params = BackupNotesUseCase.PrepareBackupParams(screen)
            backupNotesUseCase.prepareBackup(params)
        }
    }

    fun restoreNotes() {
        viewModelScope.launch {
            when (val result = backupNotesUseCase.restore()) {
                is Resource.Success -> {
                    showMessage(result.data.message)
                    _restoreSuccessEvent.value = result.data.message
                }
                is Resource.Loading -> Unit
                is Resource.Error -> showErrorMessage(result.exception)
            }
        }
    }

    private fun showErrorMessage(exception: Exception) {
        _messageEvent.value = getMessageFromError(exception)
    }

    fun backupNotes() {
        viewModelScope.launch {
            when (val result = backupNotesUseCase.backup()) {
                is Resource.Success -> {
                    showMessage(result.data.message)
                    _backupSuccessEvent.value = result.data.message
                }
                is Resource.Loading -> Unit
                is Resource.Error -> showErrorMessage(result.exception)
            }
        }
    }

    private fun showMessage(message: String) {
        _messageEvent.value = message
    }


}