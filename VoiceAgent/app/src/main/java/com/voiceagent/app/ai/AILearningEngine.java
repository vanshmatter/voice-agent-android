package com.voiceagent.app.ai;

import android.content.Context;
import android.content.SharedPreferences;
import com.voiceagent.app.data.CommandEntity;
import com.voiceagent.app.data.TrainingDatabase;
import com.voiceagent.app.data.TrainingDataDao;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AI Learning Engine that learns from user interactions and improves over time
 */
public class AILearningEngine {
    
    private Context context;
    private TrainingDatabase database;
    private TrainingDataDao dao;
    private SharedPreferences preferences;
    private Map<String, String> customCommands;
    private Map<String, Integer> commandFrequency;
    
    public AILearningEngine(Context context) {
        this.context = context;
        this.database = TrainingDatabase.getInstance(context);
        this.dao = database.trainingDataDao();
        this.preferences = context.getSharedPreferences("ai_learning", Context.MODE_PRIVATE);
        this.customCommands = new HashMap<>();
        this.commandFrequency = new HashMap<>();
        
        loadCustomCommands();
        loadCommandFrequency();
    }
    
    /**
     * Normalize command using AI learning patterns
     */
    public String normalizeCommand(String command) {
        String normalized = command.toLowerCase(Locale.getDefault()).trim();
        
        // Remove common filler words
        normalized = normalized.replaceAll("\\b(please|could you|can you|would you)\\b", "");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        
        // Check for similar commands in history
        String similarCommand = findSimilarCommand(normalized);
        if (similarCommand != null) {
            return similarCommand;
        }
        
        return normalized;
    }
    
    /**
     * Find similar command using pattern matching
     */
    private String findSimilarCommand(String command) {
        List<CommandEntity> recentCommands = dao.getRecentSuccessfulCommands(50);
        
        double maxSimilarity = 0.0;
        String mostSimilar = null;
        
        for (CommandEntity entity : recentCommands) {
            double similarity = calculateSimilarity(command, entity.command);
            if (similarity > maxSimilarity && similarity > 0.7) {
                maxSimilarity = similarity;
                mostSimilar = entity.command;
            }
        }
        
        return mostSimilar;
    }
    
    /**
     * Calculate similarity between two strings using Levenshtein distance
     */
    private double calculateSimilarity(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }
    
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1),     // insertion
                    dp[i - 1][j - 1] + cost // substitution
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Record command execution for learning
     */
    public void recordCommand(String command, String type, boolean success) {
        CommandEntity entity = new CommandEntity();
        entity.command = command;
        entity.commandType = type;
        entity.success = success;
        entity.timestamp = System.currentTimeMillis();
        entity.context = getCurrentContext();
        
        // Save to database
        new Thread(() -> dao.insert(entity)).start();
        
        // Update frequency map
        commandFrequency.put(command, commandFrequency.getOrDefault(command, 0) + 1);
        saveCommandFrequency();
    }
    
    /**
     * Record unknown command for future learning
     */
    public void recordUnknownCommand(String command) {
        CommandEntity entity = new CommandEntity();
        entity.command = command;
        entity.commandType = "unknown";
        entity.success = false;
        entity.timestamp = System.currentTimeMillis();
        entity.context = getCurrentContext();
        
        new Thread(() -> dao.insert(entity)).start();
    }
    
    /**
     * Add custom command mapping
     */
    public void addCustomCommand(String command, String action) {
        customCommands.put(command.toLowerCase(Locale.getDefault()), action);
        saveCustomCommands();
        
        // Record as successful custom command
        recordCommand(command, "custom", true);
    }
    
    /**
     * Get action for custom command
     */
    public String getCustomCommandAction(String command) {
        return customCommands.get(command.toLowerCase(Locale.getDefault()));
    }
    
    /**
     * Get current context (time, day, etc.)
     */
    private String getCurrentContext() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
        
        String timeOfDay;
        if (hour < 12) timeOfDay = "morning";
        else if (hour < 17) timeOfDay = "afternoon";
        else if (hour < 21) timeOfDay = "evening";
        else timeOfDay = "night";
        
        return timeOfDay + "_" + dayOfWeek;
    }
    
    /**
     * Get total commands processed
     */
    public int getTotalCommandsProcessed() {
        return dao.getTotalCommandCount();
    }
    
    /**
     * Get custom command count
     */
    public int getCustomCommandCount() {
        return customCommands.size();
    }
    
    /**
     * Get accuracy rate
     */
    public float getAccuracy() {
        int total = dao.getTotalCommandCount();
        if (total == 0) return 0f;
        
        int successful = dao.getSuccessfulCommandCount();
        return (float) successful / total;
    }
    
    /**
     * Get most frequent commands
     */
    public Map<String, Integer> getMostFrequentCommands(int limit) {
        Map<String, Integer> sorted = new HashMap<>();
        commandFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }
    
    /**
     * Provide user feedback on command
     */
    public void provideFeedback(String command, boolean positive) {
        new Thread(() -> {
            CommandEntity entity = dao.getCommandByText(command);
            if (entity != null) {
                entity.userFeedback = positive ? 1 : -1;
                dao.update(entity);
            }
        }).start();
    }
    
    /**
     * Save custom commands to preferences
     */
    private void saveCustomCommands() {
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<String, String> entry : customCommands.entrySet()) {
            editor.putString("custom_" + entry.getKey(), entry.getValue());
        }
        editor.apply();
    }
    
    /**
     * Load custom commands from preferences
     */
    private void loadCustomCommands() {
        Map<String, ?> all = preferences.getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith("custom_")) {
                String command = entry.getKey().substring(7);
                String action = (String) entry.getValue();
                customCommands.put(command, action);
            }
        }
    }
    
    /**
     * Save command frequency
     */
    private void saveCommandFrequency() {
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<String, Integer> entry : commandFrequency.entrySet()) {
            editor.putInt("freq_" + entry.getKey(), entry.getValue());
        }
        editor.apply();
    }
    
    /**
     * Load command frequency
     */
    private void loadCommandFrequency() {
        Map<String, ?> all = preferences.getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith("freq_")) {
                String command = entry.getKey().substring(5);
                int frequency = (Integer) entry.getValue();
                commandFrequency.put(command, frequency);
            }
        }
    }
    
    /**
     * Get learning suggestions based on patterns
     */
    public String[] getLearningSuggestions() {
        // Analyze patterns and suggest improvements
        List<CommandEntity> unknownCommands = dao.getUnknownCommands(10);
        
        String[] suggestions = new String[Math.min(3, unknownCommands.size())];
        for (int i = 0; i < suggestions.length; i++) {
            suggestions[i] = "Learn: \"" + unknownCommands.get(i).command + "\"";
        }
        
        return suggestions;
    }
}
