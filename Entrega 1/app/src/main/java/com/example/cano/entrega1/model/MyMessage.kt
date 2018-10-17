package com.example.cano.entrega1.model

import java.text.FieldPosition

class MyMessage(val message:String="", val sender: String="", var type:Int?=-1,  val createdAt: Long?=null,
                var id:String?=null, var position:Int?=null){
    fun getFirebaseMessage(id :String) : FirebaseMessage{
        return FirebaseMessage(id, message, sender, createdAt!!)
    }
}