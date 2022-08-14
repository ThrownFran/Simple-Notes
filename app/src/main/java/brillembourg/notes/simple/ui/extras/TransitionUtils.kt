package brillembourg.notes.simple.ui.extras

import android.graphics.Color
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import brillembourg.notes.simple.R
import brillembourg.notes.simple.ui.detail.DetailFragment
import brillembourg.notes.simple.ui.home.HomeFragment
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialElevationScale

fun Fragment.prepareTransition(rootView: View) {
    postponeEnterTransition()
    rootView.doOnPreDraw { startPostponedEnterTransition() }
}

fun DetailFragment.setEditNoteEnterTransition() {
    sharedElementEnterTransition = MaterialContainerTransform().apply {
        // Scope the transition to a view in the hierarchy so we know it will be added under
        // the bottom app bar but over the elevation scale of the exiting HomeFragment.
        drawingViewId = R.id.fragment_container_view
        duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        scrimColor = Color.TRANSPARENT
        setAllContainerColors(requireContext().themeColor(com.google.android.material.R.attr.colorSurface))
    }
}

fun DetailFragment.setCreateNoteEnterTransition(myStartView: View, myEndView: View) {
    enterTransition = MaterialContainerTransform().apply {
        startView = myStartView
        endView = myEndView
        duration = resources.getInteger(R.integer.reply_motion_duration_small).toLong()
        scrimColor = Color.TRANSPARENT
        containerColor =
            requireContext().themeColor(com.google.android.material.R.attr.colorSurface)
        startContainerColor =
            requireContext().themeColor(com.google.android.material.R.attr.colorSecondary)
        endContainerColor =
            requireContext().themeColor(com.google.android.material.R.attr.colorSurface)
    }
}

fun HomeFragment.setTransitionToEditNote() {
    exitTransition = MaterialElevationScale(false).apply {
        duration = resources.getInteger(R.integer.reply_motion_duration_medium).toLong()
    }
    reenterTransition = MaterialElevationScale(true).apply {
        duration = resources.getInteger(R.integer.reply_motion_duration_medium).toLong()
    }
}

fun HomeFragment.setTransitionToCreateNote() {
    exitTransition = MaterialElevationScale(false).apply {
        duration = resources.getInteger(R.integer.reply_motion_duration_small).toLong()
    }
    reenterTransition = MaterialElevationScale(true).apply {
        duration = resources.getInteger(R.integer.reply_motion_duration_small).toLong()
    }
}

fun HomeFragment.setupExtrasToDetail(sharedView: View): FragmentNavigator.Extras {
    return FragmentNavigatorExtras(
        sharedView.findViewById<View>(R.id.task_roundcontraint) to sharedView.context.getString(R.string.home_shared_detail_container),
    )
}