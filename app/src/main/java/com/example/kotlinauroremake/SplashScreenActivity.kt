package com.example.kotlinauroremake

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.layout_sign_in.*
import java.util.*
import java.util.concurrent.TimeUnit

class SplashScreenActivity : AppCompatActivity() {


    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var database: FirebaseDatabase
    private lateinit var driverInfoRef: DatabaseReference


    /**
     * Function to make user see the splashScreen before he can interact with the app
     */
    override fun onStart() {
        super.onStart()
        delaySplashScreen()
    }

    /**
     *
     */
    override fun onStop() {
        if (firebaseAuth != null && listener != null) firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }


    /**
     * Function to show SplashScreen
     */
    @SuppressLint("CheckResult")
    private fun delaySplashScreen() {
        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                firebaseAuth.addAuthStateListener(listener)
            }
    }

    /**
     * Creates the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        init()
    }

    /**
     * If the user is already log in it will take him to ... else it will take him to the log in screen
     */
    private fun init() {

        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE)

        providers = Arrays.asList(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            if (user != null)
                checkUserFromFirebase()
            else
                showLoginLayout()
        }

    }


    /**
     * Checking if user registers correctly or cancels operation
     */
    private fun checkUserFromFirebase() {
        //we access the user by the id given by firebase of the current user
        driverInfoRef
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                //if the user already exist we sent him a message otherwise we take him to the register screen
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        Toast.makeText(
                            this@SplashScreenActivity,
                            "User already register !",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        showRegisterLayout()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@SplashScreenActivity,
                        "CANCELLED",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            })
    }

    /**
     *
     */
    private fun showRegisterLayout(){


    }

    /**
     * Prepare the sign_in layout and taking user to it
     */
    private fun showLoginLayout() {

        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.layout_sign_in)
            .setPhoneButtonId(R.id.btn_phone_sign_in)
            .setGoogleButtonId(R.id.btn_google_sign_in)
            .build()

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(), Common.LOGIN_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Common.LOGIN_REQUEST_CODE) {

            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser

            } else
                Toast.makeText(
                    this@SplashScreenActivity,
                    "$response!!.error!!.message",
                    Toast.LENGTH_SHORT
                )
                    .show()
        }
    }
}