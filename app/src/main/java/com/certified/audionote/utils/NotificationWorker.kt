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

package com.certified.audionote.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.*
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
        notifyUser(appContext, noteId, noteTitle!!)

        return Result.success()
    }

    private fun notifyUser(context: Context, noteId: Int, noteTitle: String) {

        val channelId = context.getString(R.string.channelId)

        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val audioAttributes = AudioAttributes.Builder().setUsage(USAGE_NOTIFICATION_RINGTONE)
            .setContentType(CONTENT_TYPE_SONIFICATION).build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent =
            NavDeepLinkBuilder(context)
                .setGraph(R.navigation.navigation)
                .setDestination(R.id.editNoteFragment, EditNoteFragmentArgs(null, noteId).toBundle())
                .setComponentName(MainActivity::class.java)
                .createPendingIntent()

        val notificationBuilder =
            NotificationCompat.Builder(context, context.getString(R.string.channelId))
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentTitle(noteTitle)
//                .setContentText("note.description, $noteId")
                .setColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, null))
                .setSound(defaultSoundUri)
                .setDefaults(DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setPriority(PRIORITY_HIGH)
                .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(channelId)
            val channel = NotificationChannel(
                channelId,
                "Audio Notes",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channel.setSound(defaultSoundUri, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(noteId, notificationBuilder.build())
    }
}