package com.qhping.kotlin.app

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.qhping.kotlin.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            permission.entries.forEach {
                val pName = it.key
                val isGranted = it.value
                var shouldShowRationale = false
                var allPermissionsGranted = true
                if (isGranted) {
                    Toast.makeText(this, "$pName 权限已授予", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "$pName 被拒绝", Toast.LENGTH_SHORT).show()
                    allPermissionsGranted = false
                    if (shouldShowRequestPermissionRationale(pName)) {
                        shouldShowRationale = true
                    }
                }
                if (allPermissionsGranted) {
                    initView()
                } else {
                    if (shouldShowRationale) {
                        showPermissionRationale(permission.keys.toTypedArray())
                    } else {
                        showSettingDialog()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).setAnchorView(R.id.fab).show()
        }
        val permissionToRequest = arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
//            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val permissionGranted = permissionToRequest.all {
            ContextCompat.checkSelfPermission(
                this, it
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (permissionGranted) {
            Toast.makeText(this, "已有权限", Toast.LENGTH_SHORT).show()
            initView()
        } else {
            requestMultiplePermission.launch(permissionToRequest)
        }
    }


    private fun initView() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun showPermissionRationale(permissions: Array<String>) {
        AlertDialog.Builder(this).setTitle("权限请求")
            .setMessage("应用需要这些权限才能正常工作，请授权").setPositiveButton("确定") { _, _ ->
                requestMultiplePermission.launch(permissions)
            }.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showSettingDialog() {
        AlertDialog.Builder(this).setTitle("权限被拒绝")
            .setMessage("你已经永久拒绝了某些权限，请在应用设置中手动开启")
            .setPositiveButton("去设置") { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                startActivity(intent)
            }.setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }.show()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}