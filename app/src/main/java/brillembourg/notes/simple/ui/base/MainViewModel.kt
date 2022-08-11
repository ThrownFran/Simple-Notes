package brillembourg.notes.simple.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.domain.Screen
import brillembourg.notes.simple.domain.use_cases.BackupNotesUseCase
import brillembourg.notes.simple.ui.extras.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        backupNotesUseCase.prepareBackup(BackupNotesUseCase.PrepareBackupParams(screen))
            .launchIn(viewModelScope)
    }

    fun restoreNotes() {
        backupNotesUseCase.restore()
            .onEach {
                showMessage(it.message)
                _restoreSuccessEvent.value = it.message
            }
            .launchIn(viewModelScope)
    }

    fun backupNotes() {
        backupNotesUseCase.backup()
            .onEach {
                showMessage(it.message)
                _backupSuccessEvent.value = it.message
            }
            .launchIn(viewModelScope)
    }

    private fun showMessage(message: String) {
        _messageEvent.value = message
    }


}