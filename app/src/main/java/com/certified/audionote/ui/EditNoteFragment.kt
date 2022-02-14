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

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.TimePicker
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
import com.certified.audionote.utils.*
import com.certified.audionote.utils.Extensions.showToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timerx.Stopwatch
import timerx.StopwatchBuilder
import timerx.Timer
import timerx.TimerBuilder
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class EditNoteFragment : Fragment(), View.OnClickListener, DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by viewModels()
    private lateinit var navController: NavController

    private val args: EditNoteFragmentArgs by navArgs()
    private lateinit var _note: Note
    private var isRecording = false
    private var isPlayingRecord = false
    private var pickedDateTime: Calendar? = null
    private val currentDateTime by lazy { currentDate() }
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var file: File? = null
    private val noteIsRequired = "The note title is required"
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

        _note = args.note
        binding.lifecycleOwner = this
        binding.apply {
            note = _note
            uiState = viewModel.uiState
            reminderAvailableState = viewModel.reminderAvailableState
            reminderCompletionState = viewModel.reminderCompletionState
        }

        binding.apply {
            btnBack.setOnClickListener {
                navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
            }
            cardAddReminder.setOnClickListener {
                if (viewModel.reminderAvailableState.get() == ReminderAvailableState.NO_REMINDER)
                    pickDate()
                else
                    openEditReminderDialog()
            }
            btnShare.setOnClickListener { shareNote(_note) }
            btnDelete.setOnClickListener { launchDeleteNoteDialog() }
            btnRecord.setOnClickListener(this@EditNoteFragment)
            fabSaveNote.setOnClickListener(this@EditNoteFragment)

            if (args.note.id == 0) {
                viewModel.uiState.set(UIState.EMPTY)
//                chronometerNoteTimer.base = SystemClock.elapsedRealtime()
                file = null
            } else {
                setup()
            }
        }
    }

    private fun setup() {
        binding.apply {
            lifecycleScope.launch(Dispatchers.IO) {
                file = File(_note.filePath)
                Log.d("TAG", "onViewCreated: ${file!!.name}")
            }
            viewModel.apply {
                uiState.set(UIState.HAS_DATA)
                getNote(args.note.id).observe(viewLifecycleOwner) {
                    if (it.reminder != null) {
                        reminderAvailableState.set(ReminderAvailableState.HAS_REMINDER)
                        if (currentDate().timeInMillis > args.note.reminder!!) {
                            reminderCompletionState.set(ReminderCompletionState.COMPLETED)
                        } else {
                            reminderCompletionState.set(ReminderCompletionState.ONGOING)
                        }
                    } else {
                        reminderAvailableState.set(ReminderAvailableState.NO_REMINDER)
                    }
                }
            }

            tvTitle.text = getString(R.string.edit_note)
            etNoteTitle.apply {
//                inputType = InputType.TYPE_NULL
                keyListener = null
                setOnClickListener { showToast("You can't edit the note title") }
            }
            btnRecord.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_audio_not_playing,
                    null
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()

        updateStatusBarColor(binding.note!!.color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isPlayingRecord)
            mediaPlayer?.apply {
                stop()
                release()
            }
        mediaRecorder = null
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

    override fun onClick(p0: View?) {
        binding.apply {
            if (args.note.id == 0) {
                onClickWhenIdIsZero(p0)
            } else {
                onClickWhenIdIsNotZero(p0)
            }
        }
    }

    private fun onClickWhenIdIsNotZero(p0: View?) {
        binding.apply {
            when (p0) {
                btnRecord -> {
                    if (!isPlayingRecord) {
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
                    } else {
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
                fabSaveNote -> {
                    val note = _note.copy(
                        title = etNoteTitle.text.toString().trim(),
                        description = etNoteDescription.text.toString().trim(),
                        lastModificationDate = currentDate().timeInMillis
                    )
                    if (note.title.isNotBlank()) {
                        viewModel.updateNote(note)
                        if (pickedDateTime?.timeInMillis != null && pickedDateTime?.timeInMillis != currentDateTime.timeInMillis)
                            startAlarm(requireContext(), pickedDateTime!!.timeInMillis, note)
                        navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
                    } else {
                        showToast(noteIsRequired)
                        etNoteTitle.requestFocus()
                    }
                }
            }
        }
    }

    private fun onClickWhenIdIsZero(p0: View?) {
        binding.apply {
            when (p0) {
                btnRecord -> {
                    if (!isRecording) {
                        if (hasPermission(requireContext(), Manifest.permission.RECORD_AUDIO))
                            if (etNoteTitle.text.toString().isNotBlank())
                                btnRecord.setImageDrawable(
                                    ResourcesCompat.getDrawable(
                                        resources,
                                        R.drawable.ic_mic_recording,
                                        null
                                    )
                                ).run {
                                    isRecording = true
                                    startRecording()
                                }
                            else {
                                showToast(noteIsRequired)
                                etNoteTitle.requestFocus()
                            }
                        else
                            requestPermission(
                                requireActivity(),
                                "This permission is required to enable audio recording",
                                MainActivity.RECORD_AUDIO_PERMISSION_CODE,
                                Manifest.permission.RECORD_AUDIO
                            )
                    } else {
                        btnRecord.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources, R.drawable.ic_mic_not_recording, null
                            )
                        )
                            .run {
                                isRecording = false
                                stopRecording()
                            }
                    }
                }
                fabSaveNote -> {
                    if (etNoteTitle.text.toString().isNotBlank()) {
                        stopRecording()
                        val note = _note.copy(
                            title = etNoteTitle.text.toString().trim(),
                            description = etNoteDescription.text.toString().trim()
                        )
                        viewModel.insertNote(note)
                        showToast("Note saved")
                        if (pickedDateTime?.timeInMillis != null && pickedDateTime!!.timeInMillis <= currentDateTime.timeInMillis)
                            startAlarm(requireContext(), pickedDateTime!!.timeInMillis, note)
                        navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
                    } else {
                        showToast(noteIsRequired)
                        etNoteTitle.requestFocus()
                    }
                }
            }
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
//        currentDateTime = currentDate()
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
                viewModel.reminderCompletionState.set(ReminderCompletionState.ONGOING)
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

    private fun launchDeleteNoteDialog() {
        val materialDialog = MaterialAlertDialogBuilder(requireContext())
        materialDialog.apply {
            setTitle("Delete Note")
            setMessage("Are you sure you want to delete ${_note.title}?")
            setNegativeButton("No") { dialog, _ -> dialog?.dismiss() }
            setPositiveButton("Yes") { _, _ ->
                viewModel.deleteNote(_note)
                lifecycleScope.launch(Dispatchers.IO) { file?.delete() }
                navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
            }
            show()
        }
    }

    private fun startRecording() {
//        binding.chronometerNoteTimer.base = SystemClock.elapsedRealtime()
//        binding.chronometerNoteTimer.start()
        stopWatch = StopwatchBuilder()
            .startFormat("MM:SS")
            .onTick { time -> binding.tvTimer.text = time }
            .changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS")
            .build()

        val filePath = filePath(requireActivity())
        val fileName = "${binding.etNoteTitle.text.toString().trim()}.3gp"
        _note.filePath = "$filePath/$fileName"
        showToast("Started recording")
        mediaRecorder = MediaRecorder()
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile("$filePath/$fileName")
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
                stopWatch!!.start()
                disableNoteTitleEdit()
            } catch (e: IOException) {
                showToast("An error occurred")
            }
        }
    }

    private fun stopRecording() {
//        binding.chronometerNoteTimer.stop()
//        _note.audioLength =
//            binding.chronometerNoteTimer.text.toString().filter { it.isDigit() }.toLong()
        mediaRecorder?.apply {
            stop()
            release()
        }
        stopWatch?.apply {
            stop()
            _note.audioLength = stopWatch!!.getTimeIn(TimeUnit.SECONDS)
            reset()
        }
        mediaRecorder = null
        val file = File(_note.filePath)
        val fileByte = (file.readBytes().size.toDouble() / 1048576.00)
        val fileSize = roundOffDecimal(fileByte).toString()
        _note.size = fileSize
        showToast("Stopped recording")
    }

    private fun startPlayingRecording() {
        timer = TimerBuilder()
            .startTime(_note.audioLength, TimeUnit.SECONDS)
            .startFormat("HH:MM:SS")
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
            showToast("Started playing recording")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("TAG", "startPlayingRecording: ${e.localizedMessage}")
            showToast("An error occurred")
        }
    }

    private fun disableNoteTitleEdit() {
        binding.etNoteTitle.apply {
            keyListener = null
            setOnClickListener { showToast("You can't edit the note title") }
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
        showToast("Stopped playing recording")
    }

    private fun shareNote(note: Note) {
//        TODO("Not yet Implemented")
        showToast("You'll be able to share ${note.title} soon")
    }

    private fun updateStatusBarColor(color: Int) {
        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = color
    }
}