package com.voiceagent.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.voiceagent.app.utils.ApiKeyManager;

/**
 * Settings activity for API key configuration
 */
public class SettingsActivity extends AppCompatActivity {
    
    private EditText apiKeyInput;
    private Button saveButton;
    private Button testButton;
    private Switch claudeToggle;
    private TextView statusText;
    
    private ApiKeyManager apiKeyManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Initialize views
        apiKeyInput = findViewById(R.id.apiKeyInput);
        saveButton = findViewById(R.id.saveButton);
        testButton = findViewById(R.id.testButton);
        claudeToggle = findViewById(R.id.claudeToggle);
        statusText = findViewById(R.id.statusText);
        
        // Initialize API key manager
        apiKeyManager = new ApiKeyManager(this);
        
        // Load existing settings
        loadSettings();
        
        // Set up listeners
        saveButton.setOnClickListener(v -> saveApiKey());
        testButton.setOnClickListener(v -> testApiKey());
        claudeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            apiKeyManager.setClaudeEnabled(isChecked);
            updateStatus();
        });
    }
    
    private void loadSettings() {
        String apiKey = apiKeyManager.getApiKey();
        if (apiKey != null) {
            // Show masked API key
            apiKeyInput.setText(maskApiKey(apiKey));
        }
        
        claudeToggle.setChecked(apiKeyManager.isClaudeEnabled());
        updateStatus();
    }
    
    private void saveApiKey() {
        String apiKey = apiKeyInput.getText().toString().trim();
        
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Please enter an API key", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Don't save if it's the masked version
        if (apiKey.contains("***")) {
            Toast.makeText(this, "API key already saved", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!apiKeyManager.isValidApiKey(apiKey)) {
            Toast.makeText(this, "Invalid API key format. Should start with 'sk-ant-'", 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        apiKeyManager.saveApiKey(apiKey);
        apiKeyInput.setText(maskApiKey(apiKey));
        Toast.makeText(this, "API key saved successfully", Toast.LENGTH_SHORT).show();
        updateStatus();
    }
    
    private void testApiKey() {
        if (!apiKeyManager.hasApiKey()) {
            Toast.makeText(this, "Please save an API key first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        statusText.setText("Testing API key...");
        
        // Simple test - just check if key is configured
        // In production, you might want to make a test API call
        new android.os.Handler().postDelayed(() -> {
            if (apiKeyManager.hasApiKey()) {
                statusText.setText("✓ API key is configured");
                Toast.makeText(this, "API key looks good!", Toast.LENGTH_SHORT).show();
            } else {
                statusText.setText("✗ API key not found");
            }
        }, 1000);
    }
    
    private void updateStatus() {
        if (apiKeyManager.hasApiKey()) {
            if (apiKeyManager.isClaudeEnabled()) {
                statusText.setText("✓ Claude AI is enabled and ready");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                statusText.setText("Claude AI is disabled");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            }
        } else {
            statusText.setText("✗ API key not configured");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
    
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return apiKey;
        }
        
        // Show first 7 chars and last 4 chars, mask the rest
        String start = apiKey.substring(0, 7);
        String end = apiKey.substring(apiKey.length() - 4);
        return start + "***************" + end;
    }
}
