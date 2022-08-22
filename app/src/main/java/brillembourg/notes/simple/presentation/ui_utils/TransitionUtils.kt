package brillembourg.notes.simple.presentation.custom_views

import android.graphics.Color
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import brillembourg.notes.simple.R
import brillembourg.notes.simple.presentation.detail.DetailFragment
import brillembourg.notes.simple.presentation.home.HomeFragment
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialElevationScale

fun Fragment.prepareTransition(rootView: View) {
    postponeEnterTransition()
    rootView.doOnPreDraw { startPostponedEnterTransition() }
}

fun DetailFragment.setEditNoteEnteringTransition() {
    sharedElementEnterTransition = MaterialContainerTransform().apply {
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

fun Fragment.setTransitionToEditNote() {
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

