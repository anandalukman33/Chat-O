package id.my.anandalukman.otpchatappfirebase.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant
import id.my.anandalukman.otpchatappfirebase.databinding.ActivityOtpactivityBinding
import id.my.anandalukman.otpchatappfirebase.widget.Loading
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private var binding : ActivityOtpactivityBinding? = null
    private var verificationId : String? = null
    private var auth : FirebaseAuth? = null
    private var loading : Loading? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        loading = Loading(this, 0)
        loading?.setCancelable(false)
        loading?.setMessage("Sending OTP")
        loading?.show()

        auth = FirebaseAuth.getInstance()

        supportActionBar?.hide()

        val phoneNumber = intent.getStringExtra(ChatConstant.PHONE_NUMBER)
        binding?.phoneLabel?.text = "Verify $phoneNumber"

        val options = PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber(phoneNumber!!)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {}

                override fun onVerificationFailed(p0: FirebaseException) {}

                override fun onCodeSent(
                    verifyId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    loading?.dismiss()

                    verificationId = verifyId

                    val imm  = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

                    binding?.otpView?.requestFocus()

                }

            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
        binding?.otpView?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s!!.length == 6) {
                    val credential = PhoneAuthProvider.getCredential(verificationId!!, s.toString())
                    auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this@OTPActivity, SetupProfileActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        } else {
                            Toast.makeText(
                                this@OTPActivity,
                                "Failed get Credential",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

        })
    }
}