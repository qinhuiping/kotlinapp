package com.qhping.kotlin.app.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.qhping.kotlin.app.bean.ImageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GalleryViewModel(private val repository: GalleryRepository) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "这是我的相册，点击这里回到顶部"
    }
    val text: LiveData<String> = _text

    private val _galleryImages = MutableStateFlow<List<ImageItem>>(emptyList())
    val galleryImages: StateFlow<List<ImageItem>> = _galleryImages

    private var currentPage = 0
    private val pageSize = 40
    fun loadGalleryImages() {
        val images = repository.getAllImage(currentPage, pageSize)
        _galleryImages.value += images
        currentPage++
    }
}