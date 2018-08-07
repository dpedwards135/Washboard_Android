package com.davidparkeredwards.washboard

import android.content.Context

class ZipInfo() {

    var zipCode = ""
    var serviced = false
    var windows = ArrayList<Window>()
    var standardPrice = 0.0
    var addOnPrice = 0.0

    fun priceDescription(context: Context) : String {
        var string = context.getString(R.string.pricing) + "\n" + context.getString(R.string.standard) +
                " " + standardPrice + "\n" + context.getString(R.string.add_on) + " " + addOnPrice
        return string
    }
}