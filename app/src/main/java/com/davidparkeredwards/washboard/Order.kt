package com.davidparkeredwards.washboard

class Order {

    var address = ""
    var zip = ""
    var pickupNotes = ""
    var paused = false
    var window = Window()

    fun toHashMap() : HashMap<String, Any> {
        var map = HashMap<String, Any>()

        map.put("address", address)
        map.put("zip", zip)
        map.put("pickupNotes", pickupNotes)
        map.put("paused", paused)
        map.put("window", window.toHashMap())

        return map
    }
}