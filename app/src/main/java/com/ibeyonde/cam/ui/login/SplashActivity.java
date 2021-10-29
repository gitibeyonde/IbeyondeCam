package com.ibeyonde.cam.ui.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ibeyonde.cam.MainActivity;
import com.ibeyonde.cam.databinding.ActivitySplashBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG= SplashActivity.class.getCanonicalName();
    private ActivitySplashBinding binding;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        File file = new File(getApplicationContext().getFilesDir(), ".cred");
        Log.i(TAG, "Getting creds in " + file.getAbsoluteFile());

        // save cred to file
        try (BufferedReader fo = new BufferedReader(new FileReader(file))) {
            String cred = fo.readLine();
            if (cred != null && cred.contains("%%")) {
                String[] cv = cred.split("%%");
                Log.i(TAG, "Read Credential=" + cv[0] + ", " + cv[1]);
                binding.progressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(getApplicationContext(), cv[0], cv[1]);
            }
            else {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    }
                }, 2000);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            Log.i(TAG, "Failed reading cred file " + e.getMessage());
            Intent i = new Intent(getApplication(), LoginActivity.class);
            startActivity(i);
            finish();
        }

        LoginViewModel._login_token.observe(this, new Observer<String>() {
            public void onChanged(@Nullable String s) {
                Log.i(TAG, "token changed Value = " + s);
                if (LoginViewModel.FAILURE.equals(s)){
                    Intent i = new Intent(getApplication(), LoginActivity.class);
                    startActivity(i);
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Welcome to CleverCam", Toast.LENGTH_SHORT).show();
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
        binding = null;
    }
}