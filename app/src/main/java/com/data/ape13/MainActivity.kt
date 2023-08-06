package com.data.ape13

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.data.ape13.databinding.ActivityMainBinding
import com.data.ape13.databinding.EditdialoglayoutBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var startActivityLauncher: ActivityResultLauncher<Intent>
//    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var imageURI: Uri? = null

    private var name: String = ""
    private var location: String = ""
    private var email: String = ""
    private var phone: String = ""
    private var twitter: String = ""
    private var facebook: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initStorage()

//        binding.camerabtn.setOnClickListener {
//            resultLauncher.launch("image2/*")
//        }
//        binding.SaveBtn.setOnClickListener {
//            uploadImage()
//        }



        startActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = (result.data?.extras?.get("data")) as? Bitmap
                    ?: return@registerForActivityResult
                Glide.with(this)
                    .load(bitmap)
                    .centerCrop()
                    .circleCrop()
                    .into(binding.profile)
                binding.profile.setImageBitmap(bitmap)
            }

        }
//        galleryLauncher = registerForActivityResult(
//            ActivityResultContracts.GetContent()
//        ) { uri: Uri? ->
//            if (uri != null) {
//                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
//                Glide.with(this)
//                    .load(bitmap)
//                    .centerCrop()
//                    .circleCrop()
//                    .into(binding.profile)
//            }
//        }
        binding.camerabtn.setOnClickListener {
            showDialog()
        }
        binding.editBtn.setOnClickListener {
            showEditProfileDialog()
        }
        binding.SaveBtn.setOnClickListener {
            uploadImage()
        }

    }

    private fun uploadImage() {
        binding.camerabtn.isEnabled = false
        binding.SaveBtn.isEnabled = false

        storageRef = storageRef.child(System.currentTimeMillis().toString())

        imageURI?.let {
            storageRef.putFile(it).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        var name = binding.textView8.text.toString()
                        var location = binding.textView9.text.toString()
                        var email = binding.putEmail.text.toString()
                        var phone = binding.putPhone.text.toString()
                        var twitter = binding.putTwitter.text.toString()
                        var facebook = binding.putFb.text.toString()
                        val db = Firebase.firestore

                        // Create a new user with a first and last name
                        val user = hashMapOf(
                            "name" to name,
                            "location" to location,
                            "email" to email,
                            "phone" to phone,
                            "twitter" to twitter,
                            "facebook" to facebook

                        )

                        // Add a new document with a generated ID
                        db.collection("image_data2")
                            .add(user)
                            .addOnSuccessListener { _ ->
                                Toast.makeText(applicationContext, "SUCCESS!", Toast.LENGTH_LONG)
                                    .show()
                            }
                            .addOnFailureListener { _ ->
                                Toast.makeText(applicationContext, "FAILED!", Toast.LENGTH_LONG)
                                    .show()
                            }

                    }
                    binding.camerabtn.isEnabled = true
                    binding.SaveBtn.isEnabled = true

                } else {
                    Toast.makeText(applicationContext, "FAILED UPLOAD!", Toast.LENGTH_LONG).show()
                    binding.camerabtn.isEnabled = true
                    binding.SaveBtn.isEnabled = true

                }
            }
        }
    }

    private fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Image Selector")
        dialogBuilder.setPositiveButton("Camera") { dialog, _ ->
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            } else {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityLauncher.launch(intent)
            }
            dialog.dismiss()

        }
        dialogBuilder.setNegativeButton("Gallery") { dialog, _ ->
            resultLauncher.launch("image/*")
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityLauncher.launch(intent)
        }
    }

    private fun showEditProfileDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)

        val dialog = layoutInflater.inflate(R.layout.editdialoglayout, null)
        val dialogBinding = EditdialoglayoutBinding.bind(dialog)
        alertDialogBuilder.setView(dialog)

        dialogBinding.edtName.setText(name)
        dialogBinding.edtAddress.setText(location)
        dialogBinding.edtEmail.setText(email)
        dialogBinding.edtPhone.setText(phone)
        dialogBinding.edtTwitter.setText(twitter)
        dialogBinding.edtFb.setText(facebook)

        alertDialogBuilder
            .setPositiveButton("Save") { dialog, _ ->
                // Save the entered information to variables
                name = dialogBinding.edtName.text.toString()
                location = dialogBinding.edtAddress.text.toString()
                email = dialogBinding.edtEmail.text.toString()
                phone = dialogBinding.edtPhone.text.toString()
                twitter = dialogBinding.edtTwitter.text.toString()
                facebook = dialogBinding.edtFb.text.toString()

                updateTextViews()

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        alertDialogBuilder.create().show()
    }
    private fun updateTextViews() {
        // Update TextViews with the entered information
        binding.textView8.text = name
        binding.textView9.text = location
        binding.putEmail.setText(email)
        binding.putPhone.setText(phone)
        binding.putTwitter.setText(twitter)
        binding.putFb.setText(facebook)
    }


    //function for opening the gallery
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        imageURI = it
        binding.profile.setImageURI(it)
    }


    private fun initStorage(){
        //initialize firebase objects
        storageRef = FirebaseStorage.getInstance().reference.child("Images2")
        firebaseFirestore = FirebaseFirestore.getInstance()
    }
}































