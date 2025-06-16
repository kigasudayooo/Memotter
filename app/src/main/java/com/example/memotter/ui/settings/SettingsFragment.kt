package com.example.memotter.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.memotter.R
import com.example.memotter.databinding.FragmentSettingsBinding
import com.example.memotter.util.PreferencesManager
import com.example.memotter.ui.dialog.DirectorySelectDialog
import com.example.memotter.util.BackupManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var backupManager: BackupManager

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
        
        preferencesManager = PreferencesManager(requireContext())
        backupManager = BackupManager(requireContext())
        setupViews()
        loadSettings()
    }

    private fun setupViews() {
        binding.apply {
            // File mode radio group
            rgFileMode.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rb_daily_file -> {
                        preferencesManager.fileMode = PreferencesManager.FileMode.DAILY
                    }
                    R.id.rb_continuous_file -> {
                        preferencesManager.fileMode = PreferencesManager.FileMode.CONTINUOUS
                    }
                }
            }

            // Dark mode switch
            switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                preferencesManager.isDarkMode = isChecked
            }

            // Font size slider
            sliderFontSize.addOnChangeListener { _, value, _ ->
                preferencesManager.fontSize = value
            }

            // Custom directory switch
            switchCustomDirectory.setOnCheckedChangeListener { _, isChecked ->
                preferencesManager.useCustomDirectory = isChecked
                updateDirectoryVisibility()
            }

            // Directory selection button
            btnSelectDirectory.setOnClickListener {
                showDirectorySelectDialog()
            }

            // Export files button
            btnExportFiles.setOnClickListener {
                showExportDirectoryDialog(false)
            }

            // Export ZIP button
            btnExportZip.setOnClickListener {
                showExportDirectoryDialog(true)
            }

            // Help button
            btnHelp.setOnClickListener {
                findNavController().navigate(R.id.nav_help)
            }
        }
    }
    
    private fun loadSettings() {
        binding.apply {
            // Load file mode setting
            when (preferencesManager.fileMode) {
                PreferencesManager.FileMode.DAILY -> rbDailyFile.isChecked = true
                PreferencesManager.FileMode.CONTINUOUS -> rbContinuousFile.isChecked = true
            }
            
            // Load dark mode setting
            switchDarkMode.isChecked = preferencesManager.isDarkMode
            
            // Load font size setting
            sliderFontSize.value = preferencesManager.fontSize
            
            // Load directory settings
            switchCustomDirectory.isChecked = preferencesManager.useCustomDirectory
            updateDirectoryPath()
            updateDirectoryVisibility()
        }
    }

    private fun updateDirectoryVisibility() {
        binding.layoutDirectoryPath.visibility = if (preferencesManager.useCustomDirectory) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    
    private fun updateDirectoryPath() {
        val customDir = preferencesManager.customDirectoryPath
        binding.tvCurrentDirectoryPath.text = customDir ?: "デフォルトディレクトリを使用"
    }
    
    private fun showDirectorySelectDialog() {
        val dialog = DirectorySelectDialog()
        dialog.setOnDirectorySelectedListener { selectedPath ->
            preferencesManager.customDirectoryPath = selectedPath
            updateDirectoryPath()
        }
        dialog.show(parentFragmentManager, "DirectorySelectDialog")
    }
    
    private fun showExportDirectoryDialog(isZipExport: Boolean) {
        val dialog = DirectorySelectDialog()
        dialog.setOnDirectorySelectedListener { selectedPath ->
            performExport(selectedPath, isZipExport)
        }
        dialog.show(parentFragmentManager, "ExportDirectorySelectDialog")
    }
    
    private fun performExport(targetPath: String, isZipExport: Boolean) {
        val targetDirectory = java.io.File(targetPath)
        
        // エクスポート可能なファイル数を確認
        val fileCount = backupManager.getExportableFileCount()
        if (fileCount == 0) {
            Toast.makeText(requireContext(), "エクスポートするファイルがありません", Toast.LENGTH_SHORT).show()
            return
        }
        
        // プログレス表示
        Toast.makeText(requireContext(), "エクスポート中... ($fileCount ファイル)", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                if (isZipExport) {
                    backupManager.exportToZip(targetDirectory)
                } else {
                    backupManager.exportToDirectory(targetDirectory)
                }
            }
            
            when (result) {
                is BackupManager.ExportResult.Success -> {
                    val message = if (isZipExport) {
                        "ZIPファイルを作成しました\n${result.fileCount}ファイル\n${result.exportPath}"
                    } else {
                        "ファイルをエクスポートしました\n${result.fileCount}ファイル\n${result.exportPath}"
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
                is BackupManager.ExportResult.Error -> {
                    Toast.makeText(requireContext(), "エラー: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}