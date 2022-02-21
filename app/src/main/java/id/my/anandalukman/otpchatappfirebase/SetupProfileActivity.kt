package id.my.anandalukman.otpchatappfirebase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.text.parseAsHtml
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import id.my.anandalukman.otpchatappfirebase.bean.User
import id.my.anandalukman.otpchatappfirebase.databinding.ActivitySetupProfileBinding
import id.my.anandalukman.otpchatappfirebase.widget.Loading
import java.util.*


class SetupProfileActivity : AppCompatActivity() {

    private var binding : ActivitySetupProfileBinding? = null
    private var auth : FirebaseAuth? = null
    private var database : FirebaseDatabase? = null
    private var storage : FirebaseStorage? = null
    private var selectedImage : Uri? = null
    private var loading : Loading? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        loading = Loading(this, 0)
        loading?.setCancelable(false)

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
            loading?.setMessage("Update Image")
            loading?.show()
            val namey : String = binding?.editProfile?.text.toString()

            if (namey.isEmpty()) {
                binding?.editProfile?.setError("Please type a name")
            }

            if (selectedImage != null) {
                val reference = storage?.reference?.child("Profile")?.child(auth!!.uid!!)
                reference?.putFile(selectedImage!!)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnCompleteListener { uri ->
                            val imageUrl = uri.result.toString()
                            val uid = auth?.uid
                            val phoneNumber = auth?.currentUser?.phoneNumber
                            val name : String = binding?.editProfile?.text.toString()
                            val user = User(uid, name, phoneNumber, imageUrl)
                            database!!.reference
                                .child("users")
                                .child(uid!!)
                                .setValue(user)
                                .addOnCompleteListener {
                                    loading?.dismiss()
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
                                loading?.dismiss()
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
                val reference = storage.reference
                    .child("Profile")
                    .child(time.toString() + "")
                reference.putFile(uri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnCompleteListener { uri ->
                            val filePath = uri.result.toString()
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