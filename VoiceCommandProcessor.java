package com.voiceagent.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.AlarmClock;
import android.widget.Toast;
import com.voiceagent.app.ai.AILearningEngine;
import com.voiceagent.app.ai.ClaudeCommandInterpreter;
import com.voiceagent.app.data.CommandEntity;
import java.util.Calendar;
import java.util.Locale;

public class VoiceCommandProcessor {
    
    private Context context;
    private AILearningEngine aiEngine;
    private ClaudeCommandInterpreter claudeInterpreter;
    
    public interface CommandCallback {
        void onSuccess(String result);
        void onError(String error);
        void onUnknownCommand(String command);
    }
    
    public VoiceCommandProcessor(Context context, AILearningEngine aiEngine) {
        this.context = context;
        this.aiEngine = aiEngine;
        this.claudeInterpreter = new ClaudeCommandInterpreter(context);
    }
    
    public void processCommand(String command, CommandCallback callback) {
        String lowerCommand = command.toLowerCase(Locale.getDefault());
        
        // First, check if AI engine recognizes this as a custom command
        String customAction = aiEngine.getCustomCommandAction(lowerCommand);
        if (customAction != null) {
            executeCustomCommand(customAction, callback);
            aiEngine.recordCommand(lowerCommand, "custom", true);
            return;
        }
        
        // Use AI to improve command recognition
        String normalizedCommand = aiEngine.normalizeCommand(lowerCommand);
        
        // Process standard commands
        boolean executed = false;
        
        if (normalizedCommand.contains("call") || normalizedCommand.contains("dial")) {
            executed = handleCallCommand(normalizedCommand, callback);
        } else if (normalizedCommand.contains("message") || normalizedCommand.contains("text") 
                || normalizedCommand.contains("sms")) {
            executed = handleMessageCommand(normalizedCommand, callback);
        } else if (normalizedCommand.contains("search") || normalizedCommand.contains("google")) {
            executed = handleSearchCommand(normalizedCommand, callback);
        } else if (normalizedCommand.contains("open") || normalizedCommand.contains("launch")) {
            executed = handleOpenAppCommand(normalizedCommand, callback);
        } else if (normalizedCommand.contains("alarm") || normalizedCommand.contains("wake me")) {
            executed = handleAlarmCommand(normalizedCommand, callback);
        } else if (normalizedCommand.contains("time")) {
            executed = handleTimeCommand(callback);
        } else if (normalizedCommand.contains("date")) {
            executed = handleDateCommand(callback);
        } else if (normalizedCommand.contains("weather")) {
            executed = handleWeatherCommand(normalizedCommand, callback);
        } else if (normalizedCommand.contains("navigate") || normalizedCommand.contains("directions")) {
            executed = handleNavigationCommand(normalizedCommand, callback);
        } else if (normalizedCommand.contains("play music") || normalizedCommand.contains("play song")) {
            executed = handleMusicCommand(normalizedCommand, callback);
        } else {
            // Unknown command - try Claude AI if available
            if (claudeInterpreter.isAvailable()) {
                handleWithClaude(command, callback);
            } else {
                callback.onUnknownCommand(command);
                aiEngine.recordUnknownCommand(command);
            }
            return;
        }
        
        // Record command for learning
        String commandType = getCommandType(normalizedCommand);
        aiEngine.recordCommand(command, commandType, executed);
    }
    
    /**
     * Handle unknown command with Claude AI
     */
    private void handleWithClaude(String command, CommandCallback callback) {
        callback.onSuccess("Asking Claude AI for help...");
        
        claudeInterpreter.interpretCommand(command, new ClaudeCommandInterpreter.InterpretationCallback() {
            @Override
            public void onInterpretationComplete(ClaudeCommandInterpreter.InterpretationResult result) {
                // Execute the interpreted command
                executeClaudeInterpretation(result, callback);
                
                // Learn from Claude's interpretation
                aiEngine.addCustomCommand(command, result.actionType);
                aiEngine.recordCommand(command, result.actionType, true);
            }
            
            @Override
            public void onInterpretationFailed(String error) {
                callback.onError("Claude AI: " + error);
                aiEngine.recordUnknownCommand(command);
            }
        });
    }
    
    /**
     * Execute command based on Claude's interpretation
     */
    private void executeClaudeInterpretation(ClaudeCommandInterpreter.InterpretationResult result, 
                                            CommandCallback callback) {
        String actionType = result.actionType;
        
        try {
            boolean executed = false;
            
            switch (actionType) {
                case "call":
                    executed = handleCallCommand(result.getParameter("contact"), callback);
                    break;
                case "message":
                    executed = handleMessageCommand(result.originalCommand, callback);
                    break;
                case "search":
                    executed = handleSearchCommand(result.getParameter("query"), callback);
                    break;
                case "open_app":
                    executed = handleOpenAppCommand(result.getParameter("app_name"), callback);
                    break;
                case "alarm":
                    executed = handleAlarmCommand(result.originalCommand, callback);
                    break;
                case "navigation":
                    executed = handleNavigationCommand(result.getParameter("destination"), callback);
                    break;
                case "weather":
                    executed = handleWeatherCommand(result.getParameter("location"), callback);
                    break;
                case "time":
                    executed = handleTimeCommand(callback);
                    break;
                case "date":
                    executed = handleDateCommand(callback);
                    break;
                default:
                    callback.onSuccess("Claude says: " + result.explanation);
                    executed = true;
            }
            
            if (executed) {
                callback.onSuccess(result.explanation);
            }
            
        } catch (Exception e) {
            callback.onError("Failed to execute: " + e.getMessage());
        }
    }
    
