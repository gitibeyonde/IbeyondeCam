package com.ibeyonde.cam.ui.login;

import android.app.Activity;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
        final EditText repeatPasswordEditText = binding.repeatPassword;
        final EditText emailEditText = binding.email;
        final EditText phoneEditText = binding.phone;
        final Button registerButton = binding.register;
        final ProgressBar loadingProgressBar = binding.loading;

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (usernameEditText.getText().toString().length() < 5 ){
                    Toast.makeText(getApplicationContext(), "Please enter valid username with at least 5 characters.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (!usernameEditText.getText().toString().matches("[a-zA-Z0-9]+") ){
                    Toast.makeText(getApplicationContext(), "Username should only contain alphabets and numbers.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (!emailEditText.getText().toString().matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$") ){
                    Toast.makeText(getApplicationContext(), "Enter a valid Email address .", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (!phoneEditText.getText().toString().matches("[0-9+ ]{10,}") ){
                    Toast.makeText(getApplicationContext(), "Enter a valid Phone number.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if ( passwordEditText.getText().toString().length() < 6 || passwordEditText.getText().toString().length() > 20) {
                    Toast.makeText(getApplicationContext(), "Password should have be between 6 to 20 characters.", Toast.LENGTH_SHORT).show();
                }
                else if ( !passwordEditText.getText().toString().matches("^[a-zA-Z_0-9\\-_,;.:#+*?=!§$%&/()@]+$")) {
                    Toast.makeText(getApplicationContext(), "Please enter alphabets, digits and any of -_;.:#+*?=!$%/()@.", Toast.LENGTH_SHORT).show();
                }
                else if ( ! passwordEditText.getText().toString().equals(repeatPasswordEditText.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Password and Repeat password do not match.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d(TAG, "Valid values");
                    registerButton.setEnabled(true);
                }
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        emailEditText.addTextChangedListener(afterTextChangedListener);
        phoneEditText.addTextChangedListener(afterTextChangedListener);
        repeatPasswordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.register(getApplicationContext(), usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), emailEditText.getText().toString(), phoneEditText.getText().toString());
                Log.i(TAG, "onClick user=" + usernameEditText.getText().toString());
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

        loginViewModel._register_token.observe(this, new Observer<String>() {
            public void onChanged(@Nullable String s) {
                Log.i(TAG, "Register Activity token = " + s);
                loadingProgressBar.setVisibility(View.INVISIBLE);
                if (LoginViewModel.SUCCESS.equals(s)) {
                    Toast.makeText(getApplicationContext(), "Success: A validation email is sent to your email id.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplication(), LoginActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
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