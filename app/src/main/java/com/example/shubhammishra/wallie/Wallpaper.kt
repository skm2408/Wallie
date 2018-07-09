package com.example.shubhammishra.wallie

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import com.example.shubhammishra.wallie.Adapter.GridAdapter
import com.example.shubhammishra.wallie.R.id.wallRecycle
import kotlinx.android.synthetic.main.activity_wallpaper.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.Serializable

class Wallpaper : AppCompatActivity() {
    var current:Int=1
    var previous:Int?=0
    var next:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper)
        progress.visibility= View.VISIBLE
        btn_next.setOnClickListener({
            try {
                progress.visibility= View.VISIBLE
                GetWallpaper("https://api.desktoppr.co/1/wallpapers?page=$next")
            }
            catch (e:IOException){
                Log.e("Error","Invalid Url")
            }
        })
        btn_prev.setOnClickListener({
            if(previous!=null)
            {try {
                progress.visibility= View.VISIBLE
                GetWallpaper("https://api.desktoppr.co/1/wallpapers?page=$previous")
            }
            catch (e:IOException){
                Log.e("Error","Invalid Url")
            }
            }
        })
        try {
            GetWallpaper("https://api.desktoppr.co/1/wallpapers?page=1")
        }
        catch (e:IOException){
            Log.e("Error","Invalid Url")
        }
    }
    fun GetWallpaper(url:String):Unit
    {
        val client=OkHttpClient()
        val request=Request.Builder().url(url).build()
        client.newCall(request).enqueue(object:Callback{
            override fun onFailure(call: Call?, e: IOException?) {

            }

            override fun onResponse(call: Call?, response: Response?) {
                val result=response!!.body()!!.string()
                val wallArray=ArrayList<GridPojo>()
                try {
                    val jsonObject=JSONObject("$result")
                    val jsonArray=jsonObject.getJSONArray("response")
                    for(i in 0..jsonArray.length()-1)
                    {
                        val imageObject=jsonArray.getJSONObject(i)
                        val id=imageObject.getInt("id")
                        val bytes=imageObject.getInt("bytes")
                        val date=imageObject.getString("created_at")
                        val height=imageObject.getInt("height")
                        val width=imageObject.getInt("width")
                        val uploader=imageObject.getString("uploader")
                        val user=imageObject.getInt("user_count")
                        val likes=imageObject.getInt("likes_count")
                        val image=imageObject.getJSONObject("image")
                        val url=image.getString("url")
                        val thumb=image.getJSONObject("thumb")
                        val thumbUrl=thumb.getString("url")
                        val pagination=jsonObject.getJSONObject("pagination")
                        current=pagination.getInt("current")
                        if(current==1)
                            previous=null
                        else
                        {previous=pagination.getInt("previous")}
                        next=pagination.getInt("next")
                        wallArray.add(GridPojo(id,bytes,date,url,thumbUrl,height,width,uploader,user,likes))
                    }
                }
                catch (e:JSONException)
                {
                    Log.e("Error","Invalid Json")
                }
                this@Wallpaper.runOnUiThread(object :Runnable{
                    override fun run() {
                        val recyclerAdapter=GridAdapter(wallArray) { grid:GridPojo->
                            val intent= Intent(this@Wallpaper,SearchByAuthor::class.java)
                            intent.putExtra("GridPojo",grid)
                            startActivity(intent)
                        }
                        tvCurrentPage.text="Page: "+current.toString()
                        wallRecycle.layoutManager=GridLayoutManager(this@Wallpaper,2)
                        wallRecycle.adapter=recyclerAdapter
                        progress.visibility=View.GONE
                    }
                })
            }
        })
    }
}