    private boolean handleCallCommand(String command, CommandCallback callback) {
        try {
            String contact = extractContactName(command);
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + contact));
            context.startActivity(intent);
            callback.onSuccess("Calling " + contact);
            return true;
        } catch (Exception e) {
            callback.onError("Failed to make call: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleMessageCommand(String command, CommandCallback callback) {
        try {
            String contact = extractContactName(command);
            String message = extractMessageContent(command);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + contact));
            intent.putExtra("sms_body", message);
            context.startActivity(intent);
            
            callback.onSuccess("Opening message to " + contact);
            return true;
        } catch (Exception e) {
            callback.onError("Failed to send message: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleSearchCommand(String command, CommandCallback callback) {
        try {
            String query = command.replace("search", "")
                                 .replace("google", "")
                                 .replace("for", "")
                                 .trim();
            
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra("query", query);
            context.startActivity(intent);
            
            callback.onSuccess("Searching for: " + query);
            return true;
        } catch (Exception e) {
            callback.onError("Failed to search: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleOpenAppCommand(String command, CommandCallback callback) {
        try {
            String appName = command.replace("open", "")
                                   .replace("launch", "")
                                   .trim();
            
            Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(getPackageNameForApp(appName));
            
            if (intent != null) {
                context.startActivity(intent);
                callback.onSuccess("Opening " + appName);
                return true;
            } else {
                callback.onError("App not found: " + appName);
                return false;
            }
        } catch (Exception e) {
            callback.onError("Failed to open app: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleAlarmCommand(String command, CommandCallback callback) {
        try {
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false);
            
            // Extract time if specified
            // For simplicity, this opens the alarm app
            context.startActivity(intent);
            
            callback.onSuccess("Opening alarm settings");
            return true;
        } catch (Exception e) {
            callback.onError("Failed to set alarm: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleTimeCommand(CommandCallback callback) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        
        String time = String.format(Locale.getDefault(), 
            "The time is %02d:%02d", hour, minute);
        callback.onSuccess(time);
        return true;
    }
    
    private boolean handleDateCommand(CommandCallback callback) {
        Calendar calendar = Calendar.getInstance();
        String date = String.format(Locale.getDefault(),
            "Today is %tB %te, %tY", calendar, calendar, calendar);
        callback.onSuccess(date);
        return true;
    }
    
    private boolean handleWeatherCommand(String command, CommandCallback callback) {
        try {
            String location = command.replace("weather", "")
                                    .replace("in", "")
                                    .trim();
            
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra("query", "weather " + location);
            context.startActivity(intent);
            
            callback.onSuccess("Checking weather for " + location);
            return true;
        } catch (Exception e) {
            callback.onError("Failed to check weather: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleNavigationCommand(String command, CommandCallback callback) {
        try {
            String destination = command.replace("navigate", "")
                                       .replace("directions", "")
                                       .replace("to", "")
                                       .trim();
            
            Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + destination));
            context.startActivity(intent);
            
            callback.onSuccess("Navigating to " + destination);
            return true;
        } catch (Exception e) {
            callback.onError("Failed to navigate: " + e.getMessage());
            return false;
        }
    }
    
    private boolean handleMusicCommand(String command, CommandCallback callback) {
        try {
            Intent intent = new Intent("android.intent.action.MUSIC_PLAYER");
            context.startActivity(intent);
            callback.onSuccess("Opening music player");
            return true;
        } catch (Exception e) {
            callback.onError("Failed to play music: " + e.getMessage());
            return false;
        }
    }
    
    private void executeCustomCommand(String action, CommandCallback callback) {
        try {
            // Execute custom action learned by AI
            Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(action);
            
            if (intent != null) {
                context.startActivity(intent);
                callback.onSuccess("Executing custom command");
            } else {
                callback.onError("Custom action not available");
            }
        } catch (Exception e) {
            callback.onError("Failed to execute custom command: " + e.getMessage());
        }
    }
    
    private String extractContactName(String command) {
        // Simple extraction - can be improved with AI
        String[] words = command.split(" ");
        for (int i = 0; i < words.length - 1; i++) {
            if (words[i].equals("call") || words[i].equals("message") || words[i].equals("text")) {
                return words[i + 1];
            }
        }
        return "";
    }
    
    private String extractMessageContent(String command) {
        // Extract message after "saying" or similar keywords
        if (command.contains("saying")) {
            return command.substring(command.indexOf("saying") + 7).trim();
        }
        return "";
    }
    
    private String getPackageNameForApp(String appName) {
        // Map common app names to package names
        // This is a simplified version - can be enhanced with AI learning
        switch (appName.toLowerCase()) {
            case "chrome":
                return "com.android.chrome";
            case "gmail":
                return "com.google.android.gm";
            case "maps":
                return "com.google.android.apps.maps";
            case "youtube":
                return "com.google.android.youtube";
            case "camera":
                return "com.android.camera";
            case "gallery":
                return "com.google.android.apps.photos";
            default:
                return appName;
        }
    }
    
    private String getCommandType(String command) {
        if (command.contains("call")) return "call";
        if (command.contains("message") || command.contains("text")) return "message";
        if (command.contains("search")) return "search";
        if (command.contains("open")) return "open_app";
        if (command.contains("alarm")) return "alarm";
        if (command.contains("time")) return "time";
        if (command.contains("date")) return "date";
        if (command.contains("weather")) return "weather";
        if (command.contains("navigate")) return "navigation";
        if (command.contains("music")) return "music";
        return "other";
    }
}
