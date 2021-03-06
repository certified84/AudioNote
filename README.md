# AudioNotes π
A simple open source audio note-taking π Android application built to describe the use of Modern Android development tools. π. *Made with love β€οΈ by [Sammie_kt](https://github.com/certified84)*

<br />

***Find the latest stable release hereπ***

[![AudioNotes](https://github.com/certified84/AudioNote/blob/master/app/src/main/res/drawable/logo.png)](https://github.com/certified84/AudioNote/releases/tag/v0.1.3)

<br />

***Find the design on figma hereπ***

[![AudioNotes](https://github.com/certified84/AudioNote/blob/master/app/src/main/res/drawable/figma_logo.png)](https://www.figma.com/file/BxFzSMNrfXy8yrQlr8XWAS/Audio-Notes-Project?node-id=3%3A594)

<br />

## Day Mode π
Splash | Onboarding | Home(empty) | Home(content) | 
--- | --- | --- | --- | 
![](https://github.com/certified84/AudioNote/blob/master/Screenshots/splash_day.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/onboarding_day.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/home_empty_day.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/home_day.png) |

New note | Edit note | Settings | About | 
--- | --- | --- | --- | 
![](https://github.com/certified84/AudioNote/blob/master/Screenshots/new_note.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/edit_note.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/settings_day.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/about_day.png)

<br />

## We Support Dark Mode Too π
Splash | Onboarding | Home(empty) | Home(content) | 
--- | --- | --- | --- | 
![](https://github.com/certified84/AudioNote/blob/master/Screenshots/splash_dark.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/onboarding_dark.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/home_empty_dark.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/home_dark.png) |

New note | Edit note | Settings | About | 
--- | --- | --- | --- | 
![](https://github.com/certified84/AudioNote/blob/master/Screenshots/new_note.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/edit_note.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/settings_dark.png) | ![](https://github.com/certified84/AudioNote/blob/master/Screenshots/about_dark.png)

<br />


## Built With π 
- [Kotlin](https://kotlinlang.org/) - First class and official programming language for Android development.
- [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - For asynchronous and more..
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - Collection of libraries that help you design robust, testable, and maintainable apps.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that isn't destroyed on UI changes. 
  - [Room](https://developer.android.com/topic/libraries/architecture/room) - SQLite object mapping library.
  - [Jetpack Navigation](https://developer.android.com/guide/navigation) - Navigation refers to the interactions that allow users to navigate across, into, and back out from the different pieces of content within your app
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - Jetpack DataStore is a data storage solution that allows you to store key-value pairs or typed objects with protocol buffers. DataStore uses Kotlin coroutines and Flow to store data asynchronously, consistently, and transactionally.
<!--   - [Stateflow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) - StateFlow is a state-holder observable flow that emits the current and new state updates to its collectors. 
  - [Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html) - A flow is an asynchronous version of a Sequence, a type of collection whose values are lazily produced.  -->
- [Material Components for Android](https://github.com/material-components/material-components-android) - Modular and customizable Material Design UI components for Android.
- [Coil for Image loading](https://github.com/coil-kt/coil) - Kotlin based library for loading images in Android.
- [Dagger Hilt](https://github.com/google/dagger/tree/master/java/dagger/hilt) - Hilt provides a standard way to incorporate Dagger dependency injection into an Android application. For more information.
- [Easy permissions](https://github.com/vmadalin/easypermissions-ktx) - π Kotlin version of the popular google/easypermissions wrapper library to simplify basic system permissions logic on Android M or higher.
- [TimerX](https://github.com/arsvechkarev/TimerX) - Android timer and stopwatch library.

<!-- <br />

## Package Structure π¦
    
    dev.spikeysanju.expenso # Root Package
    βββ di                  # Hilt DI Modules 
    βββ data                # For data handling.
    β   βββ local           # Local Persistence Database. Room (SQLite) database
    |   β   βββ dao         # Data Access Object for Room   
    |   |   |ββ database    # Database Instance
    |
    βββ model               # Model classes [Transaction]
    |
    |-- repo                # Used to handle all data operations
    |
    βββ view                # Activity/Fragment View layer
    β   βββ main            # Main root folder
    |   β   βββ main        # Main Activity for RecyclerView
    |   β   βββ viewmodel   # Transaction ViewModel
    |   β   βββ adapter     # Adapter for RecyclerView
    β   βββ Dashboard       # Dashboard root folder
    |   |   |__ dashboard   # Dashboard 
    β   βββ Add             # Add Transaction root folder
    |   |   |__ add         # Add Transaction 
    β   βββ Edit            # Edit Transaction root folder
    |   |   |__ edit        # Edit Transaction
    β   βββ Details         # Add Transaction root folder
    |   |   |__ details     # Transaction Details
    β   βββ About           # About root folder
    |   |   |__ about       # About 
    β   βββ Dialog          # All Dialogs root folder
    |   |   |__ dialog      # Error Dialog 
    βββ utils               # All extension functions


<br />
 -->

## Architecture πΌ
This app uses [***MVVM (Model View View-Model)***](https://developer.android.com/jetpack/docs/guide#recommended-app-arch) architecture.

![](https://github.com/TheCodeMonks/Notes-App/blob/master/screenshots/ANDROID%20ROOM%20DB%20DIAGRAM.jpg)

## Build-tool π§°
You need to have [Android Studio](https://developer.android.com/studio) to build this project.

<br />

## Credit π
Credit goes to [Shazomii's Notely](https://github.com/shazomii/Notely) for project inspiration

<br />

## Contribute π€
If you want to contribute to this app, you're always welcome!
See [Contributing Guidelines](https://github.com/certified84/AudioNote/blob/master/CONTRIBUTING.md). 

<br>

## Contact π©
Have a project? DM us at π

Drop a mail to:- achiagasamson5@gmail.com

<br>

## Donation π°
If this project help you reduce time to develop, you can give me a cup of coffee :) 

<a href="https://www.buymeacoffee.com/SammieKt" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/yellow_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;" ></a>

## License π
```
    Apache 2.0 License


    Copyright 2021 Samson Achiaga

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

```

