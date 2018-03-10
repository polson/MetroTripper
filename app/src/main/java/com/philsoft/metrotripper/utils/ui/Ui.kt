package com.philsoft.metrotripper.utils.ui

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


object Ui {

    fun <T : View> findView(activity: Activity, id: Int): T {
        return activity.findViewById<View>(id) as T
    }

    fun <T : View> findView(enclosingView: View, id: Int): T {
        return enclosingView.findViewById<View>(id) as T
    }

    fun setText(enclosingView: View, textViewId: Int, text: String): TextView {
        val textView = findView<TextView>(enclosingView, textViewId)
        textView.text = text
        return textView
    }

    fun setText(activity: Activity, textViewId: Int, text: String): TextView {
        val textView = findView<TextView>(activity, textViewId)
        textView.text = text
        return textView
    }

    fun createBitmapFromView(context: Context, view: View): Bitmap {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    fun createBitmapFromDrawableResource(context: Context, widthOffset: Int, heightOffset: Int, drawableResource: Int): Bitmap {
        val d = context.resources.getDrawable(drawableResource)
        val bd = d.current as BitmapDrawable
        val b = bd.bitmap
        return Bitmap.createScaledBitmap(b, b.width + widthOffset, b.height + heightOffset, false)
    }

    fun createBitmapFromLayoutResource(activity: Activity, layoutResource: Int): Bitmap {
        val view = activity.layoutInflater.inflate(layoutResource, null, false)
        return createBitmapFromView(activity, view)
    }

}
