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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.certified.audionote.R
import com.certified.audionote.adapter.ViewPagerAdapter
import com.certified.audionote.databinding.FragmentOnboardingBinding
import com.certified.audionote.model.SliderItem
import com.certified.audionote.utils.Extensions.dataStore
import com.certified.audionote.utils.PreferenceKeys
import kotlinx.coroutines.launch

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private lateinit var sliderItem: ArrayList<SliderItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOnboardingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSliderItem()
        setUpViewPager()

        binding.btnGetStarted.setOnClickListener {
            lifecycleScope.launch {
                requireContext().dataStore.edit {
                    it[PreferenceKeys.FIRST_TIME_LOGIN] = false
                }
                findNavController().navigate(R.id.action_onboardingFragment_to_homeFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpSliderItem() {
        sliderItem = ArrayList()
        sliderItem.add(
            SliderItem(
                R.drawable.ic_undraw_empty, getString(R.string.view_pager_title_audio_recording),
                getString(R.string.view_pager_description_audio_recording)
            )
        )
        sliderItem.add(
            SliderItem(
                R.drawable.ic_undraw_empty, getString(R.string.view_pager_title_notification),
                getString(R.string.view_pager_description_notification)
            )
        )
        sliderItem.add(
            SliderItem(
                R.drawable.ic_undraw_empty, getString(R.string.view_pager_title_dark_mode),
                getString(R.string.view_pager_description_dark_mode)
            )
        )
    }

    private fun setUpViewPager() {
        val viewPagerAdapter = ViewPagerAdapter()
        viewPagerAdapter.submitList(sliderItem)
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.indicator.selection = position
                if (position == sliderItem.size - 1) {
                    binding.indicator.count = sliderItem.size
                    binding.indicator.selection = position
                }
            }
        })
    }
}