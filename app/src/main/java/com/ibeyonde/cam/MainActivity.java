package com.ibeyonde.cam;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.ibeyonde.cam.databinding.ActivityMainBinding;
import com.ibeyonde.cam.ui.device.LiveFragment;
import com.ibeyonde.cam.ui.device.history.HistoryFragment;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private static final String TAG= MainActivity.class.getCanonicalName();

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(getApplicationContext());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_device, R.id.navigation_bluetooth, R.id.navigation_notifications, R.id.navigation_setting)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deviceListClick(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager();
        fragmentManager.popBackStack();
        getSupportActionBar().setTitle( "Device List");
    }

    public void cameraLiveClick(View view) {
        Log.i(TAG, "cameraLiveClick On Click =  " + view.getContentDescription());
        FragmentManager fragmentManager = getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager();

        LiveFragment liveFragment = new LiveFragment();
        liveFragment._cameraId = view.getContentDescription().toString();
        fragmentManager.beginTransaction()
                .replace(getSupportFragmentManager().getPrimaryNavigationFragment().getId(), liveFragment, "live")
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
        getSupportActionBar().setTitle(liveFragment._cameraId + " Live ");
    }

    public void cameraHistoryClick(View view) {
        Log.i(TAG, "cameraHistoryClick On Click =  " + view.getContentDescription());
        FragmentManager fragmentManager = getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager();

        HistoryFragment historyFragment = new HistoryFragment();
        historyFragment._cameraId = view.getContentDescription().toString();
        fragmentManager.beginTransaction()
                .replace(getSupportFragmentManager().getPrimaryNavigationFragment().getId(), historyFragment, "history")
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
        getSupportActionBar().setTitle(historyFragment._cameraId  + " History ");
    }



    public void runtimeEnableAutoInit() {
        // [START fcm_runtime_enable_auto_init]
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        // [END fcm_runtime_enable_auto_init]
    }

    public void deviceGroupUpstream() {
        // [START fcm_device_group_upstream]
        String to = "a_unique_key"; // the notification key
        AtomicInteger msgId = new AtomicInteger();
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(to)
                .setMessageId(String.valueOf(msgId.get()))
                .addData("hello", "world")
                .build());
        // [END fcm_device_group_upstream]
    }

    public void sendUpstream() {
        final String SENDER_ID = "YOUR_SENDER_ID";
        final int messageId = 0; // Increment for each
        // [START fcm_send_upstream]
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(SENDER_ID + "@fcm.googleapis.com")
                .setMessageId(Integer.toString(messageId))
                .addData("my_message", "Hello World")
                .addData("my_action","SAY_HELLO")
                .build());
        // [END fcm_send_upstream]
    }

}