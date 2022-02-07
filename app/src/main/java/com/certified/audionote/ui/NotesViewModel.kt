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

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.certified.audionote.repository.Repository
import com.certified.audionote.model.Note
import com.certified.audionote.utils.ReminderAvailableState
import com.certified.audionote.utils.ReminderCompletionState
import com.certified.audionote.utils.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    val uiState = ObservableField(UIState.LOADING)
    val reminderAvailableState = ObservableField(ReminderAvailableState.NO_REMINDER)
    val reminderCompletionState = ObservableField(ReminderCompletionState.ONGOING)

    private val _notes = MutableLiveData<List<Note>?>()
    lateinit var notes: LiveData<List<Note>>
//        get() = _notes

    init {
        getNotes()
    }

    private fun getNotes() {
//        val allNotes = repository.allNotes.value
        notes = repository.allNotes
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

    fun getNote(noteId: Int) = repository.getNote(noteId)
}