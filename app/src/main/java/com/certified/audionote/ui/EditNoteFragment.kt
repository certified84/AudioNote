/*
 * Copyright (c) 2021 Samson Achiaga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.certified.audionote.ui

import android.Manifest
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.certified.audionote.R
import com.certified.audionote.database.Repository
import com.certified.audionote.databinding.FragmentEditNoteBinding
import com.certified.audionote.model.Note
import com.certified.audionote.utils.Extensions.showToast
import com.certified.audionote.utils.UIState
import com.certified.audionote.utils.filePath
import com.certified.audionote.utils.hasPermission
import com.certified.audionote.utils.requestPermission
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class EditNoteFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding: FragmentEditNoteBinding?
        get() = _binding

    @Inject
    lateinit var repository: Repository
    private val viewModel: NotesViewModel by viewModels()
    private lateinit var navController: NavController
    private val args: EditNoteFragmentArgs by navArgs()
    private lateinit var _note: Note
    private var clickCount = 0
    private var mediaRecorder: MediaRecorder? = null
    private val files: Array<String> by lazy { requireContext().fileList() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditNoteBinding.inflate(layoutInflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        binding?.lifecycleOwner = this
        binding?.uiState = viewModel.uiState

        binding?.apply {

            btnBack.setOnClickListener {
                navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
            }
            btnShare.setOnClickListener { shareNote(_note) }
            btnDelete.setOnClickListener { launchDeleteNoteDialog(_note) }
            btnRecord.setOnClickListener(this@EditNoteFragment)
            fabSaveNote.setOnClickListener(this@EditNoteFragment)

            if (args.note.id == -1) {
                viewModel.uiState.set(UIState.EMPTY)
                chronometerNoteTimer.base = SystemClock.elapsedRealtime()
                _note = args.note
                binding?.note = _note
            } else {
                viewModel.uiState.set(UIState.HAS_DATA)
                tvTitle.text = getString(R.string.edit_note)
                btnRecord.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_audio_not_playing,
                        null
                    )
                )
                    _note = args.note
                    binding?.note = args.note
                    binding?.chronometerNoteTimer?.text = args.note.audioLength
//                    binding?.chronometerNoteTimer?.base = setBase(note.audioLength)!!.toLong()
            }
        }
    }

    private fun launchDeleteNoteDialog(note: Note) {
        val materialDialog = MaterialAlertDialogBuilder(requireContext())
        materialDialog.apply {
            setTitle("Delete Note")
            setMessage("Are you sure you want to delete ${note.title}?")
            setNegativeButton("No") { dialog, _ -> dialog?.dismiss() }
            setPositiveButton("Yes") { _, _ ->
                viewModel.deleteNote(note)
                navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
            }
            show()
        }
    }

    private fun startRecording() {
        binding?.chronometerNoteTimer?.base = SystemClock.elapsedRealtime()
        binding?.chronometerNoteTimer?.start()
        val filePath = filePath(requireActivity())
        val fileName = "${binding?.etNoteTitle?.text.toString().trim()}.3gp"
        showToast("Started recording")
        mediaRecorder = MediaRecorder()
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile("$filePath/$fileName")
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                showToast("An error occurred")
            }

        }
    }

    private fun stopRecording() {
        binding?.chronometerNoteTimer?.stop()
        _note.audioLength = binding?.chronometerNoteTimer?.text.toString()
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        showToast("Stopped recording")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun startPlayingRecording() {
        binding?.chronometerNoteTimer?.isCountDown = true
        showToast("Started playing recording")
    }

    private fun stopPlayingRecording() {
        showToast("Stopped playing recording")
    }

    private fun shareNote(note: Note) {
//        TODO("Not yet Implemented")
        showToast("Coming soon: ${note.title}")
    }

    private fun updateStatusBarColor(color: Int) {
        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = color
    }

//    private fun setBase(value: String): String? {
//        var base = ""
//        value.forEach {
//
//        }
//        return if (value.length >= 5) {
//            for (i in 0..4)
//                if (i == 3)
//                    continue
//            base += value[i]
//            base
//        } else null
//    }

    override fun onResume() {
        super.onResume()

        updateStatusBarColor(binding!!.note!!.color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(p0: View?) {
        binding?.apply {
            if (args.note.id == -1) {
                when (p0) {
                    btnRecord -> {
                        if (clickCount == 0) {
                            if (hasPermission(requireContext(), Manifest.permission.RECORD_AUDIO))
                                if (etNoteTitle.text.toString().isNotBlank())
                                    btnRecord.setImageDrawable(
                                        ResourcesCompat.getDrawable(
                                            resources,
                                            R.drawable.ic_mic_recording,
                                            null
                                        )
                                    ).run {
                                        clickCount++
                                        startRecording()
                                    }
                                else {
                                    showToast("The note title is required")
                                    etNoteTitle.requestFocus()
                                }
                            else
                                requestPermission(
                                    requireActivity(),
                                    "This permission is required to enable audio recording",
                                    MainActivity.RECORD_AUDIO_PERMISSION_CODE,
                                    Manifest.permission.RECORD_AUDIO
                                )
                        } else {
                            btnRecord.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources, R.drawable.ic_mic_not_recording, null
                                )
                            )
                                .run {
                                    clickCount--
                                    stopRecording()
                                }
                        }
                    }
                    fabSaveNote -> {
                        val note = _note.copy(
                            title = etNoteTitle.text.toString().trim(),
                            description = etNoteDescription.text.toString().trim()
                        )
                        if (note.title.isNotBlank()) {
                            viewModel.insertNote(note)
                            showToast("Note saved")
                            navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
                        } else {
                            showToast("The note title is required")
                            etNoteTitle.requestFocus()
                        }
                    }
                }
            } else {
                when (p0) {
                    btnRecord -> {
                        if (clickCount == 0) {
                            btnRecord.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_audio_playing, null
                                )
                            )
                                .run {
                                    clickCount++
                                    startPlayingRecording()
                                }
                        } else {
                            btnRecord.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_audio_not_playing, null
                                )
                            )
                                .run {
                                    clickCount--
                                    stopPlayingRecording()
                                }
                        }
                    }
                    fabSaveNote -> {
                        val note = _note.copy(
                            title = etNoteTitle.text.toString().trim(),
                            description = etNoteDescription.text.toString().trim(),
                            lastModificationDate = Date()
                        )
                        if (note.title.isNotBlank()) {
                            viewModel.updateNote(note)
                            navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
                        } else {
                            showToast("The note title is required")
                            etNoteTitle.requestFocus()
                        }
                    }
                }
            }
        }
    }
}