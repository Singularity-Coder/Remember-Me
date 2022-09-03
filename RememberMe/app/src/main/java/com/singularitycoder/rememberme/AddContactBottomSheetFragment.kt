package com.singularitycoder.rememberme

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.singularitycoder.rememberme.databinding.FragmentAddContactBottomSheetBinding
import com.singularitycoder.rememberme.helpers.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddContactBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(
            contact: Contact?,
            userAction: UserAction,
            adapterPosition: Int,
        ) = AddContactBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PARAM_CONTACT, contact)
                putParcelable(ARG_PARAM_USER_ACTION, userAction)
                putInt(ARG_PARAM_ADAPTER_POSITION, adapterPosition)
            }
        }
    }

    private val viewModel: SharedViewModel by activityViewModels()

    private lateinit var binding: FragmentAddContactBottomSheetBinding

    private var contact: Contact? = null
    private var userAction: UserAction? = null
    private var adapterPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contact = arguments?.getParcelable<Contact>(ARG_PARAM_CONTACT)
        userAction = arguments?.getParcelable<UserAction>(ARG_PARAM_USER_ACTION)
        adapterPosition = arguments?.getInt(ARG_PARAM_ADAPTER_POSITION) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddContactBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransparentBackground()
        binding.setupUI()
        binding.setupUserActionListeners()
        binding.observeForData()
    }

    // https://stackoverflow.com/questions/40616833/bottomsheetdialogfragment-listen-to-dismissed-by-user-event
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
//        viewModel.contactAccidentBackupLiveData.value = Contact(
//            name = binding.etName.editText?.text.toString(),
//            mobileNumber = binding.etPhoneNumber.editText?.text.toString(),
//            dateAdded = timeNow,
//            imagePath = "",
//            videoPath = ""
//        )
    }

    private fun FragmentAddContactBottomSheetBinding.setupUI() {
        etName.editText?.setText(contact?.name)
        etPhoneNumber.editText?.setText(contact?.mobileNumber)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val bitmap = context?.getVideoThumbnailBitmap(contact?.videoPath?.toUri() ?: Uri.EMPTY)
            ivImage.setImageBitmap(bitmap)
        } else {
            ivImage.imageTintList = ColorStateList.valueOf(requireContext().color(R.color.title_color))
        }
        when (userAction) {
            UserAction.ADD -> {
                btnAddContact.text = "  Add  "
                ibRetake.isVisible = false
                tvHeader.text = "Add Contact"
                etPhoneNumber.isEnabled = true
            }
            UserAction.UPDATE -> {
                btnAddContact.text = "  Update  "
                ibRetake.isVisible = true
                tvHeader.text = "Update Contact"
                etPhoneNumber.isEnabled = false
            }
            else -> Unit
        }
    }

    private fun FragmentAddContactBottomSheetBinding.setupUserActionListeners() {
        etName.doTextFieldEmptyValidation()
        etName.setBoxStrokeOnFocusChange()

        etPhoneNumber.doTextFieldEmptyValidation()
        etPhoneNumber.setBoxStrokeOnFocusChange()

        ibRetake.setOnClickListener {
            dismiss()
            (activity as? MainActivity)?.retakeVideo(contact)
        }

        btnAddContact.setOnClickListener {
            if (isValidateInput().not()) return@setOnClickListener
            viewModel.contactAccidentBackupLiveData.value = null
            val contact = Contact(
                name = binding.etName.editText?.text.toString(),
                mobileNumber = binding.etPhoneNumber.editText?.text.toString(),
                dateAdded = timeNow,
                imagePath = "",
                videoPath = contact?.videoPath ?: ""
            )
            when (userAction) {
                UserAction.ADD -> (activity as? MainActivity)?.addContact(contact)
                UserAction.UPDATE -> (activity as? MainActivity)?.updateContact(contact, adapterPosition)
                else -> Unit
            }
            dismiss()
        }
    }

    private fun FragmentAddContactBottomSheetBinding.observeForData() {
        viewModel.contactAccidentBackupLiveData.observe(viewLifecycleOwner) { it: Contact? ->
            it ?: return@observe
//            if (userAction == UserAction.UPDATE) {
//                etName.editText?.setText(viewModel.contactAccidentBackupLiveData.value?.name)
//                etPhoneNumber.editText?.setText(viewModel.contactAccidentBackupLiveData.value?.mobileNumber)
//            }
        }
    }

    private fun FragmentAddContactBottomSheetBinding.isValidateInput(): Boolean {
        if (etName.editText?.text.isNullOrBlank()) {
            etName.boxStrokeWidth = 2.dpToPx()
            etName.error = "This is required!"
            return false
        }

        if (etPhoneNumber.editText?.text.isNullOrBlank()) {
            etPhoneNumber.boxStrokeWidth = 2.dpToPx()
            etPhoneNumber.error = "This is required!"
            return false
        }

        return true
    }

    private fun TextInputLayout.doTextFieldEmptyValidation() {
        editText?.doAfterTextChanged { it: Editable? ->
            if (editText?.text.isNullOrBlank()) {
                error = "This is required!"
            } else {
                error = null
                isErrorEnabled = false
            }
        }
    }

    private fun TextInputLayout.setBoxStrokeOnFocusChange() {
        editText?.setOnFocusChangeListener { view, isFocused ->
            boxStrokeWidth = if (isFocused) 2.dpToPx() else 0
        }
    }
}

private const val ARG_PARAM_CONTACT = "ARG_PARAM_CONTACT"
private const val ARG_PARAM_USER_ACTION = "ARG_PARAM_USER_ACTION"
private const val ARG_PARAM_ADAPTER_POSITION = "ARG_PARAM_ADAPTER_POSITION"