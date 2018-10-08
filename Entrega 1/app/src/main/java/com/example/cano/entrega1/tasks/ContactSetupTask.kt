package com.example.cano.entrega1.tasks

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.provider.ContactsContract
import com.example.cano.entrega1.FirebaseHelpers.FirebaseRoot
import com.example.cano.entrega1.model.AndroidContact
import com.example.cano.entrega1.model.MyContact
import java.lang.ref.WeakReference

/**
 * This task pushes into each possible contact's contact list (identified by email) myself as a possible contact.
 * For somebody to appear in my contact list, that somebody must first have executed this task (assuming
 * I appear in his contact list).
 */
class ContactSetupTask(val ref: WeakReference<Context>,
                       val user: String,
                       val onComplete: (List<AndroidContact>) -> Unit ) : AsyncTask<Unit, Unit, Unit>() {
    private val contacts: MutableList<AndroidContact> = ArrayList()

    override fun doInBackground(vararg p0: Unit?) {
        val context = ref.get()
        if (context != null) {
            val contactCursor = context.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME)
            while (contactCursor.moveToNext()) {
                val contactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID))
                val contactName = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                var contactEmail: String? = null

                val emailCursor = context.contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = $contactId", null, null)
                if (emailCursor.moveToFirst()) {
                    // TODO: a contact can have multiple emails, for this demo, we are only considering the first one he may have registered
                    contactEmail = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                }
                emailCursor.close()

                if (contactEmail != null) {
                    contacts.add(AndroidContact(contactEmail, contactName))
                }
            }
            contactCursor.close()

            // Cached contacts are those we know have already been uploaded to Firebase, if a Contacts Provider contact has an email
            // and is not in this list, then it needs to be uploaded.

            // NOTE: The following is O(n^2). Can be improved by listening on Contacts Provider changes.
            // That is too much of a hassle, this is good enough for the demo.
            contacts.forEach{ contact ->
                FirebaseRoot.addContact(user, contact.email)
            }
        }

        return
    }

    override fun onPostExecute(result: Unit?) {
        onComplete(contacts)
    }
}