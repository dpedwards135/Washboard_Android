package com.davidparkeredwards.washboard

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.app.NotificationCompat
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
        Log.i("MessagingService", "Message received ")

        if(p0 == null && p0?.notification == null) return



        val message = p0.notification?.body ?: "Blank body"
        val title = p0.notification?.title ?: "Blank Title"

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java), 0)
        val builder = NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVibrate(longArrayOf(1000, 1000))
                .setAutoCancel(true)

        builder.setContentIntent(intent)
        notificationManager.notify(1, builder.build())
    }
}
