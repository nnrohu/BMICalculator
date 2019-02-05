package com.digital.app.bmicalculator.fragment;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.digital.app.bmicalculator.database.AppDatabase;
import com.digital.app.bmicalculator.database.HistoryEntry;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private LiveData<List<HistoryEntry>> history;

    public HistoryViewModel(@NonNull Application application) {
        super(application);

        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        history = database.historyDao().loadAllHistory();
    }


    public LiveData<List<HistoryEntry>> getHistory(){
        return history;
   }
}
