package app.toni.drivy.fragments.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import app.toni.drivy.R
import app.toni.drivy.activities.HomeActivity
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val idiomaPref = findPreference<ListPreference>("app_language")
        idiomaPref?.setOnPreferenceChangeListener { _, newValue ->
            val langCode = newValue.toString()
            setLocale(langCode)
            true
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = requireContext().resources.configuration
        config.setLocale(locale)

        requireContext().resources.updateConfiguration(
            config,
            requireContext().resources.displayMetrics
        )

        // Guarda el idioma
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .edit()
            .putString("app_language", languageCode)
            .apply()

        // Reinicia completamente la app lanzando HomeActivity
        val intent = Intent(requireContext(), HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


}
