package com.example.cano.entrega1

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.format.DateUtils
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.cano.entrega1.FirebaseHelpers.FirebaseRoot.fetchChat
import com.example.cano.entrega1.FirebaseHelpers.FirebaseUtils
import com.example.cano.entrega1.R.id.*
import com.example.cano.entrega1.adapters.ContactsAdapter
import com.example.cano.entrega1.adapters.MessageAdapter
import com.example.cano.entrega1.adapters.MessageAdapter.Companion.TYPE_RECEIVED
import com.example.cano.entrega1.adapters.MessageAdapter.Companion.TYPE_SENT
import com.example.cano.entrega1.model.MyContact
import com.example.cano.entrega1.model.MyMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.activity_home.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ChatRoomActivity : AppCompatActivity(), MessageAdapter.MessageListener{

    private val historial: MutableList<MyMessage> = ArrayList()
    private val mAuth = FirebaseAuth.getInstance()
    private lateinit var adapter : MessageAdapter
    private lateinit var chatReference: DatabaseReference
    private var chatLoaded : Boolean = false


    companion object {

        fun getIntent(context: Context) : Intent {
            val intent = Intent(context, ChatRoomActivity::class.java)
            return intent
        }
    }

    override fun onMessageSelected(message: MyMessage) {
        android.util.Log.i("message", "clicked")
    }

    fun sendMessageToFirebase(chatReference : DatabaseReference, message : MyMessage){
        val id = chatReference.push().key!!
        message.id = id
        val firebaseMessage = message.getFirebaseMessage(id)
        val setter = chatReference.child(id).setValue(firebaseMessage)

        //setter.addOnCompleteListener {
            //val completedMessage = historial.find { it.id!! == id }
            //if (completedMessage != null && completedMessage.position != null){
            //    val view = message_list_view.layoutManager!!.findViewByPosition(completedMessage.position!!)
            //    view?.findViewById<TextView>(R.id.checks)?.text = getString(R.string.checkedMark)
            //    message_list_view.adapter!!.notifyItemChanged(completedMessage.position!!)
            //}
        //}
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val email : String = intent.extras!!.getString("email")!!
        val otherName : String = intent.extras!!.getString("name")!!
        chatReference = fetchChat(mAuth.currentUser!!.email!!, email)

        chatReference.addChildEventListener(messageListener())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        //Toast.makeText(this, email, Toast.LENGTH_LONG).show()

        message_list_view.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(this, historial, otherName)
        adapter.listener = this
        message_list_view.adapter = adapter


        doAsync {
            if (!chatLoaded) {
                chatLoaded = true

                chatReference.addListenerForSingleValueEvent(LoadChatListener())

                uiThread {
                        message_list_view.adapter!!.notifyDataSetChanged()
                    try{
                        message_list_view.smoothScrollToPosition(historial.size-1)
                    } catch (error : Exception){
                        Log.e("size", "out of boundaries")
                    }
                }
            }
        }

        sen_msg_button.setOnClickListener{
            doAsync {
                val message = MyMessage(msg_container_view.text.toString(), mAuth.currentUser!!.email!!,
                        TYPE_SENT,
                        System.currentTimeMillis())


                uiThread {
                    sendMessageToFirebase(chatReference, message)
                }

                msg_container_view.text.clear()
                msg_container_view.onEditorAction(EditorInfo.IME_ACTION_DONE)

            }

        }

    }

    inner class messageListener : ChildEventListener {
        override fun onChildMoved(data: DataSnapshot, p1: String?) {
            // TODO: implement
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            // TODO: implement
        }

        override fun onChildAdded(data: DataSnapshot, p1: String?) {
            doAsync {

                val message = data.getValue(MyMessage::class.java)
                if (message != null) {
                    val type = when (message.sender) {
                        mAuth.currentUser!!.email -> TYPE_SENT
                        else -> TYPE_RECEIVED
                    }
                    historial.add(message)

                    message_list_view.adapter!!.notifyItemInserted(historial.size - 1)
                    message_list_view.smoothScrollToPosition(historial.size-1);

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


    inner class LoadChatListener: ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            dataSnapshot.children.forEach{ child ->

                val message = child.getValue(MyMessage::class.java)
                if (message != null){
                    val type = when(message.sender){
                        mAuth.currentUser!!.email -> TYPE_SENT
                        else -> TYPE_RECEIVED
                    }

                    historial.add(message)



                    //TODO: local_historial
                }
            }


            message_list_view.adapter!!.notifyDataSetChanged()
            try{
                message_list_view.smoothScrollToPosition(historial.size-1);
            }catch (e :Exception){
                Log.e("size", "out of boundaries")
            }

        }

        override fun onCancelled(databaseError: DatabaseError) {
            println("loadPost:onCancelled ${databaseError.toException()}")
        }
    }
}
