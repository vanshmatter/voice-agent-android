package com.voiceagent.app.ai;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.voiceagent.app.utils.ApiKeyManager;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Service for communicating with Claude AI API
 */
public class ClaudeApiService {
    
    private static final String TAG = "ClaudeApiService";
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_VERSION = "2023-06-01";
    private static final String MODEL = "claude-3-5-sonnet-20241022";
    private static final int MAX_TOKENS = 1024;
    
    private final OkHttpClient client;
    private final Gson gson;
    private final ApiKeyManager apiKeyManager;
    private final Context context;
    
    public ClaudeApiService(Context context) {
        this.context = context;
        this.apiKeyManager = new ApiKeyManager(context);
        this.gson = new Gson();
        
        // Configure OkHttp client with timeouts
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }
    
    /**
     * Interpret a voice command using Claude AI
     */
    public ClaudeResponse interpretCommand(String command) {
        String apiKey = apiKeyManager.getApiKey();
        
        if (apiKey == null || apiKey.isEmpty()) {
            return new ClaudeResponse(false, "API key not configured", null, null);
        }
        
        try {
            String prompt = buildCommandInterpretationPrompt(command);
            String response = callClaudeApi(apiKey, prompt);
            return parseClaudeResponse(response);
            
        } catch (IOException e) {
            Log.e(TAG, "API call failed", e);
            return new ClaudeResponse(false, "Network error: " + e.getMessage(), null, null);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error", e);
            return new ClaudeResponse(false, "Error: " + e.getMessage(), null, null);
        }
    }
    
    /**
     * Build prompt for command interpretation
     */
    private String buildCommandInterpretationPrompt(String command) {
        return "You are an Android voice assistant interpreter. A user said: \"" + command + "\"\n\n" +
               "Your task is to interpret this command and provide:\n" +
               "1. The user's intent (what they want to do)\n" +
               "2. The action type (one of: call, message, search, open_app, alarm, reminder, navigation, weather, time, date, settings, custom)\n" +
               "3. Any parameters needed (contact name, app name, search query, etc.)\n" +
               "4. A brief explanation\n\n" +
               "Respond ONLY in this JSON format:\n" +
               "{\n" +
               "  \"intent\": \"brief description of intent\",\n" +
               "  \"action_type\": \"action type from list above\",\n" +
               "  \"parameters\": {\n" +
               "    \"key\": \"value\"\n" +
               "  },\n" +
               "  \"explanation\": \"brief explanation\",\n" +
               "  \"executable\": true/false\n" +
               "}";
    }
    
    /**
     * Call Claude API
     */
    private String callClaudeApi(String apiKey, String prompt) throws IOException {
        // Build request body
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("model", MODEL);
        requestJson.addProperty("max_tokens", MAX_TOKENS);
        requestJson.add("messages", gson.toJsonTree(new JsonObject[]{gson.fromJson(message, JsonObject.class)}));
        
        String jsonBody = gson.toJson(requestJson);
        
        RequestBody body = RequestBody.create(
            jsonBody,
            MediaType.parse("application/json")
        );
        
        // Build request
        Request request = new Request.Builder()
            .url(CLAUDE_API_URL)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", CLAUDE_VERSION)
            .addHeader("content-type", "application/json")
            .post(body)
            .build();
        
        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("API call failed: " + response.code() + " - " + errorBody);
            }
            
            return response.body() != null ? response.body().string() : "";
        }
    }
    
    /**
     * Parse Claude's response
     */
    private ClaudeResponse parseClaudeResponse(String responseJson) {
        try {
            JsonObject json = gson.fromJson(responseJson, JsonObject.class);
            
            // Extract content from Claude's response
            String content = json.getAsJsonArray("content")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
            
            // Parse the JSON content from Claude
            JsonObject interpretation = gson.fromJson(content, JsonObject.class);
            
            String intent = interpretation.get("intent").getAsString();
            String actionType = interpretation.get("action_type").getAsString();
            JsonObject parameters = interpretation.getAsJsonObject("parameters");
            String explanation = interpretation.get("explanation").getAsString();
            boolean executable = interpretation.get("executable").getAsBoolean();
            
            return new ClaudeResponse(true, explanation, actionType, parameters);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse Claude response", e);
            return new ClaudeResponse(false, "Failed to parse response", null, null);
        }
    }
    
    /**
     * Check if API key is configured
     */
    public boolean isConfigured() {
        String apiKey = apiKeyManager.getApiKey();
        return apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Response class for Claude API results
     */
    public static class ClaudeResponse {
        public final boolean success;
        public final String explanation;
        public final String actionType;
        public final JsonObject parameters;
        
        public ClaudeResponse(boolean success, String explanation, String actionType, JsonObject parameters) {
            this.success = success;
            this.explanation = explanation;
            this.actionType = actionType;
            this.parameters = parameters;
        }
        
        public String getParameter(String key) {
            if (parameters != null && parameters.has(key)) {
                return parameters.get(key).getAsString();
            }
            return null;
        }
    }
}
