package com.example.cano.entrega1.model

class FirebaseMessage(val id: String,
                      val message: String,
                      val sender: String,
                      val createdAt: Long,
                      val read: Boolean = false)