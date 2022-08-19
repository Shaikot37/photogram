package shaikot.application.photogram

import android.R
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.yalantis.ucrop.UCrop
import shaikot.application.photogram.databinding.ActivityAddpostBinding
import java.io.File
import java.util.HashMap

class AddpostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddpostBinding
    lateinit var currentUser: FirebaseUser
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePictureRef: StorageReference? = null
    val REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddpostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePictureRef = FirebaseStorage.getInstance().reference.child("Posts Image")


        pickFromGallery()


        binding.save.setOnClickListener {
            uploadPost()
        }
    }

    private fun uploadPost() {
        when{
            imageUri == null -> Toast.makeText(this, "Image not uploaded", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(binding.description.text.toString()) -> Toast.makeText(this, "No Description!", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Upload Post")
                progressDialog.setMessage("Please wait! Your post is uploading..")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()


                val fileRef = storageProfilePictureRef!!.child(System.currentTimeMillis().toString()+".jpg")
                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask<Uri?>(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{ task->
                    if(!task.isSuccessful){
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }

                    return@Continuation fileRef.downloadUrl
                }
                ).addOnCompleteListener ( OnCompleteListener<Uri>{task->
                    if(task.isSuccessful){
                        val downloadUri = task.result
                        myUrl = downloadUri.toString()

                        val postRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId = postRef.push().key

                        val postMap = HashMap<String, Any>()

                        postMap["PostID"] = postId.toString()
                        postMap["Description"] = binding.description.text.toString()
                        postMap["Publisher"] = currentUser.uid
                        postMap["PostImage"] = myUrl
                        postRef.child(postId!!).updateChildren(postMap)

                        Toast.makeText(this,"Post uploaded successfully",Toast.LENGTH_LONG).show()

                        startActivity(Intent(this,MainActivity::class.java))
                        finish()
                        progressDialog.dismiss()

                    }
                    else{
                        Toast.makeText(this,"Post upload failed",Toast.LENGTH_LONG).show()
                    }

                } )

            }
        }

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
            binding.postImage.setImageURI(imageUri)
        }

    }

}