package com.davidparkeredwards.washboard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo

import java.util.ArrayList
import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.support.annotation.NonNull
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.WindowManager
import android.widget.*
import com.crashlytics.android.Crashlytics
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult

import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via email/password.
 */

    class LoginActivity : AppCompatActivity(), LoaderCallbacks<Cursor> {
        /**
         * Keep track of the login task to ensure we can cancel it if requested.
         */

        private lateinit var mAuth: FirebaseAuth
        private lateinit var database: FirebaseDatabase
        private lateinit var dbref: DatabaseReference
        var usernameText = ""
        var nameText = ""
        var passwordText = ""
        lateinit var createAccountView: View


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)
            if (supportActionBar != null) {
                supportActionBar!!.hide()
            }

            mAuth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance()
            dbref = database.reference

            if (mAuth.currentUser != null) {
                getUserInfo()
            }


            // Set up the login form.
            populateAutoComplete()
            password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin()
                    return@OnEditorActionListener true
                }
                false
            })

            email_sign_in_button.setOnClickListener { attemptLogin() }
            findViewById<Button>(R.id.forgot_password_button).setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    resetPassword()
                }
            })
        }


        override fun onStart() {
            super.onStart()
            var currentUser = mAuth.currentUser
            if (currentUser != null) {

            }
        }

        private fun populateAutoComplete() {
            if (!mayRequestContacts()) {
                return
            }

            loaderManager.initLoader(0, null, this)
        }

        private fun mayRequestContacts(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true
            }
            if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                return true
            }
            if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
                Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok,
                                { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) })
            } else {
                requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
            }
            return false
        }

        /**
         * Callback received when a permissions request has been completed.
         */
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                                grantResults: IntArray) {
            if (requestCode == REQUEST_READ_CONTACTS) {
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    populateAutoComplete()
                }
            }
        }


        /**
         * Attempts to sign in or register the account specified by the login form.
         * If there are form errors (invalid email, missing fields, etc.), the
         * errors are presented and no actual login attempt is made.
         */
        private fun attemptLogin() {


            // Reset errors.
            email.error = null
            password.error = null

            // Store values at the time of the login attempt.
            val emailStr = email.text.toString()
            val passwordStr = password.text.toString()

            var cancel = false
            var focusView: View? = null

            // Check for a valid password, if the user entered one.
            if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
                password.error = getString(R.string.error_invalid_password)
                focusView = password
                cancel = true
            }

            // Check for a valid email address.
            if (TextUtils.isEmpty(emailStr)) {
                email.error = getString(R.string.error_field_required)
                focusView = email
                cancel = true
            } else if (!isEmailValid(emailStr)) {
                email.error = getString(R.string.error_invalid_email)
                focusView = email
                cancel = true
            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView?.requestFocus()
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true)
                loginUser()
            }
        }

        private fun isEmailValid(email: String): Boolean {
            //TODO: Replace this with your own logic
            return email.contains("@")
        }

        private fun isPasswordValid(password: String): Boolean {
            //TODO: Replace this with your own logic
            return password.length > 4
        }

        /**
         * Shows the progress UI and hides the login form.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        private fun showProgress(show: Boolean) {
            // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
            // for very easy animations. If available, use these APIs to fade-in
            // the progress spinner.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

                login_form.visibility = if (show) View.GONE else View.VISIBLE
                login_form.animate()
                        .setDuration(shortAnimTime)
                        .alpha((if (show) 0 else 1).toFloat())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                login_form.visibility = if (show) View.GONE else View.VISIBLE
                            }
                        })

                login_progress.visibility = if (show) View.VISIBLE else View.GONE
                login_progress.animate()
                        .setDuration(shortAnimTime)
                        .alpha((if (show) 1 else 0).toFloat())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                login_progress.visibility = if (show) View.VISIBLE else View.GONE
                            }
                        })
            } else {
                // The ViewPropertyAnimator APIs are not available, so simply show
                // and hide the relevant UI components.
                login_progress.visibility = if (show) View.VISIBLE else View.GONE
                login_form.visibility = if (show) View.GONE else View.VISIBLE
            }
        }

        override fun onCreateLoader(p0: Int, p1: Bundle?): Loader<Cursor> {
            return CursorLoader(this,
                    // Retrieve data rows for the device user's 'profile' contact.
                    Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                            ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                    // Select only email addresses.
                    ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(ContactsContract.CommonDataKinds.Email
                    .CONTENT_ITEM_TYPE),

                    // Show primary email addresses first. Note that there won't be
                    // a primary email address if the user hasn't specified one.
                    ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
        }

        override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
            val emails = ArrayList<String>()
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                emails.add(cursor.getString(ProfileQuery.ADDRESS))
                cursor.moveToNext()
            }

            addEmailsToAutoComplete(emails)
        }

        override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

        }

        private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
            //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
            val adapter = ArrayAdapter(this@LoginActivity,
                    android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

            email.setAdapter(adapter)
        }

        object ProfileQuery {
            val PROJECTION = arrayOf(
                    ContactsContract.CommonDataKinds.Email.ADDRESS,
                    ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
            val ADDRESS = 0
            val IS_PRIMARY = 1
        }

        /**
         * Represents an asynchronous login/registration task used to authenticate
         * the user.
         */


        companion object {

            /**
             * Id to identity READ_CONTACTS permission request.
             */
            private val REQUEST_READ_CONTACTS = 0

            /**
             * A dummy authentication store containing known user names and passwords.
             * TODO: remove after connecting to a real authentication system.
             */
            private val DUMMY_CREDENTIALS = arrayOf("foo@example.com:hello", "bar@example.com:world")
        }


        fun createAccount(view: View) {

            var alert = AlertDialog.Builder(this)

            val layoutInflater = layoutInflater
            createAccountView = layoutInflater.inflate(R.layout.create_account_dialog, null)
            alert.setTitle("Create Account")
            alert.setView(createAccountView)
            alert.setCancelable(true)

            val a: AlertDialog = alert.create()
            a.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            a.show()
            createAccountView.findViewById<Button>(R.id.go_create_account).setOnClickListener {
                signUpUser()
            }

        }

        fun signUpUser() {

            val usernameField = createAccountView.findViewById<AutoCompleteTextView>(R.id.email_in)
            usernameText = usernameField.text.toString()
            val nameField = createAccountView.findViewById<AutoCompleteTextView>(R.id.name_in)
            nameText = nameField.text.toString()
            val passwordField = createAccountView.findViewById<AutoCompleteTextView>(R.id.password_in)
            passwordText = passwordField.text.toString()

            mAuth.createUserWithEmailAndPassword(usernameText, passwordText)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Login", "createUserWithEmail:success")
                            val user = mAuth.currentUser
                            Toast.makeText(this, "Welcome to Washboard!", Toast.LENGTH_SHORT)

                            var profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(usernameText).build()
                            user?.updateProfile(profileUpdates)

                            saveUserInfo()

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Login", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            var message = getString(R.string.unable_to_create_new_user)
                            if(task.exception != null) message = task.exception!!.localizedMessage
                            WBErrorHandler(this, getString(R.string.error), message).show()
                        }
                    }


        }

        fun saveUserInfo() {
            val username = createAccountView.findViewById<AutoCompleteTextView>(R.id.email_in)
            val name = createAccountView.findViewById<AutoCompleteTextView>(R.id.name_in)

            val phone = createAccountView.findViewById<AutoCompleteTextView>(R.id.phone_in)

            val userRef = database.getReference("user/" + mAuth.currentUser?.uid)





            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot != null
                            && dataSnapshot.exists()
                            && dataSnapshot.hasChild("emailAddress")) {
                        Log.i("Login", "User is created")
                        startMainActivity()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    if(p0.code != null) WBErrorHandler(this@LoginActivity,
                            "Error: " + p0.code.toString(), p0.message)

                }

            })

            val user = User()
            user.emailAddress = username.text.toString()
            user.name = name.text.toString()
            user.phone = phone.text.toString()

            FirebaseInstanceId.getInstance().instanceId.
                    addOnCompleteListener(object: OnCompleteListener<InstanceIdResult> {
                        override fun onComplete(p0: Task<InstanceIdResult>) {
                            if(!p0.isSuccessful) {
                                Log.i("Create User", "getInstanceId failed")
                                return
                            }

                            val token = p0.getResult().token
                            user.firebaseToken = token
                            userRef.setValue(user)
                        }

                    })




        }

        fun loginUser() {

            val username = findViewById<EditText>(R.id.email)
            val password = findViewById<EditText>(R.id.password)
            mAuth.signInWithEmailAndPassword(username.text.toString(), password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Login", "signInWithEmail:success")
                            val user = mAuth.currentUser
                            getUserInfo()
                            Toast.makeText(this, "Welcome to Washboard!", Toast.LENGTH_SHORT)
                            startMainActivity()
                        } else {
                            // If sign in fails, display a message to the user.
                            var message = getString(R.string.unable_to_sign_in)
                            if(task.exception != null) message = task.exception!!.localizedMessage
                            WBErrorHandler(this, getString(R.string.error), message).show()
                        }

                        // ...
                    }
        }

        fun getUserInfo() {
            val userRef = database.getReference("user/" + mAuth.currentUser?.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot != null
                            && dataSnapshot.exists()
                            && dataSnapshot.hasChildren()) {
                        startMainActivity()
                    } else {
                        WBErrorHandler(this@LoginActivity, getString(R.string.error),
                                getString(R.string.user_not_found)).show()
                    }
                }


                override fun onCancelled(p0: DatabaseError) {

                    WBErrorHandler(this@LoginActivity, getString(R.string.error),
                            getString(R.string.user_not_found)).show()
                }
            })
        }

        fun startMainActivity() {

            val loggedInRef = database.getReference("user/" + mAuth.currentUser?.uid + "/loggedIn")
            loggedInRef.setValue(true)

            Crashlytics.setUserIdentifier("User: " + mAuth.currentUser?.uid)
            Crashlytics.log("Start Main Activity")
            val mainActivity = Intent(this, MainActivity::class.java)
            mainActivity.putExtra("login_start", true)
            //mainActivity.extras.putBoolean("login_start", true)
            startActivity(mainActivity)
        }


        fun resetPassword() {
            var alert = AlertDialog.Builder(this)

            val layoutInflater = layoutInflater
            val resetPasswordView = layoutInflater.inflate(R.layout.reset_password_dialog, null)

            alert.setView(resetPasswordView)
            alert.setCancelable(true)

            val a: AlertDialog = alert.create()
            a.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            a.show()
            resetPasswordView.findViewById<Button>(R.id.reset_button).setOnClickListener {
                var emailAddress = resetPasswordView.findViewById<EditText>(R.id.reset_email_edittext).text.toString()

                mAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        if (p0.isSuccessful) {
                            a.dismiss()
                            WBErrorHandler(this@LoginActivity, getString(R.string.reset_email_sent),
                                    "").show()

                        } else {
                            WBErrorHandler(this@LoginActivity, getString(R.string.error),
                                    getString(R.string.account_not_found)).show()
                        }
                    }
                })
            }
            resetPasswordView.findViewById<Button>(R.id.cancel_password_reset).setOnClickListener({
                a.dismiss()
            })
        }


    }
