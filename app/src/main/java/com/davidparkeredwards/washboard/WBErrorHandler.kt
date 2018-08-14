package com.davidparkeredwards.washboard

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.WindowManager
import java.util.ArrayList

class WBErrorHandler(context: Context, title: String?, message: String) {

    val context = context;
    val title = title;
    val message = message;

    fun show() {
        var alert = AlertDialog.Builder(context)

        if(title != null) alert.setTitle(title) else alert.setTitle(context.getString(R.string.error))
        alert.setMessage(message)
        alert.setCancelable(true);

        val a: AlertDialog = alert.create()
        a.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        a.show()
    }
}