package brillembourg.notes.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import brillembourg.notes.simple.ui.home.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            initFragments()
        }
    }

    private fun initFragments() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment.newInstance())
            .commitNow()
    }
}