package com.example.cano.entrega1.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.cano.entrega1.R
import com.example.cano.entrega1.model.MyContact

/**
 * Recycler view adapter for managing cells that display the different contact's information.
 */
class ContactsAdapter(val context: Context, val contacts: List<MyContact>) : RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {
    var listener: ContactListener? = null

    interface ContactListener {
        fun onContactSelected(contact: MyContact)
    }

    inner class ContactsViewHolder(v: View) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.list_item_contact, parent, false)
        return ContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val contact = contacts[position]

        val txtContact = holder.itemView.findViewById<TextView>(R.id.txt_contact_name)
        txtContact.text = contact.name

        holder.itemView.setOnClickListener{ listener?.onContactSelected(contact) }

    }

    override fun getItemCount(): Int {
        return contacts.size
    }


}