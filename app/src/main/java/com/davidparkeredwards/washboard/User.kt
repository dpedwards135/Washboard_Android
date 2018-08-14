package com.davidparkeredwards.washboard

class User {

    var name = ""
    var emailAddress = ""
    var phone = ""
    var order = Order()
    var stripeId = ""
    var last4 = ""
    var firebaseToken = ""
    //Provider Info
    var provider = false
    var washingAddress = ""
    var windowsAvailable = ArrayList<Window>()
    var orderInstanceIndex = HashMap<String, Int>()


    /*
    - Washing address
- Calendar base settings
- Calendar
- Social
- Instance Index - All instances forever with open, picked-up, returned, or closed status
     */
}