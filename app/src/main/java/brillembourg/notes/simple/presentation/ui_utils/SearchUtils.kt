package brillembourg.notes.simple.presentation.ui_utils

import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import brillembourg.notes.simple.R

class SearchManager(
    val fragment: Fragment,
    val toolbar: Toolbar,
    val onSearch: (String) -> Unit,
    val onDestroyActionMode: () -> Unit
) {

    var actionMode: ActionMode? = null

    fun onCheckState(key: String) {
        if (actionMode == null && key.isNotEmpty()) {
            startSearch(key)
        }
    }

    fun startSearch(key: String = "") {

        actionMode = toolbar.startActionMode(object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {

                val customView = LayoutInflater.from(fragment.requireContext())
                    .inflate(R.layout.layout_search, null)
                mode.customView = customView
                val searchView = customView.findViewById<SearchView>(R.id.searchView)

                val searchText =
                    searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                searchText.setTextColor(
                    ContextCompat.getColor(
                        searchView.context, R.color.md_theme_light_onPrimary
                    )
                )
                searchText.setText(key)
                searchText.setHintTextColor(
                    ContextCompat.getColor(
                        searchView.context, R.color.md_theme_light_secondaryContainer
                    )
                )
                searchView.isIconified = false
                searchView.requestFocus()
                searchView.queryHint = searchView.context.resources.getString(R.string.search_note)
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        // perform search
                        return true
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        onSearch(newText)
                        return true
                    }
                })
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                // customize action mode menu
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                actionMode = null
                onDestroyActionMode()
            }
        })
    }

}