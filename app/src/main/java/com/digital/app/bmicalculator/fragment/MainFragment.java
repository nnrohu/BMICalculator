package com.digital.app.bmicalculator.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.digital.app.bmicalculator.AppExecutors;
import com.digital.app.bmicalculator.R;
import com.digital.app.bmicalculator.Utils.Const;
import com.digital.app.bmicalculator.database.AppDatabase;
import com.digital.app.bmicalculator.database.HistoryEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainFragment extends Fragment {

    private EditText mHeightView, mWeightView;
    private Spinner mWeightUnitView;
    private Button mCalculateView;

    private String weightUnit = Const.WEIGHT_IN_KG;
    private TextView mHeightUnitView;
    private TextView mResultView;
    private RelativeLayout mRl1, mRl2, mRl3, mRl4;
    private AddHistoryViewModel mViewModel;
    private AppDatabase mDb;

    public MainFragment() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mHeightView =     view.findViewById(R.id.et_main_height);
        mWeightView =     view.findViewById(R.id.et_main_weight);
        mWeightUnitView = view.findViewById(R.id.spinner_weight_unit);
        mHeightUnitView = view.findViewById(R.id.tv_height_unit);
        mResultView =     view.findViewById(R.id.tv_main_result);
        mCalculateView =  view.findViewById(R.id.bt_main_calculate);
        mRl2           =  view.findViewById(R.id.rel_2);
        mRl1           =  view.findViewById(R.id.rel_1);
        mRl3           =  view.findViewById(R.id.rel_3);
        mRl4           =  view.findViewById(R.id.rel_4);


        setUpSpinner();

        mCalculateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBmi();
            }
        });

        AppDatabase db = AppDatabase.getInstance(getContext());

        AddHistoryViewModelFactory factory = new AddHistoryViewModelFactory(db, -1);

        final AddHistoryViewModel viewModel
                = ViewModelProviders.of(this, factory).get(AddHistoryViewModel.class);

        viewModel.getHistory().observe(this, new Observer<HistoryEntry>() {
            @Override
            public void onChanged(@Nullable HistoryEntry historyEntry) {
                viewModel.getHistory().removeObserver(this);
            }
        });

        return view;
    }

    private void calculateBmi() {
        String height = mHeightView.getText().toString().trim();
        String weight = mWeightView.getText().toString().trim();

        Date date = new Date();

        if (TextUtils.isEmpty(height) && TextUtils.isEmpty(weight)){
            Toast.makeText(getContext(), "field is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        double heightVal = Double.parseDouble(height);
        double weightVal = Double.parseDouble(weight);

        String unitFromSpinner = getWeightUnitFromSpinner(weightUnit);
        if (unitFromSpinner == Const.WEIGHT_IN_KG) {
            double result = weightVal / (heightVal * heightVal);
            double magnitude = Double.parseDouble(formatMagnitude(result * 10000));

            final HistoryEntry entry = new HistoryEntry(
                    magnitude, weightVal, heightVal,
                    Const.WEIGHT_IN_KG, "cm", date
            );

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.historyDao().insertHistory(entry);
                }
            });

            mResultView.setBackgroundColor(getMagnitudeColor(magnitude));
            mResultView.setTextColor(Color.WHITE);
        } else {
            double result2 = weightVal / (heightVal * heightVal);
            double magnitude = Double.parseDouble(formatMagnitude(result2 * 703));

            final HistoryEntry entry = new HistoryEntry(
                    magnitude, weightVal, heightVal,
                    Const.WEIGHT_IN_POUND, "inch", date
            );

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.historyDao().insertHistory(entry);
                }
            });

            mResultView.setBackgroundColor(getMagnitudeColor(magnitude));
            mResultView.setTextColor(Color.WHITE);
        }

    }

    private String getWeightUnitFromSpinner(String weightUnit) {
        if (weightUnit == Const.WEIGHT_IN_KG) {
            return Const.WEIGHT_IN_KG;
        } else {
            return Const.WEIGHT_IN_POUND;
        }
    }


    private void setUpSpinner() {

        ArrayAdapter weightSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.array_weight_options, android.R.layout.simple_spinner_item);

        weightSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mWeightUnitView.setAdapter(weightSpinnerAdapter);

        mWeightUnitView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.weight_in_kg))) {
                        weightUnit = Const.WEIGHT_IN_KG;
                        mHeightUnitView.setText("cm");
                    } else {
                        weightUnit = Const.WEIGHT_IN_POUND;
                        mHeightUnitView.setText("inch");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                weightUnit = Const.WEIGHT_IN_KG;
            }
        });
    }

    public int getMagnitudeColor(double magnitudeFloor) {

        int magnitudeColorResourceId;

        if (isBetween(magnitudeFloor, 0.0, 18.59)) {
            magnitudeColorResourceId = R.color.magnitude1;
            mResultView.setText("BMI : " + magnitudeFloor + " Underweight");
            mResultView.setVisibility(View.VISIBLE);
            mRl1.setBackgroundColor(Color.WHITE);

        } else if (isBetween(magnitudeFloor, 18.60, 24.90)) {
            magnitudeColorResourceId = R.color.magnitude2;
            mResultView.setText("BMI : " + magnitudeFloor + " Healthy");
            mResultView.setVisibility(View.VISIBLE);
            mRl2.setBackgroundColor(Color.WHITE);

        } else if (isBetween(magnitudeFloor, 25.0, 29.90)) {
            magnitudeColorResourceId = R.color.magnitude3;
            mResultView.setText("BMI : " + magnitudeFloor + " Overweight");
            mResultView.setVisibility(View.VISIBLE);
            mRl3.setBackgroundColor(Color.WHITE);

        } else {
            magnitudeColorResourceId = R.color.magnitude4;
            mResultView.setText("BMI : " + magnitudeFloor + " Obese");
            mResultView.setVisibility(View.VISIBLE);
            mRl4.setBackgroundColor(Color.WHITE);

        }

        return ContextCompat.getColor(getContext(), magnitudeColorResourceId);
    }

    private static boolean isBetween(double x, double lower, double upper) {
        return lower <= x && x <= upper;
    }

    private String formatMagnitude(double magnitude) {
        DecimalFormat magnitudeFormat = new DecimalFormat("0.00");
        return magnitudeFormat.format(magnitude);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDb = AppDatabase.getInstance(getContext());
        mViewModel = ViewModelProviders.of(this).get(AddHistoryViewModel.class);


    }
}
