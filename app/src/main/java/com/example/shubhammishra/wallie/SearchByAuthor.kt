package com.example.shubhammishra.wallie

import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.shubhammishra.wallie.Adapter.GridAdapter
import kotlinx.android.synthetic.main.activity_search_by_author.*
import kotlinx.android.synthetic.main.activity_wallpaper.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.centerCrop
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.example.shubhammishra.wallie.R.drawable.placeholder
import com.example.shubhammishra.wallie.R.id.progress
import kotlinx.android.synthetic.main.list_view_wallie.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URL
import java.security.AccessController.getContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest


class SearchByAuthor : AppCompatActivity() {
    lateinit var grid: GridPojo

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode==100)
        {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                GetImage().execute(grid.imgUrl)
            }
            else
            {
                Toast.makeText(this@SearchByAuthor,"Can't download the image without your permission",Toast.LENGTH_SHORT).show()
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    lateinit var progressDialog:AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_author)
        grid = intent.getSerializableExtra("GridPojo") as GridPojo
        Glide.with(this).load(grid.imgUrl)
                .thumbnail(Glide.with(this).load(R.drawable.loadgif))
                .fitCenter()
                .crossFade()
                .into(ivMain);
        tvSize.text = "Size: " + (grid.bytes / 1024).toString() + "kb"
        tvDate.text = "Date:" + grid.date.substring(0, 10)

        tvResolution.text = "Resolution: " + grid.width.toString() + "x" + grid.height.toString()
        tvDownloads.text = "Downloads: " + grid.users.toString()
        tvUploader.text = "Uploader: " + grid.uploader
        tvSuggestion.text = "More Wallpapers by " + grid.uploader
        btnSetas.setOnClickListener({
            Log.d("Worked", "Worked Properly")
            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            try {
                wallpaperManager.setBitmap(viewToBitmap(ivMain, ivMain.width, ivMain.height))
                Toast.makeText(this@SearchByAuthor, "Set AS Wallpaper", Toast.LENGTH_SHORT).show()
                val perm=ContextCompat.checkSelfPermission(this@SearchByAuthor,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if(perm==PackageManager.PERMISSION_GRANTED)
                {
                    GetImage().execute(grid.imgUrl)
                }
                else {
                    ActivityCompat.requestPermissions(this@SearchByAuthor, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
        btnDownload.setOnClickListener {
            val alert = AlertDialog.Builder(this@SearchByAuthor).create()
            alert.setTitle("Download")
            alert.setMessage("Do you want to download the image?")
            alert.setButton("Yes", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val perm=ContextCompat.checkSelfPermission(this@SearchByAuthor,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if(perm==PackageManager.PERMISSION_GRANTED)
                    {
                        GetImage().execute(grid.imgUrl)
                    }
                    else {
                        ActivityCompat.requestPermissions(this@SearchByAuthor, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
                    }
                }
            })
            alert.setButton2("No", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    alert.dismiss()
                }
            })
            alert.show()
        }
        GetWallpaper("https://api.desktoppr.co/1/users/${grid.uploader}/wallpapers")
    }

    inner class GetImage:AsyncTask<String,Unit,Bitmap?>()
    {
        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            val finalBitmap = result
            val root = Environment.getExternalStorageDirectory().toString()
            val myDir = File("$root/Wallie")
            myDir.mkdirs()
            val n1=grid.id.toString()
            val fname = "Image-$n1.jpg"
            val file = File(myDir,fname)
            if (file.exists())
                file.delete()
            try {
                val out = FileOutputStream(file)
                finalBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
                Toast.makeText(this@SearchByAuthor, "Wallpaper Downloaded Successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("Error","Directory Not Found")
                Toast.makeText(this@SearchByAuthor, "Directory Not Found!", Toast.LENGTH_SHORT).show()
            }
            refreshGallery(file)
        }

        override fun doInBackground(vararg params: String?): Bitmap? {
            val url=params[0]
            var bitmap:Bitmap?=null
            try{
                val input=URL(url).openStream()
                bitmap=BitmapFactory.decodeStream(input)

            }
            catch (e:Exception)
            {
                e.printStackTrace()
            }
            return bitmap
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog=AlertDialog.Builder(this@SearchByAuthor).create()
            progressDialog.setMessage("Downloading....")
            progressDialog.show()
        }
    }
    private fun refreshGallery(new_file: File) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.setData(Uri.fromFile(new_file))
        sendBroadcast(intent)
    }
    fun viewToBitmap(view: View, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    fun GetWallpaper(url: String): Unit {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {

            }

            override fun onResponse(call: Call?, response: Response?) {
                val result = response!!.body()!!.string()
                val wallArray = ArrayList<GridPojo>()
                try {
                    val jsonObject = JSONObject("$result")
                    val jsonArray = jsonObject.getJSONArray("response")
                    for (i in 0..jsonArray.length() - 1) {
                        val imageObject = jsonArray.getJSONObject(i)
                        val id = imageObject.getInt("id")
                        val bytes = imageObject.getInt("bytes")
                        val date = imageObject.getString("created_at")
                        val height = imageObject.getInt("height")
                        val width = imageObject.getInt("width")
                        val uploader = imageObject.getString("uploader")
                        val user = imageObject.getInt("user_count")
                        val likes = imageObject.getInt("likes_count")
                        val image = imageObject.getJSONObject("image")
                        val url = image.getString("url")
                        val thumb = image.getJSONObject("thumb")
                        val thumbUrl = thumb.getString("url")
                        wallArray.add(GridPojo(id, bytes, date, url, thumbUrl, height, width, uploader, user, likes))
                    }
                } catch (e: JSONException) {
                    Log.e("Error", "Invalid Json")
                }
                this@SearchByAuthor.runOnUiThread(object : Runnable {
                    override fun run() {
                        val recyclerAdapter = GridAdapter(wallArray, { grid1: GridPojo ->
                            grid = grid1
                            Glide.with(this@SearchByAuthor).load(grid.imgUrl)
                                    .thumbnail(Glide.with(this@SearchByAuthor).load(R.drawable.loadgif))
                                    .fitCenter()
                                    .crossFade()
                                    .into(ivMain);
                            tvSize.text = "Size: " + (grid.bytes / 1024).toString() + "kb"
                            tvDate.text = "Date:" + grid.date.substring(0, 10)

                            tvResolution.text = "Resolution: " + grid.width.toString() + "x" + grid.height.toString()
                            tvDownloads.text = "Downloads: " + grid.users.toString()
                            tvUploader.text = "Uploader: " + grid.uploader
                            tvSuggestion.text = "More Wallpapers by " + grid.uploader
                            GetWallpaper("https://api.desktoppr.co/1/users/${grid.uploader}/wallpapers")
                        })
                        uploaderRecycler.layoutManager = GridLayoutManager(this@SearchByAuthor, 2)
                        uploaderRecycler.adapter = recyclerAdapter
                    }
                })
            }
        })
    }
}
