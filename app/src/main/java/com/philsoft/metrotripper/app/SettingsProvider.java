package com.philsoft.metrotripper.app;

import android.app.Activity;
import android.os.Bundle;

import com.google.common.collect.Sets;
import com.philsoft.metrotripper.fragment.BaseFragment;
import com.philsoft.metrotripper.prefs.Prefs;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by polson on 2/5/15.
 */
public class SettingsProvider extends BaseFragment {


	public final static String TAG = "TAG_SETTINGS_PROVIDER";

	Activity activity;
	LinkedHashSet<Long> savedStopIds = Sets.newLinkedHashSet();
	Set<SettingsListener> listeners = Sets.newHashSet();

	public interface SettingsListener {
		public void onStopSaved(long stopId);

		public void onStopUnsaved(long stopId);
	}

	public void addListener(SettingsListener listener) {
		listeners.add(listener);
	}

	public void removeListener(SettingsListener listener) {
		listeners.remove(listener);
	}

	private void notifyStopSaved(long stopId) {
		for (SettingsListener listener : listeners) {
			listener.onStopSaved(stopId);
		}
	}

	private void notifyStopUnsaved(long stopId) {
		for (SettingsListener listener : listeners) {
			listener.onStopUnsaved(stopId);
		}
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
		setRetainInstance(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		savedStopIds.clear();
		savedStopIds.addAll(Prefs.getSavedStopIds(activity));
	}

	public void saveStop(long stopId) {
		savedStopIds.add(stopId);
		Prefs.saveStopId(activity, stopId);
		notifyStopSaved(stopId);
	}

	public void unsaveStop(long stopId) {
		savedStopIds.remove(stopId);
		Prefs.removeStopId(activity, stopId);
		notifyStopUnsaved(stopId);
	}

	public LinkedHashSet<Long> getSavedStopIds() {
		return savedStopIds;
	}

	public boolean isStopSaved(long stopId) {
		return savedStopIds.contains(stopId);
	}
}

