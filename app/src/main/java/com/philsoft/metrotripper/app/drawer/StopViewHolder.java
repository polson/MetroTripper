package com.philsoft.metrotripper.app.drawer;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.philsoft.metrotripper.R;
import com.philsoft.metrotripper.utils.ui.Ui;

public class StopViewHolder extends RecyclerView.ViewHolder {
	public View root;
	public TextView header;
	public TextView text;

	public StopViewHolder(View root) {
		super(root);
		this.root = root;
		header = Ui.findView(root, R.id.stop_drawer_item_header);
		text = Ui.findView(root, R.id.stop_drawer_item_text);
	}
}