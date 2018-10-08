package com.example.cano.entrega1.model

class AndroidContact(val email: String, val name: String) {
    fun getMyContact(): MyContact {
        return MyContact(email, name)
    }
}