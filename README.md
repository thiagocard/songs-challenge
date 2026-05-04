# songs-challenge

This project is an Android application demonstrating modern Android development practices, focusing on a modular architecture, Jetpack Compose for UI, and efficient data handling. It functions as a music browsing and playback application.

## Bonus Features

*   **Error/States handling**: The app gracefully handles various states such as loading, empty results, and error scenarios, providing appropriate feedback to the user.
*   **Offline support**: Integration with Room suggests local caching and offline capabilities.
*   **Swipe to Refresh**: Refresh the list of songs with a pull-to-refresh gesture.
*   **Player**: A fully functional music player that allows users to play, pause, skip and toggle repeat songs.
*   **Adaptive UI**: UI adjusts for different screen sizes and orientations, ensuring a consistent experience across devices.
*   **Shared Element Transitions**: Smooth animations between screens.

## Architecture

The project follows a clean, modular architecture to promote separation of concerns, scalability, and maintainability. It is structured into several Gradle modules:

*   **`:app`**: The main application module, responsible for assembling the features and setting up the overall application graph.
*   **`:core`**: Contains foundational modules used across various features.
    *   **`:core:common`**: General utility functions, constants, and common interfaces.
    *   **`:core:database`**: Handles local data persistence using `Room`.
    *   **`:core:navigation`**: Defines navigation graphs and routes for the application.
    *   **`:core:networking`**: Manages network requests using `Ktor` and `OkHttp`.
    *   **`:core:ui`**: Contains reusable UI components and themes for Jetpack Compose.
*   **`:feature`**: Contains independent feature modules. Each feature is typically broken down into presentation, domain, and data layers.
    *   **`:feature:home`**: Manages the main song browsing and search functionality.
    *   **`:feature:player`**: Handles music playback functionality.
*   **`:build-logic`**: Contains convention plugins for consistent build configurations across modules.
*   **`:support:mock`**: Provides mock implementations for both testing and previewing purposes.

This modularization allows features to be developed and tested in isolation and facilitates a clear dependency flow.

## Architectural Decisions

### Handling API Pagination with Duplicate Results

A significant architectural decision was made in the `home` feature, specifically within the `core:database` module, to address challenges with the external API's pagination. The API, when queried for song lists or search results, occasionally returns duplicate entries across different pages. This behavior is problematic for features like infinite scrolling with `Jetpack Paging 3`, as it leads to inconsistent UI and potential data integrity issues.

To mitigate this, a dedicated table named `search_results` (represented by `SearchResultsEntity` in `core:database`) was introduced. This table serves as a cache and a de-duplication layer between the network and the UI. When new pages of data are fetched from the API:

1.  The incoming results are processed to remove duplicates based on a unique identifier (e.g., song ID).
2.  The de-duplicated results are then inserted into the `search_results` table.
3.  The UI, via `Jetpack Paging 3`, observes changes in this local `search_results` table, ensuring that only unique and correctly ordered items are displayed.

This approach ensures a smooth and reliable user experience for pagination, even when dealing with an imperfect external API, and also provides basic offline capabilities for recently viewed search results.

## Tech Stack

The project leverages a modern Android tech stack:

*   **Kotlin**: Primary programming language.
*   **Jetpack Compose**: Declarative UI toolkit for building native Android UIs.
*   **Jetpack Paging 3**: For efficient loading and displaying large datasets.
*   **Jetpack Navigation**: For navigating between Composables.
*   **Hilt**: Dependency Injection framework built on Dagger.
*   **Room**: Persistence library for local database storage.
*   **Ktor**: Multiplatform asynchronous HTTP client for networking.
*   **Kotlinx Serialization**: For JSON serialization/deserialization.
*   **Coil**: Image loading library for Compose.
*   **Material 3**: Latest iteration of Material Design for Android.

## Setup and Installation

To get the project up and running:

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/songs-challenge.git
    cd songs-challenge
    ```
2.  **Open in Android Studio**:
    Open the project in Android Studio (Jellyfish or newer recommended).
3.  **Gradle Sync**:
    Allow Android Studio to perform a Gradle sync to download all necessary dependencies.
4.  **Run on Device/Emulator**:
    Select the `:app` module and run the application on an Android emulator or a physical device.

## Usage

*   Navigate through the list of songs.
*   Use the search bar to find specific songs.
*   Tap on a song to play it.
*   Use the menu on a song item to view its album.
