package com.davidparkeredwards.washboard

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.crashlytics.android.Crashlytics
import com.davidparkeredwards.washboard.R.string.orders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stripe.android.SourceCallback
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Source
import com.stripe.android.model.SourceParams
import com.stripe.android.model.Token
import com.stripe.android.view.CardInputWidget
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.read_order_layout.*
import org.threeten.bp.DayOfWeek
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap

class MainActivity : WBNavigationActivity() {


    var editMode = false
    var zipInfo = ZipInfo()


    override fun setUpUi() {
        super.setUpUi()
        if(!editMode && user.order.window.pickupDay != 100) {
            findViewById<FrameLayout>(R.id.read_order_container).visibility = View.VISIBLE
            findViewById<FrameLayout>(R.id.edit_order_container).visibility = View.GONE
            setupReadView()
        } else {
            findViewById<FrameLayout>(R.id.read_order_container).visibility = View.GONE
            findViewById<FrameLayout>(R.id.edit_order_container).visibility = View.VISIBLE
            setupEditView()
        }
    }

    fun setupEditView() {

        Log.i("Main", "setUpEditView")

        val zipView = findViewById<AutoCompleteTextView>(R.id.zip_in)

        zipView.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if(p0.toString().count() == 5) {
                    getZipInfo(p0.toString())
                } else {
                    displayOrderInput(false)
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        if(zipView.text.count() == 0) zipView.setText(user.order.zip)
    }

    fun displayOrderInput(boolean: Boolean) {
        if(!boolean) {
            findViewById<LinearLayout>(R.id.order_input_layout).visibility = View.GONE
            return
        }

        if(findViewById<LinearLayout>(R.id.order_input_layout).visibility == View.VISIBLE) {
            return
        }

        findViewById<LinearLayout>(R.id.order_input_layout).visibility = View.VISIBLE

        val radioGroup = RadioGroup(this)
        radioGroup.tag = "window_radios"
        if (zipInfo != null) {
            var radioCount = 0
            for (window in zipInfo!!.windows) {
                Log.i("Windows", "Add Window")
                val radio = RadioButton(this)
                radio.text = window.description(this)
                radio.tag = window.id()


                radio.setPadding(0, 0, 0, 20)
                radioCount = radioCount + 1
                radioGroup!!.addView(radio)
            }
            findViewById<LinearLayout>(R.id.radio_group_layout).removeAllViews()
            findViewById<LinearLayout>(R.id.radio_group_layout).addView(radioGroup)


            //New order check
            if(user.order.window.pickupDay == 100) {
                findViewById<Button>(R.id.cancel_button).visibility = View.GONE
                return
            }
            findViewById<Button>(R.id.cancel_button).visibility = View.VISIBLE

            findViewById<AutoCompleteTextView>(R.id.zip_in).setText(user.order.zip)
            val checkRadio = radioGroup.findViewWithTag<RadioButton>(user.order.window.id())
            if(checkRadio != null) {
                checkRadio.isChecked = true
            } else {
                Toast.makeText(this, "Your window is no longer available. Please choose another.",
                        Toast.LENGTH_SHORT).show()
            }
            findViewById<AutoCompleteTextView>(R.id.pickup_address).setText(user.order.address)
            findViewById<AutoCompleteTextView>(R.id.additional_instructions).setText(user.order.pickupNotes)
        }


    }

    fun setupReadView() {
        Log.i("Main", "setUpReadView")


        findViewById<TextView>(R.id.pickup_time_view).text = user.order.window.description(this)
        var pauseText = ""
        if(user.order.paused) {
            pauseText = getString(R.string.paused)
        } else {
            pauseText = getString(R.string.next_pickup_is)
            var beforeOrAfter = DayOfWeek.values()[user.order.window.pickupDay].value -
                    org.threeten.bp.LocalDate.now().dayOfWeek.value

            Log.i("Main", "Today: " + org.threeten.bp.LocalDate.now().dayOfWeek.value +
                    " Order: " + DayOfWeek.values()[user.order.window.pickupDay].value)
            when(beforeOrAfter) {
                0 -> pauseText = pauseText + " " + getString(R.string.today)
                in -7..-1 -> {
                    beforeOrAfter = beforeOrAfter + 7
                    pauseText = pauseText + " " + getString(R.string.`in`) + " " +
                            beforeOrAfter + " " + (if(beforeOrAfter == 1) getString(R.string.day) else getString(R.string.days))
                }
                in 1..7 -> {
                    pauseText = pauseText + " " + getString(R.string.`in`) + " " +
                            beforeOrAfter + " " + (if(beforeOrAfter == 1) getString(R.string.day) else getString(R.string.days))
                }
            }



        }
        findViewById<TextView>(R.id.pause_description).text = pauseText
        findViewById<TextView>(R.id.address_description).text = user.order.address + "\n" + user.order.zip
        findViewById<TextView>(R.id.pickup_instructions_description).text = user.order.pickupNotes
        findViewById<TextView>(R.id.price_description).text = zipInfo.priceDescription(this)
    }

    fun saveOrder(view: View) {
        var newOrder = Order()
        newOrder.zip = findViewById<AutoCompleteTextView>(R.id.zip_in).text.toString()

        var radioGroup = findViewById<LinearLayout>(R.id.radio_group_layout).findViewWithTag<RadioGroup>("window_radios")
        var checkedRadio = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
        for (window in zipInfo.windows) {
            if (window.id() == (checkedRadio.tag as String)) {
                newOrder.window = window
            }
        }
        newOrder.address = findViewById<AutoCompleteTextView>(R.id.pickup_address).text.toString()
        newOrder.pickupNotes = findViewById<AutoCompleteTextView>(R.id.additional_instructions).text.toString()

        if(validateOrder(newOrder)) {

            val userRef = db.getReference("user/" + mAuth.currentUser?.uid + "/order")
            userRef.updateChildren(newOrder.toHashMap())
            savePaymentSource()
        } else {
            Toast.makeText(this, "Please complete all sections", Toast.LENGTH_SHORT).show()
        }

    }

    fun validateOrder(order: Order): Boolean {
        if(order.zip == "" || order.address == "" || order.window.pickupDay == 100 || user.stripeId == "") {
            Log.i("MAIN", "Invalid order")
            return false
        }
        return true
    }

    fun toggleMode(view: View) {
        if(editMode) {

        }
        editMode = !editMode
        setUpUi()
    }

    //Firebase Methods

    fun getZipInfo(zip: String) {
        val dbref = FirebaseDatabase.getInstance().getReference("zip/" + zip)
        dbref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot != null
                        && dataSnapshot.exists()
                        && dataSnapshot.hasChildren()
                        && dataSnapshot.hasChild("serviced")) {



                    zipInfo = ZipInfo()

                    zipInfo.zipCode = (dataSnapshot.child("zip_code").value) as String
                    zipInfo.serviced = (dataSnapshot.child("serviced").value) as Boolean
                    var windowList = (dataSnapshot.child("windows").value) as ArrayList<HashMap<String, Any>>
                    for(windowSnapshot in windowList) {
                        var window = Window()
                        window.pickupDay = (windowSnapshot.get("pick_up_day") as Long).toInt()
                        window.pickupStart = (windowSnapshot.get("pick_up_start") as Long).toInt()
                        window.pickupStop = (windowSnapshot.get("pick_up_stop") as Long).toInt()
                        window.returnDay = (windowSnapshot.get("drop_off_day") as Long).toInt()
                        window.returnStart = (windowSnapshot.get("drop_off_start") as Long).toInt()
                        window.returnStop = (windowSnapshot.get("drop_off_stop") as Long).toInt()
                        zipInfo.windows.add(window)
                    }
                    zipInfo.standardPrice = ((dataSnapshot.child("standard_price").value) as Long).toDouble()
                    zipInfo.addOnPrice = ((dataSnapshot.child("add_on_price").value) as Long).toDouble()

                    setUpUi()
                    Log.i("Main", "Got ZipInfo")
                    if(editMode) displayOrderInput(true)

                } else {
                    Toast.makeText(this@MainActivity, "Service not available for this zip code", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@MainActivity, "Unable to find zip code info", Toast.LENGTH_SHORT).show()


            }

        })
    }

    override fun orderIncomplete(boolean: Boolean) {
        super.orderIncomplete(boolean)
        editMode = boolean
        if(zipInfo.zipCode != user.order.zip) {
            getZipInfo(user.order.zip)
        }
    }


    //Edit Order Methods

    fun pauseOrder(view: View) {
        val pausedRef = db.getReference("user/" + mAuth.currentUser?.uid + "/order/paused/")
        pausedRef.setValue(!user.order.paused)
    }

    fun savePaymentSource() {
        val mCardInputWidget = findViewById<CardInputWidget>(R.id.card_input_widget)
        val cardToSave = mCardInputWidget.getCard()

        val stripe = Stripe(this, "pk_test_ngGsAXUpi79PSFkAs2MzOAc1")
        if(cardToSave == null) {
            Toast.makeText(this, "Please complete payment information", Toast.LENGTH_SHORT).show()
        } else {

            stripe.createSource(SourceParams.createCardParams(cardToSave), object : SourceCallback {

                override fun onSuccess(source: Source?) {
               // override fun onSuccess(source: Source) {
                    if(source == null) {
                        Log.i("MAIN", "Source is null")
                        return
                    }
                    val srcIdRef = db.getReference("user/" + mAuth.currentUser?.uid + "/srcId")
                    srcIdRef.setValue(source.id)
                    Log.i("Main", "TokenId: " + source.id)
                    val last4Ref = db.getReference("user/" + mAuth.currentUser?.uid + "/last4")
                    last4Ref.setValue(cardToSave.last4)
                    Log.i("Main", "last4: " + cardToSave.last4)
                }

                override fun onError(error: java.lang.Exception?) {
                    Log.i("EOC", error.toString())
                }
            })
        }
    }
}
