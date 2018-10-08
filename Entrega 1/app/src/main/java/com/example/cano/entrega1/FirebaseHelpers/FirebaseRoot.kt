package com.example.cano.entrega1.FirebaseHelpers

import android.net.Uri
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

    val firebase = FirebaseDatabase.getInstance().reference

    /**
     * Adds a contact for [user]
     */
    fun addContact(user: String, contact: String) {
        val userHtml = FirebaseUtils.encode(user)
        val contactHtml = FirebaseUtils.encode(contact)

        firebase.child(NODE_USERS).child(contactHtml).child(NODE_CONTACTS).child(userHtml).setValue(true)
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

}