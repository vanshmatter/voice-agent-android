package com.voiceagent.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Manages secure storage of Claude API key
 */
public class ApiKeyManager {
    
    private static final String PREFS_NAME = "encrypted_prefs";
    private static final String KEY_API_KEY = "claude_api_key";
    private static final String KEY_ENABLED = "claude_enabled";
    
    private final SharedPreferences preferences;
    
    public ApiKeyManager(Context context) {
        try {
            // Use encrypted shared preferences for security
            MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
            
            preferences = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // Fallback to regular SharedPreferences if encryption fails
            preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }
    
    /**
     * Save API key
     */
    public void saveApiKey(String apiKey) {
        preferences.edit()
            .putString(KEY_API_KEY, apiKey)
            .apply();
    }
    
    /**
     * Get API key
     */
    public String getApiKey() {
        return preferences.getString(KEY_API_KEY, null);
    }
    
    /**
     * Check if API key is set
     */
    public boolean hasApiKey() {
        String key = getApiKey();
        return key != null && !key.isEmpty();
    }
    
    /**
     * Validate API key format
     */
    public boolean isValidApiKey(String apiKey) {
        // Basic validation - Claude API keys start with "sk-ant-"
        return apiKey != null && apiKey.startsWith("sk-ant-") && apiKey.length() > 20;
    }
    
    /**
     * Clear API key
     */
    public void clearApiKey() {
        preferences.edit()
            .remove(KEY_API_KEY)
            .apply();
    }
    
    /**
     * Enable/disable Claude AI
     */
    public void setClaudeEnabled(boolean enabled) {
        preferences.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply();
    }
    
    /**
     * Check if Claude AI is enabled
     */
    public boolean isClaudeEnabled() {
        return preferences.getBoolean(KEY_ENABLED, true);
    }
}
