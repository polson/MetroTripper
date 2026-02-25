package com.philsoft.metrotripper.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.lifecycleScope
import com.philsoft.metrotripper.app.ui.helper.LocationHelper
import com.philsoft.metrotripper.app.ui.view.MetroTripperApp
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.viewmodel.MainUiEffect
import com.philsoft.metrotripper.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModel()
    private val locationHelper by lazy { LocationHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        locationHelper.checkLocationPermission()
        viewModel.initialize()

        setContent {
            MetroTripperApp(
                viewModel = viewModel,
                onEffect = ::handleEffect
            )
        }

        lifecycleScope.launch {
            locationHelper.locationEvents.collect {
                viewModel.onLocationUpdate(it)
            }
        }
    }

    private fun handleEffect(effect: MainUiEffect) {
        when (effect) {
            is MainUiEffect.ShowError -> EZ.showSnackbar(findViewById(android.R.id.content), effect.message)
            else -> {}
        }
    }
}
