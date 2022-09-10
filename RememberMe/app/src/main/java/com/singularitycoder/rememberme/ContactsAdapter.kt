package com.singularitycoder.rememberme

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.singularitycoder.rememberme.databinding.ListItemContactBinding
import com.singularitycoder.rememberme.helpers.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var openCardPosition: Int = -1
    private var closedCardPosition: Int = -1

    var contactsList = mutableListOf<Contact>()
    private var itemLongClickListener: (contact: Contact, position: Int) -> Unit = { contact, position -> }
    private var itemClickListener: (contact: Contact, isExpanded: Boolean) -> Unit = { contact, isExpanded -> }
    private var imageClickListener: (contact: Contact) -> Unit = {}
    private var editContactClickListener: (contact: Contact, position: Int) -> Unit = { contact, position -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ContactViewHolder).setData(contactsList[position])
    }

    override fun getItemCount(): Int = contactsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setItemClickListener(listener: (contact: Contact, isExpanded: Boolean) -> Unit) {
        itemClickListener = listener
    }

    fun setItemLongClickListener(listener: (contact: Contact, position: Int) -> Unit) {
        itemLongClickListener = listener
    }

    fun setImageClickListener(listener: (contact: Contact) -> Unit) {
        imageClickListener = listener
    }

    fun setEditContactClickListener(listener: (contact: Contact, position: Int) -> Unit) {
        editContactClickListener = listener
    }

    inner class ContactViewHolder(
        private val itemBinding: ListItemContactBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("SetTextI18n")
        fun setData(contact: Contact) {
            itemBinding.apply {
                tvAlphabet.isVisible = contact.isAlphabetShown
                tvAlphabet.text = contact.name.subSequence(0, 1)
                tvContactName.text = contact.name
                tvContactPhoneNumber.text = contact.mobileNumber
                tvDateAdded.text = contact.dateAdded.toIntuitiveDateTime()
                ivImage.setOnClickListener {
                    imageClickListener.invoke(contact)
                }
                root.setOnLongClickListener {
                    itemLongClickListener.invoke(contact, bindingAdapterPosition)
                    false
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    if (contact.videoPath.isNotBlank()) {
                        CoroutineScope(IO).launch {
                            val videoBitmap = root.context.getVideoThumbnailBitmap(contact.videoPath.toUri())
                            withContext(Main) {
                                ivImage.load(videoBitmap) {
                                    placeholder(R.drawable.ic_placeholder)
//                                error(R.drawable.ic_placeholder)
                                }
                            }
                        }
                    }
                } else {
                    if (contact.imagePath.isNotBlank()) {
                        ivImage.load(contact.imagePath.toUri()) {
                            placeholder(R.drawable.ic_placeholder)
//                        error(R.drawable.ic_placeholder)
                        }
                    }
                }
                root.setOnClickListener {
                    if (openCardPosition != -1 && openCardPosition != closedCardPosition) {
                        clContact.isVisible = true
                        itemClickListener.invoke(contact, false)
                        layoutListItemContactExpanded.root.isVisible = false
                        notifyItemChanged(openCardPosition)
                        closedCardPosition = openCardPosition
                        return@setOnClickListener
                    }
                    clContact.isVisible = false
                    itemClickListener.invoke(contact, true)
                    layoutListItemContactExpanded.root.isVisible = true
                    openCardPosition = bindingAdapterPosition
                }
            }

            itemBinding.layoutListItemContactExpanded.apply {
                tvContactName.text = contact.name
                tvContactPhoneNumber.text = contact.mobileNumber
                tvDateAdded.text = contact.dateAdded.toIntuitiveDateTime()
                ivImage.setOnClickListener {
                    imageClickListener.invoke(contact)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    if (contact.videoPath.isNotBlank()) {
                        CoroutineScope(IO).launch {
                            val videoBitmap = root.context.getVideoThumbnailBitmap(contact.videoPath.toUri())
                            withContext(Main) {
                                ivImage.load(videoBitmap) {
                                    placeholder(R.drawable.ic_placeholder)
//                                error(R.drawable.ic_placeholder)
                                }
                            }
                        }
                    }
                } else {
                    if (contact.imagePath.isNotBlank()) {
                        ivImage.load(contact.imagePath.toUri()) {
                            placeholder(R.drawable.ic_placeholder)
//                        error(R.drawable.ic_placeholder)
                        }
                    }
                }
                tvEditContact.setOnClickListener {
                    editContactClickListener.invoke(contact, bindingAdapterPosition)
                }
                ivWhatsapp.setOnClickListener {
                    root.context.sendWhatsAppMessage(whatsAppPhoneNum = contact.mobileNumber)
                }
                ivSms.setOnClickListener {
                    root.context.sendSms(phoneNum = contact.mobileNumber)
                }
                ivCall.setOnClickListener {
                    root.context.makeCall(phoneNum = contact.mobileNumber)
                }
                ivShare.setOnClickListener {
                    root.context.shareImageAndTextViaApps(
                        uri = contact.imagePath.toUri(),
                        title = contact.name,
                        subtitle = contact.mobileNumber,
                        intentTitle = "Share with..."
                    )
                }
            }
        }
    }
}
