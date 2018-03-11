package com.philsoft.metrotripper.app.state

sealed class AppEvent {
    object ShowStops : AppEvent()
    object ShowSchedule : AppEvent()
    object ShowCurrentStopLocation : AppEvent()
    class SearchStop(val stopId: Long) : AppEvent()
}