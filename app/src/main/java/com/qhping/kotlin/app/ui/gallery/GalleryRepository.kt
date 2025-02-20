package com.qhping.kotlin.app.ui.gallery

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.qhping.kotlin.app.bean.ImageItem

class GalleryRepository(private val context: Context) {

    fun getAllImage(page: Int, pageSize: Int): List<ImageItem> {
        val imageList = mutableListOf<ImageItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        val startIndex = page * pageSize
        var indexCurrent = 0

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder
            )
            cursor?.use {
                while (it.moveToNext() && indexCurrent < startIndex + pageSize) {
                    if (indexCurrent >= startIndex) {
                        val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                        val path =
                            it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                        val dateToken =
                            it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN))
                        imageList.add(ImageItem(id, path, dateToken))
                    }
                    indexCurrent++
                }
            }
        } catch (e: Exception) {
            Log.e("Exception", "getAllImage: $e")
        }
        return imageList
    }
}