package brillembourg.notes.simple.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brillembourg.notes.simple.data.TaskRepositoryImp
import brillembourg.notes.simple.domain.models.Task
import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import brillembourg.notes.simple.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val getTaskListUseCase: GetTaskListUseCase) : ViewModel() {

    var state: MutableLiveData<HomeState> = MutableLiveData()
    val navigateToDetail: SingleLiveEvent<Task> = SingleLiveEvent()

    init {
        getTaskList()
    }

    fun getTaskList () {
        getTaskListUseCase.execute(GetTaskListUseCase.Params())
            .onEach { state.value = HomeState.TaskListSuccess(it.taskList) }
            .launchIn(viewModelScope)
    }

    fun clickItem(it: Task) {
        navigateToDetail.value = it
    }

}