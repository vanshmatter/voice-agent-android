package com.voiceagent.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.voiceagent.app.ai.WakeWordDetector;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Foreground service for continuous wake word detection
 */
public class WakeWordService extends Service {
    
    private static final String TAG = "WakeWordService";
    private static final String CHANNEL_ID = "WakeWordChannel";
    private static final int NOTIFICATION_ID = 1001;
    
    private SpeechRecognizer speechRecognizer;
    private WakeWordDetector wakeWordDetector;
    private boolean isListening = false;
    private boolean shouldRestart = true;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        
        wakeWordDetector = new WakeWordDetector();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification("Listening for Nekro..."));
        
        initializeSpeechRecognizer();
        startListening();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY; // Restart if killed
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        shouldRestart = false;
        
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
    
    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(TAG, "Ready for speech");
                    isListening = true;
                }
                
                @Override
                public void onBeginningOfSpeech() {
                    Log.d(TAG, "Speech started");
                }
                
                @Override
                public void onRmsChanged(float rmsdB) {
                    // Audio level changed
                }
                
                @Override
                public void onBufferReceived(byte[] buffer) {
                    // Audio buffer received
                }
                
                @Override
                public void onEndOfSpeech() {
                    Log.d(TAG, "Speech ended");
                    isListening = false;
                }
                
                @Override
                public void onError(int error) {
                    Log.e(TAG, "Recognition error: " + error);
                    isListening = false;
                    
                    // Restart listening after error
                    if (shouldRestart) {
                        restartListening();
                    }
                }
                
                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                    
                    if (matches != null && !matches.isEmpty()) {
                        String recognizedText = matches.get(0);
                        Log.d(TAG, "Recognized: " + recognizedText);
                        
                        // Check for wake word
                        if (wakeWordDetector.detectWakeWord(recognizedText)) {
                            onWakeWordDetected(recognizedText);
                        } else {
                            // Not wake word, continue listening
                            if (shouldRestart) {
                                restartListening();
                            }
                        }
                    }
                }
                
                @Override
                public void onPartialResults(Bundle partialResults) {
                    // Partial results available
                }
                
                @Override
                public void onEvent(int eventType, Bundle params) {
                    // Event occurred
                }
            });
        } else {
            Log.e(TAG, "Speech recognition not available");
        }
    }
    
    private void startListening() {
        if (speechRecognizer != null && !isListening) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
            
            speechRecognizer.startListening(intent);
            Log.d(TAG, "Started listening");
        }
    }
    
    private void restartListening() {
        // Small delay before restarting to avoid rapid restarts
        new android.os.Handler().postDelayed(() -> {
            if (shouldRestart) {
                startListening();
            }
        }, 500);
    }
    
    private void onWakeWordDetected(String recognizedText) {
        Log.i(TAG, "Wake word detected: " + recognizedText);
        
        // Update notification
        updateNotification("Wake word detected!");
        
        // Launch main activity
        Intent launchIntent = new Intent(this, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchIntent.putExtra("wake_word_activated", true);
        startActivity(launchIntent);
        
        // Wait a bit before resuming wake word listening
        new android.os.Handler().postDelayed(() -> {
            updateNotification("Listening for Nekro...");
            if (shouldRestart) {
                restartListening();
            }
        }, 5000); // 5 second delay
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Wake Word Detection",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Listening for wake word 'Nekro'");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Agent AI")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }
    
    private void updateNotification(String contentText) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification(contentText));
        }
    }
}
