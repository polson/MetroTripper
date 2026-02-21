package com.philsoft.metrotripper.app.drawer

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.inputmethod.EditorInfo
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.editorActionEvents
import com.philsoft.metrotripper.databinding.SearchDrawerItemBinding

class SearchViewHolder(val binding: SearchDrawerItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val searchAction = binding.entry.editorActionEvents()
            .filter { event ->
                event.actionId() == EditorInfo.IME_ACTION_GO
            }
    val searchEvent = RxView.clicks(binding.searchButton)
            .mergeWith(searchAction)
            .map { binding.entry.text.toString().toLong() }!!
}