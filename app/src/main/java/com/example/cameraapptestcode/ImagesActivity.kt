package com.example.cameraapptestcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cameraapptestcode.databinding.ActivityImagesBinding

class ImagesActivity : AppCompatActivity() {

    private lateinit var cameraHelper: CameraHelper
    private lateinit var adapter: RecyclerAdapter

    private lateinit var imageList: List<String>

    private val binding by lazy {
        ActivityImagesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)

        cameraHelper = CameraHelper()

        val dcimDirectory = getExternalStoragePublicDirectory(DIRECTORY_DCIM)
        imageList =
            cameraHelper.getImagesFromFolder(dcimDirectory.absolutePath.toString() + "/Mycamera")
        binding.recyclerView.layoutManager = LinearLayoutManager(this@ImagesActivity)
        adapter = RecyclerAdapter(imageList)
        binding.recyclerView.adapter = adapter

    }
}