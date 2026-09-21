package com.example.myapplication.ui.arithmetic

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import java.io.File

object AvatarUtil {

    fun load(view: ImageView, path: String?) {
        view.setImageBitmap(path?.let { decodeFile(File(it), 256) })
    }

    fun loadFile(view: ImageView, file: File?) {
        view.setImageBitmap(file?.let { decodeFile(it, 256) })
    }

    private fun decodeFile(file: File, maxSize: Int): Bitmap? {
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (bounds.outWidth / sample > maxSize || bounds.outHeight / sample > maxSize) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(file.path, options)
    }
}