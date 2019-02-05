package com.digital.app.bmicalculator.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY created_at")
    LiveData<List<HistoryEntry>> loadAllHistory();

    @Insert
    void insertHistory(HistoryEntry historyEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateHistory(HistoryEntry historyEntry);

    @Delete
    void deleteHistory(HistoryEntry historyEntry);

    @Query("SELECT * FROM history WHERE id = :id")
    LiveData<HistoryEntry> loadHistoryById(int id);

}
