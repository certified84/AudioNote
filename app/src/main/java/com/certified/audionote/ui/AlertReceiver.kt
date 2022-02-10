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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.certified.audionote.model.Note
import com.certified.audionote.utils.NotificationWorker
import java.util.concurrent.TimeUnit

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val noteId = intent.getIntExtra("noteId", 0)
        val noteTitle = intent.getStringExtra("noteTitle")
        val noteDescription = intent.getStringExtra("noteDescription")
        val noteColor = intent.getIntExtra("noteColor", -1)
        val noteLastModificationDate = intent.getLongExtra("noteLastModificationDate", -1L)
        val noteSize = intent.getStringExtra("noteSize")
        val noteAudioLength = intent.getLongExtra("noteAudioLength", 0L)
        val noteFilePath = intent.getStringExtra("noteFilePath")
        val noteStarted = intent.getBooleanExtra("noteStarted", false)
        val noteReminder = intent.getLongExtra("noteReminder", -1L)

        Log.d(
            "TAG", "onReceive: Alert received: Note: ${
                Note(
                    noteId,
                    noteTitle!!,
                    noteDescription!!,
                    noteColor,
                    noteLastModificationDate,
                    noteSize!!,
                    noteAudioLength,
                    noteFilePath!!,
                    noteStarted,
                    noteReminder
                )
            }"
        )

        val data = Data.Builder()
        data.apply {
            putInt("noteId", noteId)
            putString("noteTitle", noteTitle)
            putString("noteDescription", noteDescription)
            putInt("noteColor", noteColor)
            putLong("noteLastModificationDate", noteLastModificationDate)
            putString("noteSize", noteSize)
            putLong("noteAudioLength", noteAudioLength)
            putString("noteFilePath", noteFilePath)
            putBoolean("noteStarted", noteStarted)
            putLong("noteReminder", noteReminder)
        }
        val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        notificationRequest
            .setInitialDelay(10000, TimeUnit.MILLISECONDS).setInputData(data.build())
        WorkManager.getInstance(context).beginUniqueWork(
            "Audio Notes notification work",
            ExistingWorkPolicy.REPLACE,
            notificationRequest.build()
        ).enqueue()
    }
}