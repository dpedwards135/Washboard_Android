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

open class WBNavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val mAuth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance()

    var menu : Menu? = null

    val user = User()


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

            R.id.my_laundry_day -> {

                Crashlytics.setUserIdentifier("User: " + mAuth.currentUser?.uid)
                Crashlytics.log("Start Main Activity from menu")
                val mainActivity = Intent(this, MainActivity::class.java)
                startActivity(mainActivity)
            }
            R.id.account -> {

                val accountActivity = Intent(this, AccountActivity::class.java)

                startActivity(accountActivity)

            }
            R.id.order_history -> {

                val activity = Intent(this, OrderHistoryActivity::class.java)

                startActivity(activity)

            }
            R.id.provider_sign_up -> {

                val activity = Intent(this, ProviderActivity::class.java)

                startActivity(activity)
            }
            R.id.open_instance_board -> {
                val activity = Intent(this, OpenOrdersActivity::class.java)

                startActivity(activity)

            }
            R.id.manual -> {

            }
            R.id.provider_schedule -> {

            }
            R.id.provider_support -> {

            }
            R.id.help -> {

            }

            R.id.logout -> {

                logout()
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

    open fun setUpUi() {
        Log.i("WBNA", "setUpUi")
        val navHeader = findViewById<NavigationView>(R.id.nav_view)
        if(navHeader != null) {
            navHeader.getHeaderView(0).findViewById<TextView>(R.id.textView)
                    .setText("" + user.name + "\n" + user.emailAddress)
            Log.i("WBNA", "Setup navHeader")
        } else {
            Log.i("WBNA", "Setup navHeader failed")

        }
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
                    user.emailAddress = dataSnapshot.child("emailAddress").value.toString()
                    user.name = dataSnapshot.child("name").value.toString()
                    user.phone = dataSnapshot.child("phone").value.toString()
                    user.stripeId = dataSnapshot.child("stripeId").value.toString()
                    user.last4 = dataSnapshot.child("last4").value.toString()
                    if(dataSnapshot.child("provider").value != null) user.provider = dataSnapshot.child("provider").value as Boolean

                    user.washingAddress = dataSnapshot.child("washingAddress").value.toString()
                    user.openInstanceIndex = ArrayList<String>()
                    if(dataSnapshot.hasChild("openInstanceIndex")) {
                        val instanceSnap = dataSnapshot.child("openInstanceIndex") as HashMap<Long, String>
                        for(key in instanceSnap.keys) {
                            user.openInstanceIndex.add(instanceSnap.get(key)!!)
                        }
                    }
                    user.windowsAvailable = ArrayList<Window>()
                    if(dataSnapshot.hasChild("windowsAvailable")) {
                        val windowSnap = dataSnapshot.child("windowsAvailable") as HashMap<Long, HashMap<String, Any>>
                        for (key in windowSnap.keys) {
                            val xMap = windowSnap.get(key)
                            if(xMap != null) {
                                var window = Window()
                                window.pickupDay = (xMap.get("pickupDay") as Long).toInt()
                                window.pickupStart = (xMap.get("pickupStart") as Long).toInt()
                                window.pickupStop = (xMap.get("pickupStop") as Long).toInt()
                                window.returnDay = (xMap.get("returnDay") as Long).toInt()
                                window.returnStart = (xMap.get("returnStart") as Long).toInt()
                                window.returnStop = (xMap.get("returnStop") as Long).toInt()
                            }
                        }
                    }

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

                ///////////////////////////////
                if(user.order.window.id() == "100100100100100100") {
                    orderIncomplete(true)


                } else {
                    orderIncomplete(false)

                }
                setUpUi()

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

    }


    //Edit Order Methods

    open fun orderIncomplete(boolean: Boolean) {
        Log.i("WBNA", "Order is not complete: " + boolean)
    }


}