# 🔒 MobileTheftDetectionSystem - Android Anti-Theft App

**Mobile Thesft Detection System** is a stealthy Android security application designed to capture intruder information by simulating a device shutdown. It records the front camera photo, audio, location, and sends them silently to a registered email address — all while the phone appears to be off.

## 🚀 Features

- 🕵️‍♂️ Fake shutdown screen to fool intruders
- 📸 Captures photo from front camera
- 🎤 Records audio in the background
- 🌍 Logs real-time location (GPS)
- 📧 Sends data (photo, audio, location) to registered email
- 📱 Works offline (with silent SMS)
- 👤 Maintains session login to manage user data
- 📇 Store and manage emergency contacts and backup emails using Room DB
- 🔐 Built with offline-first and privacy-first principles

## 📂 Project Structure
FakeApp/
├── app/
│ ├── java/com/example/fakeapp/
│ │ ├── activities/
│ │ ├── database/
│ │ ├── helpers/
│ │ ├── models/
│ │ └── services/
│ ├── res/
│ │ ├── layout/
│ │ ├── drawable/
│ │ └── values/
├── .gitignore
├── README.md
├── build.gradle
└── settings.gradle


## 🧪 How it Works

1. User logs in using registered email and password.
2. Emergency contacts and backup emails can be added for recovery actions.
3. When the intruder attempts to power off the device:
   - A fake shutdown screen is displayed.
   - Simultaneously, photo/audio/location are captured.
   - All data is sent silently to the user's registered email.
4. Silent SMS feature works offline to send alerts to emergency contacts.

## 🔧 Technologies Used

- Java
- Android SDK
- Room Database
- Camera API
- Location Services (GPS)
- Audio Recorder
- Email Intent
- BroadcastReceiver & Background Services

## 📦 Setup Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/FakeShutdown.git
   cd FakeShutdown
