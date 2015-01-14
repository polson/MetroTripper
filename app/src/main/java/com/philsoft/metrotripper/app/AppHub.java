package com.philsoft.metrotripper.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.philsoft.metrotripper.app.nextrip.NexTripManager;
import com.philsoft.metrotripper.database.DataProvider;
import com.philsoft.metrotripper.database.DatabasePopulator;
import com.philsoft.metrotripper.database.contracts.StopContract;
import com.philsoft.metrotripper.utils.ui.Ui;

import java.io.IOException;

/**
 * Created by polson on 1/15/15.
 */
public class AppHub {

    private final Logger log = LoggerManager.getLogger(getClass());
    private Activity activity;
    private NexTripManager nexTripManager;
    private FragmentManager fragmentManager;
    private DataProvider dataProvider;
    private SettingsProvider settingsProvider;

    public AppHub(Activity activity) {
        this.activity = activity;
        this.fragmentManager = activity.getFragmentManager();
        this.nexTripManager = getNexTripManager();
        this.settingsProvider = getSettingsProvider();
        this.dataProvider = new DataProvider(activity);

        populateStops();
    }

    private void populateStops() {
        DatabasePopulator populator = new DatabasePopulator(activity);
        try {
            if (populator.isTableEmpty(StopContract.TABLE_NAME)) {
                populator.populateStopsFast();
            } else {
                log.d("Unable to populate stops: Stop table not empty");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NexTripManager getNexTripManager() {
        if (nexTripManager == null) {
            nexTripManager = findFrag(NexTripManager.TAG);
            if (nexTripManager == null) {
                nexTripManager = new NexTripManager();
                commitFrag(nexTripManager, NexTripManager.TAG);
            }
        }
        return nexTripManager;
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
