package shaikot.application.photogram

import android.R
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import shaikot.application.photogram.databinding.ActivityAccountSettingsBinding
import shaikot.application.photogram.model.UserModel
import java.io.File
import java.util.*


class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountSettingsBinding
    lateinit var currentUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePictureRef: StorageReference? = null
    val REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePictureRef = FirebaseStorage.getInstance().reference.child("Profile Image")

        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SigninActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.changeImage.setOnClickListener {
            pickFromGallery()
        }

        binding.save.setOnClickListener {
            if(checker == "clicked"){
                updateUserImage()
            }
            updateUserInfo()
        }

        setUserInfo()
    }




    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType("image/*")
            .addCategory(Intent.CATEGORY_OPENABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        startActivityForResult(
            Intent.createChooser(
                intent,
                getString(R.string.ok)
            ), REQUEST_CODE
        )
    }

    private fun updateUserImage() {

        val fileRef = storageProfilePictureRef!!.child(currentUser.uid+".jpg")
        var uploadTask: StorageTask<*>
        uploadTask = fileRef.putFile(imageUri!!)

        uploadTask.continueWithTask<Uri?>(Continuation<UploadTask.TaskSnapshot,Task<Uri>>{ task->
            if(!task.isSuccessful){
                task.exception?.let {
                    throw it

                }
            }

            return@Continuation fileRef.downloadUrl
        }
        ).addOnCompleteListener ( OnCompleteListener<Uri>{task->
            if(task.isSuccessful){
                val downloadUri = task.result
                myUrl = downloadUri.toString()

                val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
                val userMap = HashMap<String, Any>()
                userMap["ProfileImage"] = myUrl
                userRef.child(currentUser.uid).updateChildren(userMap)

                Toast.makeText(this,"Image updated successfully",Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this,"Image updated failed",Toast.LENGTH_LONG).show()
            }

        } )

    }

    private fun updateUserInfo() {
        when {
            TextUtils.isEmpty(binding.fullName.text.toString()) -> {
                Toast.makeText(this, "Full Name required", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(binding.userName.text.toString()) -> {
                Toast.makeText(this, "User Name required", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(binding.bio.text) -> {
                Toast.makeText(this, "Bio required", Toast.LENGTH_LONG).show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Signup")
                progressDialog.setMessage("Please wait! Your account is updating..")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val userRef: DatabaseReference =
                    FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()
                userMap["FullName"] = binding.fullName.text.toString()
                userMap["UserName"] = binding.userName.text.toString()
                userMap["SearchName"] =
                    binding.fullName.text.toString().lowercase(Locale.getDefault())
                userMap["Bio"] = binding.bio.text.toString()

                userRef.child(currentUser.uid).updateChildren(userMap)

                Toast.makeText(this,"Account is updated successfully",Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


    private fun setUserInfo() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue<UserModel>(UserModel::class.java)
                binding.fullName.setText(user?.FullName)
                binding.userName.setText(user?.UserName)
                binding.bio.setText(user?.Bio)
                Picasso.get().load(user?.ProfileImage).placeholder(R.drawable.ic_dialog_info)
                    .into(binding.img)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
                val uri = data?.data
                val maxWidth = 520
                val maxHeight = 520
                UCrop.of(uri!!, Uri.fromFile(File(cacheDir, "FileName")))
                    .withMaxResultSize(maxWidth, maxHeight)
                    .withAspectRatio(5f, 5f)
                    .start(this)
            }
            else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
                imageUri = UCrop.getOutput(data!!)
                binding.img.setImageURI(imageUri)
                checker = "clicked"
            }

        }


}