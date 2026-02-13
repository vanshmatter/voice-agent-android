package com.voiceagent.app.ai;

import android.util.Log;
import java.util.Locale;

/**
 * Detects wake word "Nekro" in recognized speech
 */
public class WakeWordDetector {
    
    private static final String TAG = "WakeWordDetector";
    private static final String WAKE_WORD = "nekro";
    private static final float SIMILARITY_THRESHOLD = 0.7f;
    
    /**
     * Check if the recognized text contains the wake word
     */
    public boolean detectWakeWord(String recognizedText) {
        if (recognizedText == null || recognizedText.isEmpty()) {
            return false;
        }
        
        String normalized = normalizeText(recognizedText);
        Log.d(TAG, "Checking for wake word in: " + normalized);
        
        // Direct match
        if (normalized.contains(WAKE_WORD)) {
            Log.d(TAG, "Wake word detected (direct match)");
            return true;
        }
        
        // Check individual words with fuzzy matching
        String[] words = normalized.split("\\s+");
        for (String word : words) {
            if (isSimilarToWakeWord(word)) {
                Log.d(TAG, "Wake word detected (fuzzy match): " + word);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Normalize text for comparison
     */
    private String normalizeText(String text) {
        return text.toLowerCase(Locale.getDefault())
                   .replaceAll("[^a-z0-9\\s]", "")
                   .trim();
    }
    
    /**
     * Check if word is similar to wake word using Levenshtein distance
     */
    private boolean isSimilarToWakeWord(String word) {
        if (word.length() < 3) {
            return false; // Too short to be wake word
        }
        
        int distance = levenshteinDistance(word, WAKE_WORD);
        float similarity = 1.0f - ((float) distance / Math.max(word.length(), WAKE_WORD.length()));
        
        return similarity >= SIMILARITY_THRESHOLD;
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
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
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                        Math.min(dp[i - 1][j], dp[i][j - 1]),
                        dp[i - 1][j - 1]
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Get confidence score for wake word detection
     */
    public float getConfidence(String recognizedText) {
        if (recognizedText == null || recognizedText.isEmpty()) {
            return 0.0f;
        }
        
        String normalized = normalizeText(recognizedText);
        
        // Direct match = 100% confidence
        if (normalized.contains(WAKE_WORD)) {
            return 1.0f;
        }
        
        // Find best matching word
        String[] words = normalized.split("\\s+");
        float maxSimilarity = 0.0f;
        
        for (String word : words) {
            if (word.length() >= 3) {
                int distance = levenshteinDistance(word, WAKE_WORD);
                float similarity = 1.0f - ((float) distance / Math.max(word.length(), WAKE_WORD.length()));
                maxSimilarity = Math.max(maxSimilarity, similarity);
            }
        }
        
        return maxSimilarity;
    }
}
