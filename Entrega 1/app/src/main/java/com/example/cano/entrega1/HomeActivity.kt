package com.example.cano.entrega1

import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.util.Log.e
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.example.cano.entrega1.FirebaseHelpers.FirebaseRoot
import com.example.cano.entrega1.FirebaseHelpers.FirebaseRoot.addToken
import com.example.cano.entrega1.FirebaseHelpers.FirebaseUtils
import com.example.cano.entrega1.R.id.*
import com.example.cano.entrega1.adapters.ContactsAdapter
import com.example.cano.entrega1.model.AndroidContact
import com.example.cano.entrega1.model.MyContact
import com.example.cano.entrega1.tasks.ContactSetupTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId


import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.no_contact_permission.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.ref.WeakReference
import java.util.logging.Logger
import kotlin.math.log

class HomeActivity : Activity(), ContactsAdapter.ContactListener {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var localContacts: List<AndroidContact>
    private lateinit var user : FirebaseUser

    private val chatContacts: MutableList<MyContact> = ArrayList()
    private val contactEventListener = ContactListener()
    private var firebaseContactsNode: DatabaseReference? = null

    private var fabHeight : Int? = null
    private var fabWidth : Int? = null

    @Volatile private var contactsLoaded = false
    @Volatile private var contactsLoadedFromAsync = false

    private var mContext : Context? = null


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

        val token= FirebaseInstanceId.getInstance().getToken()!!
        addToken(mAuth.currentUser!!.email!!, token)

        mContext = this

        contacts_list.layoutManager = LinearLayoutManager(this)
        val adapter = ContactsAdapter(this, chatContacts)
        adapter.listener = this
        contacts_list.adapter = adapter
        contacts_list.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))

        toolbar.title = mAuth.currentUser!!.email

        fabHeight = floatingMenu.layoutParams.height
        fabWidth = floatingMenu.layoutParams.width

        fabAddContact.setOnClickListener{
            addContact()
        }

        fabLogout.setOnClickListener{
            logout()
        }

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)

        for (i in 0.until(menu!!.size())) {
            val icon: Drawable? = menu.getItem(i)!!.icon
            if (icon != null) {
                val drawable = icon.mutate()
                drawable.setColorFilter(getColor(R.color.primary_dark_material_dark),
                        PorterDuff.Mode.SRC_IN) //TODO: Choose color
            }
        }

        return true
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                R.id.action_logout -> logout()
            }
        }
        return true
    }

    private fun addContact() {
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = ContactsContract.Contacts.CONTENT_TYPE
        startActivity(intent)
    }

    override fun onContactSelected(contact: MyContact) {
        // TODO: start chat room with selected contact

        val intent = ChatRoomActivity.getIntent(this)
        intent.putExtra("email", contact.email)
        intent.putExtra("name", contact.name)
        startActivity(intent)
    }

    private fun configureWidgetVisibility() {
        if (contactsPermissionGranted) {
            contacts_list.visibility = View.VISIBLE
            contacts_permission_button.visibility = View.GONE
            no_contact_permission_msg.visibility = View.GONE
            floatingMenu.layoutParams.height = fabHeight!!
            floatingMenu.layoutParams.width = fabWidth!!
            floatingMenu.visibility = View.VISIBLE
        } else {
            contacts_list.visibility = View.GONE
            floatingMenu.layoutParams.height = 0
            floatingMenu.layoutParams.width = 0
            floatingMenu.visibility = View.GONE

            contacts_permission_button.visibility = View.VISIBLE
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
                    .show()
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

            doAsync {
                if (!contactsLoadedFromAsync) {
                    contactsLoadedFromAsync = true

                    firebaseContacts.addListenerForSingleValueEvent(LoadContactListener())

                    uiThread {
                        if (localContacts.isNotEmpty()) {
                            contacts_list.adapter!!.notifyDataSetChanged()
                        }
                    }
                }
            }

        }
        setupTask.execute()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = LoginActivity.getIntent(this)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
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
                val contact = localContacts.find {it.email == FirebaseUtils.decode(data.key!!) }
                if (contact != null && chatContacts.find { it.email == contact.email } == null) {
                    val newContact = contact.getMyContact()

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

    inner class LoadContactListener: ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            var toastText = "users:"
            dataSnapshot.children.forEach{child ->

                val contact = localContacts.find {it.email == FirebaseUtils.decode(child.key!!) }
                if (contact != null && chatContacts.find { it.email == contact.email } == null) {
                    val newContact = contact.getMyContact()
                    chatContacts.add(newContact)
                    toastText = "${toastText} | ${contact.name}"
                }
            }

            //Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show()


            if (localContacts.isNotEmpty()) { contacts_list.adapter!!.notifyDataSetChanged() }

        }

        override fun onCancelled(databaseError: DatabaseError) {
            println("loadPost:onCancelled ${databaseError.toException()}")
        }
    }

    override fun onBackPressed() {
        Log.e("back", "·")
        //super.onBackPressed()
    }

}
