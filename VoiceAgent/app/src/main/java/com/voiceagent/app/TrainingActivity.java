package com.voiceagent.app;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.voiceagent.app.ai.AILearningEngine;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Activity for training custom voice commands
 */
public class TrainingActivity extends AppCompatActivity {
    
    private EditText commandInput;
    private EditText actionInput;
    private Button recordButton;
    private Button saveButton;
    private TextView statusText;
    private TextView instructionsText;
    
    private SpeechRecognizer speechRecognizer;
    private AILearningEngine aiEngine;
    private String recordedCommand = "";
    private boolean isRecording = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        
        // Initialize views
        commandInput = findViewById(R.id.commandInput);
        actionInput = findViewById(R.id.actionInput);
        recordButton = findViewById(R.id.recordButton);
        saveButton = findViewById(R.id.saveButton);
        statusText = findViewById(R.id.statusText);
        instructionsText = findViewById(R.id.instructionsText);
        
        // Initialize AI engine
        aiEngine = new AILearningEngine(this);
        
        // Initialize speech recognizer
        initializeSpeechRecognizer();
        
        // Set up buttons
        recordButton.setOnClickListener(v -> toggleRecording());
        saveButton.setOnClickListener(v -> saveCustomCommand());
        
        // Set instructions
        instructionsText.setText(
            "1. Tap 'Record Command' and speak your custom command\n" +
            "2. Enter the action (e.g., package name or action type)\n" +
            "3. Tap 'Save' to train the AI"
        );
    }
    
    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                statusText.setText("Listening for command...");
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
                statusText.setText("Error recording command");
                isRecording = false;
                updateRecordButton();
            }
            
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    recordedCommand = matches.get(0);
                    commandInput.setText(recordedCommand);
                    statusText.setText("Command recorded: " + recordedCommand);
                }
                isRecording = false;
                updateRecordButton();
            }
            
            @Override
            public void onPartialResults(Bundle partialResults) {}
            
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }
    
    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }
    
    private void startRecording() {
        android.content.Intent intent = new android.content.Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        
        speechRecognizer.startListening(intent);
        isRecording = true;
        updateRecordButton();
    }
    
    private void stopRecording() {
        speechRecognizer.stopListening();
        isRecording = false;
        updateRecordButton();
    }
    
    private void updateRecordButton() {
        if (isRecording) {
            recordButton.setText("Stop Recording");
            recordButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            recordButton.setText("Record Command");
            recordButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        }
    }
    
    private void saveCustomCommand() {
        String command = commandInput.getText().toString().trim();
        String action = actionInput.getText().toString().trim();
        
        if (command.isEmpty()) {
            Toast.makeText(this, "Please record or enter a command", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (action.isEmpty()) {
            Toast.makeText(this, "Please enter an action", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save custom command to AI engine
        aiEngine.addCustomCommand(command, action);
        
        Toast.makeText(this, "Custom command saved!", Toast.LENGTH_SHORT).show();
        statusText.setText("Command '" + command + "' has been learned!");
        
        // Clear inputs
        commandInput.setText("");
        actionInput.setText("");
        recordedCommand = "";
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
