package isomora.com.greendoctor

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.widget.Button
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import isomora.com.greendoctor.models.Posts
import isomora.com.greendoctor.models.Users
import kotlinx.android.synthetic.main.activity_main.*;
import kotlinx.android.synthetic.main.item_post.*
import java.io.IOException

private const val TAG = "MainActivity"
private const val PICK_PHOTO_CODE = 1234

class MainActivity : AppCompatActivity() {
    private var uri: Uri? = null
    private var results: Classifier.Recognition? = null
    private lateinit var mClassifier: Classifier
    private lateinit var mBitmap: Bitmap
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private var constUser: Users? = null



    private val mCameraRequestCode = 0
    private val mGalleryRequestCode = 2

    private val mInputSize = 224
    private val mModelPath = "plant_disease_model.tflite"
    private val mLabelPath = "plant_labels.txt"
    private val mSamplePath = "soybean.JPG"


    @SuppressLint("QueryPermissionsNeeded")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)
        mClassifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)

        storageRef = FirebaseStorage.getInstance().reference
        firestoreDb = FirebaseFirestore.getInstance()
        resources.assets.open(mSamplePath).use {
            mBitmap = BitmapFactory.decodeStream(it)
            mBitmap = Bitmap.createScaledBitmap(mBitmap, mInputSize, mInputSize, true)
            mPhotoImageView.setImageBitmap(mBitmap)
        }

        mCameraButton.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(callCameraIntent, mCameraRequestCode)
        }

        mGalleryButton.setOnClickListener {



            Log.i(TAG, "Open up image picker on device")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if(imagePickerIntent.resolveActivity(packageManager) != null)
            {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }

        }
        mDetectButton.setOnClickListener {
                results = mClassifier.recognizeImage(mBitmap).firstOrNull()
                mResultTextView.text= results?.title+"\n Confidence:"+results?.confidence

        }



        postBtn.setOnClickListener {
            val intent = Intent(this, AskActivity::class.java)
            startActivity(intent);
        }

        askBtn.setOnClickListener {
            askBtn.isEnabled = false
            if(uri == null) {
                Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show()
                askBtn.isEnabled = true
                return@setOnClickListener
            }
            if(results?.title == "Default image set now…"){
                Toast.makeText(this, "Please Detect Disease first", Toast.LENGTH_SHORT).show()
                askBtn.isEnabled = true
                return@setOnClickListener
            }

            val photoReference = storageRef.child("images/${System.currentTimeMillis()}--photo.jpg")

            photoReference.putFile(uri!!)
                .continueWithTask { photoUploadTask ->
                    photoReference.downloadUrl
                }.continueWithTask { downloadUrlTask ->
                    val post = Posts (
                        results?.title.toString(),
                        downloadUrlTask.result.toString(),
                        System.currentTimeMillis(),
                        constUser
                        )
                    firestoreDb.collection("posts").add(post)

                }.addOnCompleteListener { postCreationTask ->
                    askBtn.isEnabled = true
                    if (!postCreationTask.isSuccessful) {
                        Log.e(TAG, "Exception during firebase operations", postCreationTask.exception)
                        Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
                    }

                    Toast.makeText(this, "Uploaded Successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AskActivity::class.java)
                    startActivity(intent);
                    finish()
                }


        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == mCameraRequestCode){

            if(resultCode == Activity.RESULT_OK && data != null) {
                mBitmap = data.extras!!.get("data") as Bitmap
                mBitmap = scaleImage(mBitmap)
                val toast = Toast.makeText(this, ("Image crop to: w= ${mBitmap.width} h= ${mBitmap.height}"), Toast.LENGTH_LONG)
                toast.setGravity(Gravity.BOTTOM, 0, 20)
                toast.show()
                mPhotoImageView.setImageBitmap(mBitmap)
                mResultTextView.text= "Your photo image set now."
            } else {
                Toast.makeText(this, "Camera cancel..", Toast.LENGTH_LONG).show()
            }
        } else if(requestCode == PICK_PHOTO_CODE) { // changing mGalleryRequestCode to PICK_PHOTO_CODE
            if (data != null) {
                uri = data.data

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                println("Success!!!")
                mBitmap = scaleImage(mBitmap)
                mPhotoImageView.setImageBitmap(mBitmap)

            }
        } else {
            Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_LONG).show()

        }
    }




    fun scaleImage(bitmap: Bitmap?): Bitmap {
        val orignalWidth = bitmap!!.width
        val originalHeight = bitmap.height
        val scaleWidth = mInputSize.toFloat() / orignalWidth
        val scaleHeight = mInputSize.toFloat() / originalHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, orignalWidth, originalHeight, matrix, true)
    }

}

