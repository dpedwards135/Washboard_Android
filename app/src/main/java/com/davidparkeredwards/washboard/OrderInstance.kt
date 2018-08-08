package com.davidparkeredwards.washboard

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter


class OrderInstance {

    var id = ""
    var order = Order()
    var date = ""
    var status = 100 //0 = Open, 1 = Picked up, 2 = Returned, 3 = Closed, 100 = Invalid

    fun date() : LocalDate? {
        if(date != "") {
            return LocalDate.parse(date, formatter)
        }
        else return null
    }

    val formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");


}