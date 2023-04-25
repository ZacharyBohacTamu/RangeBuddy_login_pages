package com.example.mainpage_rangebuddy


import android.annotation.SuppressLint
//import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import java.io.File
import android.net.Uri
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mainpage_rangebuddy.Constants.TAG
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.*
import android.view.*
import com.example.mainpage_rangebuddy.databinding.ActivityPhotoPageBinding

//import androidx.camera.core.*
//import androidx.camera.core.AspectRatio
//import android.view.ScaleGestureDetector.OnScaleGestureListener
//import androidx.camera.core.CameraX
//import androidx.lifecycle.LifecycleOwner
//import com.example.mainpage_rangebuddy.R
//import android.widget.SeekBar
//import android.location.GnssAntennaInfo.Listener
//import com.example.mainpage_rangebuddy.databinding.ActivityMainBinding
//import java.io.FileOutputStream
//import android.widget.ImageButton
//import android.os.Environment
//import android.os.Handler
//import android.os.HandlerThread
//import android.media.ImageReader
//import android.media.ImageReader.OnImageAvailableListener

class PhotoPage : AppCompatActivity() {
    //ui functions variables
    private lateinit var ToSessionData: Button
    //Picture function variables
    lateinit var binding: ActivityPhotoPageBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    //runs most functions, button mapping, etc
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hiding the title bar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        //setting variables to use in the xml
        binding = ActivityPhotoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        //begins code of starting up the camera if Perms are granted
        if (allPermissionGranted()) {
            startCamera()
        } else {
            //requires perms to be needed
            ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }
        binding.btnTakePicture.setOnClickListener {
            takePhoto()
        }
        //toast for stating set has been grouped by pressing Grouping button
        val grouping = findViewById<Button>(R.id.Grouping)
        grouping.setOnClickListener {
            Toast.makeText(this, "Set Grouped", Toast.LENGTH_SHORT).show()
        }

        //button mapping to other pages
        ToSessionData = findViewById(R.id.ToSessionsPage)
        ToSessionData.setOnClickListener {
            val session = Intent(this, SessionsData::class.java)
            startActivity(session)
        }
    }

    //sends picture to directory will be later changed to server in 404
    private fun getOutputDirectory(): File {
        //gets the directory of the app
        val mediaDir = externalMediaDirs.firstOrNull()?.let { mFile ->
            File(mFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    //takes the photo and saves to file
    private fun takePhoto() {
        //gets the image capture
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                Constants.FILE_NAME_FORMAT,
                Locale.getDefault()
            )   //creates a file name in terms of time.jpg
                .format(
                    System
                        .currentTimeMillis()
                ) + ".jpg"
        )
        //sets up the output file
        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()
        //takes the picture
        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    val savedUri = Uri.fromFile(photoFile) //maybe other URI
                    val msg = "Photo Saved"

                    Toast.makeText(this@PhotoPage, "$msg $savedUri ", Toast.LENGTH_LONG).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "onError: ${exception.message}", exception)
                }


            }
        )


    }

    //starts the camera up in app
    private fun startCamera() {
        //starts up camera
        val cameraProviderFuture = ProcessCameraProvider
            .getInstance(this)
        //sets up camera
        cameraProviderFuture.addListener({
            //used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { mPreview ->

                    mPreview.setSurfaceProvider(
                        binding.cameraView.surfaceProvider // why?
                    )
                }
            imageCapture = ImageCapture.Builder()
                .build()
            //selects back camera as default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            //unbinds all cameras
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector,
                    preview, imageCapture
                )
            } catch (e: Exception) {
                Log.d(TAG, "startCamera Fail:", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    //asks for permissions
    private fun allPermissionGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    //checks if permissions are granted and outputs a toast
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                Toast.makeText(this, "We Have Permission", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                this,
                "Permissions not granted", Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    //stops the camera when app is closed
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


}

