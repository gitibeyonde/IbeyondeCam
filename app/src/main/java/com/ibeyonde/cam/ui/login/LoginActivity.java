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

import com.ibeyonde.cam.MainActivity;
import com.ibeyonde.cam.databinding.ActivityLoginBinding;

import java.io.File;
import java.io.FileWriter;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG=LoginActivity.class.getCanonicalName();


    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel =  new ViewModelProvider(this).get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;
        loginButton.setEnabled(false);

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (usernameEditText.getText().toString().length() < 4 ){
                    return;
                }
                else if (!usernameEditText.getText().toString().matches("[a-zA-Z0-9]+") ){
                    return;
                }
                else {
                    loginButton.setEnabled(true);
                }
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(getApplicationContext(), usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
                Log.i(TAG, "onClick user=" + usernameEditText.getText().toString() + " pass=" + passwordEditText.getText().toString());
            }
        });

        binding.regLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(), RegistrationActivity.class);
                startActivity(i);
                finish();
            }
        });

        binding.resetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(), ResetActivity.class);
                startActivity(i);
                finish();
            }
        });

        loginViewModel._login_token.observe(this, new Observer<String>() {
            public void onChanged(@Nullable String s) {
                Log.i(TAG, "Login Activity token = " + s);
                if (LoginViewModel.FAILURE.equals(s)) {
                    Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                    loadingProgressBar.setVisibility(View.INVISIBLE);
                    usernameEditText.setText(null);
                    passwordEditText.setText(null);
                } else {
                    Toast.makeText(getApplicationContext(), "Welcome to CleverCam", Toast.LENGTH_SHORT).show();
                    File file = new File(getApplicationContext().getFilesDir(), ".cred");
                    Log.i(TAG, "Saving credentials to " + file.getAbsoluteFile());
                    try (FileWriter fo = new FileWriter(file)) {
                        String cred = usernameEditText.getText().toString() + "%%" + passwordEditText.getText().toString();
                        fo.write(cred);
                        Log.i(TAG, "Creds= " + cred);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent i = new Intent(getApplication(), MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}