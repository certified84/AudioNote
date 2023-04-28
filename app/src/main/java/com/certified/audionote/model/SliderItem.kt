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

package com.certified.audionote.model

/**
 * The SliderItem class represent a domain model i.e an
 * object visible to the app user. It is used in conjunction
 * with ViewPager2 to create the Onboarding
 *
 * @param image             image of the sliderItem
 * @param title             title of the sliderItem
 * @param description      description of the sliderItem
 *
 **/
data class SliderItem (val image: Int, val title: String, val description: String)