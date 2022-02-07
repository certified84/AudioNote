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

import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
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

        notifyUser(appContext)

        return Result.success()
    }

    private fun notifyUser(context: Context) {
        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val pendingIntent =
            NavDeepLinkBuilder(context)
                .setGraph(R.navigation.navigation)
                .setDestination(R.id.editNoteFragment, EditNoteFragmentArgs(Note(1)).toBundle())
                .setComponentName(MainActivity::class.java)
                .createPendingIntent()

        val notificationBuilder =
            NotificationCompat.Builder(context, context.getString(R.string.channelId))
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentTitle("Test note title")
                .setContentText("Test note content")
                .setColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, null))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}