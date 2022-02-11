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

package com.certified.audionote.adapter

import android.graphics.Paint
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.certified.audionote.model.Note

@BindingAdapter("listItems")
fun bindItemRecyclerView(recyclerView: RecyclerView, data: List<Note>?) {
    val adapter = recyclerView.adapter as NoteRecyclerAdapter
    adapter.submitList(data)
}

@BindingAdapter("visible")
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("editNoteVisible")
fun View.setEditNoteVisible(audioLength: Long) {
    visibility = when (audioLength) {
        0L -> View.GONE
        else -> View.VISIBLE
    }
}

@BindingAdapter("timerVisible")
fun View.setTimerVisible(audioLength: Long) {
    visibility = when (audioLength) {
        0L -> View.GONE
        -1L -> View.GONE
        else -> View.VISIBLE
    }
}

@BindingAdapter("chronometerVisibility")
fun Chronometer.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

@BindingAdapter("strikeThrough")
fun strikeThrough(textView: TextView, strikeThrough: Boolean) {
    if (strikeThrough) {
        textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        textView.paintFlags = 0
    }
}

@BindingAdapter("timeText")
fun TextView.timeText(value: Long) {
    text = if (value >= 3600)
        String.format("%02d:%02d:%02d", value / 3600, (value % 3600) / 60, value % 60)
    else
        String.format("%02d:%02d", (value % 3600) / 60, value % 60)
}

@BindingAdapter("image")
fun ImageView.loadImage(image: Int) {
    this.load(image)
}