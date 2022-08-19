package shaikot.application.photogram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import shaikot.application.photogram.databinding.ActivitySigninBinding
import shaikot.application.photogram.databinding.ActivitySignupBinding
import java.util.*
import kotlin.collections.HashMap

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.signin.setOnClickListener {
            startActivity(Intent(this,SigninActivity::class.java))
        }

        binding.signup.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {

        val fullName = binding.fullName.text.toString()
        val userName = binding.userName.text.toString()
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()

        when{
            TextUtils.isEmpty(fullName) ->{Toast.makeText(this,"Full Name required",Toast.LENGTH_LONG).show()}
            TextUtils.isEmpty(userName) ->{Toast.makeText(this,"User Name required",Toast.LENGTH_LONG).show()}
            TextUtils.isEmpty(email) ->{Toast.makeText(this,"Email required",Toast.LENGTH_LONG).show()}
            TextUtils.isEmpty(password) ->{Toast.makeText(this,"Password required",Toast.LENGTH_LONG).show()}

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Signup")
                progressDialog.setMessage("Please wait! It may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener {task->
                        if(task.isSuccessful){
                            saveUserInfo(fullName,userName,email,progressDialog)
                        }
                        else{
                            val message = task.exception!!.toString()
                            Toast.makeText(this,"Error: $message",Toast.LENGTH_LONG).show()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String,Any>()
        userMap["Uid"] = currentUserId
        userMap["FullName"] = fullName
        userMap["UserName"] = userName
        userMap["SearchName"] = fullName.lowercase(Locale.getDefault())
        userMap["Email"] = email
        userMap["Bio"] = "Hey! I am using Photogram."
        userMap["ProfileImage"] = "gs://photogram-97cb4.appspot.com/Profile Image/default.jpeg"

        userRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this,"Account is created successfully",Toast.LENGTH_LONG).show()


                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserId)
                        .child("Following").child(currentUserId)
                        .setValue(true)


                    val intent = Intent(this,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else{
                    val message = task.exception!!.toString()
                    Toast.makeText(this,"Error: $message",Toast.LENGTH_LONG).show()
                    progressDialog.dismiss()
                }
            }


    }
}