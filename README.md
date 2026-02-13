# Voice Agent AI - Android Application

## 🎯 Overview

**Voice Agent AI** is an intelligent Android application that uses voice recognition with **AI-powered learning capabilities**. The agent executes voice commands and learns from user interactions, adapts to preferences, recognizes patterns, and allows users to train custom commands.

## ✨ Key Features

### 🎤 Voice Recognition
- Real-time voice command processing
- Support for multiple command types
- Background listening capability
- High accuracy speech recognition

### 🧠 AI Learning Engine
- **Pattern Recognition**: Learns common command patterns and user habits
- **Custom Command Training**: Users can teach the agent new commands
- **Adaptive Behavior**: Adjusts responses based on user feedback
- **Preference Learning**: Remembers user preferences (favorite contacts, apps, etc.)
- **Context Awareness**: Learns from time, location, and usage patterns
- **On-Device Learning**: All learning happens locally using TensorFlow Lite

### 🤖 Claude AI Integration
- **Intelligent Command Interpretation**: Claude analyzes unknown commands
- **Natural Language Understanding**: Handles complex, conversational requests
- **Execution Guidance**: Claude provides step-by-step instructions
- **Auto-Learning**: Learns from Claude's interpretations for future use
- **Smart Fallback**: Only uses Claude when local AI can't understand
- **Privacy-Focused**: API calls made only for unknown commands

### 🎙️ Wake Word Detection
- **Hands-Free Activation**: Say "Nekro" to activate the agent
- **Continuous Listening**: Background service always ready
- **Fuzzy Matching**: Recognizes variations (Necro, Nikro, etc.)
- **Battery Optimized**: Low-power listening mode
- **Visual Feedback**: Status indicator shows when listening
- **User Control**: Enable/disable in settings

### 📱 Supported Commands

#### Communication
- `"Call [contact name]"` - Make phone calls
- `"Send message to [contact]"` - Send SMS messages

#### Apps & Navigation
- `"Open [app name]"` - Launch applications
- `"Navigate to [place]"` - Get directions

#### Search & Information
- `"Search for [query]"` - Web search
- `"What time is it?"` - Get current time
- `"What's the date?"` - Get current date
- `"Weather in [location]"` - Check weather

#### Utilities
- `"Set alarm"` - Set alarms
- `"Play music"` - Open music player

#### Custom Commands
- Train your own commands like `"lights on"` → open specific app
- Unlimited custom command possibilities

## 🏗️ Architecture

### Core Components

1. **MainActivity.java** - Main UI with voice recognition
2. **VoiceCommandProcessor.java** - Command processing and execution
3. **AILearningEngine.java** - Machine learning and pattern recognition
4. **TrainingActivity.java** - Custom command training interface
5. **VoiceRecognitionService.java** - Background voice recognition

### Data Layer

- **Room Database** - Stores command history and training data
- **SharedPreferences** - Stores custom commands and preferences
- **TensorFlow Lite** - On-device machine learning

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 24 (Android 7.0) or higher
- Physical Android device or emulator with microphone

### Installation

1. **Clone or Download** the project
2. **Open in Android Studio**:
   - File → Open → Select the `VoiceAgent` folder
3. **Sync Gradle**: Wait for Gradle sync to complete
4. **Build the Project**: Build → Make Project
5. **Run**: Click Run or press Shift+F10

### First Run

1. Grant required permissions when prompted:
   - Microphone access
   - Phone access (for calls)
   - SMS access (for messages)
   - Contacts access
   - Location access (for context awareness)

2. Tap the microphone button and start speaking commands

## 🤖 Setting Up Claude AI

Claude AI integration is **optional** but highly recommended for handling complex or unknown commands.

### Get Your API Key

1. Visit [console.anthropic.com](https://console.anthropic.com/)
2. Sign up or log in to your account
3. Navigate to **API Keys** section
4. Click **Create Key**
5. Copy your API key (starts with `sk-ant-`)

### Configure in App

1. Open the Voice Agent app
2. Tap the **Settings** icon (⚙️) in the top-right
3. Enter your Claude API key
4. Tap **Save**
5. Enable **Claude AI** toggle
6. Tap **Test** to verify

### How It Works

- **Local AI First**: The app tries to understand commands locally
- **Claude Fallback**: Unknown commands are sent to Claude
- **Auto-Learning**: Claude's interpretations are saved for future use
- **Cost-Effective**: Claude is only called when needed

### Example Scenarios

**Complex Command**:
- You: "Remind me to call mom when I get home"
- Claude: Interprets as location-based reminder
- App: Opens reminder app with pre-filled data

**Natural Language**:
- You: "Find a good Italian restaurant nearby"
- Claude: Interprets as restaurant search + location
- App: Opens Google Maps with search query

**Learning**:
- You: "Turn on do not disturb"
- Claude: Interprets as DND mode activation
- App: Executes and saves for next time (no Claude needed)

## 🎓 Training Custom Commands

1. Tap **"Train Custom Commands"** button
2. Tap **"Record Command"** and speak your custom phrase
3. Enter the **action** (package name or action type)
4. Tap **"Save Custom Command"**
5. Test your custom command from the main screen

### Example Custom Commands

- **Command**: "lights on"  
  **Action**: `com.philips.lighting.hue2`

- **Command**: "open camera"  
  **Action**: `com.android.camera`

- **Command**: "start workout"  
  **Action**: `com.nike.plusgps`

## 🔧 Configuration

### Adding New Command Types

Edit `VoiceCommandProcessor.java` to add new command handlers:

```java
else if (normalizedCommand.contains("your_keyword")) {
    executed = handleYourCustomCommand(normalizedCommand, callback);
}
```

### Customizing AI Learning

Modify `AILearningEngine.java` to adjust:
- Similarity threshold for pattern matching
- Context analysis parameters
- Learning rate and adaptation speed

## 📊 Learning Statistics

The app displays:
- **Total Commands**: Number of commands processed
- **Custom Commands**: Number of user-trained commands
- **Accuracy**: Success rate of command execution

## 🔒 Privacy & Security

- **All data stays on device** - No external servers
- **Local AI processing** - TensorFlow Lite runs on-device
- **Secure storage** - Room database with encryption support
- **Permission-based** - Only requests necessary permissions

## 🛠️ Technologies Used

- **Language**: Java
- **UI**: Material Design 3
- **Database**: Room (SQLite)
- **ML Framework**: TensorFlow Lite
- **Speech**: Android SpeechRecognizer API
- **Build System**: Gradle

## 📱 Minimum Requirements

- Android 7.0 (API 24) or higher
- Microphone
- Internet connection (for online speech recognition)
- 50 MB storage space

## 🐛 Troubleshooting

### Voice recognition not working
- Check microphone permissions
- Ensure internet connection
- Verify Google app is updated

### Commands not executing
- Grant required permissions (Phone, SMS, Contacts)
- Check if apps are installed
- Try retraining the command

### AI not learning
- Use feedback buttons (thumbs up/down)
- Train more custom commands
- Check storage permissions

## 📄 License

This project is open source and available for educational purposes.

## 🤝 Contributing

Contributions are welcome! Feel free to:
- Add new command types
- Improve AI learning algorithms
- Enhance UI/UX
- Fix bugs

## 📞 Support

For issues or questions, please check:
1. This README
2. Code comments
3. Android documentation

---

**Made with ❤️ using AI-powered development**
