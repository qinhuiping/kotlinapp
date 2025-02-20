package com.qhping.kotlin.app.ui.gallery

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.qhping.kotlin.app.bean.ImageItem
import com.qhping.kotlin.app.databinding.FragmentGalleryBinding

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
        binding.rvImage.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvImage.adapter = imageAdapter


        if (permissionGranted) {
            Toast.makeText(requireContext(), "已有权限", Toast.LENGTH_SHORT).show()
            galleryViewModel.loadGalleryImages()
        } else {
            requestMultiplePermission.launch(permissionToRequest)
        }
        galleryViewModel.galleryImages.observe(viewLifecycleOwner) { imageList ->
            imageAdapter.setList(imageList)
        }
    }


    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            permission.entries.forEach { it ->
                val pName = it.key
                val isGranted = it.value
                if (isGranted) {
                    Toast.makeText(requireContext(), "$pName 权限已授予", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(requireContext(), "$pName 被拒绝", Toast.LENGTH_SHORT).show()
                }

                val allPermissionsGranted = permission.values.all { it }
                if (allPermissionsGranted){
                    galleryViewModel.loadGalleryImages()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}