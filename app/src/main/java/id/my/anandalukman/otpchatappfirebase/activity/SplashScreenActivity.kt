package id.my.anandalukman.otpchatappfirebase.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import id.my.anandalukman.otpchatappfirebase.asset.User
import id.my.anandalukman.otpchatappfirebase.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private var auth : FirebaseAuth? = null
    private var binding : ActivitySplashScreenBinding? = null
    private lateinit var handler : Handler
    private var user : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        auth = FirebaseAuth.getInstance()
        setupSplashScreen()
    }

    private fun setupSplashScreen() {
        handler = Handler()
        handler.postDelayed({
            if (auth!!.currentUser != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
                finish()
            }
            val intent = Intent(this, VerificationActivity::class.java)
            startActivity(intent)
            finishAffinity()
            finish()
        },4000) // delaying 4 seconds
    }
}