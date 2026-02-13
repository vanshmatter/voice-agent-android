package com.voiceagent.app.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CommandEntity.class}, version = 1, exportSchema = false)
public abstract class TrainingDatabase extends RoomDatabase {
    
    private static TrainingDatabase instance;
    
    public abstract TrainingDataDao trainingDataDao();
    
    public static synchronized TrainingDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                TrainingDatabase.class,
                "voice_agent_training_db"
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
}
