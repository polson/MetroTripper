package com.philsoft.metrotripper.app.drawer

import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import com.philsoft.metrotripper.databinding.SearchDrawerItemBinding

class SearchViewHolder(val binding: SearchDrawerItemBinding) : RecyclerView.ViewHolder(binding.root) {
    var onSearchListener: (Long) -> Unit = {}

    init {
        binding.searchButton.setOnClickListener {
            onSearchListener(binding.entry.text.toString().toLong())
        }
        binding.entry.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onSearchListener(binding.entry.text.toString().toLong())
                true
            } else {
                false
            }
        }
    }
}