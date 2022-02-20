package id.my.anandalukman.otpchatappfirebase

import android.R
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Window
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import id.my.anandalukman.otpchatappfirebase.bean.User
import id.my.anandalukman.otpchatappfirebase.databinding.ActivitySetupProfileBinding
import id.my.anandalukman.otpchatappfirebase.widget.Loading
import java.util.*


class SetupProfileActivity : AppCompatActivity() {

    var binding : ActivitySetupProfileBinding? = null
    var auth : FirebaseAuth? = null
    var database : FirebaseDatabase? = null
    var storage : FirebaseStorage? = null
    var selectedImage : Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        var loading = Loading(this)


        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        supportActionBar?.hide()

        binding?.ivProfile?.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }

        binding?.profileBtn?.setOnClickListener {
            loading.startLoading()
            val namey : String = binding?.editProfile?.text.toString()

            if (namey.isEmpty()) {
                binding?.editProfile?.setError("Please type a name")
            }

            if (selectedImage != null) {
                val reference = storage?.reference?.child("Profile")?.child(auth!!.uid!!)
                reference?.putFile(selectedImage!!)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnCompleteListener { uri ->
                            val imageUrl = uri.toString()
                            val uid = auth?.uid
                            val phoneNumber = auth?.currentUser?.phoneNumber
                            val name : String = binding?.editProfile?.text.toString()
                            val user = User(uid, name, phoneNumber, imageUrl)
                            database?.reference
                                ?.child("users")
                                ?.child(uid!!)
                                ?.setValue(user)
                                ?.addOnCompleteListener {
                                    loading.dismissLoading()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    } else {
                        val uid = auth?.uid
                        val phoneNumber = auth?.currentUser?.phoneNumber
                        val namex : String = binding?.editProfile?.text.toString()
                        val user = User(uid, namex, phoneNumber, "No Image")

                        database!!.reference
                            .child("users")
                            .child(uid!!)
                            .setValue(user)
                            .addOnCanceledListener {
                                loading.dismissLoading()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (data.data != null) {
                val uri = data.data // filePath
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage?.reference
                    .child("Profile")
                    .child(time.toString() + "")
                reference.putFile(uri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnCompleteListener { uri ->
                            val filePath = uri.toString()
                            val obj = HashMap<String, Any>()
                            obj["image"] = filePath
                            database!!.reference
                                .child("users")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj).addOnSuccessListener { }
                        }
                    }
                }
                binding?.ivProfile?.setImageURI(data.data)
                selectedImage = data.data
            }
        }
    }
}