package com.philsoft.metrotripper.app.nextrip;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.philsoft.metrotripper.R;
import com.philsoft.metrotripper.model.Trip;
import com.philsoft.metrotripper.utils.ui.Ui;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private final Logger log = LoggerManager.getLogger(getClass());

    private Activity activity;
    private List<Trip> trips = Lists.newArrayList();

    public TripAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public TripAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View drawerItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_item, parent, false);
        ViewHolder vh = new ViewHolder(drawerItem);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.timeUnit.setVisibility(View.VISIBLE);

        Trip trip = trips.get(position);
        holder.route.setText(trip.vehicle.route + trip.vehicle.terminal);
        holder.description.setText(trip.description);

        String[] timeAndText = trip.departureText.split("\\s+");
        holder.timeNumber.setText(timeAndText[0]);
        if (timeAndText.length > 1) {
            holder.timeUnit.setText(R.string.minutes);
        } else {
            holder.timeUnit.setVisibility(View.GONE);
        }
    }

    public void setTrips(List<Trip> trips) {
        this.trips.clear();
        this.trips.addAll(trips);
        notifyDataSetChanged();
    }

    public void clear() {
        this.trips.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView route;
        public TextView description;
        public TextView timeNumber;
        public TextView timeUnit;

        public ViewHolder(View root) {
            super(root);
            route = Ui.findView(root, R.id.trip_item_route);
            description = Ui.findView(root, R.id.trip_item_description);
            timeNumber = Ui.findView(root, R.id.trip_item_time_number);
            timeUnit = Ui.findView(root, R.id.trip_item_time_unit);
        }
    }
}