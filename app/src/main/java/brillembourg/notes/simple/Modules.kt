package brillembourg.notes.simple

import brillembourg.notes.simple.data.TaskRepositoryImp
import brillembourg.notes.simple.domain.repositories.TaskRepository
import brillembourg.notes.simple.domain.use_cases.GetTaskListUseCase
import brillembourg.notes.simple.ui.detail.DetailViewModel
import brillembourg.notes.simple.ui.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.get
import org.koin.dsl.module


val appModule = module {
//    single { TaskRepositoryImp() }
//    single { GetTaskListUseCase(get()) }

//    viewModel { HomeViewModel() }
//    viewModel { DetailViewModel() }
}