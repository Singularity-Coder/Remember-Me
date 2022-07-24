package com.singularitycoder.rememberme

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.rememberme.databinding.ListItemContactBinding

class ContactsAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var contactsList = emptyList<Contact>()
    private var callClickListener: (contact: Contact) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ContactViewHolder).setData(contactsList[position])
    }

    override fun getItemCount(): Int = contactsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setCallClickListener(listener: (contact: Contact) -> Unit) {
        callClickListener = listener
    }

    inner class ContactViewHolder(
        private val itemBinding: ListItemContactBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun setData(contact: Contact) {
            itemBinding.apply {
                tvContactName.text = contact.name
                tvContactPhoneNumber.text = contact.mobileNumber
                tvDateAdded.text = contact.dateAdded.toIntuitiveDateTime()
                ivCall.setOnClickListener {
                    callClickListener.invoke(contact)
                }
                ivImage.load(contact.photo) {
                    placeholder(R.drawable.ic_placeholder)
                }
            }
        }
    }
}
