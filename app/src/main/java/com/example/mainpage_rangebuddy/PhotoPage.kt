package com.example.mainpage_rangebuddy


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import java.io.File
import android.net.Uri
import android.provider.MediaStore
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
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.mainpage_rangebuddy.databinding.ActivityPhotoPageBinding

class PhotoPage : AppCompatActivity() {
    //ui functions variables
    private lateinit var toSessionData: Button
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
        val grouping = findViewById<Button>(R.id.Grouping) // can set this later for Zahra's audio subsystem
        grouping.setOnClickListener {
            Toast.makeText(this, "Set Grouped", Toast.LENGTH_SHORT).show()
        }

        //button mapping to other pages
        toSessionData = findViewById(R.id.toSessionsPage)
        toSessionData.setOnClickListener {
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

    //image calling function
    fun ContentResolver.imagegrabber(
        directoryPath: String,
        imageCount: Int,
    ): List<String> {
        val images = mutableListOf<String>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Images.Media.DATA} like ? "
        val selectionArgs = arrayOf("%$directoryPath%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        // Add this before the cursor:
        Log.d("DirectoryPath", directoryPath)

        cursor?.use { c ->
            val dataIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (dataIndex >= 0) {
                var count = 0
                while (c.moveToNext() && count < imageCount) {
                    val imagePath = c.getString(dataIndex)
                    images.add(imagePath)
                    count++
                }
            }
        }
        // After the cursor, check if any images were found:
        Log.d("ImageCount", images.size.toString())
        return images
    }

    //python calling functions
    private fun comparison() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        //starts up python interpreter
        val py = Python.getInstance()
        //gets the python files
        val pyobj = py.getModule("Comparison")
        val pyobj1 = py.getModule("bullet_coords")
        val pyobj2 = py.getModule("Grouping")
        val pyobj3 = py.getModule("Point_Calcs")
        //sets variables  for image grabbing function
        val directoryPath = "/storage/emulated/0/Android/media/com.example.mainpage_rangebuddy/Main Page-Range Buddy/"
        val output_image = "/storage/emulated/0/Android/media/com.example.mainpage_rangebuddy/Main Page-Range Buddy/bullets/Bullet_Holes.jpg"

        // Check if the directory exists
        val directory = File(directoryPath)
        if (directory.exists()) {
            // Directory exists, log a message
            Log.d("DirectoryCheck", "Directory exists at path: $directoryPath")
        } else {
            // Directory does not exist, log an error message
            Log.e("DirectoryCheck", "Directory does not exist at path: $directoryPath")
        }

        val imageCount = 2
        val images = contentResolver.imagegrabber(directoryPath, imageCount)
        //calls the python function

        //val image1 = "/storage/emulated/0/Android/media/com.example.mainpage_rangebuddy/Main Page-Range Buddy/23-09-21-13-40-43-748.jpg"
        //val image2 = "/storage/emulated/0/Android/media/com.example.mainpage_rangebuddy/Main Page-Range Buddy/23-10-05-12-49-18-367.jpg"
        Toast.makeText(this, "comparison is beginning to run", Toast.LENGTH_SHORT).show()
        val bullet_image = pyobj.callAttr("comparison", images[0], images[1], output_image)
        //Log.d("Python function comparison is being ran", bullet_image.toString())
        Toast.makeText(this, "comparison is finished running & bullet_coords is beginning to run", Toast.LENGTH_LONG).show()

        val bullets_XnY = pyobj1.callAttr("bullet_coords", bullet_image)
        Log.d("Python function bullet_coords is being ran", bullets_XnY.toString())
        Toast.makeText(this, "bullet_coords is finished running", Toast.LENGTH_LONG).show()

        val bullet_group = pyobj2.callAttr("Grouping", bullets_XnY)
        Log.d("Python function Grouping is being ran", bullet_group.toString())
        Toast.makeText(this, "Grouping is finished running", Toast.LENGTH_LONG).show()

        val point_calc = pyobj3.callAttr("Point_Calcs", bullets_XnY, bullet_image) //needs more inputs but might fix it to just have one input to make sure it works
        Log.d("Python function Point_Calcs is being ran", point_calc.toString())
        Toast.makeText(this, "Point_Calcs is finished running", Toast.LENGTH_LONG).show()

    }

}

