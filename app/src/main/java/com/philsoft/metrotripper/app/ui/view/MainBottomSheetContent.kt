package com.philsoft.metrotripper.app.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.philsoft.metrotripper.viewmodel.MainUiState

@Composable
internal fun MainBottomSheetContent(
    uiState: MainUiState,
    onScheduleClick: () -> Unit,
    onLocationClick: () -> Unit,
    onSaveClick: () -> Unit,
    onHeaderClick: () -> Unit
) {
    if (uiState.selectedStop == null) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        StopHeading(
            stop = uiState.selectedStop,
            isSaved = uiState.isSelectedStopSaved,
            isLoading = uiState.isLoadingTrips,
            onScheduleClick = onScheduleClick,
            onLocationClick = onLocationClick,
            onSaveClick = onSaveClick,
            onHeaderClick = onHeaderClick
        )
        TripList(trips = uiState.trips)
    }
}
