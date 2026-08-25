# AI Teacher - Android APK Project (Mithila Academy)

Package ID: com.mithilaacademy.spaai
App Name: AI Teacher

## How to Build the APK:

### Option 1: Using Android Studio (Recommended)
1. Unzip this folder.
2. Open Android Studio -> Select "Open an existing project" -> Choose this folder.
3. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. The generated APK will be in: `app/build/outputs/apk/debug/app-debug.apk`.

### Option 2: Command Line / Terminal
```bash
chmod +x ./gradlew
./gradlew assembleDebug
```
The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Features inside this Android App:
- Full-screen native immersive app
- Mithila Academy AI Teacher Voice Assistant
- Real-time Gemini Live voice streaming
- Android Microphone permission pre-configured
- Touch gesture customization (drag, pinch-to-resize, rotation)
- LocalStorage layout persistence
- Google Drive file browser & avatar integration
