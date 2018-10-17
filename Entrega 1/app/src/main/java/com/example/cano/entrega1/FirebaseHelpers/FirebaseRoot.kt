package com.example.cano.entrega1.FirebaseHelpers

import android.net.Uri
import android.util.Log
import com.example.cano.entrega1.model.FirebaseMessage
import com.example.cano.entrega1.model.MyMessage
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

/**
 * Entry point for the database stored in the cloud.
 *
 * The following is the schema for the DB:
 *
 * + root
 *     + users
 *         + {userEmail}
 *             + contacts
 *                 + {contactEmail} : true
 *
 */
object FirebaseRoot {
    const val NODE_USERS = "users"
    const val NODE_CONTACTS = "contacts"
    const val NODE_HISTORY = "history"
    const val NODE_CONST = "const"
    const val NODE_TOKEN = "token"

    val firebase = FirebaseDatabase.getInstance().reference

    /**
     * Adds a contact for [user]
     */
    fun addContact(user: String, contact: String) {
        val userHtml = FirebaseUtils.encode(user)
        val contactHtml = FirebaseUtils.encode(contact)

        firebase.child(NODE_USERS).child(contactHtml).child(NODE_CONTACTS).child(userHtml).child(NODE_CONST).setValue(true)
    }

    /**
     * Fetches a user's contacts registered on Firebase. Do note this list is not necessarily the
     * list of all contacts one can chat with, it still needs to be compared with the local contact
     * list for determining valid chat contacts.
     */
    fun fetchContacts(user: String) : DatabaseReference {
        val userHtml = FirebaseUtils.encode(user)

        return firebase.child(NODE_USERS).child(userHtml).child(NODE_CONTACTS)
    }

    fun fetchChat(user: String, other: String) : DatabaseReference{
        val userHtml = FirebaseUtils.encode(user)
        val otherHtml = FirebaseUtils.encode(other)

        val lengthThenNatural = compareBy<String> { it.length }
                .then(naturalOrder())

        val list = listOf(userHtml,otherHtml)
        val first = list.sortedWith(lengthThenNatural)[0]
        val second = list.sortedWith(lengthThenNatural)[1]

        Log.w("reference", first)

        return firebase.child(NODE_USERS).child(first).child(NODE_CONTACTS).child(second)
                .child(NODE_HISTORY)
    }

    fun addToken(user: String, token: String){
        val userHtml = FirebaseUtils.encode(user)

        firebase.child(NODE_USERS).child(userHtml).child(NODE_TOKEN).setValue(token)
    }

}