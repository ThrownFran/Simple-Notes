package brillembourg.notes.simple.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import javax.inject.Inject

class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val updateTaskListUseCase: GetTaskListUseCase
) : ViewModel() {


    val task = DetailFragmentArgs.fromSavedStateHandle(savedStateHandle)

    init {

    }


}