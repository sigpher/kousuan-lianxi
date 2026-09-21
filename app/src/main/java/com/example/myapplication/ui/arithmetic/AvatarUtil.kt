package com.example.myapplication.ui.arithmetic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import java.io.File

object AvatarUtil {

    fun load(view: ImageView, path: String?) {
        view.setImageBitmap(path?.let { decodeFile(File(it), 256) })
    }

    fun loadDefault(view: ImageView, context: Context) {
        view.setImageBitmap(defaultAvatar(context))
    }

    fun defaultAvatar(context: Context): Bitmap? =
        decodeDrawable(context, R.mipmap.ic_launcher)

    private fun decodeDrawable(context: Context, resId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, resId) ?: return null
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 256
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 256
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
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