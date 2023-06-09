package com.example.cameraapptestcode

import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Environment.*
import android.view.TextureView
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.cameraapptestcode.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var cameraHelper: CameraHelper

    private lateinit var imageList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)

        var isFirstSelection = true
        cameraHelper = CameraHelper()
        getPermission()

        binding.textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                cameraHelper.openCamera(binding.textureView)
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
            }
        }

        binding.buttonCapture.setOnClickListener {
            cameraHelper.imageCapRequest()
            cameraHelper.takePicture(binding.buttonViewImage)
            Toast.makeText(this@MainActivity, "Image Captured", Toast.LENGTH_SHORT).show()
            //binding.buttonViewImage.setImageURI(imageList[imageList.size - 1].toUri())

        }

        binding.buttonSwitchCamera.setOnClickListener {
            cameraHelper.switchCamera(binding.textureView)
        }

        binding.buttonZoomIn.setOnClickListener{
            cameraHelper.zoomIn(binding.textureView)
        }

        binding.buttonZoomOut.setOnClickListener{
            cameraHelper.zoomOut(binding.textureView)
        }


        // Create an ArrayAdapter using the string array and a default spinner layout
        val items = listOf("1:1", "4:3", "16:9")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, items)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }
                cameraHelper.setAspectRatioBySpinner(p0?.selectedItem.toString(), binding.textureView)
                //Toast.makeText(this@MainActivity, "Selected item: " + p0!!.selectedItem.toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        val dcimDirectory = getExternalStoragePublicDirectory(DIRECTORY_DCIM)

        imageList =
            cameraHelper.getImagesFromFolder(dcimDirectory.absolutePath.toString() + "/Mycamera")

        if (imageList.isNotEmpty()) {
            binding.buttonViewImage.setImageURI(imageList[imageList.size - 1].toUri())
        }
        binding.buttonViewImage.setOnClickListener {
            startActivity(Intent(applicationContext, ImagesActivity::class.java))
        }
    }

    private fun getPermission() {
        val permissionlist = mutableListOf<String>()

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) permissionlist.add(
            android.Manifest.permission.CAMERA
        )
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) permissionlist.add(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) permissionlist.add(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permissionlist.size > 0) {
            requestPermissions(permissionlist.toTypedArray(), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                getPermission()
            }
        }
    }

}