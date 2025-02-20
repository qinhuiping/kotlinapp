package com.qhping.kotlin.app.ui.gallery

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qhping.kotlin.app.databinding.FragmentGalleryBinding
import kotlinx.coroutines.launch

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private lateinit var galleryViewModel: GalleryViewModel
    private val binding get() = _binding!!
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val repository = GalleryRepository(requireContext())
        val factory = GalleryViewModelFactory(repository)
        galleryViewModel = ViewModelProvider(this, factory)[GalleryViewModel::class.java]

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
//                android.Manifest.permission.CAMERA
            )
        } else {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
//                android.Manifest.permission.CAMERA
            )
        }
        val permissionGranted = permissionToRequest.all {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) == PackageManager.PERMISSION_GRANTED
        }


        imageAdapter = ImageAdapter(requireContext(), mutableListOf())
        binding.rvImage.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvImage.adapter = imageAdapter

        binding.rvImage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (lastVisibleItemPosition == totalItemCount - 1) {
                    galleryViewModel.loadGalleryImages()
                }
            }
        })


        if (permissionGranted) {
            Toast.makeText(requireContext(), "已有权限", Toast.LENGTH_SHORT).show()
            galleryViewModel.loadGalleryImages()
        } else {
            requestMultiplePermission.launch(permissionToRequest)
        }
        lifecycleScope.launch {
            galleryViewModel.galleryImages.collect { imageList ->
                imageAdapter.setList(imageList)
            }
        }

        binding.textGallery.setOnClickListener {
            binding.rvImage.scrollToPosition(0)
            binding.rvImage.smoothScrollToPosition(0)
        }
    }


    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            permission.entries.forEach {
                val pName = it.key
                val isGranted = it.value
                var shouldShowRationale = false
                var allPermissionsGranted = true
                if (isGranted) {
                    Toast.makeText(requireContext(), "$pName 权限已授予", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(requireContext(), "$pName 被拒绝", Toast.LENGTH_SHORT).show()
                    allPermissionsGranted = false
                    if (shouldShowRequestPermissionRationale(pName)) {
                        shouldShowRationale = true
                    }
                }

//                allPermissionsGranted = permission.values.all { it }
                if (allPermissionsGranted) {
                    galleryViewModel.loadGalleryImages()
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

    }

    private fun showPermissionRationale(permissions: Array<String>) {
        AlertDialog.Builder(requireContext()).setTitle("权限请求")
            .setMessage("应用需要这些权限才能正常工作，请授权").setPositiveButton("确定") { _, _ ->
                requestMultiplePermission.launch(permissions)
            }.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showSettingDialog() {
        AlertDialog.Builder(requireContext()).setTitle("权限被拒绝")
            .setMessage("你已经永久拒绝了某些权限，请在应用设置中手动开启")
            .setPositiveButton("去设置") { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }.setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}