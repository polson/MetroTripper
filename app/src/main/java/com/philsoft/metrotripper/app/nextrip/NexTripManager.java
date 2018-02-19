package com.philsoft.metrotripper.app.nextrip;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.philsoft.metrotripper.constants.Urls;
import com.philsoft.metrotripper.fragment.BaseFragment;
import com.philsoft.metrotripper.model.Trip;
import com.philsoft.metrotripper.model.Vehicle;
import com.philsoft.metrotripper.utils.EZ;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class NexTripManager extends BaseFragment {

    public final static String TAG = "TAG_NEXTRIPMANAGER";

    private List<Trip> trips = Lists.newArrayList();
    private Set<NexTripListener> listeners = Sets.newHashSet();
    private Handler handler = new Handler(Looper.getMainLooper());
    private GetTripsRunnable currentRunnable = null;

    public interface NexTripListener {
        public void onNexTripLoadComplete(List<Trip> trips);

        public void onNexTripLoadFailed(String message);

        public void onNexTripLoadingStopped();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
    }

    public void addListener(NexTripListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NexTripListener listener) {
        listeners.remove(listener);
    }

    public void getTripsRepeating(long stopId, int interval) {

        if (currentRunnable != null && currentRunnable.stopId == stopId) {
            // Same stop selected, return existing data
            Timber.d("Trips requested for same stop: " + stopId + ", returning existing trip data");
            notifyLoadComplete(Lists.newArrayList(trips));
            return;
        }

        // Different stop selected, cancel any existing runnables
        Timber.d("Started updating trips for stopId: " + stopId);
        stopUpdatingTrips();
        currentRunnable = new GetTripsRunnable(stopId, interval);
        currentRunnable.run();
    }

    public void stopUpdatingTrips() {
        if (currentRunnable != null) {
            Timber.d("Stopped updating trips for stopId: " + currentRunnable.stopId);
            handler.removeCallbacks(currentRunnable);
            currentRunnable = null;
            notifyLoadingStopped();
        }
    }

    public void pauseUpdatingTrips() {
        if (currentRunnable != null) {
            Timber.d("Paused updating trips for stopId: " + currentRunnable.stopId);
            handler.removeCallbacks(currentRunnable);
        }
    }

    public void resumeUpdatingTrips() {
        if (currentRunnable != null) {
            Timber.d("Resumed updating trips for stopId: " + currentRunnable.stopId);
            currentRunnable.run();
        }
    }

    private void notifyLoadingStopped() {
        for (NexTripListener listener : listeners) {
            listener.onNexTripLoadingStopped();
        }
    }

    private void notifyLoadComplete(List<Trip> trips) {
        for (NexTripListener listener : listeners) {
            listener.onNexTripLoadComplete(trips);
        }
    }

    private void notifyLoadFailed(String message) {
        for (NexTripListener listener : listeners) {
            listener.onNexTripLoadFailed(message);
        }
    }

    private void getTrips(final long stopId) {
        Timber.d("Getting trips for stopId: " + stopId);
        String url = Urls.getNexTripUrl(stopId);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Timber.d("Loaded trips for stopId: " + stopId);
                    trips = buildTrips(response);
                    Collections.sort(trips, tripTimeComparator);
                    notifyLoadComplete(Lists.newArrayList(trips));
                } catch (JSONException e) {
                    notifyLoadFailed(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                notifyLoadFailed(error.getMessage());
            }
        });
        requestQueue.add(request);
    }

    private Comparator<Trip> tripTimeComparator = new Comparator<Trip>() {
        @Override
        public int compare(Trip lhs, Trip rhs) {
            if (lhs.getDepartureTime() > rhs.getDepartureTime()) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    private List<Trip> buildTrips(String response) throws JSONException {
        List<Trip> trips = Lists.newArrayList();
        JSONArray array = new JSONArray(response);
        for (int i = 0; i < array.length(); i++) {
            JSONObject tripJson = array.optJSONObject(i);
            Vehicle vehicle = new Vehicle(tripJson.optString("Route"), tripJson.optString("Terminal"), ((Number) tripJson
                    .get("VehicleHeading")).doubleValue(), ((Number) tripJson.get("VehicleLatitude")).doubleValue(), ((Number) tripJson
                    .get("VehicleLongitude")).doubleValue());

            Trip trip = new Trip(vehicle, tripJson.optBoolean("Actual"), tripJson.optInt("BlockNumber"), tripJson
                    .optString("DepartureText"), EZ.parseLocationTime(tripJson.optString("DepartureTime")), tripJson
                    .optString("Description"), tripJson.optString("Gate"), tripJson.optString("RouteDirection"));
            trips.add(trip);
        }
        return trips;
    }

    private class GetTripsRunnable implements Runnable {
        public final long stopId;
        public final int interval;

        private GetTripsRunnable(long stopId, int interval) {
            this.stopId = stopId;
            this.interval = interval;
        }

        @Override
        public void run() {
            getTrips(stopId);
            handler.postDelayed(this, interval);
        }
    }
}
