package com.davidparkeredwards.washboard

import android.content.Context
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

class Window {


    var pickupDay = 100
    var pickupStart = 100
    var pickupStop = 100
    var returnDay = 100
    var returnStart = 100
    var returnStop = 100

    fun id(): String {
        return "" + pickupDay + pickupStart + pickupStop + returnDay + returnStart + returnStop
    }

    fun description(context: Context): String {

        val day = context.getString(R.string.pickup) + " " +
                DayOfWeek.values()[pickupDay] + ": " +
                formatHour(pickupStart, context) + " - " + formatHour(pickupStop, context) + "\n" +
                context.getString(R.string.dropoff) + " " +
                DayOfWeek.values()[returnDay] + ": " +
                formatHour(returnStart, context) + " - " + formatHour(returnStop, context)
        return day

    }

    fun formatHour(hour: Int, context: Context): String {
        if (android.text.format.DateFormat.is24HourFormat(context)) {
            return hour.toString()
        } else {
            if (hour == 0) {
                return "12 " + context.getString(R.string.am)
            } else if (hour == 12) {
                return "12 " + context.getString(R.string.pm)
            } else if (hour < 12) {
                return "" + hour + " " + context.getString(R.string.am)
            } else if (hour > 12) {
                return "" + hour + " " + context.getString(R.string.pm)
            }
        }
        return ""
    }

    fun toHashMap(): HashMap<String, Any> {
        var map = HashMap<String, Any>()

        map.put("pickupDay", pickupDay)
        map.put("pickupStart", pickupStart)
        map.put("pickupStop", pickupStop)
        map.put("returnDay", returnDay)
        map.put("returnStart", returnStart)
        map.put("returnStop", returnStop)

        return map
    }
}