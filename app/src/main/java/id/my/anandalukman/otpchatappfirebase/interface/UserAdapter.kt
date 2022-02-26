package id.my.anandalukman.otpchatappfirebase.`interface`

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import id.my.anandalukman.otpchatappfirebase.R
import id.my.anandalukman.otpchatappfirebase.asset.User
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.SELECTED_IMAGE_CHAT
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.SELECTED_NAME_CHAT
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.SELECTED_UID_CHAT
import id.my.anandalukman.otpchatappfirebase.databinding.ItemProfileBinding
import id.my.anandalukman.otpchatappfirebase.message.ChatMain

class UserAdapter(

    private var context: Context,
    private var userList: ArrayList<User>

) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding : ItemProfileBinding = ItemProfileBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.itemUsername.text = user.name
        Picasso.get()
            .load(user.profileImage)
            .placeholder(R.drawable.avatar)
            .error(R.mipmap.ic_chato)
            .into(holder.binding.itemProfile)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatMain::class.java)
            intent.putExtra(SELECTED_NAME_CHAT, user.name)
            intent.putExtra(SELECTED_IMAGE_CHAT, user.profileImage)
            intent.putExtra(SELECTED_UID_CHAT, user.uid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

}