![alt text](https://github.com/Singularity-Coder/Remember-Me/blob/main/assets/logo192.png)
# Remember-Me
You will never forget a face again! This is a contacts App that allows you to add 10 second videos as profile pictures to remember the reason he/she is a connection.

# Screenshots
![alt text](https://github.com/Singularity-Coder/Remember-Me/blob/main/assets/ss1.png)
![alt text](https://github.com/Singularity-Coder/Remember-Me/blob/main/assets/ss2.png)
![alt text](https://github.com/Singularity-Coder/Remember-Me/blob/main/assets/ss3.png)

## Tech stack & Open-source libraries
- Minimum SDK level 21
-  [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [LiveData](https://developer.android.com/topic/libraries/architecture/livedatahttps://developer.android.com/topic/libraries/architecture/livedata) for asynchronous.
- Jetpack
  - Lifecycle: Observe Android lifecycles and handle UI states upon the lifecycle changes.
  - DataBinding: Binds UI components in your layouts to data sources in your app using a declarative format rather than programmatically.
  - Room: Constructs Database by providing an abstraction layer over SQLite to allow fluent database access.
  - [Hilt](https://dagger.dev/hilt/): for dependency injection.
  - WorkManager: WorkManager allows you to schedule work to run one-time or repeatedly using flexible scheduling windows.
- Architecture
  - MVVM Architecture (View - DataBinding - ViewModel - Model)
- [Coil](https://github.com/coil-kt/coil): Image loading for Android and Compose Multiplatform.
- [ExoPlayer](https://github.com/google/ExoPlayer): An extensible media player for Android.
- [Material-Components](https://github.com/material-components/material-components-android): Material design components for building ripple animation, and CardView.
- [jsoup](https://mvnrepository.com/artifact/org.jsoup/jsoup): jsoup is a Java library that simplifies working with real-world HTML and XML.

## Architecture
![alt text](https://github.com/Singularity-Coder/Remember-Me/blob/main/assets/arch.png)

This App is based on the MVVM architecture and the Repository pattern, which follows the [Google's official architecture guidance](https://developer.android.com/topic/architecture).

The overall architecture of this App is composed of two layers; the UI layer and the data layer. Each layer has dedicated components and they have each different responsibilities.