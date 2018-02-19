package com.philsoft.metrotripper.app.drawer;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.philsoft.metrotripper.R;
import com.philsoft.metrotripper.app.SelectedStopProvider;
import com.philsoft.metrotripper.app.SettingsProvider;
import com.philsoft.metrotripper.app.ui.MapHelper;
import com.philsoft.metrotripper.database.DataProvider;
import com.philsoft.metrotripper.model.Stop;
import com.philsoft.metrotripper.utils.EZ;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.commons.lang.StringUtils;

import java.util.List;

public class DrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int VIEW_TYPE_SEARCH = 0;
	private static final int VIEW_TYPE_STOP = 1;

	private Activity activity;
	private DrawerLayout drawer;
	private DataProvider dataProvider;
	private MapHelper mapHelper;
	private SettingsProvider settingsProvider;
	private List<Long> items = Lists.newArrayList();
	private SelectedStopProvider stopProvider;
	private SlidingUpPanelLayout panel;

	public <T extends Activity & SelectedStopProvider> DrawerAdapter(T activity, DrawerLayout drawer, DataProvider dataProvider,
			MapHelper mapHelper, SettingsProvider settingsProvider, SlidingUpPanelLayout panel) {
		this.activity = activity;
		this.stopProvider = activity;
		this.drawer = drawer;
		this.dataProvider = dataProvider;
		this.mapHelper = mapHelper;
		this.settingsProvider = settingsProvider;
		this.panel = panel;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder vh = null;
		View drawerItem = null;
		switch (viewType) {
		case VIEW_TYPE_SEARCH:
			drawerItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_drawer_item, parent, false);
			vh = new SearchViewHolder(drawerItem);
			break;
		case VIEW_TYPE_STOP:
			drawerItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.stop_drawer_item, parent, false);
			vh = new StopViewHolder(drawerItem);
			break;
		default:
			break;

		}
		return vh;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return VIEW_TYPE_SEARCH;
		} else {
			return VIEW_TYPE_STOP;
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
		switch (getItemViewType(position)) {
		case VIEW_TYPE_SEARCH:
			buildSearchRow((SearchViewHolder) holder);
			break;
		case VIEW_TYPE_STOP:
			long stopId = items.get(position - 1);
			buildStopRow((StopViewHolder) holder, stopId);
			break;
		default:
			break;
		}
	}

	private void buildSearchRow(final SearchViewHolder holder) {
		holder.entry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					String stopIdStr = holder.entry.getText().toString();
					searchStop(stopIdStr, holder.entry);
					return true;
				}
				return false;
			}
		});

		holder.searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EZ.hideKeyboard(activity);
				String stopIdStr = holder.entry.getText().toString();
				searchStop(stopIdStr, holder.entry);
			}
		});
	}

	private void searchStop(String stopIdStr, EditText entry) {
		if (!StringUtils.isEmpty(stopIdStr) && StringUtils.isNumeric(stopIdStr)) {
			long stopId = Long.valueOf(stopIdStr);
			Stop stop = dataProvider.getStopById(stopId);
			if (stop != null) {
				selectStopOnMap(stop, true);
				entry.setText("");
			} else {
				Toast.makeText(activity, R.string.stop_not_found, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void buildStopRow(StopViewHolder holder, long stopId) {
		final Stop stop = dataProvider.getStopById(stopId);
		holder.header.setText(String.valueOf(stopId));
		holder.text.setText(String.valueOf(stop.getStopName()));
		holder.root.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectStopOnMap(stop, true);
			}
		});
		if (isStopSelected(stopId)) {
			holder.root.setBackgroundResource(R.color.dark_blue);
		} else {
			holder.root.setBackgroundResource(R.color.sidebar_bg);
		}
	}

	private boolean isStopSelected(long stopId) {
		return stopProvider.getSelectedStop() != null && stopId == stopProvider.getSelectedStop().getStopId();
	}

	public void selectStopOnMap(Stop stop, boolean animate) {
		drawer.closeDrawers();
		stopProvider.showStop(stop);
		panel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
		mapHelper.centerCameraOnLatLng(stop.getLatLng(), animate);
	}

	@Override
	public int getItemCount() {
		return items.size() + 1;
	}

	public void refresh() {
		items = Lists.newArrayList(settingsProvider.getSavedStopIds());
		notifyDataSetChanged();
	}
}