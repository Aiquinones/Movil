package com.example.cano.entrega1

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.example.cano.entrega1.FirebaseHelpers.FirebaseRoot
import com.example.cano.entrega1.FirebaseHelpers.FirebaseUtils
import com.example.cano.entrega1.adapters.ContactsAdapter
import com.example.cano.entrega1.model.AndroidContact
import com.example.cano.entrega1.model.MyContact
import com.example.cano.entrega1.tasks.ContactSetupTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference


import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.no_contact_permission.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.ref.WeakReference

class HomeActivity : Activity(), ContactsAdapter.ContactListener {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var localContacts: List<AndroidContact>
    private lateinit var user : FirebaseUser

    private val chatContacts: MutableList<MyContact> = ArrayList()
    private val contactEventListener = ContactListener()
    private var firebaseContactsNode: DatabaseReference? = null



    private val contactsPermissionGranted
        get() = checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED



    companion object {

        fun getIntent(context: Context) : Intent {
            val intent = Intent(context, HomeActivity::class.java)
            return intent
        }
        const val REQUEST_CODE_CONTACTS_PERMISSION = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mAuth = FirebaseAuth.getInstance()
        contacts_list.layoutManager = LinearLayoutManager(this)
        val adapter = ContactsAdapter(this, chatContacts)
        adapter.listener = this
        contacts_list.adapter = adapter
        contacts_list.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))



        configureWidgetVisibility()
        contacts_permission_button.setOnClickListener{requestContactsPermission()}


    }

    override fun onStart() {
        super.onStart()

        if (contactsPermissionGranted) {
            loadContacts()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseContactsNode?.removeEventListener(contactEventListener)

    }

    private fun addContact() {
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = ContactsContract.Contacts.CONTENT_TYPE
        startActivity(intent)
    }

    override fun onContactSelected(contact: MyContact) {
        // TODO: start chat room with selected contact
    }

    private fun configureWidgetVisibility() {
        if (contactsPermissionGranted) {
            contacts_list.visibility = View.VISIBLE
            no_contact_permission_msg.visibility = View.GONE
        } else {
            contacts_list.visibility = View.GONE
            no_contact_permission_msg.visibility = View.VISIBLE
        }
    }

    private fun requestContactsPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS)) {
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.contacts_permissions_rationale))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), REQUEST_CODE_CONTACTS_PERMISSION)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create()
                    .show()

        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), REQUEST_CODE_CONTACTS_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            configureWidgetVisibility()
            loadContacts()
        } else if (!shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS)) {
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.contacts_permissions_denied_forever))
                    .setPositiveButton(getString(R.string.ok),  null)
                    .create()
                    .show();
        }
    }

    private fun loadContacts(){
        user =  mAuth.currentUser!!
        val setupTask = ContactSetupTask(WeakReference(this),user.email!!) { androidContacts ->
            localContacts = androidContacts

            configureWidgetVisibility()
            val firebaseContacts = FirebaseRoot.fetchContacts(user.email!!)

            firebaseContacts.addChildEventListener(contactEventListener)

            firebaseContactsNode = firebaseContacts
        }
        setupTask.execute()
    }

    /**
     * Firebase child listener for updating contact list whenever a new contact is added.
     */
    inner class ContactListener : ChildEventListener {
        override fun onChildMoved(data: DataSnapshot, p1: String?) {
            // TODO: implement
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            // TODO: implement
        }

        override fun onChildAdded(data: DataSnapshot, p1: String?) {
            doAsync {
                //latch.await()
                // For a MemeContact from Firebase to be added locally, it needs to appear on the android contact list and NOT appear on the cached contact list
                val contact = localContacts.find { it.email != user.email && it.email == FirebaseUtils.decode(data.key!!) }
                if (contact != null && chatContacts.find { it.email == contact.email } == null) {
                    val newContact = MyContact(contact.email, contact.name)

                    chatContacts.add(newContact)

                    uiThread {
                        contacts_list.adapter!!.notifyItemInserted(chatContacts.size - 1)
                    }
                }

                // TODO: remove from cached contacts those that do not appear on firebase
            }


        }

        override fun onChildRemoved(p0: DataSnapshot) {
            // TODO: implement
        }

        override fun onCancelled(data: DatabaseError) {
            // TODO: implement
        }
    }

}
