package com.philsoft.metrotripper.app.drawer;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.philsoft.metrotripper.R;
import com.philsoft.metrotripper.utils.ui.Ui;

public class SearchViewHolder extends RecyclerView.ViewHolder {
    public View root;
    public EditText entry;
    public ImageView searchButton;


    public SearchViewHolder(View root) {
        super(root);
        this.root = root;
        entry = Ui.findView(root, R.id.search_drawer_item_entry);
        searchButton = Ui.findView(root, R.id.search_drawer_item_button);
    }
}