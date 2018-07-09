package com.example.shubhammishra.wallie

import java.io.Serializable

data class GridPojo(val id:Int,val bytes:Int,val date:String,val imgUrl:String,val thumb:String,
                    val height:Int,val width:Int,val uploader:String,val users:Int,val likes:Int):Serializable