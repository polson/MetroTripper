package com.philsoft.metrotripper.app.drawer

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.inputmethod.EditorInfo
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.editorActionEvents
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.search_drawer_item.*

class SearchViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    private val searchAction = entry.editorActionEvents()
            .filter { event ->
                event.actionId() == EditorInfo.IME_ACTION_GO
            }
    val searchEvent = RxView.clicks(searchButton)
            .mergeWith(searchAction)
            .map { entry.text.toString() }
}