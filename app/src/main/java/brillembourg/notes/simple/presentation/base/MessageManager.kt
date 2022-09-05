package brillembourg.notes.simple.presentation.base

import brillembourg.notes.simple.util.UiText
import brillembourg.notes.simple.util.getMessageFromError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Class responsible for managing Snackbar messages to show on the screen
 */
interface MessageManager {
    val message: StateFlow<UiText?>
    fun showMessage(message: UiText)
    fun showError(exception: Exception)
    fun onMessageShown(message: UiText)
}

class MessageManagerImp : MessageManager {

    private val _message: MutableStateFlow<UiText?> = MutableStateFlow(null)
    override val message = _message.asStateFlow()

    override fun showMessage(message: UiText) {
        _message.update { message }
    }

    override fun onMessageShown(message: UiText) {
        _message.update { null }
    }

    override fun showError(exception: Exception) {
        showMessage(getMessageFromError(exception))
    }
}

data class Message(val id: Long, val message: UiText)






















