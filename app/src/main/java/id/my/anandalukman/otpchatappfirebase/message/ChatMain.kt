package id.my.anandalukman.otpchatappfirebase.message

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import id.my.anandalukman.otpchatappfirebase.R
import id.my.anandalukman.otpchatappfirebase.`interface`.MessagesAdapter
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.GET_PHOTO_MESSAGE
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.SELECTED_IMAGE_CHAT
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.SELECTED_NAME_CHAT
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.SELECTED_UID_CHAT
import id.my.anandalukman.otpchatappfirebase.databinding.ActivityChatBinding
import id.my.anandalukman.otpchatappfirebase.widget.Loading
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        dialog = Loading(this, 0)
        dialog!!.setMessage("Uploading image")
        dialog!!.setCancelable(false)

        message = ArrayList()

        var name = intent.getStringExtra(SELECTED_NAME_CHAT)
        var profile = intent.getStringExtra(SELECTED_IMAGE_CHAT)

        binding!!.tvName.text = name

        Picasso.get()
            .load(profile)
            .placeholder(R.drawable.placeholder)
            .error(R.mipmap.ic_chato)
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
                            binding!!.tvStatus.visibility = View.GONE
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
}