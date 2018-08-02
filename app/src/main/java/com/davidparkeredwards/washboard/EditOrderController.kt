package com.davidparkeredwards.washboard

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Created by davidedwards on 7/27/18.
 */
class EditOrderController(activity: AppCompatActivity) {
    /*


    var activity = activity
    var delegate = delegate

    var order = Order()
    var checkedWindowTag = ""
    var windowsFragment : WindowsFragment? = null

    //val list = ArrayList<Window>()
    var radioGroup: RadioGroup? = null

    val mAuth = FirebaseAuth.getInstance()

    var userAddress = ""
    var userCityAndState = ""
    var userZip = ""
    var orderZip = ""
    var zipInfo: ZipInfo? = null
    var orderToEdit: String = ""
    var optionsFragment : OptionsFragment? = null

    var cards = ArrayList<CardData>()

    fun setup(order: Order) {

        this.order = order
        if (this.order.zip != "") {
            orderZip = this.order.zip
        } else {
            orderZip = userZip
        }
        if(order.window != null) {
            checkedWindowTag = order.window!!.id()
        }
        //getZipData(true)


        getUserAddress()


    }


    fun setupInstructions() {
        var soiledBox = activity.findViewById<CheckBox>(R.id.soiled_check)
        var coldBox = activity.findViewById<CheckBox>(R.id.cold_check)
        var softenerBox = activity.findViewById<CheckBox>(R.id.softener_check)

        var soiledTextView = (activity.findViewById<EditText>(R.id.soiled_text))
        var coldTextView = (activity.findViewById<EditText>(R.id.cold_text))
        var softenerTextView = (activity.findViewById<EditText>(R.id.softener_text))

        soiledBox.setOnCheckedChangeListener { soiledBox, isChecked ->
            Log.i("SETUP", "Soiled Box is " + isChecked)
            if (isChecked) soiledTextView.visibility = View.VISIBLE else soiledTextView.visibility = View.GONE
        }

        coldBox.setOnCheckedChangeListener { coldBox, isChecked ->

            if (isChecked) coldTextView.visibility = View.VISIBLE else coldTextView.visibility = View.GONE
        }

        softenerBox.setOnCheckedChangeListener { softenerBox, isChecked ->

            if (isChecked) softenerTextView.visibility = View.VISIBLE else softenerTextView.visibility = View.GONE
        }


        if (order.soiled) {
            soiledBox.isChecked = true
            soiledTextView.setText(order.soiledNote)
        }
        if (order.cold) {
            coldBox.isChecked = true
            coldTextView.setText(order.coldNote)
        }
        if (order.softener) {
            softenerBox.isChecked = true
            softenerTextView.setText(order.softenerNote)
        }

        if (orderZip != null && orderZip != "") {
            (activity.findViewById<TextView>(R.id.pickup_zip)).setText(orderZip)
        }
        if (order.address != "" && order.zip == orderZip) {
            (activity.findViewById<AutoCompleteTextView>(R.id.pickup_address)).setText(order.address)
            (activity.findViewById<AutoCompleteTextView>(R.id.pickup_city)).setText(order.cityAndState)

        } else if (userZip == orderZip) {
            (activity.findViewById<AutoCompleteTextView>(R.id.pickup_address)).setText(userAddress)
            (activity.findViewById<AutoCompleteTextView>(R.id.pickup_city)).setText(userCityAndState)
        }


    }

    fun setupOptions() {
        //val group = activity.findViewById<RadioGroup>(R.id.option_radios)
        Log.i("EOC", "Setup Options" + orderZip)
        if(optionsFragment == null || optionsFragment!!.view == null) return
        optionsFragment!!.view!!.findViewById<EditText>(R.id.zip_edittext).setText(orderZip)
        optionsFragment!!.view!!.findViewById<Button>(R.id.update_button).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                orderZip = (activity.findViewById<EditText>(R.id.zip_edittext)).text.toString()
                getZipData(true) //FIX
                Log.i("EOC", "button click")

                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (activity.currentFocus != null) {
                    imm.hideSoftInputFromWindow(activity.currentFocus.getWindowToken(), 0)
                }


            }
        })

        Log.i("EOC", "1")
        if (zipInfo == null || zipInfo?.serviced != true) {
            activity.findViewById<RadioGroup>(R.id.option_radios).visibility = View.GONE
            activity.findViewById<TextView>(R.id.options_intro).setText(activity.getString(R.string.service_unavailable_zip))
            return
        } else {
            activity.findViewById<RadioGroup>(R.id.option_radios).visibility = View.VISIBLE
            activity.findViewById<TextView>(R.id.options_intro).setText(activity.getString(R.string.options_intro))

        }
        Log.i("EOC", "2")

        activity.findViewById<RadioButton>(R.id.single_option)
                .setText("$" + zipInfo!!.singlePrice + " " + activity.getString(R.string.option1))
        activity.findViewById<RadioButton>(R.id.standard_option)
                .setText("$" + zipInfo!!.standardPrice + " " + activity.getString(R.string.option2))
        activity.findViewById<RadioButton>(R.id.enterprise_option)
                .setText("$" + zipInfo!!.enterprisePrice + " " + activity.getString(R.string.option3))

        Log.i("EOC", "3")

        when (order.orderType) {
            "SINGLE" -> activity.findViewById<RadioButton>(R.id.single_option).isChecked = true
            "STANDARD" -> activity.findViewById<RadioButton>(R.id.standard_option).isChecked = true
            "ENTERPRISE" -> activity.findViewById<RadioButton>(R.id.enterprise_option).isChecked = true
            else -> { // Note the block
                activity.findViewById<RadioButton>(R.id.standard_option).isChecked = true
            }

        }

        Log.i("EOC", "4")
        if (zipInfo!!.serviced) {
            delegate.onRadioClick()
        }

    }

    fun saveOrder() {
        /*
        val progressBar = ProgressBar(activity)
        if(activity is SetupActivity) {
            val progressBar = activity.findViewById<ProgressBar>(R.id.save_order_progress_bar)

            progressBar.animate()
            progressBar.visibility = View.VISIBLE
        } */


