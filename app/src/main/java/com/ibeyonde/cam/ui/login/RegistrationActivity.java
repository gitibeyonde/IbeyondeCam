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

import com.ibeyonde.cam.databinding.ActivityRegistrationBinding;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG=RegistrationActivity.class.getCanonicalName();


    private LoginViewModel loginViewModel;
    private ActivityRegistrationBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel =  new ViewModelProvider(this).get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button registerButton = binding.register;
        final ProgressBar loadingProgressBar = binding.loading;

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (loginViewModel.isPasswordValid(passwordEditText.getText().toString()) &&
                        loginViewModel.isUserNameValid(usernameEditText.getText().toString())) {
                    registerButton.setEnabled(true);
                }
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.register(getApplicationContext(), usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), binding.email.getText().toString(), binding.phone.getText().toString());
                Log.i(TAG, "onClick user=" + usernameEditText.getText().toString() + " pass=" + passwordEditText.getText().toString());
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

        loginViewModel._login_token.observe(this, new Observer<String>() {
            public void onChanged(@Nullable String s) {
                Log.i(TAG, "Login Activity token = " + s);
                loadingProgressBar.setVisibility(View.INVISIBLE);
                if ("FAILED".equals(s)) {
                    Toast.makeText(getApplicationContext(), "Registration Failed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Welcome to Ibeyonde CleverCam", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplication(), LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        //close app
        /**AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage("Do you want to exit application?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        finishAffinity();
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Log.d(TAG, "setNegativeButton");
                    }
                })
                .show();**/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}