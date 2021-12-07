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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.certified.audionote.model.Note

class NotesViewModel(private val noteList: List<Note>?) : ViewModel() {

    private val _notes = MutableLiveData<List<Note>?>()
    val notes: LiveData<List<Note>?>
        get() = _notes

    private val _showEmptyNotesDesign = MutableLiveData<Boolean>()
    val showEmptyNotesDesign: LiveData<Boolean>
        get() = _showEmptyNotesDesign

    init {
        getNotes()
    }

    private fun getNotes() {
        _notes.value = noteList
        _showEmptyNotesDesign.value = noteList == null
    }
}