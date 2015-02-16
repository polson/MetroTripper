package com.philsoft.metrotripper.utils.map;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.util.Property;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

/**
 * Created by polson on 1/15/15.
 */
public class MapUtils {

	private static final Logger log = LoggerManager.getLogger();

	public static ObjectAnimator animateMarkerTo(Marker marker, LatLng finalPosition, final LatLngInterpolator latLngInterpolator, int duration) {
		TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
			@Override
			public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
				return latLngInterpolator.interpolate(fraction, startValue, endValue);
			}
		};
		Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
		ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
		animator.setDuration(duration);
		animator.start();
		return animator;
	}

	public static ValueAnimator fadeMarkerToNewLocation(final Marker marker, final LatLng newLocation, final int duration) {
		//Fade out
		ValueAnimator ani = ValueAnimator.ofFloat(1, 0);
		ani.setDuration(duration);
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				marker.setAlpha((float) animation.getAnimatedValue());
			}
		});
		ani.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				//Fade in at new location
				marker.setPosition(newLocation);
				ValueAnimator ani = ValueAnimator.ofFloat(0, 1);
				ani.setDuration(duration);
				ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						marker.setAlpha((float) animation.getAnimatedValue());
					}
				});
				ani.start();
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		ani.start();
		return ani;
	}

	public static ValueAnimator fadeOutMarker(final Marker marker, int duration) {
		ValueAnimator ani = ValueAnimator.ofFloat(1, 0);
		ani.setDuration(duration);
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				marker.setAlpha((float) animation.getAnimatedValue());
			}
		});
		ani.start();
		return ani;
	}

	public static ValueAnimator fadeOutMarkerAndRemove(final Marker marker, int duration) {
		log.d("Fading marker: " + marker.getTitle());
		ValueAnimator ani = ValueAnimator.ofFloat(1, 0);
		ani.setDuration(duration);
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				marker.setAlpha((float) animation.getAnimatedValue());
			}
		});
		ani.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				marker.remove();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				marker.remove();
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		ani.start();
		return ani;
	}


	public static ValueAnimator fadeInMarker(final Marker marker, int duration) {
		ValueAnimator ani = ValueAnimator.ofFloat(0, 1);
		ani.setDuration(duration);
		ani.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				marker.setAlpha((float) animation.getAnimatedValue());
			}
		});
		ani.start();
		return ani;
	}

	public static void showViewForTime(final View view, int duration) {
		view.setVisibility(View.VISIBLE);
		view.postDelayed(new Runnable() {
			public void run() {
				view.setVisibility(View.GONE);
			}
		}, duration);
	}

	public static void hideViewForTime(final View view, int duration) {
		view.setVisibility(View.GONE);
		view.postDelayed(new Runnable() {
			public void run() {
				view.setVisibility(View.VISIBLE);
			}
		}, duration);
	}
}
