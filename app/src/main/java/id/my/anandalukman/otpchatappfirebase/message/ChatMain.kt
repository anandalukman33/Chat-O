package id.my.anandalukman.otpchatappfirebase.message

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import id.my.anandalukman.otpchatappfirebase.R
import id.my.anandalukman.otpchatappfirebase.`interface`.MessagesAdapter
import id.my.anandalukman.otpchatappfirebase.api.RetrofitInstance
import id.my.anandalukman.otpchatappfirebase.asset.NotificationBean
import id.my.anandalukman.otpchatappfirebase.asset.NotificationData
import id.my.anandalukman.otpchatappfirebase.asset.User
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.GET_PHOTO_MESSAGE
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.SELECTED_IMAGE_CHAT
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.SELECTED_NAME_CHAT
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.SELECTED_UID_CHAT
import id.my.anandalukman.otpchatappfirebase.databinding.ActivityChatBinding
import id.my.anandalukman.otpchatappfirebase.widget.GsonUtils
import id.my.anandalukman.otpchatappfirebase.widget.Loading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatMain : AppCompatActivity() {

    var binding : ActivityChatBinding? = null
    var adapter : MessagesAdapter? = null
    var message : ArrayList<MessageMain>? = null
    var senderRoom : String? = null
    var senderUid : String? = null
    var receiverUid : String? = null
    var receiverRoom : String? = null
    var database : FirebaseDatabase? = null
    var storage : FirebaseStorage? = null
    var dialog : Loading? = null
    var firebaseUser : FirebaseUser? = null
    var databaseReference : DatabaseReference? = null
    var userBean : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        dialog = Loading(this, 0)
        dialog!!.setMessage("Please wait...")
        dialog!!.setCancelable(false)

        message = ArrayList()

        var name = intent.getStringExtra(SELECTED_NAME_CHAT)
        var profile = intent.getStringExtra(SELECTED_IMAGE_CHAT)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        databaseReference =
            FirebaseDatabase.getInstance().getReference("users").child(firebaseUser!!.uid)

        binding!!.tvName.text = name

        Picasso.get()
            .load(profile)
            .placeholder(R.drawable.placeholder)
            .into(binding!!.iconProfile)

        binding!!.ivBack.setOnClickListener { finish() }
        receiverUid = intent.getStringExtra(SELECTED_UID_CHAT)

        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (status == "Offline") {
                            binding!!.tvStatus.visibility = View.VISIBLE
                            binding!!.tvStatus.text = status
                        } else {
                            binding!!.tvStatus.text = status
                            binding!!.tvStatus.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adapter = MessagesAdapter(this, message!!, senderRoom!!, receiverRoom!!)

        binding!!.recyclerChat.layoutManager = LinearLayoutManager(this)
        binding!!.recyclerChat.adapter = adapter

        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    message!!.clear()
                    for (i in snapshot.children) {
                        val messageBean : MessageMain? = i.getValue(MessageMain::class.java)
                        messageBean!!.messageId = i.key
                        message!!.add(messageBean)
                    }
                    adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}

            })

        binding!!.sendMessage.setOnClickListener {
            val messageTxt : String = binding!!.messageBox.text.toString()
            val date = Date()
            val message = MessageMain(messageTxt, senderUid, date.time)

            binding!!.messageBox.setText("")


            databaseReference!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    var topic = "/topics/$receiverUid"
                    NotificationBean(
                        NotificationData( user?.name!!,messageTxt),
                        topic
                    ).also {
                        sendNotification(it)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }

            })

            val randomKey = database!!.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time

            database!!.reference.child("chats")
                .child(senderRoom!!)
                .updateChildren(lastMsgObj)

            database!!.reference.child("chats")
                .child(receiverRoom!!)
                .updateChildren(lastMsgObj)

            database!!.reference.child("chats")
                .child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {  }
                }

        }

        binding!!.attachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        val handler = Handler()
        binding!!.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }
            var userStoppedTyping = Runnable {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("Online")
            }
        })

        supportActionBar?.setDisplayShowTitleEnabled(false)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var name = intent.getStringExtra(SELECTED_NAME_CHAT)

        if (requestCode == 25) {
            if (data != null) {
                if (data.data != null) {
                    val selectedImage = data.data
                    val calendar = Calendar.getInstance()
                    var referencex = storage!!.reference.child("chats")
                        .child(calendar.timeInMillis.toString() + "")
                    dialog?.show()

                    referencex.putFile(selectedImage!!)
                        .addOnCompleteListener { task ->
                            dialog?.dismiss()
                            if (task.isSuccessful) {
                                referencex.downloadUrl.addOnSuccessListener { uri ->
                                    val filePath = uri.toString()
                                    val messageTxt : String = binding!!.messageBox.text.toString()
                                    val date = Date()

                                    val message = MessageMain(messageTxt, senderUid, date.time)
                                    message.message = GET_PHOTO_MESSAGE
                                    message.imageUrl = filePath
                                    binding!!.messageBox.setText("")

                                    databaseReference!!.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val user = snapshot.getValue(User::class.java)
                                            var topic = "/topics/$receiverUid"
                                            NotificationBean(
                                                NotificationData( user?.name!!,"${user.name!!} mengirim "+message.message),
                                                topic
                                            ).also {
                                                sendNotification(it)
                                            }

                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                                        }

                                    })

                                    val randomKey = database!!.reference.push().key
                                    val lastMsgObj = HashMap<String, Any>()
                                    lastMsgObj["lastMsg"] = message.message!!
                                    lastMsgObj["lastMsgTime"] = date.time

                                    database!!.reference.child("chats")
                                        .updateChildren(lastMsgObj)

                                    database!!.reference.child("chats")
                                        .child(receiverRoom!!)
                                        .updateChildren(lastMsgObj)

                                    database!!.reference.child("chats")
                                        .child(senderRoom!!)
                                        .child("messages")
                                        .child(randomKey!!)
                                        .setValue(message).addOnSuccessListener {
                                            database!!.reference.child("chats")
                                                .child(receiverRoom!!)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message)
                                                .addOnSuccessListener {  }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("Offline")
    }

    private fun sendNotification(notification: NotificationBean) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d("TAG", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("TAG", response.errorBody()!!.string())
            }
        } catch(e: Exception) {
            Log.e("TAG", e.toString())
        }
    }
}