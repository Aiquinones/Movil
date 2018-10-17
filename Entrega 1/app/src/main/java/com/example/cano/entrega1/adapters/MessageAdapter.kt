package com.example.cano.entrega1.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.cano.entrega1.ChatRoomActivity
import com.example.cano.entrega1.R
import com.example.cano.entrega1.model.MyContact
import com.example.cano.entrega1.model.MyMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.item_message_received.view.*
import kotlinx.android.synthetic.main.item_message_sent.view.*



/**
 * Recycler view adapter for managing cells that display the different contact's information.
 */
class MessageAdapter(val context: Context, val messageList: List<MyMessage>, val nickname: String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listener: MessageListener? = null

    val mAuth = FirebaseAuth.getInstance()

    companion object {
        const val TYPE_SENT = 0
        const val TYPE_RECEIVED = 1
    }

    interface MessageListener {
        fun onMessageSelected(message: MyMessage)
    }

    inner class SentMessageHolder(v: View) : RecyclerView.ViewHolder(v)
    inner class ReceivedMessageHolder(v: View) : RecyclerView.ViewHolder(v)

    override fun getItemViewType(position: Int): Int {
        val currentUserMail = mAuth.currentUser!!.email!!
        try{
            val senderName = messageList[position].sender
            messageList[position].type = when (messageList[position].sender) {
                mAuth.currentUser!!.email -> TYPE_SENT
                else -> TYPE_RECEIVED
            }
            return  messageList[position].type!!
        } catch (e : Exception){
            return TYPE_RECEIVED
        }

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        return when (viewType){
            TYPE_RECEIVED -> ReceivedMessageHolder(
                    layoutInflater.inflate(R.layout.item_message_received, parent, false))
            else -> SentMessageHolder(
                    layoutInflater.inflate(R.layout.item_message_sent, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val message = messageList[position]
        message.position = position

        holder.itemView.setOnClickListener{ listener?.onMessageSelected(message) }

        if (message.type == TYPE_SENT){

            val text_message_body = holder.itemView.findViewById<TextView>(
                    R.id.text_message_body_sent)!!
            val text_message_time = holder.itemView.findViewById<TextView>(
                    R.id.text_message_time_sent)!!


            text_message_body.text = message.message
            text_message_time.text = DateUtils.formatDateTime(context, message.createdAt!!,
                    DateUtils.FORMAT_SHOW_TIME)
        }else{

            val text_message_body = holder.itemView.findViewById<TextView>(
                    R.id.text_message_body)!!
            val text_message_time = holder.itemView.findViewById<TextView>(
                    R.id.text_message_time)!!
            val text_message_name = holder.itemView.findViewById<TextView>(
                    R.id.text_message_name)!!

            text_message_body.text = message.message
            text_message_time.text = DateUtils.formatDateTime(context, message.createdAt!!,
                    DateUtils.FORMAT_SHOW_TIME)
            text_message_name.text = nickname
        }


    }

    override fun getItemCount(): Int {
        return messageList.size
    }


}