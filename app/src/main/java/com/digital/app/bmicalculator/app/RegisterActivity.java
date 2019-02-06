package com.digital.app.bmicalculator.app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.digital.app.bmicalculator.BuildConfig;
import com.digital.app.bmicalculator.R;
import com.digital.app.bmicalculator.Utils.Const;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    public static final String BMI_URL = "http://seoeasygo.com/Bmi_app/bmi_reg.php";

    private RadioGroup mRadioGroup;

    Calendar myCalendar = Calendar.getInstance();

    private EditText mDateOfBirth, mFullName, mEmail, mAge;

    private Button mRegister, mGuest;

    private String mGender = Const.GENDER_MALE;
    private String mId, mUserName,mUserEmail;


    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("id",mId);
        editor.putString("name",mUserName);
        editor.putString("email",mUserEmail);
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences mPreference = PreferenceManager.getDefaultSharedPreferences(this);
        String id = mPreference.getString("id", "");
        String uName = mPreference.getString("name", "");
        String uEmail = mPreference.getString("email", "");
        if (!id.equalsIgnoreCase("")){
            mId = id;
            mUserName = uName;
            mUserEmail = uEmail;
            if (mId != ""){
               Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
               intent.putExtra("name", mUserName);
               intent.putExtra("email", mUserEmail);
                startActivity(intent);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        enableStrictMode();

        mFullName = findViewById(R.id.et_reg_full_name);
        mAge = findViewById(R.id.et_reg_age);
        mEmail = findViewById(R.id.et_reg_email);

        mRegister = findViewById(R.id.bt_reg_register);
        mGuest = findViewById(R.id.bt_reg_as_guest);

        mDateOfBirth = findViewById(R.id.et_reg_date_of_birth);
        mDateOfBirth.setText(getCurrentDate());
        mDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(RegisterActivity.this, dateSetListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        mRadioGroup = findViewById(R.id.rg_reg_gender);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.rb_reg_male) {
                    mGender = Const.GENDER_MALE;
                } else {
                    mGender = Const.GENDER_FEMALE;
                }
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNetworkConnection()) {
                    saveToServer();
                } else {
                    Toast.makeText(RegisterActivity.this, getString(R.string.no_intermet_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public boolean checkNetworkConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void saveToServer() {
        mUserName = mFullName.getText().toString().trim();
        mUserEmail = mEmail.getText().toString().trim();
        final String userAge = mAge.getText().toString().trim();
        final String userDob = mDateOfBirth.getText().toString().trim();

        if (!validateName() || !validateEmail() || !validateAge()){
            Toast.makeText(this, "fill the blank field", Toast.LENGTH_SHORT).show();
            return;
        }


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST,
                BMI_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    String msg = object.get(getString(R.string.response_message)).toString();
                    mId = object.get(getString(R.string.reponse_uid)).toString();
                    if (msg.equals("Success")) {
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.putExtra("name", mUserName);
                        intent.putExtra("email", mUserEmail);
                        startActivity(intent);
                        finish();
                    }
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                final Map<String, String> jsonParam = new HashMap<>();
                jsonParam.put(Const.COLUMN_USER_NAME, mUserName);
                jsonParam.put(Const.COLUMN_USER_EMAIL, mUserEmail);
                jsonParam.put(Const.COLUMN_USER_AGE, userAge);
                jsonParam.put(Const.COLUMN_USER_DOB, userDob);
                jsonParam.put(Const.COLUMN_USER_GENDER, mGender);
                return jsonParam;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        requestQueue.add(sr);
    }

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.DATE, dayOfMonth);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.YEAR, year);
            updateLabel();
        }
    };

    private void updateLabel() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
        mDateOfBirth.setText(simpleDateFormat.format(myCalendar.getTime()));
    }

    private String getCurrentDate() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
        return df.format(c);
    }

    private boolean validateName() {
        if (mFullName.getText().toString().isEmpty()) {
            mFullName.setError(getString(R.string.error_msg_for_required_field));
            return false;
        } else {
            return true;
        }
    }

    private boolean validateEmail() {
        if (mEmail.getText().toString().isEmpty()) {
            mEmail.setError(getString(R.string.error_msg_for_required_field));
            return false;
        } else {
            return true;
        }
    }

    private boolean validateAge() {
        if (mAge.getText().toString().isEmpty()) {
            mAge.setError(getString(R.string.error_msg_for_required_field));
            return false;
        } else {
            return true;
        }
    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll().penaltyLog().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public void test(){
        Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show();
    }
}
