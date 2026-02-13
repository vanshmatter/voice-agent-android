package com.voiceagent.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "commands")
public class CommandEntity {
    
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String command;
    public String commandType;
    public boolean success;
    public long timestamp;
    public String context;
    public int userFeedback; // 1 for positive, -1 for negative, 0 for none
    
    public CommandEntity() {
        this.userFeedback = 0;
    }
}
