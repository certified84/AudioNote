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