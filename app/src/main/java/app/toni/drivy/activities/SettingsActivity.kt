package app.toni.drivy.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.toni.drivy.R
import app.toni.drivy.fragments.menu.SettingsFragment

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }
}
