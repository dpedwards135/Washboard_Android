package com.davidparkeredwards.washboard

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import com.davidparkeredwards.washboard.R.string.orders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val mAuth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance()
    var editMode = false
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
        if(!editMode) {
            fab.setImageDrawable(resources.getDrawable(R.drawable.abc_edit_text_material))
            findViewById<FrameLayout>(R.id.read_order_container).visibility = View.VISIBLE
            findViewById<FrameLayout>(R.id.edit_order_container).visibility = View.GONE
        } else {
            fab.setImageDrawable(resources.getDrawable(R.drawable.abc_btn_check_material))
            findViewById<FrameLayout>(R.id.read_order_container).visibility = View.GONE
            findViewById<FrameLayout>(R.id.edit_order_container).visibility = View.VISIBLE
        }
    }

    fun toggleMode() {
        editMode = !editMode
        setUpUi()
    }

    //Firebase Methods

    fun getUserInfo() {
        if(mAuth.currentUser == null) {
            logout()
        }
        val userRef = db.getReference("user/" + mAuth.currentUser?.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
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
