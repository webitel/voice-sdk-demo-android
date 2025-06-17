# 📞 Android Demo: Audio Calling with voice-sdk-android

This is a simple demo Android app for initiating audio calls using a `voice-sdk-android` library.  
It shows how to configure a connection, start and manage a VoIP call, and handle basic call states such as mute, hold, and disconnect.


## Features

• Start and stop audio calls  
• Mute / unmute microphone  
• Hold / resume calls  
• Send DTMF tones  
• Call duration timer  
• Clean MVVM architecture with LiveData  
• Persistent auth config via SharedPreferences  
• Call event handling via CallListener interface  


## Project Structure

├── MainActivity              # UI and call control logic  
├── MainActivityVM            # ViewModel: state and call actions  
├── AuthInfo                  # Auth data model (host, token, issuer, username)  
├── AuthStorage               # SharedPreferences wrapper  
├── SettingDialogFragment     # Dialog for connection settings input  
├── DtmfDialogFragment        # Dialog for DTMF tone input  
├── VoiceClient               # External SDK dependency  
└── NotificationCallService   # Optional foreground call service  


## How to Use

1. **Clone the project**

```bash
git clone https://github.com/webitel/voice-sdk-demo-android.git
```