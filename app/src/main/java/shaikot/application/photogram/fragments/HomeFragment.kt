package shaikot.application.photogram.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import shaikot.application.photogram.R
import shaikot.application.photogram.adapter.PostAdapter
import shaikot.application.photogram.databinding.FragmentHomeBinding
import shaikot.application.photogram.model.PostModel


class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding

    private var adapter:PostAdapter? = null
    private var postList:MutableList<PostModel>? = null
    private var followingList:MutableList<PostModel>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater,container,false)

        val recyclerView:RecyclerView = binding.rvHome
        val linearLayoutManager:LinearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        checkFollowingList()

        postList = ArrayList()
        adapter = PostAdapter(context!!,postList as List<PostModel>)
        recyclerView.adapter = adapter


        return binding.root
    }

    private fun checkFollowingList() {
        followingList = ArrayList()
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    (followingList as ArrayList<String>).clear()


                    for(followingUser in snapshot.children){
                        (followingList as ArrayList<String>).add(followingUser.key!!)
                    }

                    retrieveAllPost()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun retrieveAllPost() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")

        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                postList?.clear()

                for (pId in snapshot.children){

                    val post = pId.getValue<PostModel>(PostModel::class.java)

                    for(id in (followingList as ArrayList<String>)){

                        if(post!!.Publisher == id){
                            postList?.add(post)

                        }
                    }
                    adapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


}