package id.my.anandalukman.otpchatappfirebase.`interface`

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

import id.my.anandalukman.otpchatappfirebase.R
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.GET_PHOTO_MESSAGE
import id.my.anandalukman.otpchatappfirebase.databinding.DeleteLayoutBinding
import id.my.anandalukman.otpchatappfirebase.databinding.ReceiveMsgBinding
import id.my.anandalukman.otpchatappfirebase.databinding.SendMsgBinding
import id.my.anandalukman.otpchatappfirebase.message.MessageMain

class MessagesAdapter (
    var context: Context,
    messages: ArrayList<MessageMain>,
    senderRoom: String, receiverRoom: String) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private var senderRoom : String
    private var receiverRoom : String
    lateinit var messages : ArrayList<MessageMain>
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view : View = LayoutInflater.from(context)
                .inflate(R.layout.send_msg, parent, false)
            SentMsgHolder(view)
        } else {
            val view : View = LayoutInflater.from(context)
                .inflate(R.layout.receive_msg, parent, false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId) { ITEM_SENT } else { ITEM_RECEIVE }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder.javaClass == SentMsgHolder::class.java) {
            val viewHolder = holder as SentMsgHolder
            if (message.message.equals(GET_PHOTO_MESSAGE)) {
                viewHolder.binding.sendImage.visibility = View.VISIBLE
                viewHolder.binding.sendMessage.visibility = View.GONE
                viewHolder.binding.sendMLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.sendImage)
            }
            viewHolder.binding.sendMessage.text = message.message
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.delete_layout, null)
                val binding : DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()

                // Delete All for everyone
                binding.everyone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId.let { mId ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(mId!!).setValue(message)
                    }
                    message.messageId.let { xId ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(xId!!).setValue(message)
                    }
                    dialog.dismiss()
                }

                // Delete Message for me
                binding.delete.setOnClickListener {
                    message.messageId.let { yId ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(yId!!).setValue(null)
                    }
                    dialog.dismiss()
                }

                // Cancel Options Message Long Click
                binding.cancel.setOnClickListener { dialog.dismiss() }

                dialog.show()

                false
            }
        } else {
            val viewHolder = holder as ReceiveMsgHolder
            if (message.message.equals(GET_PHOTO_MESSAGE)) {
                viewHolder.binding.receiveImage.visibility = View.VISIBLE
                viewHolder.binding.receiveMessage.visibility = View.GONE
                viewHolder.binding.receiveMLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.receiveImage)
            }
            viewHolder.binding.receiveMessage.text = message.message
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.delete_layout, null)
                val binding : DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()

                // Delete All for everyone
                binding.everyone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId.let { mId ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(mId!!).setValue(message)
                    }
                    message.messageId.let { xId ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(xId!!).setValue(message)
                    }
                    dialog.dismiss()
                }

                // Delete Message for me
                binding.delete.setOnClickListener {
                    message.messageId.let { yId ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(yId!!).setValue(null)
                    }
                    dialog.dismiss()
                }

                // Cancel Options Message Long Click
                binding.cancel.setOnClickListener { dialog.dismiss() }

                dialog.show()

                false
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }


    inner class SentMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMsgBinding

        init {
            binding = SendMsgBinding.bind(itemView)
        }
    }

    inner class ReceiveMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ReceiveMsgBinding

        init {
            binding = ReceiveMsgBinding.bind(itemView)
        }
    }

    init {
        this.messages = messages
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }

}