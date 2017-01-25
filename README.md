# Abfall LRO

[Abfall LRO](https://play.google.com/store/apps/details?id=de.beusterse.abfallkalenderlandkreisrostock) is an Android app that provides information about the pickup days of the different trash cans in the district **Landkreis Rostock** here in Germany.

This is done by simply showing a countdown of days for each type of can, as well as illustrating whether there is a pickup on the current day or not. Users can pick their locations they want to have checked, change the schedule they're on, as well as set reminders for a pickup on the day before.

The app can be downloaded in the [Google Playstore](https://play.google.com/store/apps/details?id=de.beusterse.abfallkalenderlandkreisrostock).

In the following I provide information about requirements and setup instructions to run this app on your computer.

## Requirements

- Andorid Studio
- Android SDK
- Android 6.0 (API Level 23) installed
- Android SDK Build-Tools 23.0.2

## Setup

1. Clone the repository into a desired location.
2. Create the ```keystore.properties``` file according to the example below and save it in the main directory of the project.

  ```
  KEY_ALIAS=your_key_name
  KEY_PASSWORD=your_key_password
  STORE_FILE=/path/to/your/keystore/file
  STORE_PASSWORD=your_keystore_password
  ```
3. In Adnroid Studio, import Abfall LRO via **Open an existing Android Studio project**
