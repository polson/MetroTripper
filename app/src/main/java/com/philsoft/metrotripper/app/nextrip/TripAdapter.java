package com.philsoft.metrotripper.app.nextrip;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.philsoft.metrotripper.R;
import com.philsoft.metrotripper.constants.Direction;
import com.philsoft.metrotripper.model.Trip;
import com.philsoft.metrotripper.utils.ui.Ui;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private Activity activity;
    private List<Trip> trips = Lists.newArrayList();

    public TripAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View drawerItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_item, parent, false);
        TripViewHolder vh = new TripViewHolder(drawerItem);
        return vh;
    }

    @Override
    public void onBindViewHolder(TripViewHolder holder, final int position) {
        holder.timeUnit.setVisibility(View.VISIBLE);

        Trip trip = trips.get(position);
        holder.route.setText(trip.vehicle.route + trip.vehicle.terminal);
        holder.description.setText(trip.description);
        holder.tripDirection.setImageResource(getTripDirectionResource(trip));

        String[] timeAndText = trip.departureText.split("\\s+");
        holder.timeNumber.setText(timeAndText[0]);
        if (timeAndText.length > 1) {
            holder.timeUnit.setText(R.string.minutes);
        } else {
            holder.timeUnit.setVisibility(View.GONE);
        }
    }

    private int getTripDirectionResource(Trip trip) {
        Direction direction = Direction.valueOf(trip.routeDirection);
        switch (direction) {
            case NORTHBOUND:
                return R.drawable.ic_up_arrow;
            case SOUTHBOUND:
                return R.drawable.ic_down_arrow;
            case EASTBOUND:
                return R.drawable.ic_right_arrow;
            case WESTBOUND:
                return R.drawable.ic_left_arrow;
            default:
                return 0;
        }
    }

    private void setColors(TripViewHolder holder, int mainColorResId, int topLineColorResId, int bottomLineColorResId) {
        holder.mainLayout.setBackgroundResource(mainColorResId);
        holder.topLine.setBackgroundResource(topLineColorResId);
        holder.bottomLine.setBackgroundResource(bottomLineColorResId);
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

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        public TextView route;
        public TextView description;
        public TextView timeNumber;
        public TextView timeUnit;
        public View topLine;
        public View bottomLine;
        public View mainLayout;
        public ImageView tripDirection;

        public TripViewHolder(View root) {
            super(root);
            route = Ui.findView(root, R.id.trip_item_route);
            description = Ui.findView(root, R.id.trip_item_description);
            timeNumber = Ui.findView(root, R.id.trip_item_time_number);
            timeUnit = Ui.findView(root, R.id.trip_item_time_unit);
            topLine = Ui.findView(root, R.id.trip_item_line_top);
            bottomLine = Ui.findView(root, R.id.trip_item_line_bottom);
            mainLayout = Ui.findView(root, R.id.trip_item_main_layout);
            tripDirection = Ui.findView(root, R.id.trip_item_route_direction);
        }
    }
}