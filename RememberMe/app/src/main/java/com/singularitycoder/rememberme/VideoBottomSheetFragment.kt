package com.singularitycoder.rememberme

import android.app.Dialog
import android.content.DialogInterface.OnShowListener
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.rememberme.databinding.FragmentVideoBottomSheetBinding
import com.singularitycoder.rememberme.helpers.deviceHeight
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VideoBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(videoPath: String) = VideoBottomSheetFragment().apply {
            arguments = Bundle().apply { putString(ARG_PARAM_VIDEO_PATH, videoPath) }
        }
    }

    private var isPlayWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L
    private var exoPlayer: ExoPlayer? = null
    private var videoPath: String? = null

    private lateinit var binding: FragmentVideoBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoPath = arguments?.getString(ARG_PARAM_VIDEO_PATH)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVideoBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
    }

    private fun FragmentVideoBottomSheetBinding.setupUI() {
        exoPlayerView.setOnClickListener {
            exoPlayer ?: return@setOnClickListener
            if (exoPlayer?.isPlaying == true) {
                ivPlay.isVisible = true
                exoPlayer?.pause()
            } else {
                ivPlay.isVisible = false
                exoPlayer?.play()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT <= 23 || exoPlayer == null)) initializePlayer()
        setupExoPlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) releaseExoPlayer()
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) releaseExoPlayer()
    }

    private fun setupExoPlayer() {
        exoPlayer = ExoPlayer.Builder(requireContext()).build().also { it: ExoPlayer ->
            binding.exoPlayerView.player = it
        }.apply {
//            val rawVideoUri = RawResourceDataSource.buildRawResourceUri(R.raw.video)
            val mediaItem = MediaItem.fromUri(videoPath?.toUri() ?: Uri.EMPTY)
            addMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE // Loop video
//            volume = 0f // Mute Video
            prepare()
            play()
        }
    }

    private fun initializePlayer() {
        exoPlayer?.playWhenReady = isPlayWhenReady
        exoPlayer?.seekTo(currentItem, playbackPosition)
        exoPlayer?.prepare()
    }

    private fun releaseExoPlayer() {
        exoPlayer?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            isPlayWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        exoPlayer = null
    }

    // https://stackoverflow.com/questions/42301845/android-bottom-sheet-after-state-changed
    // https://stackoverflow.com/questions/35937453/set-state-of-bottomsheetdialogfragment-to-expanded
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener(OnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout? ?: return@OnShowListener
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
            bottomSheet.layoutParams.height = deviceHeight()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.addBottomSheetCallback(object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    println("bottom sheet state: ${behavior.state}")
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            behavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> Unit
                        BottomSheetBehavior.STATE_EXPANDED -> Unit
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> Unit
                        BottomSheetBehavior.STATE_HIDDEN -> Unit
                        BottomSheetBehavior.STATE_SETTLING -> {
                            behavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // React to dragging events
                }
            })
        })
        return dialog
    }
}

private const val ARG_PARAM_VIDEO_PATH = "ARG_PARAM_VIDEO_PATH"