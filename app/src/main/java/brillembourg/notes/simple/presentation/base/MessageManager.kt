package brillembourg.notes.simple.presentation.trash

import brillembourg.notes.simple.util.UiText
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
    fun onMessageShown(message: UiText)
}

class MessageManagerImp : MessageManager {

    //    private val _messages: MutableStateFlow<List<UiText>> = MutableStateFlow(emptyList())
    private val _message: MutableStateFlow<UiText?> = MutableStateFlow(null)
    override val message = _message.asStateFlow()

    override fun showMessage(message: UiText) {
        _message.update { message }
//        _messages.update { currentMessages ->
//            currentMessages + message
//        }
    }

    override fun onMessageShown(message: UiText) {
        _message.update { null }
//        _messages.update { currentMessages ->
//            currentMessages - message
//        }
    }
}

data class Message(val id: Long, val message: UiText)