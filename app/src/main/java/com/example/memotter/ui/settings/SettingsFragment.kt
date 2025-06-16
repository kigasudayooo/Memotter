package com.example.memotter.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.memotter.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            // File mode radio group
            rgFileMode.setOnCheckedChangeListener { _, checkedId ->
                // TODO: Save file mode preference
            }

            // Dark mode switch
            switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                // TODO: Apply dark mode
            }

            // Font size slider
            sliderFontSize.addOnChangeListener { _, value, _ ->
                // TODO: Apply font size
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}