        val database = FirebaseDatabase.getInstance()
        val dbref = database.getReference("user/" + mAuth.currentUser?.uid + "/orders")
        dbref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null
                        && dataSnapshot.exists()) {
                    if (order.id != null
                            && order.id != ""
                            && dataSnapshot.hasChildren()
                            && dataSnapshot.hasChild(order.id)) {
                        val dbOrder = order.toDb()
                        database.getReference("user/" + mAuth.currentUser?.uid + "/orders/" + order.id).updateChildren(dbOrder)
                        Log.i("SETUP", "UpdateChildren")
                    } else {
                        order.id = dbref.push().key
                        val dbOrder = order.toDb()
                        database.getReference("user/" + mAuth.currentUser?.uid + "/orders/" + order.id).setValue(dbOrder)
                        Log.i("SETUP", "SetValue 1")
                    }
                    /*
                    if(activity is SetupActivity) {
                        progressBar.visibility = View.GONE
                    } */
                } else {
                    order.id = dbref.push().key
                    val dbOrder = order.toDb()
                    database.getReference("user/" + mAuth.currentUser?.uid + "/orders/" + order.id).setValue(dbOrder)
                    /* if(activity is SetupActivity) {
                        progressBar.visibility = View.GONE
                    } */
                    Log.i("SETUP", "SetValue 2")
                }
            }

            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    fun getUserAddress() {

        val mAuth = FirebaseAuth.getInstance()
        val dbref = FirebaseDatabase.getInstance().getReference("user/" + mAuth.currentUser?.uid)
        dbref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null
                        && dataSnapshot.exists()
                        && dataSnapshot.hasChildren()
                        && dataSnapshot.hasChild("street_address")) {
                    userAddress = dataSnapshot.child("street_address").value as String
                    userCityAndState = dataSnapshot.child("city_state").value as String
                    userZip = dataSnapshot.child("zip").value as String
                    if (orderZip == "") orderZip = userZip
                    getZipData(true)

                    Log.i("SETUP", "Get Address")
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        })
    }

    fun getZipData(changingZip: Boolean) {
        Log.i("EOC", "ZipData")

        val dbref = FirebaseDatabase.getInstance().getReference("zip/" + orderZip)
        dbref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null
                        && dataSnapshot.exists()
                        && dataSnapshot.hasChildren()
                        && dataSnapshot.hasChild("serviced")) {

                    zipInfo = ZipInfo().fromDb(dataSnapshot, activity)

                    if (changingZip) {
                        setupOptions()
                        setupWindows()
                    }

                    Log.i("SETUP", "Get Address")
                } else {
                    Toast.makeText(activity, "Service not available for this zip code", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        })
    }

    fun setupWindows() {
        windowsFragment = WindowsFragment()
        radioGroup = RadioGroup(activity)

        if (zipInfo != null) {
            var radioCount = 0
            for (window in zipInfo!!.windows) {
                Log.i("Windows", "Add Window")
                val radio = RadioButton(activity)
                radio.text = window.toText()
                radio.tag = window.id()

                radio.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        delegate.onRadioClick()
                    }
                })

                radio.setPadding(0, 0, 0, 20)
                radioCount = radioCount + 1
                radioGroup!!.addView(radio)

                if (window.id() == checkedWindowTag) {
                    radio.isChecked = true
                    delegate.onRadioClick()
                }

            }





            windowsFragment?.radioGroup = radioGroup!!

            Log.i("EOC", "change windows 2")
            if(windowsFragment != null && activity != null && windowsFragment!!.view != null) {
                Log.i("EOC", "change windows 1")
                activity!!.findViewById<LinearLayout>(R.id.radio_group_layout).removeAllViews()
                activity!!.findViewById<LinearLayout>(R.id.radio_group_layout).addView(radioGroup)
            }

        }
    }

        fun updateFBZipCodes() {
            val zips = ArrayList<String>()
            zips.add("84111")
            zips.add("84101")
            zips.add("84105")
            zips.add("84112")
            zips.add("84103")
            zips.add("84102")
            //Set ZipData
            for (zip in zips) {
                var zipInfo = ZipInfo()
                zipInfo.zipCode = zip
                zipInfo.serviced = true
                zipInfo.singlePrice = 25
                zipInfo.enterprisePrice = 18
                zipInfo.standardPrice = 20
                zipInfo.windows = ArrayList<Window>() //Add windows here

                var dbZipInfo = zipInfo.toDb()

                FirebaseDatabase.getInstance().getReference("zip/" + zipInfo.zipCode).setValue(dbZipInfo)
            }
        }

        fun setOrder() {
            val string = "user/" + mAuth.currentUser!!.uid + "/orders/" + orderToEdit
            Log.i("MAIN", "String: " + string)
            val dbref = FirebaseDatabase.getInstance().getReference(string)
            dbref.addValueEventListener((object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    Log.i("MAIN", "Getting orders")
                    if (dataSnapshot != null) {
                        Log.i("MAIN", "Getting orders 2")
                        //orders = Order.ordersFromDb(dataSnapshot.value as HashMap<String, Any>, this@MainActivity)
                        order = Order().fromDb(dataSnapshot.value as HashMap<String, Any>, activity)
                        orderZip = order.zip

                    } else {

                    }
                }

                override fun onCancelled(p0: DatabaseError?) {

                }

            }))
        }


        fun saveOrderType() {
            val group = activity.findViewById<RadioGroup>(R.id.option_radios)
            when (group.checkedRadioButtonId) {
                R.id.single_option -> order.orderType = "SINGLE"
                R.id.standard_option -> order.orderType = "STANDARD"
                R.id.enterprise_option -> order.orderType = "ENTERPRISE"
                else -> {
                    // Note the block
                    order.orderType = "INCOMPLETE"
                }
            }
        }

        fun saveWindowInfo() {
            if (activity.findViewById<RadioButton>(radioGroup!!.checkedRadioButtonId) != null) {
                val checkRadio = activity.findViewById<RadioButton>(radioGroup!!.checkedRadioButtonId)
                checkedWindowTag = checkRadio.tag as String

                for (window in zipInfo!!.windows) {
                    if (window.id() == checkedWindowTag) {
                        order.window = window
                    }
                }
            }
        }

        fun saveInstructionInfo() {
            order.soiled = (activity.findViewById<CheckBox>(R.id.soiled_check).isChecked)
            if (order.soiled) {
                order.soiledNote = (activity.findViewById<EditText>(R.id.soiled_text)).text.toString()
            } else {
                order.soiledNote = ""
            }

            order.cold = (activity.findViewById<CheckBox>(R.id.cold_check).isChecked)
            if (order.cold) {
                order.coldNote = (activity.findViewById<EditText>(R.id.cold_text)).text.toString()
            } else {
                order.coldNote = ""
            }

            order.softener = (activity.findViewById<CheckBox>(R.id.softener_check).isChecked)
            if (order.softener) {
                order.softenerNote = (activity.findViewById<EditText>(R.id.softener_text)).text.toString()
            } else {
                order.softenerNote = ""
            }
            order.notes = (activity.findViewById<EditText>(R.id.additional_instructions)).text.toString()

            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (activity.currentFocus != null) {
                imm.hideSoftInputFromWindow(activity.currentFocus.getWindowToken(), 0)
            }

            order.address = (activity.findViewById<EditText>(R.id.pickup_address)).text.toString()
            order.cityAndState = (activity.findViewById<EditText>(R.id.pickup_city)).text.toString()
            order.zip = orderZip
        }

        fun getFragment(stage: Int): Fragment { //leave fab and toolbar stuff in setupActivity
            var fragment = android.support.v4.app.Fragment()
            when (stage) {
                0 -> {
                    fragment = IntroFragment()

                }
                1 -> {
                    optionsFragment = OptionsFragment()
                    fragment = optionsFragment!!


                }
                2 -> {
                    setupWindows()
                    if(windowsFragment != null) fragment = windowsFragment!!


                }
                3 -> {
                    fragment = InstructionsFragment()


                }
                4 -> {

                    fragment = PaymentFragment()

                }
                5 -> {
                    fragment = ConfirmationFragment()
                }
            }

            return fragment
        }

    fun saveFromSingleView() {
        saveOrderType()
        saveWindowInfo()
        saveInstructionInfo()
        saveOrder()
    }


    fun getCard() {

        val cardData = CardData()

        val mCardInputWidget = activity.findViewById<CardInputWidget>(R.id.card_input_widget)
        val cardToSave = mCardInputWidget.getCard()
        if (cardToSave == null) {
            Log.i("EOC", "Invalid Card Data")
            return
        }
        if(!cardToSave.validateCard()) return

        cardData.cardType = cardToSave.brand
        cardData.lastFour = cardToSave.last4
        if(cardToSave.expMonth != null) cardData.expMonth = cardToSave.expMonth!!
        cardData.expYear = cardToSave.expYear

        val stripe = Stripe(activity, "pk_test_ngGsAXUpi79PSFkAs2MzOAc1")

        stripe.createToken(cardToSave, object: TokenCallback{
            override fun onSuccess(token: Token?) {
                cardData.token = token
            }

            override fun onError(error: java.lang.Exception?) {
                Log.i("EOC", error.toString())
            }
        })


    }

    //Check if user has a card in the system and give them option to charge that one or add a new one

    fun getCardData() {
        val database = FirebaseDatabase.getInstance()
        val dbref = database.getReference("user/" + mAuth.currentUser?.uid + "/cards/")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null
                        && dataSnapshot.exists()
                        && dataSnapshot.hasChildren()) {

                    cards = ArrayList<CardData>()

                    for (child in dataSnapshot.children) {
                        val card = child.value as CardData
                        if (card.valid == 1) {
                            cards.add(card)
                        }
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
    })
    }

    fun saveCardData(cardData: CardData) {
        val database = FirebaseDatabase.getInstance()
        val dbref = database.getReference("user/" + mAuth.currentUser?.uid + "/cards/")
        cardData.id = dbref.push().key
        val newCardRef = dbref.child(cardData.id)
        newCardRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null
                        && dataSnapshot.exists()
                        && dataSnapshot.hasChild("valid")) {

                    if(dataSnapshot.child("valid").value == 1) {
                        continueToConfirmationPage
                    } else if(dataSnapshot.child("valid").value == 100) {
                        Log.i("EOC", "Unable to authorize card")
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        dbref.child(cardData.id).setValue(cardData)
    }
*/

}


