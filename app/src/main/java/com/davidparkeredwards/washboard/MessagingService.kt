package com.davidparkeredwards.washboard

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    val db = FirebaseDatabase.getInstance()
    val mAuth = FirebaseAuth.getInstance()

    override fun onNewToken(p0: String?) {

        if(p0 == null) return

        val userRef = db.getReference("user/" + mAuth.currentUser?.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot != null
                        && dataSnapshot.exists()
                        && dataSnapshot.hasChild("firebaseToken")) {
                    Log.i("Login", "FirebaseToken updated")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                if(p0.code != null) WBErrorHandler(this@MessagingService,
                        "Error: " + p0.code.toString(), p0.message)
            }

        })
        val map = HashMap<String, String>()
        map.put("firebaseToken", p0)
        userRef.updateChildren(map as Map<String, Any>)



        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        Log.i("MessagingService", "Message received " + p0!!.notification.toString())
        if(p0 != null && p0.notification != null && p0.notification!!.body != null)
        //
           Handler(Looper.getMainLooper()).post(Runnable() {

               run {
                   Toast.makeText(this@MessagingService, "Show the message", Toast.LENGTH_SHORT).show()
                   //WBErrorHandler(this, "Message", p0.notification!!.body!!).show()
               }

           })
    }
}
