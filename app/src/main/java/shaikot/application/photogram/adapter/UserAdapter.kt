package shaikot.application.photogram.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import shaikot.application.photogram.R
import shaikot.application.photogram.databinding.UserItemLayoutBinding
import shaikot.application.photogram.fragments.ProfileFragment
import shaikot.application.photogram.model.UserModel

class UserAdapter(
    private var mContext: Context,
    private var userList: List<UserModel>,
    private var isFragment: Boolean = false
) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    class ViewHolder(val binding: UserItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            UserItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.userName.text = user.FullName
        holder.binding.userProfileName.text = user.UserName
        Picasso.get().load(user.ProfileImage).placeholder(R.drawable.profile_logo)
            .into(holder.binding.img)


        holder.itemView.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREF",Context.MODE_PRIVATE).edit()
            pref.putString("profileUid",user.Uid)
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_view,ProfileFragment())
                .commit()
        }



        checkFollowingStatus(user.Uid, holder.binding.follow)

        holder.binding.follow.setOnClickListener {
            if (holder.binding.follow.text.equals("Follow")) {
                currentUser?.uid.let { currentUserUid ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserUid.toString())
                        .child("Following").child(user.Uid)
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                currentUser?.uid.let { currentUserUid ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.Uid)
                                        .child("Followers").child(currentUserUid.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            } else {
                currentUser?.uid.let { currentUserUid ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserUid.toString())
                        .child("Following").child(user.Uid)
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                currentUser?.uid.let { currentUserUid ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.Uid)
                                        .child("Followers").child(currentUserUid.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }

    }

    private fun checkFollowingStatus(uid: String, follow: AppCompatButton) {
        val followingRef = currentUser?.uid.let { currentUserUid ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(currentUserUid.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(uid).exists()){
                    follow.text = "Following"
                }
                else{
                    follow.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun getItemCount(): Int {
        return userList.size
    }

}