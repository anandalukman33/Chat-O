package id.my.anandalukman.otpchatappfirebase.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import id.my.anandalukman.otpchatappfirebase.R
import id.my.anandalukman.otpchatappfirebase.bean.User
import id.my.anandalukman.otpchatappfirebase.databinding.ItemProfileBinding

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
//        Glide.with(context)
//            .load(user.profileImage)
//            .placeholder(R.drawable.avatar)
//            .into(holder.binding.itemProfile)
        Picasso.get()
            .load(user.profileImage)
            .placeholder(R.drawable.avatar)
            .error(R.mipmap.ic_chato)
            .into(holder.binding.itemProfile)
    }

    override fun getItemCount(): Int = userList.size

}