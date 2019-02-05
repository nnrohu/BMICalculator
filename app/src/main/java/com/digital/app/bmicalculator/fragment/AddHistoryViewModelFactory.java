package com.digital.app.bmicalculator.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.digital.app.bmicalculator.database.AppDatabase;
import com.digital.app.bmicalculator.database.HistoryEntry;

public class AddHistoryViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final int mHistoryId;

    public AddHistoryViewModelFactory(AppDatabase database, int historyId) {
        mDb = database;
        mHistoryId = historyId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new AddHistoryViewModel(mDb, mHistoryId);
    }

}
