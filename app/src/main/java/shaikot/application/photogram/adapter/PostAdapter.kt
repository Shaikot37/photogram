package shaikot.application.photogram.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import shaikot.application.photogram.databinding.PostLayoutBinding
import shaikot.application.photogram.databinding.UserItemLayoutBinding
import shaikot.application.photogram.model.PostModel
import shaikot.application.photogram.model.UserModel

class PostAdapter(private val context: Context, private val mPost: List<PostModel>): RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var currentUser: FirebaseUser? = null

    class ViewHolder(val binding: PostLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            PostLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        currentUser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]
        Picasso.get().load(post.PostImage).into(holder.binding.postImage)
        setPublisherInfo(holder.binding.profileImage,holder.binding.userName,holder.binding.publisher,post.Publisher)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }


    private fun setPublisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherID: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue<UserModel>(UserModel::class.java)
                    Picasso.get().load(user!!.ProfileImage).into(profileImage)
                    userName.text = user.UserName
                    publisher.text = user.FullName
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}