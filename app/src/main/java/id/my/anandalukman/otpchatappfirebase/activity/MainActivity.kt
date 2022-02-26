package id.my.anandalukman.otpchatappfirebase.activity

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import id.my.anandalukman.otpchatappfirebase.R
import id.my.anandalukman.otpchatappfirebase.`interface`.UserAdapter
import id.my.anandalukman.otpchatappfirebase.asset.User
import id.my.anandalukman.otpchatappfirebase.databinding.ActivityMainBinding
import id.my.anandalukman.otpchatappfirebase.notification.NotificationMain
import id.my.anandalukman.otpchatappfirebase.widget.Loading

class MainActivity : AppCompatActivity() {

    private var binding : ActivityMainBinding? = null
    private var database : FirebaseDatabase? = null
    private var users : ArrayList<User>? = null
    private var usersAdapter : UserAdapter? = null
    private var loading: Loading? = null
    private var user : User? = null
    private var title : TextView? = null
    private var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        NotificationMain.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            NotificationMain.token = it.token
            Log.d(TAG, "tokennya : ${it.token}")
        }

        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        var userid = firebase.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")

        title = findViewById(R.id.tv_title)

        val paint = title!!.paint
        val width = paint.measureText(title!!.text.toString())
        val textShader: Shader = LinearGradient(0f, 0f, width, title!!.textSize, intArrayOf(
            Color.parseColor("#578fb2"),
            Color.parseColor("#64a2c8"),
            Color.parseColor("#578fb2"),
            Color.parseColor("#64a2c8"),
            Color.parseColor("#578fb2")
        ), null, Shader.TileMode.REPEAT)

        title!!.paint.shader = textShader

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        loading = Loading(this, 0)
        loading?.setMessage("Getting data user")
        loading?.setCancelable(false)
        loading?.show()

        database = FirebaseDatabase.getInstance()

        users = ArrayList<User>()
        usersAdapter = UserAdapter(this, users!!)

        val layoutManager = GridLayoutManager(this, 2)
        binding?.mRec?.layoutManager = layoutManager

        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        binding!!.mRec.adapter = usersAdapter
        database!!.reference.child("users").addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    users!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val user : User? = snapshot1.getValue(User::class.java)
                        if (!user!!.uid.equals(firebase.uid)) users!!.add(user)
                    }
                    loading?.dismiss()
                    usersAdapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!).setValue("Offline")
    }

    override fun onBackPressed() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Information")
            .setMessage("Close this App?")
            .setPositiveButton("Yes") { dialog, which ->
                finishAffinity()
                finish()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
        dialog.show()
    }
}