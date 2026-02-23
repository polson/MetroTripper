package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.AbstractComposeView
import com.philsoft.metrotripper.model.Stop
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StopListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private val stopsState = mutableStateListOf<Stop>()
    private var selectedStopIdState = mutableLongStateOf(0L)

    private var onStopSearchedListener: (Long) -> Unit = {}
    private var onStopSelectedListener: (Stop) -> Unit = {}

    val stopSearchedEvent: Flow<Long> = callbackFlow {
        onStopSearchedListener = { stopId -> trySend(stopId) }
        awaitClose { onStopSearchedListener = {} }
    }

    val stopSelectedEvent: Flow<Stop> = callbackFlow {
        onStopSelectedListener = { stop -> trySend(stop) }
        awaitClose { onStopSelectedListener = {} }
    }

    @Composable
    override fun Content() {
        StopList(
            stops = stopsState,
            selectedStopId = selectedStopIdState.longValue,
            onStopSearched = onStopSearchedListener,
            onStopSelected = onStopSelectedListener
        )
    }

    fun showStops(stops: List<Stop>) {
        stopsState.clear()
        stopsState.addAll(stops)
    }

    fun selectStop(stopId: Long) {
        selectedStopIdState.longValue = stopId
    }
}
