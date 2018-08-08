package com.davidparkeredwards.washboard

class User {

    var name = ""
    var emailAddress = ""
    var phone = ""
    var order = Order()
    var stripeId = ""
    var last4 = ""
    //Provider Info
    var provider = false
    var washingAddress = ""
    var windowsAvailable = ArrayList<Window>()
    var openInstanceIndex = ArrayList<String>()

    /*
    - Washing address
- Calendar base settings
- Calendar
- Social
- Instance Index - All instances forever with open, picked-up, returned, or closed status
     */
}