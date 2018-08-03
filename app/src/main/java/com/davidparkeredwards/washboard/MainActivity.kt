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
import com.davidparkeredwards.washboard.R.string.orders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.read_order_layout.*
import org.threeten.bp.DayOfWeek
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val mAuth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance()
    var editMode = false
    val user = User()
    var zipInfo = ZipInfo()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        getUserInfo()

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        fab.setOnClickListener { view ->
            toggleMode()
        }




    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun logout() {
        var auth = FirebaseAuth.getInstance()
        auth.signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    fun setUpUi() {
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
        fab.setImageDrawable(resources.getDrawable(R.drawable.abc_btn_check_material))
        fab2.visibility = View.VISIBLE

        val zipView = findViewById<AutoCompleteTextView>(R.id.zip_in)
        zipView.setText(user.order.zip)
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
    }

    fun displayOrderInput(boolean: Boolean) {
        if(!boolean) {
            findViewById<LinearLayout>(R.id.order_input_layout).visibility = View.GONE
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

                /*
                radio.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        for (window in zipInfo.windows) {
                            if (window.id().contentEquals(radio.tag as String)) {
                                newOrder.window = window
                            }
                        }
                    }
                })
                */

                radio.setPadding(0, 0, 0, 20)
                radioCount = radioCount + 1
                radioGroup!!.addView(radio)
            }
            findViewById<LinearLayout>(R.id.radio_group_layout).removeAllViews()
            findViewById<LinearLayout>(R.id.radio_group_layout).addView(radioGroup)


            //New order check
            if(user.order.window.pickupDay == 100) {
                return
            }
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
        fab.setImageDrawable(resources.getDrawable(R.drawable.abc_edit_text_material))
        fab2.visibility = View.GONE

        findViewById<TextView>(R.id.pickup_time_view).text = user.order.window.description(this)
        var pauseText = ""
        if(user.order.paused) {
            pauseText = getString(R.string.paused)
        } else {
            pauseText = getString(R.string.next_pickup_is)
            var beforeOrAfter = org.threeten.bp.LocalDate.now().dayOfWeek.value -
                    DayOfWeek.values()[user.order.window.pickupDay].value
            when(beforeOrAfter) {
                0 -> pauseText = pauseText + " " + getString(R.string.today)
                in -7..-1 -> {
                    beforeOrAfter = beforeOrAfter + 7
                    val calendar = Calendar.getInstance()
                    val today = org.threeten.bp.LocalDate.now()
                    calendar.set(today.year, today.monthValue, today.dayOfWeek.value)
                    calendar.add(Calendar.DATE, beforeOrAfter)
                    pauseText = pauseText + " " + calendar.get(Calendar.DATE).toShort()
                }
                in 1..7 -> {
                    val calendar = Calendar.getInstance()
                    val today = org.threeten.bp.LocalDate.now()
                    calendar.set(today.year, today.monthValue, today.dayOfWeek.value)
                    calendar.add(Calendar.DATE, beforeOrAfter)
                    pauseText = pauseText + " " + calendar.get(Calendar.DATE).toShort()

                }
            }



        }
        findViewById<TextView>(R.id.pause_description).text = pauseText
        findViewById<TextView>(R.id.address_description).text = user.order.address
        findViewById<TextView>(R.id.pickup_instructions_description).text = user.order.pickupNotes
        findViewById<TextView>(R.id.price_description).text = "No price data"
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
        } else {
            Toast.makeText(this, "Please complete all sections", Toast.LENGTH_SHORT).show()
        }

    }

    fun validateOrder(order: Order): Boolean {
        if(order.zip == "" || order.address == "" || order.window.pickupDay == 100) {
            return false
        }
        return true
    }

    fun toggleMode() {
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
                    zipInfo.standardPrice = ((dataSnapshot.child("standard_price").value) as Long).toInt()

                    displayOrderInput(true)

                } else {
                    Toast.makeText(this@MainActivity, "Service not available for this zip code", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@MainActivity, "Unable to find zip code info", Toast.LENGTH_SHORT).show()


            }

        })
    }

    fun getUserInfo() {
        if(mAuth.currentUser == null) {
            logout()
        }
        val userRef = db.getReference("user/" + mAuth.currentUser?.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot != null
                        && dataSnapshot.exists()
                        && dataSnapshot.hasChildren()) {
                    user.emailAddress = dataSnapshot.child("email_address").toString()
                    user.stripeId = dataSnapshot.child("stripeId").toString()

                    if (dataSnapshot.hasChild("order")) {


                        val orderSnapshot = dataSnapshot.child("/order/").value as HashMap<String, Any>
                        user.order.zip = orderSnapshot.get("zip") as String
                        user.order.address = orderSnapshot.get("address") as String
                        user.order.paused = orderSnapshot.get("paused") as Boolean
                        user.order.pickupNotes = orderSnapshot.get("pickupNotes") as String
                        if (orderSnapshot.containsKey("window")) {
                            val windowSnapshot = orderSnapshot.get("window") as HashMap<String, Any>
                            Log.i("Main", "Window: " + windowSnapshot.toString())

                            user.order.window.pickupDay = (windowSnapshot.get("pickupDay") as Long).toInt()
                            user.order.window.pickupStart = (windowSnapshot.get("pickupStart") as Long).toInt()
                            user.order.window.pickupStop = (windowSnapshot.get("pickupStop") as Long).toInt()
                            user.order.window.returnDay = (windowSnapshot.get("returnDay") as Long).toInt()
                            user.order.window.returnStart = (windowSnapshot.get("returnStart") as Long).toInt()
                            user.order.window.returnStop = (windowSnapshot.get("returnStop") as Long).toInt()
                        } else {
                            user.order.window = Window()
                        }
                    } else {
                        user.order = Order()
                    }
                }
                if(user.order.window.id() == "100100100100100100") {
                    editMode = true
                } else {
                    editMode = false
                }
                setUpUi()
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            })

    }


    //Edit Order Methods

    fun pauseOrder() {

    }
}
