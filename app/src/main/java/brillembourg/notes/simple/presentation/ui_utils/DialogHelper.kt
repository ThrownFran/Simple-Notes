package brillembourg.notes.simple.presentation.ui_utils

import androidx.fragment.app.Fragment
import brillembourg.notes.simple.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showArchiveConfirmationDialog(
    fragment: Fragment,
    size: Int,
    onPositive: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    val title =
        if (size > 1) fragment.getString(R.string.move_tasks_archive) else fragment.getString(R.string.move_task_archive)

    MaterialAlertDialogBuilder(
        fragment.requireContext()
    )
        .setTitle(title)
        .setIcon(R.drawable.ic_outline_archive_on_surface_24)
        //            .setMessage(resources.getString(R.string.supporting_text))
        .setNegativeButton(fragment.resources.getString(R.string.all_cancel)) { dialog, which ->
        }
        .setPositiveButton(fragment.resources.getString(R.string.archive)) { dialog, which ->
            onPositive.invoke()
        }
        .setOnDismissListener {
            onDismiss?.invoke()
        }
        .showWithLifecycle(fragment.viewLifecycleOwner)
}

fun showDeleteTasksDialog(
    fragment: Fragment,
    size: Int,
    onPositive: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {

    val title =
        if (size <= 1) fragment.getString(R.string.delete_task_permanently) else fragment.getString(
            R.string.delete_tasks_permanently
        )

    MaterialAlertDialogBuilder(
        fragment.requireContext()
    )
        .setTitle(title)
        .setIcon(R.drawable.ic_baseline_delete_on_surface_24)
        .setNegativeButton(fragment.resources.getString(R.string.all_cancel)) { dialog, which ->
        }
        .setPositiveButton(fragment.resources.getString(R.string.all_delete)) { dialog, which ->
            onPositive()
        }.setOnDismissListener {
            onDismiss?.invoke()
        }
        .showWithLifecycle(fragment.viewLifecycleOwner)
}

fun showDeleteCategoriesDialog(
    fragment: Fragment,
    size: Int,
    onPositive: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {

    val title =
        if (size <= 1) fragment.getString(R.string.delete_category) else fragment.getString(
            R.string.delete_categories
        )

    MaterialAlertDialogBuilder(
        fragment.requireContext()
    )
        .setTitle(title)
        .setIcon(R.drawable.ic_baseline_delete_on_surface_24)
//            .setMessage(resources.getString(R.string.supporting_text))
        .setNegativeButton(fragment.resources.getString(R.string.all_cancel)) { dialog, which ->
        }
        .setPositiveButton(fragment.resources.getString(R.string.all_delete)) { dialog, which ->
            onPositive()
        }.setOnDismissListener {
            onDismiss?.invoke()
        }
        .showWithLifecycle(fragment.viewLifecycleOwner)
}

