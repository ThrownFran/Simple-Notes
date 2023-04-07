package brillembourg.notes.simple.presentation.about

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import brillembourg.notes.simple.R
import brillembourg.notes.simple.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLinks()
        setupVersion()
    }

    private fun setupVersion() {
        try {
            val pInfo = requireActivity().packageManager
                .getPackageInfo(requireActivity().packageName, 0)
            val version = pInfo.versionName
            binding.aboutTextVersion.text = requireActivity().getString(R.string.about_version)
            binding.aboutTextVersion.append(" ")
            binding.aboutTextVersion.append(version)
            binding.aboutTextVersion.visibility = View.VISIBLE
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            binding.aboutTextVersion.setVisibility(View.INVISIBLE)
        }
    }

    private fun setupLinks() {
        binding.aboutTextTodeveloper.setMovementMethod(LinkMovementMethod.getInstance())
    }

}