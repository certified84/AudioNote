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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.certified.audionote.R
import com.certified.audionote.databinding.FragmentEditNoteBinding
import com.certified.audionote.utils.colors

class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding: FragmentEditNoteBinding?
        get() = _binding
    private lateinit var navController: NavController
    private val args: EditNoteFragmentArgs by navArgs()

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

            var clickCount = 0
            if (args.noteId == -1) {
                btnShare.visibility = View.GONE
                btnDelete.visibility = View.GONE
                btnRecord.setOnClickListener {
                    if (clickCount == 0)
                        btnRecord.setImageResource(R.drawable.ic_mic_recording).run { clickCount++ }
                    else
                        btnRecord.setImageResource(R.drawable.ic_mic_not_recording)
                            .run { clickCount-- }
                }
            } else {
                btnRecord.setImageResource(R.drawable.ic_audio_not_playing)
                btnRecord.setOnClickListener {
                    if (clickCount == 0)
                        btnRecord.setImageResource(R.drawable.ic_audio_not_playing)
                            .run { clickCount++ }
                    else
                        btnRecord.setImageResource(R.drawable.ic_audio_playing)
                            .run { clickCount-- }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val color = args.noteColor
        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = color

        binding?.apply {
            parent.setBackgroundColor(color)
            parentAddReminder.setBackgroundColor(color)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}