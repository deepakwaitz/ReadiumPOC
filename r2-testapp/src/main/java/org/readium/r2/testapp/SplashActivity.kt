package org.readium.r2.testapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import org.readium.r2.testapp.dashboard.MainActivity
import org.readium.r2.testapp.library.LibraryActivity

class SplashActivity : AppCompatActivity() {

    // This is the loading time of the splash screen
    private val SPLASH_TIME_OUT:Long = 3000 // 1 sec
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity

            startActivity(Intent(this,LibraryActivity::class.java))

            // close this activity
            finish()
        }, SPLASH_TIME_OUT)
    }
}