package shaikot.application.photogram.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import shaikot.application.photogram.AccountSettingsActivity
import shaikot.application.photogram.R
import shaikot.application.photogram.databinding.FragmentProfileBinding
import shaikot.application.photogram.model.UserModel


class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding
    lateinit var profileUid: String
    lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater,container,false)

        currentUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREF",Context.MODE_PRIVATE)
        if(pref!=null){
            this.profileUid = pref.getString("profileUid","none")!!
        }

        if(profileUid == currentUser.uid){
            binding.editProfileButton.text = "Edit Profile"
        }
        else{
            checkFollowAndFollowing()
        }



        binding.editProfileButton.setOnClickListener {
            when(binding.editProfileButton.text.toString()){
                "Edit Profile"->{
                    startActivity(Intent(context,AccountSettingsActivity::class.java))
                }
                "Follow"->{
                    currentUser?.uid.let { currentUserUid ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(currentUserUid.toString())
                            .child("Following").child(profileUid)
                            .setValue(true).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    currentUser?.uid.let { currentUserUid ->
                                        FirebaseDatabase.getInstance().reference
                                            .child("Follow").child(profileUid)
                                            .child("Followers").child(currentUserUid.toString())
                                            .setValue(true).addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    getFollowers()
                                                    getFollowings()
                                                }
                                            }
                                    }
                                }
                            }
                    }
                }
                "Following"->{
                    currentUser?.uid.let { currentUserUid ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(currentUserUid.toString())
                            .child("Following").child(profileUid)
                            .removeValue().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    currentUser?.uid.let { currentUserUid ->
                                        FirebaseDatabase.getInstance().reference
                                            .child("Follow").child(profileUid)
                                            .child("Followers").child(currentUserUid.toString())
                                            .removeValue().addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    getFollowers()
                                                    getFollowings()
                                                }
                                            }
                                    }
                                }
                            }
                    }
                }
            }

        }


        getFollowers()
        getFollowings()
        userInfo()

        return binding.root
    }


    private fun checkFollowAndFollowing(){
        val followingRef = currentUser?.uid.let { currentUserUid ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(currentUserUid.toString())
                .child("Following")
        }

        if(followingRef != null){
            followingRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child(profileUid).exists()){
                        binding.editProfileButton.text = "Following"
                    }
                    else{
                        binding.editProfileButton.text = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }




    private fun getFollowings(){

        val followingRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileUid)
                .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    binding.following.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        } )

    }


    private fun getFollowers(){

        val followingRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileUid)
                .child("Followers")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    binding.follower.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        } )

    }



    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileUid)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue<UserModel>(UserModel::class.java)
                binding.fullName.text = user?.FullName
                binding.userName.text = user?.UserName
                binding.bio.text = user?.Bio
                Picasso.get().load(user?.ProfileImage).into(binding.proImg)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREF",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileUid",currentUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREF",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileUid",currentUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREF",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileUid",currentUser.uid)
        pref?.apply()
    }

}