package com.philsoft.metrotripper.app.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.OnMapReadyCallback
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.about.AboutDialog
import com.philsoft.metrotripper.app.ui.theme.DarkBlue
import com.philsoft.metrotripper.app.ui.theme.LightBlueStatusBar
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.viewmodel.MainUiEffect
import com.philsoft.metrotripper.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetroTripperApp(
    viewModel: MainViewModel,
    supportFragmentManager: FragmentManager,
    mapReadyCallback: OnMapReadyCallback,
    onEffect: (MainUiEffect, BottomSheetScaffoldState, CoroutineScope) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }

    // Notify ViewModel of drawer state changes
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            viewModel.onDrawerOpened()
        } else {
            viewModel.onDrawerClosed()
            if (context is android.app.Activity) {
                EZ.hideKeyboard(context)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Light blue strip behind the status bar
        Box(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(LightBlueStatusBar)
        )
        // Main content padded below the status bar
        Box(
            Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            if (showAboutDialog) {
                AboutDialog(onDismiss = { showAboutDialog = false })
            }
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        drawerShape = RectangleShape,
                        drawerContainerColor = Color(0xFF333333)
                    ) {
                        StopList(
                            stops = uiState.savedStops.values.toList(),
                            selectedStopId = uiState.selectedStop?.stopId ?: 0L,
                            onStopSearched = { viewModel.onStopSearched(it) },
                            onStopSelected = { viewModel.onStopSelectedFromDrawer(it) }
                        )
                    }
                }
            ) {
                MainScreen(
                    viewModel = viewModel,
                    supportFragmentManager = supportFragmentManager,
                    mapReadyCallback = mapReadyCallback,
                    topBar = {
                        TopAppBar(
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            title = { Text(text = stringResource(R.string.app_name), color = Color.White) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = DarkBlue,
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White,
                                actionIconContentColor = Color.White
                            ),
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(painterResource(id = R.drawable.ic_menu), contentDescription = stringResource(R.string.drawer_open))
                                }
                            },
                            actions = {
                                IconButton(onClick = { showAboutDialog = true }) {
                                    Icon(painterResource(id = R.drawable.ic_info), contentDescription = "About")
                                }
                            }
                        )
                    },
                    onEffect = { effect, scaffoldState, scaffoldScope ->
                        if (effect is MainUiEffect.CloseDrawer) {
                            scope.launch { drawerState.close() }
                        } else {
                            onEffect(effect, scaffoldState, scaffoldScope)
                        }
                    }
                )
            }
        }
    }
}

