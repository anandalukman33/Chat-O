package id.my.anandalukman.otpchatappfirebase

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant
import id.my.anandalukman.otpchatappfirebase.databinding.ActivityOtpactivityBinding
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    var binding : ActivityOtpactivityBinding? = null
    var verificationId : String? = null
    var auth : FirebaseAuth? = null
    var dialog : ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        dialog = ProgressDialog(this)
        dialog?.setMessage("Sending Otp...")
        dialog?.setCancelable(false)
        dialog?.show()

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
                    dialog!!.dismiss()

                    verificationId = verifyId

                    val imm  = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

                    binding?.otpView?.requestFocus()

                }

            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
        binding?.otpView?.setOtpCompletionListener { otp ->
            val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
            auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, SetupProfileActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        Toast.makeText(
                            this,
                            "Failed get Credential",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}