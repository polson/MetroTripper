package com.philsoft.metrotripper.utils.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class Ui {

    @SuppressWarnings("unchecked")
    public static <T extends Fragment> T findFrag(Activity activity, String tag) {
        return (T) activity.getFragmentManager().findFragmentByTag(tag);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findView(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findView(View enclosingView, int id) {
        return (T) enclosingView.findViewById(id);
    }

    public static TextView setText(View enclosingView, int textViewId, String text) {
        TextView textView = findView(enclosingView, textViewId);
        textView.setText(text);
        return textView;
    }

    public static TextView setText(Activity activity, int textViewId, String text) {
        TextView textView = findView(activity, textViewId);
        textView.setText(text);
        return textView;
    }

    public static Bitmap createBitmapFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public static Bitmap createBitmapFromDrawableResource(Context context, int widthOffset, int heightOffset, int drawableResource) {
        Drawable d = context.getResources().getDrawable(drawableResource);
        BitmapDrawable bd = (BitmapDrawable) d.getCurrent();
        Bitmap b = bd.getBitmap();
        return Bitmap.createScaledBitmap(b, b.getWidth() + widthOffset, b.getHeight() + heightOffset, false);
    }

    public static Bitmap createBitmapFromLayoutResource(Activity activity, int layoutResource) {
        View view = activity.getLayoutInflater().inflate(layoutResource, null, false);
        return createBitmapFromView(activity, view);
    }

}
