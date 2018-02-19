package com.philsoft.metrotripper.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;

import com.philsoft.metrotripper.database.DataProvider;
import com.philsoft.metrotripper.database.DatabasePopulator;
import com.philsoft.metrotripper.utils.ui.Ui;

import java.io.IOException;

import timber.log.Timber;

public class AppHub {

    private Activity activity;
    private FragmentManager fragmentManager;
    private DataProvider dataProvider;
    private SettingsProvider settingsProvider;

    public AppHub(Activity activity) {
        this.activity = activity;
        this.fragmentManager = activity.getFragmentManager();
        this.settingsProvider = getSettingsProvider();
        this.dataProvider = new DataProvider(activity);

        populateStops();
    }

    private void populateStops() {
        DatabasePopulator populator = new DatabasePopulator(activity);
        try {
            if (populator.isTableEmpty()) {
                populator.populateStopsFast();
            } else {
                Timber.d("Unable to populate stops: Stop table not empty");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SettingsProvider getSettingsProvider() {
        if (settingsProvider == null) {
            settingsProvider = findFrag(SettingsProvider.TAG);
            if (settingsProvider == null) {
                settingsProvider = new SettingsProvider();
                commitFrag(settingsProvider, SettingsProvider.TAG);
            }
        }
        return settingsProvider;
    }

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    private <T extends Fragment> T findFrag(String tag) {
        return Ui.findFrag(activity, tag);
    }

    private void commitFrag(Fragment frag, String tag) {
        fragmentManager.beginTransaction().add(frag, tag).commit();
        fragmentManager.executePendingTransactions();
    }
}
