package com.qhping.kotlin.app.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.qhping.kotlin.app.bean.ImageItem

class GalleryViewModel(private val repository: GalleryRepository) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text

    private val _galleryImages = MutableLiveData<List<ImageItem>>()
    val galleryImages: LiveData<List<ImageItem>> = _galleryImages

    fun loadGalleryImages() {
        val images = repository.getAllImage()
        _galleryImages.value = images
    }
}