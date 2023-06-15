package com.example.bus

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bus.data.UmriBusDatabase
import com.example.bus.data.tiket.UmriTiket
import android.Manifest
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.bus.data.tiket.UmriAddTiketFragment
import com.example.bus.databinding.ActivityTiketBinding
import kotlinx.coroutines.launch
import java.io.File

class UmriTiketActivity : AppCompatActivity() {

    private var _binding: ActivityTiketBinding? = null
    private val binding get() = _binding!!

    private val STORAGE_PERMISSION_CODE = 102
    private val TAG = "PERMSSION_TAG"

    lateinit var tiketRecyclerView: RecyclerView

    lateinit var busDB: UmriBusDatabase

    lateinit var umritiketList : ArrayList<UmriTiket>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityTiketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!checkPermission()) {
            requestPermission()
        }

        busDB = UmriBusDatabase(this@UmriTiketActivity)

        loadDataTiket()

        binding.btnAddTiket.setOnClickListener {
            UmriAddTiketFragment().show(supportFragmentManager, "newTiketTag")
        }

        swipeDelete()

        binding.txtSearchTiket.addTextChangedListener {
            val keyword: String = "${binding.txtSearchTiket.text.toString()}%"
            if(keyword.count() > 2) {
                searchDataTiket(keyword)
            }
            else {
                loadDataTiket()
            }
        }

    }

    private fun checkPermission() : Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        }
        else{
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try{
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
            }
            catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            }
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE)
        }
    }

    fun loadDataTiket() {
        var layoutManager = LinearLayoutManager(this)
        tiketRecyclerView = binding.tiketListView
        tiketRecyclerView.layoutManager = layoutManager
        tiketRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch{
            umritiketList = busDB.getUmriTiketBus().getAllUmriTiket() as ArrayList<UmriTiket>
            Log.e("List tiket", umritiketList.toString())
            tiketRecyclerView.adapter = UmriTiketAdapter(umritiketList)
        }
    }

    fun deleteTiket(tiket: UmriTiket) {
        val builder = AlertDialog.Builder(this@UmriTiketActivity)
        builder.setMessage("Apakah ${tiket.nama_penumpang} ingin dihapus ?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                lifecycleScope.launch {
                    busDB.getUmriTiketBus().deleteTiket(tiket)
                    loadDataTiket()
                }
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory("")
                val foto_delete = File(imagesDir, tiket.foto_KTP)

                if (foto_delete.exists()) {
                    if(foto_delete.delete()) {
                        val toastDelete = Toast.makeText(applicationContext,
                        "file edit foto delete", Toast.LENGTH_LONG)
                        toastDelete.show()
                    }
                }
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
                loadDataTiket()
            }
        val alert = builder.create()
        alert.show()
    }

    fun swipeDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
        ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                lifecycleScope.launch {
                    umritiketList = busDB.getUmriTiketBus().getAllUmriTiket() as ArrayList<UmriTiket>
                    Log.e("position swiped", umritiketList[position].toString())
                    Log.e("position swiped", umritiketList.size.toString())


                    deleteTiket(umritiketList[position])
                }
            }
        }).attachToRecyclerView(tiketRecyclerView)
    }

    fun searchDataTiket(keyword: String){
        var layoutManager = LinearLayoutManager(this)
        tiketRecyclerView = binding.tiketListView
        tiketRecyclerView.layoutManager = layoutManager
        tiketRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch {
            umritiketList = busDB.getUmriTiketBus().searchTiket(keyword) as ArrayList<UmriTiket>
            Log.e("list tiket", umritiketList.toString())
            tiketRecyclerView.adapter = UmriTiketAdapter(umritiketList)
        }
    }

}