/*
 * Copyright (c) 2023 Samson Achiaga
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.certified.audionote.R
import com.certified.audionote.adapter.NoteRecyclerAdapter
import com.certified.audionote.databinding.FragmentHomeBinding
import com.certified.audionote.model.Note
import com.certified.audionote.repository.Repository
import com.certified.audionote.utils.Extensions.flags
import com.certified.audionote.utils.Extensions.safeNavigate
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var repository: Repository
    private val viewModel: NotesViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        NoteRecyclerAdapter { note ->
            val action = HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(note)
            navController.safeNavigate(action)
        }.apply {
            val layoutManager = GridLayoutManager(requireContext(), 2)
            binding.recyclerViewNotes.also {
                it.layoutManager = layoutManager
                it.adapter = this
            }
        }

        binding.apply {
            btnSettings.setOnClickListener { navController.safeNavigate(HomeFragmentDirections.actionHomeFragmentToSettingsFragment()) }
            fabAddNote.setOnClickListener {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToAddNoteFragment(Note(audioLength = 0L))
                navController.safeNavigate(action)
            }

            val bottomSheetBehavior =
                BottomSheetBehavior.from(bottomSheetDialogLayout.bottomSheetDialog)
            bottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) bottomSheetBehavior.state =
                        BottomSheetBehavior.STATE_COLLAPSED
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                    Unused
                }
            })
            bottomSheetDialogLayout.linearLayoutCompat.setOnClickListener {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) bottomSheetBehavior.state =
                    BottomSheetBehavior.STATE_EXPANDED
                else bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    override fun onResume() {
        super.onResume()
        flags(R.color.fragment_background)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}