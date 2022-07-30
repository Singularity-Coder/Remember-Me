package com.singularitycoder.rememberme

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.rememberme.databinding.ListItemContactBinding

class ContactsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var contactsList = emptyList<Contact>()
    private var itemClickListener: (contact: Contact) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ContactViewHolder).setData(contactsList[position])
    }

    override fun getItemCount(): Int = contactsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setItemClickListener(listener: (contact: Contact) -> Unit) {
        itemClickListener = listener
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
                ivImage.setOnClickListener {
                    itemClickListener.invoke(contact)
                }
                ivImage.load(contact.imagePath.toUri()) {
                    placeholder(R.drawable.ic_placeholder)
                }
            }
        }
    }
}
