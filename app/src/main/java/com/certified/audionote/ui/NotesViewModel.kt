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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.certified.audionote.model.Note
import com.certified.audionote.repository.Repository
import com.certified.audionote.utils.ReminderAvailableState
import com.certified.audionote.utils.ReminderCompletionState
import com.certified.audionote.utils.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    val _reminderAvailableState = MutableStateFlow(ReminderAvailableState.NO_REMINDER)
    val reminderAvailableState = _reminderAvailableState.asStateFlow()

    val _reminderCompletionState = MutableStateFlow(ReminderCompletionState.ONGOING)
    val reminderCompletionState = _reminderCompletionState.asStateFlow()

    private val _uiState = MutableStateFlow(UIState.LOADING)
    val uiState = _uiState.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>?>(null)
    val notes = _notes.asStateFlow()

    private val _note = MutableStateFlow<Note?>(null)
    val note = _note.asStateFlow()

    init {
        getNotes()
    }

    private fun getNotes() {
        viewModelScope.launch {
            repository.allNotes.collect {
                _notes.value = it
                if (it.isNullOrEmpty())
                    _uiState.value = UIState.EMPTY
                else
                    _uiState.value = UIState.HAS_DATA
            }
        }
    }

    fun insertNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    fun getNote(noteId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getNote(noteId).collect {
                _note.value = it
            }
        }
    }
}