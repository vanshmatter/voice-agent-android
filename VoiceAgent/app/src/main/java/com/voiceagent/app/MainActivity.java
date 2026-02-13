package com.voiceagent.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.voiceagent.app.ai.AILearningEngine;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private static final int REQUEST_RECORD_AUDIO = 1;
    private static final int REQUEST_PERMISSIONS = 100;
    
    private SpeechRecognizer speechRecognizer;
    private ImageButton micButton;
    private TextView statusText;
    private TextView learningStatsText;
    private TextView wakeWordStatusText;
    private boolean isListening = false;
    private boolean wakeWordServiceRunning = false;
    
    private AILearningEngine aiEngine;
    private VoiceCommandProcessor commandProcessor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI components
        micButton = findViewById(R.id.micButton);
        statusText = findViewById(R.id.statusText);
        learningStatsText = findViewById(R.id.learningStatsText);
        wakeWordStatusText = findViewById(R.id.wakeWordStatusText);
        
        // Initialize AI engine and command processor
        aiEngine = new AILearningEngine(this);
        commandProcessor = new VoiceCommandProcessor(this, aiEngine);
        
        // Request permissions
        requestPermissions();
        
        // Initialize speech recognizer
        initializeSpeechRecognizer();
        
        // Set up mic button
        micButton.setOnClickListener(v -> toggleListening());
        
        // Training button
        findViewById(R.id.trainingButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrainingActivity.class);
            startActivity(intent);
        });
        
        // Settings button
        findViewById(R.id.settingsButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        
        // Update learning stats
        updateLearningStats();
        
        // Start wake word service
        startWakeWordService();
        
        // Check if activated by wake word
        handleWakeWordActivation(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleWakeWordActivation(intent);
    }
    
    private void handleWakeWordActivation(Intent intent) {
        if (intent != null && intent.getBooleanExtra("wake_word_activated", false)) {
            statusText.setText("Wake word detected! Listening for command...");
            startListening();
        }
    }
    
    private void startWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        wakeWordServiceRunning = true;
        updateWakeWordStatus();
    }
    
    private void stopWakeWordService() {
        Intent serviceIntent = new Intent(this, WakeWordService.class);
        stopService(serviceIntent);
        wakeWordServiceRunning = false;
        updateWakeWordStatus();
    }
    
    private void updateWakeWordStatus() {
        if (wakeWordStatusText != null) {
            if (wakeWordServiceRunning) {
                wakeWordStatusText.setText("🎤 Listening for \"Nekro\"");
                wakeWordStatusText.setVisibility(View.VISIBLE);
            } else {
                wakeWordStatusText.setVisibility(View.GONE);
            }
        }
    }
    
    private void requestPermissions() {
        String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION
        };
        
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                permissionsToRequest.toArray(new String[0]), 
                REQUEST_PERMISSIONS);
        }
    }
    
    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    statusText.setText("Listening...");
                }
                
                @Override
                public void onBeginningOfSpeech() {
                    statusText.setText("Speak now");
                }
                
                @Override
                public void onRmsChanged(float rmsdB) {}
                
                @Override
                public void onBufferReceived(byte[] buffer) {}
                
                @Override
                public void onEndOfSpeech() {
                    statusText.setText("Processing...");
                }
                
                @Override
                public void onError(int error) {
                    String errorMessage = getErrorText(error);
                    statusText.setText("Error: " + errorMessage);
                    isListening = false;
                    updateMicButton();
                }
                
                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String command = matches.get(0);
                        processCommand(command);
                    }
                    isListening = false;
                    updateMicButton();
                }
                
                @Override
                public void onPartialResults(Bundle partialResults) {}
                
                @Override
                public void onEvent(int eventType, Bundle params) {}
            });
        } else {
            Toast.makeText(this, "Speech recognition not available", 
                Toast.LENGTH_SHORT).show();
        }
    }
    
    private void toggleListening() {
        if (isListening) {
            stopListening();
        } else {
            startListening();
        }
    }
    
    private void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO);
            return;
        }
        
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        
        speechRecognizer.startListening(intent);
        isListening = true;
        updateMicButton();
    }
    
    private void stopListening() {
        speechRecognizer.stopListening();
        isListening = false;
        updateMicButton();
        statusText.setText("Tap to speak");
    }
    
    private void updateMicButton() {
        if (isListening) {
            micButton.setImageResource(R.drawable.ic_mic_active);
            micButton.setBackgroundResource(R.drawable.mic_button_active);
        } else {
            micButton.setImageResource(R.drawable.ic_mic);
            micButton.setBackgroundResource(R.drawable.mic_button_background);
        }
    }
    
    private void processCommand(String command) {
        statusText.setText("You said: " + command);
        
        // Process command with AI learning
        commandProcessor.processCommand(command, new VoiceCommandProcessor.CommandCallback() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    statusText.setText(result);
                    updateLearningStats();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    statusText.setText("Error: " + error);
                });
            }
            
            @Override
            public void onUnknownCommand(String command) {
                runOnUiThread(() -> {
                    statusText.setText("Unknown command. Would you like to train me?");
                    // Optionally open training activity
                });
            }
        });
    }
    
    private void updateLearningStats() {
        int commandCount = aiEngine.getTotalCommandsProcessed();
        int customCommands = aiEngine.getCustomCommandCount();
        float accuracy = aiEngine.getAccuracy();
        
        String stats = String.format(Locale.getDefault(),
            "Commands: %d | Custom: %d | Accuracy: %.1f%%",
            commandCount, customCommands, accuracy * 100);
        learningStatsText.setText(stats);
    }
    
    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match found";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error";
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(this, "Microphone permission required", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }
}
