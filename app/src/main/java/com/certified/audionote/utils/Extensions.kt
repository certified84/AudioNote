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

import android.content.Context
import android.view.WindowManager
import android.widget.Toast
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

object Extensions {
    fun Fragment.flags(color: Int) {
        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = this.resources.getColor(color)
    }

    fun Fragment.showToast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    fun Date.toReadableString(): String {
        val format = SimpleDateFormat("dd/MM/yy")
        return format.format(this)
    }

    val Context.dataStore by preferencesDataStore(name = "com.certified.audionotes.preferences")
}