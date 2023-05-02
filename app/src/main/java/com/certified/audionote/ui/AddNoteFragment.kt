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

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.certified.audionote.R
import com.certified.audionote.databinding.DialogEditReminderBinding
import com.certified.audionote.databinding.FragmentAddNoteBinding
import com.certified.audionote.model.Note
import com.certified.audionote.utils.Extensions.safeNavigate
import com.certified.audionote.utils.Extensions.showKeyboardFor
import com.certified.audionote.utils.Extensions.showToast
import com.certified.audionote.utils.ReminderAvailableState
import com.certified.audionote.utils.cancelAlarm
import com.certified.audionote.utils.currentDate
import com.certified.audionote.utils.filePath
import com.certified.audionote.utils.formatReminderDate
import com.certified.audionote.utils.roundOffDecimal
import com.certified.audionote.utils.startAlarm
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import timerx.Stopwatch
import timerx.StopwatchBuilder
import timerx.Timer
import java.io.File
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class AddNoteFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by viewModels()
    private lateinit var navController: NavController

    private val args: EditNoteFragmentArgs by navArgs()
    private lateinit var _note: Note
    private var isRecording = false
    private var pickedDateTime: Calendar? = null
    private val currentDateTime by lazy { currentDate() }
    private var mediaRecorder: MediaRecorder? = null
    private var file: File? = null
    private var stopWatch: Stopwatch? = null
    private var timer: Timer? = null

    private val requestAudioRecordingPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(getString(R.string.audio_record_permission))
                setMessage(getString(R.string.permission_required))
                setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
                show()
            }
            else startRecording()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddNoteBinding.inflate(layoutInflater, container, false)
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
            if (binding.etNoteTitle.text.toString().isNotBlank())
                saveNote()
            else
                try {
                    file?.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            navController.safeNavigate(AddNoteFragmentDirections.actionAddNoteFragmentToHomeFragment())
        }
        binding.cardAddReminder.setOnClickListener {
            if (viewModel.reminderAvailableState.value == ReminderAvailableState.NO_REMINDER)
                pickDate()
            else
                openEditReminderDialog()
        }
        binding.btnRecord.setOnClickListener { recordAudio() }
        binding.fabSaveNote.setOnClickListener {
            if (binding.etNoteTitle.text.toString().isNotBlank())
                saveNote()
            else {
                showToast(requireContext().getString(R.string.title_required))
                binding.etNoteTitle.requestFocus()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.etNoteTitle.apply {
            requestFocus()
            showKeyboardFor(requireContext())
        }
        updateStatusBarColor(binding.note!!.color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaRecorder = null
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

    private fun recordAudio() {
        binding.apply {
            if (!isRecording) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    btnRecord.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_mic_recording,
                            null
                        )
                    ).run {
                        try {
                            file?.delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        isRecording = true
                        startRecording()
                    }
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO))
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setTitle(getString(R.string.audio_record_permission))
                        setMessage(getString(R.string.permission_required))
                        setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
                        show()
                    }
                else requestAudioRecordingPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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

    private fun saveNote() {
        binding.apply {
            stopRecording()
            if (_note.audioLength <= 0) {
                showToast(requireContext().getString(R.string.record_note_before_saving))
                return
            }
            val note = _note.copy(
                title = etNoteTitle.text.toString().trim(),
                description = etNoteDescription.text.toString().trim()
            )
            this@AddNoteFragment.viewModel.insertNote(note)
            showToast(requireContext().getString(R.string.note_saved))
            if (pickedDateTime?.timeInMillis != null && pickedDateTime!!.timeInMillis <= currentDateTime.timeInMillis)
                startAlarm(requireContext(), pickedDateTime!!.timeInMillis, note)
            navController.safeNavigate(AddNoteFragmentDirections.actionAddNoteFragmentToHomeFragment())
        }
    }

    private fun startRecording() {
        val filePath = filePath(requireActivity())
        val fileName = "${System.currentTimeMillis()}.3gp"
        _note.filePath = "$filePath/$fileName"

        stopWatch = StopwatchBuilder()
            .startFormat("MM:SS")
            .onTick { time -> binding.tvTimer.text = time }
            .changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS")
            .build()

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile("$filePath/$fileName")
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
                stopWatch!!.start()
                file = File("$filePath/$fileName")
            } catch (e: IOException) {
                showToast(requireContext().getString(R.string.error_occurred))
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        stopWatch?.apply {
            stop()
            _note.audioLength = stopWatch!!.getTimeIn(TimeUnit.SECONDS)
            reset()
        }
        stopWatch = null
        if (_note.audioLength <= 0)
            return
        file = File(_note.filePath)
        val fileByte = (file!!.readBytes().size.toDouble() / 1048576.00)
        val fileSize = roundOffDecimal(fileByte)
        _note.size = fileSize
    }

    private fun updateStatusBarColor(color: Int) {
        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = color
    }
}