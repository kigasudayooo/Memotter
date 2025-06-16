package com.example.memotter

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.example.memotter.databinding.ActivityMainBinding
import com.example.memotter.ui.dialog.FileOpenDialog
import com.example.memotter.ui.dialog.DirectorySelectDialog
import com.example.memotter.util.PreferencesManager
import com.example.memotter.util.BackupManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var backupManager: BackupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferencesManager = PreferencesManager(this)
        backupManager = BackupManager(this)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        
        // NavController will be initialized in onStart()
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_new_memo).setOnClickListener { view ->
            // Navigate to new memo fragment
            if (::navController.isInitialized) {
                navController.navigate(R.id.nav_new_memo)
            }
        }

        // Navigation setup will be done in onStart()
    }

    override fun onStart() {
        super.onStart()
        
        // Initialize NavController after the activity is fully started
        navController = findNavController(R.id.nav_host_fragment_content_main)
        
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_timeline,
                R.id.nav_new_memo,
                R.id.nav_search,
                R.id.nav_favorites,
                R.id.nav_templates,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Set up navigation item selected listener
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_timeline -> {
                    navController.navigate(R.id.nav_timeline)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_new_memo -> {
                    navController.navigate(R.id.nav_new_memo)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_search -> {
                    navController.navigate(R.id.nav_search)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_favorites -> {
                    navController.navigate(R.id.nav_favorites)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_templates -> {
                    navController.navigate(R.id.nav_templates)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    navController.navigate(R.id.nav_settings)
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_file -> {
                showFileOpenDialog()
                true
            }
            R.id.action_select_directory -> {
                showDirectorySelectDialog()
                true
            }
            R.id.action_export -> {
                showExportOptionsDialog()
                true
            }
            R.id.action_search -> {
                navController.navigate(R.id.nav_search)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFileOpenDialog() {
        val dialog = FileOpenDialog.newInstance { file ->
            // ファイルが選択された時の処理
            openMarkdownFile(file)
        }
        dialog.show(supportFragmentManager, "FileOpenDialog")
    }
    
    private fun showDirectorySelectDialog() {
        val dialog = DirectorySelectDialog()
        dialog.setOnDirectorySelectedListener { selectedPath ->
            preferencesManager.useCustomDirectory = true
            preferencesManager.customDirectoryPath = selectedPath
            
            // Show confirmation message
            Snackbar.make(binding.root, "ディレクトリが設定されました: $selectedPath", Snackbar.LENGTH_LONG).show()
        }
        dialog.show(supportFragmentManager, "DirectorySelectDialog")
    }
    
    private fun showExportOptionsDialog() {
        val options = arrayOf("ファイルをエクスポート", "ZIPでエクスポート")
        
        AlertDialog.Builder(this)
            .setTitle("エクスポート方法を選択")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showExportDirectoryDialog(false) // ファイル
                    1 -> showExportDirectoryDialog(true)  // ZIP
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }
    
    private fun showExportDirectoryDialog(isZipExport: Boolean) {
        val dialog = DirectorySelectDialog()
        dialog.setOnDirectorySelectedListener { selectedPath ->
            performExport(selectedPath, isZipExport)
        }
        dialog.show(supportFragmentManager, "ExportDirectorySelectDialog")
    }
    
    private fun performExport(targetPath: String, isZipExport: Boolean) {
        val targetDirectory = java.io.File(targetPath)
        
        // エクスポート可能なファイル数を確認
        val fileCount = backupManager.getExportableFileCount()
        if (fileCount == 0) {
            Toast.makeText(this, "エクスポートするファイルがありません", Toast.LENGTH_SHORT).show()
            return
        }
        
        // プログレス表示
        Toast.makeText(this, "エクスポート中... ($fileCount ファイル)", Toast.LENGTH_SHORT).show()
        
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
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                }
                is BackupManager.ExportResult.Error -> {
                    Toast.makeText(this@MainActivity, "エラー: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openMarkdownFile(file: java.io.File) {
        // MarkdownViewerFragmentに遷移してファイルを表示
        val bundle = Bundle().apply {
            putString("file_path", file.absolutePath)
        }
        navController.navigate(R.id.nav_markdown_viewer, bundle)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}