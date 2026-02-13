package com.voiceagent.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Background service for continuous voice recognition
 */
public class VoiceRecognitionService extends Service {
    
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        initializeSpeechRecognizer();
    }
    
    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(android.os.Bundle params) {}
            
            @Override
            public void onBeginningOfSpeech() {}
            
            @Override
            public void onRmsChanged(float rmsdB) {}
            
            @Override
            public void onBufferReceived(byte[] buffer) {}
            
            @Override
            public void onEndOfSpeech() {}
            
            @Override
            public void onError(int error) {
                // Restart listening on error
                if (isListening) {
                    startListening();
                }
            }
            
            @Override
            public void onResults(android.os.Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0);
                    processCommand(command);
                }
                
                // Continue listening
                if (isListening) {
                    startListening();
                }
            }
            
            @Override
            public void onPartialResults(android.os.Bundle partialResults) {}
            
            @Override
            public void onEvent(int eventType, android.os.Bundle params) {}
        });
    }
    
    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        
        speechRecognizer.startListening(intent);
    }
    
    private void processCommand(String command) {
        // Process command in background
        // This can be enhanced to work with the main activity
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isListening = true;
        startListening();
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isListening = false;
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
