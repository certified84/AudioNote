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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import com.certified.audionote.utils.hasPermission
import com.certified.audionote.utils.requestPermission
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditNoteFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding: FragmentEditNoteBinding?
        get() = _binding

    @Inject
    lateinit var repository: Repository
    private lateinit var navController: NavController
    private val args: EditNoteFragmentArgs by navArgs()
    private val viewModel: NotesViewModel by viewModels()
    private lateinit var _note: Note
    private var clickCount = 0

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

        binding?.apply {

            btnBack.setOnClickListener {
                navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
            }
            btnShare.setOnClickListener { shareNote(_note) }
            btnRecord.setOnClickListener(this@EditNoteFragment)
            fabSaveNote.setOnClickListener(this@EditNoteFragment)

            if (args.noteId == -1) {
                _note = Note()
                binding?.note = _note
            } else
                viewModel.getNote(args.noteId)?.observe(viewLifecycleOwner) { note ->
                    _note = note
                    binding?.note = note
                }

            if (args.noteId == -1) {
//                TODO("Use viewBinding to hide these views")
                btnShare.visibility = View.GONE
                btnDelete.visibility = View.GONE

            } else {
                btnRecord.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_audio_not_playing,
                        null
                    )
                )
            }
        }
    }

    private fun startRecording() {
        showToast("Started recording")
    }

    private fun stopRecording() {
        showToast("Stopped recording")
    }

    private fun startPlayingRecording() {
        showToast("Started playing recording")
    }

    private fun stopPlayingRecording() {
        showToast("Stopped playing recording")
    }

    private fun shareNote(note: Note) {
        showToast("Coming soon")
//        TODO("Not yet Implemented")
    }

    private fun updateStatusBarColor(color: Int) {
        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = color
    }

    override fun onResume() {
        super.onResume()

        updateStatusBarColor(args.noteColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(p0: View?) {
        binding?.apply {
            if (args.noteId == -1) {
                when (p0) {
                    btnRecord -> {
                        if (clickCount == 0) {
                            if (hasPermission(requireContext(), Manifest.permission.RECORD_AUDIO))
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
                            else
                                requestPermission(
                                    requireActivity(),
                                    "This permission is required to able to enable audio recording",
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
                        val noteTitle = tvNoteTitle.text.toString().trim()
                        if (noteTitle.isNotBlank()) {
                            viewModel.insertNote(Note(title = noteTitle, color = args.noteColor))
                            showToast("Note saved")
                            navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
                        } else {
                            showToast("The note title is required")
                            tvNoteTitle.requestFocus()
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
                            title = tvNoteTitle.text.toString().trim(),
                            description = tvNoteDescription.text.toString().trim()
                        )
                        if (note.title.isNotBlank()) {
                            viewModel.updateNote(note)
                            navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
                        } else {
                            showToast("The note title is required")
                            tvNoteTitle.requestFocus()
                        }
                    }
                }
            }
        }
    }
}