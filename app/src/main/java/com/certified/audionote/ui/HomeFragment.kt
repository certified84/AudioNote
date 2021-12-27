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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.certified.audionote.R
import com.certified.audionote.adapter.NoteRecyclerAdapter
import com.certified.audionote.database.Repository
import com.certified.audionote.databinding.FragmentHomeBinding
import com.certified.audionote.model.Note
import com.certified.audionote.utils.Extensions.flags
import com.certified.audionote.utils.UIState
import com.certified.audionote.utils.colors
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding?
        get() = _binding

    @Inject
    lateinit var repository: Repository
    private val viewModel: NotesViewModel by viewModels()
    private lateinit var navController: NavController
    private val adapter by lazy { NoteRecyclerAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
        binding?.uiState = viewModel.uiState

        viewModel.run {
            notes.observe(viewLifecycleOwner) {
                if (it != null && it.isNotEmpty())
                    uiState.set(UIState.HAS_DATA)
                else
                    uiState.set(UIState.EMPTY)
                adapter.submitList(it)
            }
        }

        binding?.apply {

            btnSettings.setOnClickListener { navController.navigate(R.id.action_homeFragment_to_settingsFragment) }
            fabAddNote.setOnClickListener {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(Note())
                navController.navigate(action)
            }

            setUpRecyclerView(recyclerViewNotes)

            val bottomSheetBehavior =
                BottomSheetBehavior.from(bottomSheetDialogLayout.bottomSheetDialog)
            bottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }
            })
            bottomSheetDialogLayout.linearLayoutCompat.setOnClickListener {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                else
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun setUpRecyclerView(recyclerViewNotes: RecyclerView) {
        val layoutManager = GridLayoutManager(requireContext(), resources.getInteger(R.integer.span_count))
        recyclerViewNotes.also {
            it.layoutManager = layoutManager
            it.adapter = adapter
        }
        adapter.setOnItemClickedListener(object : NoteRecyclerAdapter.OnItemClickedListener {
            override fun onItemClick(item: Note) {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(item)
                navController.navigate(action)
            }
        })
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