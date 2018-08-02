package com.davidparkeredwards.washboard

class Window {


    var pickupDay = 100
    var pickupStart = 100
    var pickupStop = 100
    var returnDay = 100
    var returnStart = 100
    var returnStop = 100

    fun id() : String {
        return "" + pickupDay + pickupStart + pickupStop + returnDay + returnStart + returnStop
    }
}