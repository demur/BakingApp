# Baking App
[Udacity Grow with Google](https://www.udacity.com/grow-with-google) [Android Developer Nanodegree Program](https://www.udacity.com/course/android-developer-nanodegree-by-google--nd801)

## Overview
The task was to create an Android Baking application that will allow Udacity's resident baker-in-chief, Miriam, to share her recipes with the world.
This application provides users with a step by step baking recipe guide. It lets users seamlessly navigate between steps and stream videos from the URLs given in the stated JSON. As well there is a home screen widget that renders a list of ingredients for the last viewed recipe with no need to reopen the application.
The application has separate responsive UI layouts for phones and tablets.
Sample mocks provided by Udacity can be found [here](https://go.udacity.com/android-baking-app-mocks-pdf).

## Screenshots
<p align="center">
    <img src="screenshots/Screenshot_1.png?raw=true" width=275 />
    <img src="screenshots/Screenshot_2.png?raw=true" width=275 />
</p>
<p align="center">
    <img src="screenshots/Screenshot_4.png?raw=true" height=275 />
</p>
<p align="center">
    <img src="screenshots/Screenshot_3.png?raw=true" width=275 />
    <img src="screenshots/Screenshot_5.png?raw=true" width=275 />
</p>
<p align="center">
    <img src="screenshots/Screenshot_6.png?raw=true" height=275 />
</p>

## How to work with the project
Just clone this repository or download as an archive and import in Android Studio.
This application (optionally) uses [Unsplash](https://unsplash.com/developers) API to retrieve random covers for recipe cards. You can provide Your API key to see those images, just replace `<Your_Unsplash_API_key>` in:
    ```
    /gradle.properties
    ```

## Project Requirements

### Common
- [x] App is written solely in the Java Programming Language
- [x] App utilizes stable release versions of all libraries, Gradle, and Android Studio

### General App Usage
- [x] App should display recipes from [provided network resource](https://go.udacity.com/android-baking-app-json)
- [x] App should allow navigation between individual recipes and recipe steps
- [x] App uses RecyclerView and can handle recipe steps that include videos or images
- [x] App conforms to common standards found in the [Android Nanodegree General Project Guidelines](http://udacity.github.io/android-nanodegree-guidelines/core.html)

### Components and Libraries
- [x] Application uses Master Detail Flow to display recipe steps and navigation between them
- [x] Application uses Exoplayer to display videos
- [x] Application properly initializes and releases video assets when appropriate
- [x] Application should properly retrieve media assets from the provided network links. It should properly handle network requests
- [x] Application makes use of Espresso to test aspects of the UI
- [x] Application sensibly utilizes a third-party library to enhance the App's features. That could be helper library to interface with Content Providers if you choose to store the recipes, a UI binding library to avoid writing findViewById a bunch of times, or something similar

### Homescreen Widget
- [x] Application has a companion homescreen widget
- [x] Widget displays ingredient list for desired recipe

## What have I learnt?
* Using Exoplayer to play media files
* Adding a widget to an app experience
* Leveraging third-party libraries in an app
* Using Fragments to create a responsive design that works on phones and tablets

## Libraries
* [AndroidX](https://developer.android.com/jetpack/androidx/) previously known as *'Android support Library'*
    * [ConstraintLayout](https://developer.android.com/training/constraint-layout) allows to create large and complex layouts
    * [RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview) is a more advanced and flexible version of *ListView*
    * [CardView](https://developer.android.com/guide/topics/ui/layout/cardview)
    * [DataBinding](https://developer.android.com/topic/libraries/data-binding/) allows to bind UI components in layouts to data sources in app
* [Retrofit 2](https://github.com/square/retrofit) type-safe HTTP client by Square, Inc.
* [Gson](https://github.com/google/gson) helps with serialization/deserialization of Java Objects into JSON and back
* [Picasso](https://square.github.io/picasso/) allows for hassle-free image loading
* [ExoPlayer](https://github.com/google/ExoPlayer) an extensible media player for Android
* [Espresso](https://developer.android.com/training/testing/espresso/) a testing framework that makes it easy to write reliable user interface tests
* [AndroidUnsplash](https://github.com/KeenenCharles/AndroidUnplash) an interface to get images from Unsplash

## License
    Copyright 2020 demur

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.