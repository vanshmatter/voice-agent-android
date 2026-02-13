package com.voiceagent.app.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TrainingDataDao {
    
    @Insert
    void insert(CommandEntity command);
    
    @Update
    void update(CommandEntity command);
    
    @Query("SELECT * FROM commands ORDER BY timestamp DESC LIMIT :limit")
    List<CommandEntity> getRecentCommands(int limit);
    
    @Query("SELECT * FROM commands WHERE success = 1 ORDER BY timestamp DESC LIMIT :limit")
    List<CommandEntity> getRecentSuccessfulCommands(int limit);
    
    @Query("SELECT * FROM commands WHERE commandType = 'unknown' ORDER BY timestamp DESC LIMIT :limit")
    List<CommandEntity> getUnknownCommands(int limit);
    
    @Query("SELECT * FROM commands WHERE command = :commandText LIMIT 1")
    CommandEntity getCommandByText(String commandText);
    
    @Query("SELECT COUNT(*) FROM commands")
    int getTotalCommandCount();
    
    @Query("SELECT COUNT(*) FROM commands WHERE success = 1")
    int getSuccessfulCommandCount();
    
    @Query("SELECT * FROM commands WHERE commandType = :type ORDER BY timestamp DESC")
    List<CommandEntity> getCommandsByType(String type);
    
    @Query("DELETE FROM commands WHERE timestamp < :timestamp")
    void deleteOldCommands(long timestamp);
    
    @Query("SELECT * FROM commands WHERE context = :context ORDER BY timestamp DESC LIMIT :limit")
    List<CommandEntity> getCommandsByContext(String context, int limit);
}
