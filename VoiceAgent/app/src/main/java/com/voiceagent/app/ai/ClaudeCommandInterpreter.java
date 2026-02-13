package com.voiceagent.app.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.gson.JsonObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Integrates Claude AI into the command interpretation flow
 */
public class ClaudeCommandInterpreter {
    
    private static final String TAG = "ClaudeCommandInterpreter";
    
    private final ClaudeApiService claudeService;
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    public interface InterpretationCallback {
        void onInterpretationComplete(InterpretationResult result);
        void onInterpretationFailed(String error);
    }
    
    public ClaudeCommandInterpreter(Context context) {
        this.claudeService = new ClaudeApiService(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Interpret command using Claude AI (async)
     */
    public void interpretCommand(String command, InterpretationCallback callback) {
        executor.execute(() -> {
            try {
                ClaudeApiService.ClaudeResponse response = claudeService.interpretCommand(command);
                
                if (response.success) {
                    InterpretationResult result = new InterpretationResult(
                        command,
                        response.actionType,
                        response.parameters,
                        response.explanation
                    );
                    
                    mainHandler.post(() -> callback.onInterpretationComplete(result));
                } else {
                    mainHandler.post(() -> callback.onInterpretationFailed(response.explanation));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Interpretation failed", e);
                mainHandler.post(() -> callback.onInterpretationFailed(e.getMessage()));
            }
        });
    }
    
    /**
     * Check if Claude is available
     */
    public boolean isAvailable() {
        return claudeService.isConfigured();
    }
    
    /**
     * Result of command interpretation
     */
    public static class InterpretationResult {
        public final String originalCommand;
        public final String actionType;
        public final JsonObject parameters;
        public final String explanation;
        
        public InterpretationResult(String originalCommand, String actionType, 
                                   JsonObject parameters, String explanation) {
            this.originalCommand = originalCommand;
            this.actionType = actionType;
            this.parameters = parameters;
            this.explanation = explanation;
        }
        
        public String getParameter(String key) {
            if (parameters != null && parameters.has(key)) {
                return parameters.get(key).getAsString();
            }
            return null;
        }
        
        public boolean hasParameter(String key) {
            return parameters != null && parameters.has(key);
        }
    }
}
