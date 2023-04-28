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

package com.certified.audionote.utils

import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.certified.audionote.R
import com.certified.audionote.model.Note
import com.certified.audionote.ui.EditNoteFragmentArgs
import com.certified.audionote.ui.MainActivity

class NotificationWorker(private val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        val noteId = inputData.getInt("noteId", 0)
        val noteTitle = inputData.getString("noteTitle")
        val noteDescription = inputData.getString("noteDescription")
        val noteColor = inputData.getInt("noteColor", -1)
        val noteLastModificationDate = inputData.getLong("noteLastModificationDate", -1L)
        val noteSize = inputData.getString("noteSize")
        val noteAudioLength = inputData.getLong("noteAudioLength", 0L)
        val noteFilePath = inputData.getString("noteFilePath")
        val noteStarted = inputData.getBoolean("noteStarted", false)
        val noteReminder = inputData.getLong("noteReminder", -1L)

        val note = Note(
            noteTitle!!,
            noteDescription!!,
            noteColor,
            noteLastModificationDate,
            noteSize!!,
            noteAudioLength,
            noteFilePath!!,
            noteStarted,
            noteReminder,
            noteId,
        )
        notifyUser(appContext, note)

        return Result.success()
    }

    private fun notifyUser(context: Context, note: Note) {

        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent =
            NavDeepLinkBuilder(context)
                .setGraph(R.navigation.navigation)
                .setDestination(
                    R.id.editNoteFragment,
                    EditNoteFragmentArgs(note).toBundle()
                )
                .setComponentName(MainActivity::class.java)
                .createPendingIntent()

        val notificationBuilder =
            NotificationCompat.Builder(context, context.getString(R.string.channelId))
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentTitle(note.title)
                .setContentText("Hi there, it's time to check out ${note.title}")
                .setColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))
                .setSound(defaultSoundUri)
                .setDefaults(DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setPriority(PRIORITY_HIGH)
                .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationBuilder.setChannelId(context.getString(R.string.channelId))

        notificationManager.notify(note.id, notificationBuilder.build())
    }
}