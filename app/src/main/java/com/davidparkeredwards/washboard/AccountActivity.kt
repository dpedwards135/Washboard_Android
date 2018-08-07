package com.davidparkeredwards.washboard

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

class AccountActivity : WBNavigationActivity() {


    override fun setUpUi() {
        super.setUpUi()
        findViewById<FrameLayout>(R.id.account_info_container).visibility = View.VISIBLE
        findViewById<TextView>(R.id.email_display).setText(user.emailAddress)
        findViewById<TextView>(R.id.name_display).setText(user.name)
        findViewById<TextView>(R.id.phone_display).setText(user.phone)
    }

    override fun onDestroy() {
        findViewById<FrameLayout>(R.id.account_info_container).visibility = View.GONE
        super.onDestroy()
    }
}