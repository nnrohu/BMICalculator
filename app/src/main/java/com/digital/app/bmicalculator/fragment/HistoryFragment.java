package com.digital.app.bmicalculator.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.digital.app.bmicalculator.AppExecutors;
import com.digital.app.bmicalculator.R;
import com.digital.app.bmicalculator.database.AppDatabase;
import com.digital.app.bmicalculator.database.HistoryEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private static final int DEFAULT_HISTORY_ID = -1;
    private static final String LOG_TAG = HistoryFragment.class.getSimpleName();
    private int mHistoryId = DEFAULT_HISTORY_ID;

    // Member variable for the Database
    private AppDatabase mDb;

    private HistoryViewModel mViewModel;
    private HistoryAdapter mAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        mAdapter = new HistoryAdapter(getContext());

        RecyclerView recyclerView = view.findViewById(R.id.history_item_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        recyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<HistoryEntry> history = mAdapter.getHistory();
                        mDb.historyDao().deleteHistory(history.get(position));
                    }
                });
            }
        }).attachToRecyclerView(recyclerView);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDb = AppDatabase.getInstance(getContext());
        mViewModel = ViewModelProviders.of(this).get(HistoryViewModel.class);
        mViewModel.getHistory().observe(this, new Observer<List<HistoryEntry>>() {
            @Override
            public void onChanged(@Nullable List<HistoryEntry> historyEntries) {
                mAdapter.setHistory(historyEntries);
            }
        });

    }

    public static class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder>{
        private Context mContext;

        private List<HistoryEntry> mHistoryEntry;

        // Constant for date format
        private static final String DATE_FORMAT = "dd/MM/yyyy 'at' HH:mm";
        private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

        public HistoryAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.history_item, parent, false);
            return new HistoryViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {

            HistoryEntry historyEntry = mHistoryEntry.get(position);
            double magnitude = historyEntry.getMagnitude();
            double weight = historyEntry.getWeight();
            String heightUnit = historyEntry.getHeightUnit();
            double height = historyEntry.getHeight();
            String weightUnit = historyEntry.getWeightUnit();
            Date createdAt = historyEntry.getCreatedAt();

            holder.magnitudeView.setText(String.valueOf(magnitude));
            holder.weightView.setText(weight + " " + weightUnit);
            holder.heightView.setText(height + " " + heightUnit);
            holder.dateView.setText(dateFormat.format(createdAt));

            GradientDrawable magnitudeCircle = (GradientDrawable) holder.magnitudeView.getBackground();
            // Get the appropriate background color based on the priority
            int magnitudeColor = getMagnitudeColor(magnitude);
            magnitudeCircle.setColor(magnitudeColor);
        }

        public int getMagnitudeColor(double magnitude) {

            int magnitudeColorResourceId;

            if (isBetween(magnitude, 0.0, 18.59)) {
                magnitudeColorResourceId = R.color.magnitude1;
            } else if (isBetween(magnitude, 18.60, 24.90)) {
                magnitudeColorResourceId = R.color.magnitude2;
            } else if (isBetween(magnitude, 25.0, 29.90)) {
                magnitudeColorResourceId = R.color.magnitude3;
            } else {
                magnitudeColorResourceId = R.color.magnitude4;
            }

            return ContextCompat.getColor(mContext, magnitudeColorResourceId);
        }

        private  boolean isBetween(double x, double lower, double upper) {
            return lower <= x && x <= upper;
        }



        public void setHistory(List<HistoryEntry> historyEntry) {
            mHistoryEntry = historyEntry;
            notifyDataSetChanged();
        }


        @Override
        public int getItemCount() {
            return mHistoryEntry == null ? 0 : mHistoryEntry.size();
        }


        public List<HistoryEntry> getHistory() {
            return mHistoryEntry;
        }

    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder{

        TextView magnitudeView;
        TextView weightView;
        TextView heightView;
        TextView dateView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            heightView = itemView.findViewById(R.id.his_height);
            weightView = itemView.findViewById(R.id.his_weight);
            dateView = itemView.findViewById(R.id.his_date);
            magnitudeView = itemView.findViewById(R.id.his_magnitude);
        }


    }
}
