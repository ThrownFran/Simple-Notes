package brillembourg.notes.simple.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentMainBinding
import brillembourg.notes.simple.ui.base.MainViewModel
import brillembourg.notes.simple.ui.extras.showToast
import brillembourg.notes.simple.ui.models.TaskPresentationModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment(), MenuProvider {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private val viewModel: HomeViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentMainBinding? = null
    private lateinit var binding: FragmentMainBinding
    private var recylerViewState: Parcelable? = null
    private var actionMode: ActionMode? = null

    private var isStaggered = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null) _binding = FragmentMainBinding.inflate(inflater, container, false)
        binding = _binding as FragmentMainBinding
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupObservers()
        unlockToolbar()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_home, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        menu.findItem(R.id.menu_home_vertical).apply { isVisible = isStaggered }
        menu.findItem(R.id.menu_home_staggered).apply { isVisible = !isStaggered }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val menuHost = requireActivity()
        when (menuItem.itemId) {
            R.id.menu_home_vertical -> {
                clickVerticalLayout()
                menuHost.invalidateMenu()
                return true
            }
            R.id.menu_home_staggered -> {
                clickStaggeredLayout()
                menuHost.invalidateMenu()
                return true
            }
        }
        return false
    }

    private fun clickChangeLayout(isStaggered: Boolean) {
        this.isStaggered = isStaggered
        binding.homeRecycler.apply {
            layoutManager = when {
                isStaggered -> buildStaggeredManager()
                else -> buildLinearManager()
            }

            val taskAdapter = adapter as TaskAdapter
            taskAdapter.itemTouchHelper =
                taskAdapter.setupDragAndDropTouchHelper(getDragDirs(isStaggered)).also {
                    it.attachToRecyclerView(this)
                }
            taskAdapter.notifyDataSetChanged()
        }
    }

    private fun clickStaggeredLayout() {
        clickChangeLayout(true)
    }

    private fun clickVerticalLayout() {
        clickChangeLayout(false)
    }

    private fun buildLinearManager() = LinearLayoutManager(context)


    override fun onDestroyView() {
        saveRecyclerState()
        super.onDestroyView()
    }

    private fun setupObservers() {
        viewModel.navigateToDetailEvent.observe(viewLifecycleOwner) {
            navigateToDetail(it)
        }

        viewModel.navigateToCreateEvent.observe(viewLifecycleOwner) {
            navigateToCreateTask()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is HomeState.Loading -> {

                }
                is HomeState.ShowError -> {
                    showMessage(it.message)
                }
            }
        }

        viewModel.messageEvent.observe(viewLifecycleOwner) {
            showMessage(it)
        }

        viewModel.observeTaskList().observe(viewLifecycleOwner) {
            setupTaskList(it)
        }

        activityViewModel.restoreSuccessEvent.observe(viewLifecycleOwner) {
            restartApp()
        }

        activityViewModel.backupSuccessEvent.observe(viewLifecycleOwner) {
            restartApp()
        }

    }

    private fun restartApp() {
        val packageManager = context!!.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context!!.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context!!.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)

