package brillembourg.notes.simple.presentation.license

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import brillembourg.notes.simple.R
import com.mikepenz.aboutlibraries.LibsBuilder

class LicensesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_source_licenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLicensesView()
    }

    private fun setupLicensesView() {
        val fragTransaction: FragmentTransaction = childFragmentManager.beginTransaction()

        val fragment = LibsBuilder()
////            .withFields(R.string::class.java.fields) // in some cases it may be needed to provide the R class, if it can not be automatically resolved
////            .withLibraryModification("aboutlibraries", Libs.LibraryFields.LIBRARY_NAME, "_AboutLibraries") // optionally apply modifications for library information
            .supportFragment()

        fragTransaction.add(R.id.source_licenses_contraint_container, fragment, "LicensesFragment")
        fragTransaction.commit()
    }
}