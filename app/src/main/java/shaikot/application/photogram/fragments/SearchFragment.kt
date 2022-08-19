package shaikot.application.photogram.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import shaikot.application.photogram.adapter.UserAdapter
import shaikot.application.photogram.databinding.FragmentSearchBinding
import shaikot.application.photogram.model.UserModel
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private var userAdapter: UserAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var userList: MutableList<UserModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)

        recyclerView = binding.rvSearch
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.setHasFixedSize(true)

        userList = ArrayList()
        userAdapter = UserAdapter(requireContext(), userList as ArrayList<UserModel>, true)

        recyclerView?.adapter = userAdapter


        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(binding.searchText.text.toString()==""){
                    recyclerView?.visibility = View.GONE
                }
                else {
                    recyclerView?.visibility = View.VISIBLE
                    //retrieveUsers()
                    searchUser(p0.toString().lowercase(Locale.getDefault()))
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        return binding.root
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("SearchName")
            .startAt(input)
            .endAt(input+"\uf8ff")

        query.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                userList?.clear()

                for (ss in snapshot.children) {
                    val user = ss.getValue(UserModel::class.java)
                    if (user != null) {
                        userList?.add(user)
                    }
                }
                userAdapter?.notifyDataSetChanged()


            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    private fun retrieveUsers() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        usersRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (binding.searchText.text.toString() == "") {
                    userList?.clear()
                    for (ss in snapshot.children) {
                        val user = ss.getValue(UserModel::class.java)
                        if (user != null) {
                            userList?.add(user)
                        }
                    }
                    userAdapter?.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

}