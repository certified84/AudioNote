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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.certified.audionote.databinding.ItemNoteBinding
import com.certified.audionote.model.Note
import java.io.File

class NoteRecyclerAdapter(private val files: Array<File>) :
    ListAdapter<Note, NoteRecyclerAdapter.ViewHolder>(DIFF_CALLBACK) {

    private lateinit var listener: OnItemClickedListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class ViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.note = note
        }

        init {
//            val size = (files[position].totalSpace / 1048576L).toString()
//            binding.tvNoteSize.text = "${size}MB"
            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position))
                }
            }
        }
    }

    interface OnItemClickedListener {
        fun onItemClick(item: Note)
    }

    fun setOnItemClickedListener(listener: OnItemClickedListener) {
        this.listener = listener
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem

        }
    }
}