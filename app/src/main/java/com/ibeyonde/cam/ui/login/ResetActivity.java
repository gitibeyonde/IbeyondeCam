package com.ibeyonde.cam.ui.login;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ibeyonde.cam.databinding.ActivityResetBinding;

public class ResetActivity extends AppCompatActivity {
    private static final String TAG=ResetActivity.class.getCanonicalName();


    private LoginViewModel loginViewModel;
    private ActivityResetBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityResetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel =  new ViewModelProvider(this).get(LoginViewModel.class);

        final EditText useremailEditText = binding.useremail;
        final Button resetButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if ( loginViewModel.isUserEmailValid(useremailEditText.getText().toString())) {
                    resetButton.setEnabled(true);
                }
            }
        };
        useremailEditText.addTextChangedListener(afterTextChangedListener);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                loadingProgressBar.setVisibility(View.VISIBLE);
                String email = useremailEditText.getText().toString();
                loginViewModel.reset(getApplicationContext(), email);
            }
        });

        binding.loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(), LoginActivity.class);
                startActivity(i);
                finish();
            }
        });


        loginViewModel._reset_token.observe(this, new Observer<String>() {
            public void onChanged(@Nullable String s) {
                Log.i(TAG, "Login Activity token = " + s);
                loadingProgressBar.setVisibility(View.INVISIBLE);
                if (LoginViewModel.FAILURE.equals(s)) {
                    Toast.makeText(getApplicationContext(), "Reset password failed email address does not exists.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Instructions sent on registered email.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(getApplication(), LoginActivity.class);
        startActivity(i);
        finish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}