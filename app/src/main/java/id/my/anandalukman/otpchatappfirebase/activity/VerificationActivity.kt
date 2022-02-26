package id.my.anandalukman.otpchatappfirebase.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant
import id.my.anandalukman.otpchatappfirebase.databinding.ActivityVerificationBinding

class VerificationActivity : AppCompatActivity() {

    private var binding : ActivityVerificationBinding? = null
    private var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        auth = FirebaseAuth.getInstance()

        if (auth!!.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
            finish()
        }
        supportActionBar?.hide()
        binding?.editNumber?.requestFocus()
        binding?.continueBtn?.setOnClickListener {
            val intent = Intent(this, OTPActivity::class.java)
            intent.putExtra(ChatConstant.PHONE_NUMBER, binding?.editNumber?.text.toString())
            startActivity(intent)
            finishAffinity()
            finish()
        }
    }
}