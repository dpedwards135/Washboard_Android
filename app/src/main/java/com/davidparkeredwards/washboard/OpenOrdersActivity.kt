package com.davidparkeredwards.washboard

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OpenOrdersActivity : WBNavigationActivity() {

    private var mRecyclerView: RecyclerView? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var recyclerAdapter: RecyclerAdapter? = null
    private var list = ArrayList<OrderInstance>()


    override fun setUpUi() {
        findViewById<FrameLayout>(R.id.provider_open_orders_container).visibility = View.VISIBLE
        getOpenOrders()
        newRecyclerView()
    }

    fun getOpenOrders() {

        list = ArrayList<OrderInstance>() //Change to <OrderInstance> which will be an immutable copy of Order, plus a date

        if(user.openInstanceIndex.isEmpty()) {
            var orderInstance = OrderInstance()
            orderInstance.order = user.order
            orderInstance.date = "This Date"
            orderInstance.status = 0
            orderInstance.id = "101010101010"
            list.add(orderInstance)
            if(recyclerAdapter != null) recyclerAdapter!!.notifyDataSetChanged()
        }
        for (instanceId in user.openInstanceIndex) {
            val dbref = db.getReference("order_instances")
            dbref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot != null
                            && dataSnapshot.exists()
                            && dataSnapshot.hasChildren()
                            && dataSnapshot.hasChild(instanceId)) {

                        val instanceSnap = dataSnapshot.child(instanceId).value as HashMap<String, Any>

                        var orderInstance = OrderInstance()
                        orderInstance.id = instanceId
                        orderInstance.date = instanceSnap.get("date") as String
                        orderInstance.status = (instanceSnap.get("status") as Long).toInt()

                        val orderSnapshot = dataSnapshot.child("/order/").value as HashMap<String, Any>
                        orderInstance.order.zip = orderSnapshot.get("zip") as String
                        orderInstance.order.address = orderSnapshot.get("address") as String
                        orderInstance.order.paused = orderSnapshot.get("paused") as Boolean
                        orderInstance.order.pickupNotes = orderSnapshot.get("pickupNotes") as String
                        if (orderSnapshot.containsKey("window")) {
                            val windowSnapshot = orderSnapshot.get("window") as HashMap<String, Any>
                            Log.i("Main", "Window: " + windowSnapshot.toString())

                            orderInstance.order.window.pickupDay = (windowSnapshot.get("pickupDay") as Long).toInt()
                            orderInstance.order.window.pickupStart = (windowSnapshot.get("pickupStart") as Long).toInt()
                            orderInstance.order.window.pickupStop = (windowSnapshot.get("pickupStop") as Long).toInt()
                            orderInstance.order.window.returnDay = (windowSnapshot.get("returnDay") as Long).toInt()
                            orderInstance.order.window.returnStart = (windowSnapshot.get("returnStart") as Long).toInt()
                            orderInstance.order.window.returnStop = (windowSnapshot.get("returnStop") as Long).toInt()
                        }
                        list.add(orderInstance)

                        if(recyclerAdapter != null) recyclerAdapter!!.notifyDataSetChanged()

                    }


                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }


            })
        }
    }

    fun newRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_view) as RecyclerView
        mLinearLayoutManager = LinearLayoutManager(this)
        recyclerAdapter = RecyclerAdapter(this, list)
        val view = mRecyclerView
        if(view != null) {
            Log.i("Recycler", "View not null")
            view.layoutManager = mLinearLayoutManager
            view.adapter = recyclerAdapter
            mRecyclerView = view
        } else {
            Log.i("Recycler", "View null")
        }


    }


}