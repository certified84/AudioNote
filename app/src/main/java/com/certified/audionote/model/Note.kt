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

package com.certified.audionote.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.certified.audionote.utils.colors
import com.certified.audionote.utils.currentDate
import kotlinx.parcelize.Parcelize

/**
 * The Note class represent the domain model i.e the
 * object visible to the app user.
 *
 * @param id        id of the note
 * @param title     title of the note
 * @param description      content of the note
 * @param color     color of the note
 * @param lastModificationDate      date the note was created/modified
 * @param audioLength       The length of the audio recording
 * @param size      Basically the size of the audio in MB in the device
 * @param started   whether or note the reminder is active
 * @param reminder  date set for a reminder in the note
 *
 **/

@Parcelize
@Entity(tableName = "notes_table")
data class Note(
    var title: String = "",
    var description: String = "",
    var color: Int = colors.random(),
    var lastModificationDate: Long = currentDate().timeInMillis,
    var size: String = "",
    var audioLength: Long = -1L,
    var filePath: String = "",
    var started: Boolean = false,
    var reminder: Long? = null,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
) : Parcelable