//        val intent = Intent(activity, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        context?.startActivity(intent)
//        if (context is Activity) {
//            (context as Activity).finish()
//        }
//        Runtime.getRuntime().exit(0)
    }

    private fun showMessage(message: String) {
        context?.showToast(message)
    }

    private fun navigateToCreateTask() {
        lockToolbar()
        finishActionIfActive()

        val directions = HomeFragmentDirections.actionHomeFragmentToDetailFragment()
        findNavController().navigate(directions)
    }

    private fun unlockToolbar() {
        val toolbar: Toolbar? =
            activity?.findViewById(R.id.toolbar) // or however you need to do it for your code
        val params: AppBarLayout.LayoutParams = toolbar?.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
    }

    private fun lockToolbar() {
        val toolbar: Toolbar? =
            activity?.findViewById(R.id.toolbar) // or however you need to do it for your code
        val params: AppBarLayout.LayoutParams = toolbar?.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
    }

    private fun finishActionIfActive() {
        actionMode?.finish()
        actionMode = null
    }

    private fun navigateToDetail(it: TaskPresentationModel) {
        lockToolbar()
        finishActionIfActive()

        //navigate to detail fragment
        val directions = HomeFragmentDirections.actionHomeFragmentToDetailFragment()
        directions.task = it
        findNavController().navigate(directions)
    }

    private fun setupTaskList(taskList: List<TaskPresentationModel>) {
        if (binding.homeRecycler.adapter == null) {
            binding.homeRecycler.apply {
                adapter = buildTaskAdapter(this, taskList, getDragDirs(isStaggered))
                layoutManager = buildLayoutManager(isStaggered)
            }
        } else {
            (binding.homeRecycler.adapter as TaskAdapter).apply {
                val isInsertingInList = currentList.size < taskList.size

                submitList(taskList) {
                    if (isInsertingInList) binding.homeRecycler.scrollToPosition(0)
                }

                notifyDataSetChanged()
            }
        }

    }

    private fun buildLayoutManager(isStaggered: Boolean): RecyclerView.LayoutManager {
        return if (isStaggered) buildStaggeredManager() else buildLinearManager()
            .also { layoutManager ->
                retrieveRecyclerStateIfApplies(layoutManager)
            }
    }

    private fun buildTaskAdapter(
        recyclerView: RecyclerView,
        taskList: List<TaskPresentationModel>,
        dragDirs: Int
    ): TaskAdapter {

        return TaskAdapter(
            dragDirs,
            recyclerView,
            onSelection = {
                recyclerView.setupContextualActionBar()
            },
            onClick = {
                clickItem(it)
            },
            onReorderSuccess = { tasks ->
                clickReorder(tasks)
            },
            onReorderCanceled = {
                clickReorderCancelled()
            })
            .also {
                it.submitList(taskList)
                it.itemTouchHelper.attachToRecyclerView(recyclerView)
            }
    }

    private fun clickReorder(tasks: List<TaskPresentationModel>) {
        actionMode?.finish()
        viewModel.reorderList(tasks)
    }

    private fun clickReorderCancelled() {
        actionMode?.finish()
    }

    private fun clickItem(it: TaskPresentationModel) {
        viewModel.clickItem(it)
    }

    private fun getDragDirs(isStaggered: Boolean) = if (isStaggered) {
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END
    } else {
        ItemTouchHelper.UP or ItemTouchHelper.DOWN
    }

    private fun RecyclerView.setupContextualActionBar() {
        val adapter = adapter as TaskAdapter
        val taskList = adapter.currentList
        val selectedList = taskList.filter { it.isSelected }

        if (selectedList.isEmpty()) {
            actionMode?.finish().also { actionMode = null }
            return
        }

        if (actionMode != null) {
            setActionModeTitle(selectedList)
            return
        }


        actionMode = requireActivity().findViewById<Toolbar>(R.id.toolbar)
            ?.startActionMode(object : ActionMode.Callback {
                // Called when the action mode is created; startActionMode() was called
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    // Inflate a menu resource providing context menu items
                    val inflater: MenuInflater = mode.menuInflater
                    inflater.inflate(R.menu.menu_context, menu)
                    return true
                }

                // Called each time the action mode is shown. Always called after onCreateActionMode, but
                // may be called multiple times if the mode is invalidated.
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.menu_context_menu_delete -> {
                        clickDeleteTasks(adapter.currentList.filter { it.isSelected })
                        true
                    }
                    else -> false
                }
            }

            // Called when the user exits the action mode
            override fun onDestroyActionMode(mode: ActionMode) {

                taskList.forEachIndexed { index, taskPresentationModel ->
                    if (taskPresentationModel.isSelected) {
                        taskPresentationModel.isSelected = false
                        try {
                            (findViewHolderForAdapterPosition(index) as TaskAdapter.ViewHolder).setBackgroundTransparent()
                        } catch (e: Exception) {
                            adapter.notifyItemChanged(index)
                        }
                    }
                }

                actionMode = null
            }
        })

        setActionModeTitle(selectedList)
    }

    private fun clickDeleteTasks(taskList: List<TaskPresentationModel>) {
        if (taskList.isEmpty()) throw IllegalArgumentException("Nothing to delete but trash was pressed")

        val title =
            if (taskList.size > 1) getString(R.string.move_tasks_to_trash) else getString(R.string.move_task_to_trash)

        MaterialAlertDialogBuilder(
            requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle(title)
            .setIcon(R.drawable.ic_baseline_delete_24)
//            .setMessage(resources.getString(R.string.supporting_text))
            .setNegativeButton(resources.getString(R.string.all_cancel)) { dialog, which ->
            }
            .setPositiveButton(resources.getString(R.string.all_move_to_trash)) { dialog, which ->
                viewModel.clickDeleteTasks(taskList)
                actionMode?.finish()
            }
            .show()

    }

    private fun setActionModeTitle(selectedList: List<TaskPresentationModel>) {
        val noteString =
            if (selectedList.size > 1) getString(R.string.notes) else getString(R.string.note)
        actionMode?.title = "${selectedList.size} ${noteString.lowercase()} selected"
    }

    private fun buildStaggeredManager() =
        StaggeredGridLayoutManager(2, RecyclerView.VERTICAL).also {
            it.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        }

    private fun retrieveRecyclerStateIfApplies(layoutManager: RecyclerView.LayoutManager) {
        recylerViewState?.let { layoutManager.onRestoreInstanceState(it) }
    }

    private fun saveRecyclerState() {
        recylerViewState = binding.homeRecycler.layoutManager?.onSaveInstanceState()
    }

}