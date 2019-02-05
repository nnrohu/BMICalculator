package com.digital.app.bmicalculator.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.digital.app.bmicalculator.database.AppDatabase;
import com.digital.app.bmicalculator.database.HistoryEntry;

public class AddHistoryViewModel extends ViewModel {

    private LiveData<HistoryEntry> history;

    public AddHistoryViewModel(AppDatabase database, int historyId) {
        history = database.historyDao().loadHistoryById(historyId);
    }

    public LiveData<HistoryEntry> getHistory() {
        return history;
    }

}
