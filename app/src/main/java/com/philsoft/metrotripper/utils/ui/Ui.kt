package com.philsoft.metrotripper.utils.ui

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.view.WindowManager
import com.philsoft.metrotripper.utils.createBitmap


object Ui {
    fun createBitmapFromDrawableResource(context: Context, widthOffset: Int, heightOffset: Int, drawableResource: Int): Bitmap {
        val d = context.resources.getDrawable(drawableResource)
        val bd = d.current as BitmapDrawable
        val b = bd.bitmap
        return Bitmap.createScaledBitmap(b, b.width + widthOffset, b.height + heightOffset, false)
    }

    fun createBitmapFromLayoutResource(activity: Activity, layoutResource: Int): Bitmap {
        val view = activity.layoutInflater.inflate(layoutResource, null, false)
        return view.createBitmap()
    }

    fun getCurrentScreenHeight(context: Context): Int {
        val size = getDisplaySize(context)
        return size.y
    }

    private fun getDisplaySize(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }
}
