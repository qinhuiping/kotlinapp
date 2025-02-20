package com.qhping.kotlin.app.ui.gallery

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.qhping.kotlin.app.R
import com.qhping.kotlin.app.bean.ImageItem

class ImageAdapter(mContext: Context, data: MutableList<ImageItem>) :
    BaseQuickAdapter<ImageItem, BaseViewHolder>(R.layout.item_image, data) {

    private val c = mContext
    override fun convert(holder: BaseViewHolder, item: ImageItem) {
        val imageView = holder.getView<AppCompatImageView>(R.id.item_iv)
        Glide.with(c).load(item.path).thumbnail(0.1f).centerCrop()
            .into(imageView)
    }
}