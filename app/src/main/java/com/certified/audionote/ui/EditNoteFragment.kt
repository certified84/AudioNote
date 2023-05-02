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

package com.certified.audionote.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.certified.audionote.R
import com.certified.audionote.databinding.DialogEditReminderBinding
import com.certified.audionote.databinding.FragmentEditNoteBinding
import com.certified.audionote.model.Note
import com.certified.audionote.utils.Extensions.safeNavigate
import com.certified.audionote.utils.Extensions.showToast
import com.certified.audionote.utils.ReminderAvailableState
import com.certified.audionote.utils.ReminderCompletionState
import com.certified.audionote.utils.cancelAlarm
import com.certified.audionote.utils.currentDate
import com.certified.audionote.utils.formatReminderDate
import com.certified.audionote.utils.startAlarm
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timerx.Stopwatch
import timerx.Timer
import timerx.TimerBuilder
import java.io.File
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class EditNoteFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by viewModels()
    private lateinit var navController: NavController

    private val args: EditNoteFragmentArgs by navArgs()
    private lateinit var _note: Note
    private var isPlayingRecord = false
    private var pickedDateTime: Calendar? = null
    private val currentDateTime by lazy { currentDate() }
    private var mediaPlayer: MediaPlayer? = null
    private var file: File? = null
    private var stopWatch: Stopwatch? = null
    private var timer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditNoteBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        args.note.let {
            _note = it
            binding.note = it
        }

        binding.btnBack.setOnClickListener {
            navController.safeNavigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment())
        }
        binding.cardAddReminder.setOnClickListener {
            if (viewModel.reminderAvailableState.value == ReminderAvailableState.NO_REMINDER)
                pickDate()
            else
                openEditReminderDialog()
        }
        binding.btnShare.setOnClickListener { shareNote() }
        binding.btnDelete.setOnClickListener { launchDeleteNoteDialog(requireContext()) }
        binding.btnRecord.setOnClickListener { playPauseRecord() }
        binding.fabUpdateNote.setOnClickListener { updateNote() }

        setup()
    }

    private fun setup() {
        lifecycleScope.launch(Dispatchers.IO) {
            file = File(_note.filePath)
            Log.d("TAG", "onViewCreated: ${file!!.name}")
        }
        viewModel.apply {
            if (_note.reminder != null) {
                _reminderAvailableState.value = ReminderAvailableState.HAS_REMINDER
                if (currentDate().timeInMillis > args.note.reminder!!) {
                    _reminderCompletionState.value = ReminderCompletionState.COMPLETED
                } else {
                    _reminderCompletionState.value = ReminderCompletionState.ONGOING
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatusBarColor(args.note.color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isPlayingRecord)
            mediaPlayer?.apply {
                stop()
                release()
            }
        mediaPlayer = null
        timer?.apply {
            stop()
            reset()
        }
        timer = null
        stopWatch?.apply {
            stop()
            reset()
        }
        stopWatch = null
        _binding = null
    }

    private fun playPauseRecord() {
        binding.apply {
            if (!isPlayingRecord)
                btnRecord.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_audio_playing, null
                    )
                )
                    .run {
                        if (timer == null)
                            startPlayingRecording()
                        else
                            continuePlayingRecording()
                        isPlayingRecord = true
                    }
            else
                btnRecord.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_audio_not_playing, null
                    )
                )
                    .run {
                        if (timer?.getRemainingTimeIn(TimeUnit.SECONDS) != 0L)
                            pausePlayingRecording()
                        else
                            stopPlayingRecording()
                        isPlayingRecord = false
                    }
        }
    }

    private fun updateNote() {
        val note = _note.copy(
            title = binding.etNoteTitle.text.toString().trim(),
            description = binding.etNoteDescription.text.toString().trim(),
            lastModificationDate = currentDate().timeInMillis
        )
        if (note.title.isNotBlank()) {
            viewModel.updateNote(note)
            if (pickedDateTime?.timeInMillis != null && pickedDateTime?.timeInMillis != currentDateTime.timeInMillis)
                startAlarm(requireContext(), pickedDateTime!!.timeInMillis, note)
            navController.safeNavigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment())
        } else {
            showToast(requireContext().getString(R.string.title_required))
            binding.etNoteTitle.requestFocus()
        }
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        pickedDateTime = currentDate()
        pickedDateTime!!.set(p1, p2, p3)
        val hourOfDay = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val minuteOfDay = currentDateTime.get(Calendar.MINUTE)
        val timePickerDialog =
            TimePickerDialog(requireContext(), this, hourOfDay, minuteOfDay, false)
        timePickerDialog.setOnDismissListener {
            viewModel._reminderAvailableState.value = ReminderAvailableState.HAS_REMINDER
            viewModel._reminderCompletionState.value = ReminderCompletionState.ONGOING
            _note.reminder = pickedDateTime!!.timeInMillis
            binding.tvReminderDate.text = formatReminderDate(pickedDateTime!!.timeInMillis)
        }
        timePickerDialog.show()
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        pickedDateTime!!.set(Calendar.HOUR_OF_DAY, p1)
        pickedDateTime!!.set(Calendar.MINUTE, p2)
        if (pickedDateTime!!.timeInMillis <= currentDate().timeInMillis) {
            pickedDateTime!!.run {
                set(Calendar.DAY_OF_MONTH, currentDateTime.get(Calendar.DAY_OF_MONTH) + 1)
                set(Calendar.YEAR, currentDateTime.get(Calendar.YEAR))
                set(Calendar.MONTH, currentDateTime.get(Calendar.MONTH))
            }
        }
    }

    private fun pickDate() {
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog =
            DatePickerDialog(requireContext(), this, startYear, startMonth, startDay)
        datePickerDialog.show()
    }

    private fun openEditReminderDialog() {
        val view = DialogEditReminderBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        view.apply {
            note = _note
            btnDeleteReminder.setOnClickListener {
                viewModel._reminderAvailableState.value = ReminderAvailableState.NO_REMINDER
                _note.reminder = null
                cancelAlarm(requireContext(), _note.id)
                bottomSheetDialog.dismiss()
            }
            btnModifyReminder.setOnClickListener {
                bottomSheetDialog.dismiss()
                pickDate()
            }
        }
        bottomSheetDialog.edgeToEdgeEnabled
        bottomSheetDialog.setContentView(view.root)
        bottomSheetDialog.show()
    }

    private fun launchDeleteNoteDialog(context: Context) {
        val materialDialog = MaterialAlertDialogBuilder(context)
        materialDialog.apply {
            setTitle(context.getString(R.string.delete_note))
            setMessage("${context.getString(R.string.confirm_deletion)} ${_note.title}?")
            setNegativeButton(context.getString(R.string.no)) { dialog, _ -> dialog?.dismiss() }
            setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                viewModel.deleteNote(_note)
                lifecycleScope.launch(Dispatchers.IO) { file?.delete() }
                navController.safeNavigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment())
            }
            show()
        }
    }

    private fun startPlayingRecording() {
        timer = TimerBuilder()
            .startTime(_note.audioLength, TimeUnit.SECONDS)
            .startFormat(if (_note.audioLength >= 3600000L) "HH:MM:SS" else "MM:SS")
            .onTick { time -> binding.tvTimer.text = time }
            .actionWhen(0, TimeUnit.SECONDS) {
                binding.btnRecord.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_audio_not_playing,
                        null
                    )
                ).run {
                    isPlayingRecord = false
                    stopPlayingRecording()
                }
            }
            .build()
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer?.apply {
                setDataSource(file?.absolutePath)
                prepare()
                start()
            }
            timer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("TAG", "startPlayingRecording: ${e.localizedMessage}")
            showToast(requireContext().getString(R.string.error_occurred))
        }
    }

    private fun pausePlayingRecording() {
        mediaPlayer?.pause()
        timer?.stop()
    }

    private fun continuePlayingRecording() {
        mediaPlayer?.start()
        timer?.start()
    }

    private fun stopPlayingRecording() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        timer?.apply {
            reset()
            stop()
        }
        timer = null
    }

    private fun shareNote() {
        if (file == null) {
            showToast(requireContext().getString(R.string.file_not_found))
            return
        }
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "com.certified.audionote.provider",
                file!!
            )
            Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                startActivity(Intent.createChooser(this, "Share using"))
            }
        } catch (t: Throwable) {
            showToast(requireContext().getString(R.string.error_occurred))
            Log.d("TAG", "shareNote: ${t.localizedMessage}")
        }
    }

    private fun updateStatusBarColor(color: Int) {
        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = color
    }
}