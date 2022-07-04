package brillembourg.notes.simple.ui.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import org.koin.androidx.viewmodel.ext.android.viewModel

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()
//    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setupObservers()
        viewModel.getTaskList()
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is HomeState.Loading -> {

                }
                is HomeState.TaskListError -> {}
                is HomeState.TaskListSuccess -> {
                    setupTaskList(it)
                }
            }
        }
    }

    private fun setupTaskList(it: HomeState.TaskListSuccess) {
        binding.homeRecycler.apply {
            adapter = TaskAdapter(it.taskList) {
                viewModel.clickItem(it)
            }
            layoutManager = LinearLayoutManager(context)
        }
    }

}