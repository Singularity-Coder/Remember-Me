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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var openCardPosition: Int = -1
    private var closedCardPosition: Int = -1

    var contactsList = emptyList<Contact>()
    private var imageClickListener: (contact: Contact) -> Unit = {}
    private var editContactClickListener: (contact: Contact) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = ListItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ContactViewHolder).setData(contactsList[position])
    }

    override fun getItemCount(): Int = contactsList.size

    override fun getItemViewType(position: Int): Int = position

    fun setImageClickListener(listener: (contact: Contact) -> Unit) {
        imageClickListener = listener
    }

    fun setEditContactClickListener(listener: (contact: Contact) -> Unit) {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    CoroutineScope(IO).launch {
                        val videoBitmap = root.context.getVideoThumbnailBitmap(contact.videoPath.toUri())
                        withContext(Main) {
                            ivImage.load(videoBitmap) {
                                placeholder(R.drawable.ic_placeholder)
                                error(R.drawable.ic_placeholder)
                            }
                        }
                    }
                } else {
                    ivImage.load(contact.imagePath.toUri()) {
                        placeholder(R.drawable.ic_placeholder)
                        error(R.drawable.ic_placeholder)
                    }
                }
                root.setOnClickListener {
                    if (openCardPosition != -1 && openCardPosition != closedCardPosition) {
                        clContact.isVisible = true
                        layoutListItemContactExpanded.root.isVisible = false
                        notifyItemChanged(openCardPosition)
                        closedCardPosition = openCardPosition
                    }
                    clContact.isVisible = false
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
                    CoroutineScope(IO).launch {
                        val videoBitmap = root.context.getVideoThumbnailBitmap(contact.videoPath.toUri())
                        withContext(Main) {
                            ivImage.load(videoBitmap) {
                                placeholder(R.drawable.ic_placeholder)
                                error(R.drawable.ic_placeholder)
                            }
                        }
                    }
                } else {
                    ivImage.load(contact.imagePath.toUri()) {
                        placeholder(R.drawable.ic_placeholder)
                        error(R.drawable.ic_placeholder)
                    }
                }
                tvEditContact.setOnClickListener {
                    editContactClickListener.invoke(contact)
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
