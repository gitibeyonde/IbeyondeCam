package com.ibeyonde.cam;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
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
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.ibeyonde.cam.databinding.ActivityMainBinding;
import com.ibeyonde.cam.ui.device.live.LiveFragment;
import com.ibeyonde.cam.ui.device.history.HistoryFragment;
import com.ibeyonde.cam.utils.CCFirebaseMessagingService;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private static final String TAG= MainActivity.class.getCanonicalName();

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_device, R.id.navigation_bluetooth, R.id.navigation_history, R.id.navigation_notifications, R.id.navigation_setting)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String uuid = extras.getString("uuid");
            String view = extras.getString("view");

            if (uuid != null) {
                Log.i(TAG, "Bell Alert =  " + uuid);
                HistoryFragment._cameraId = uuid;
                HistoryFragment._list_size = 10;
                NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.mobile_navigation);
                navGraph.setStartDestination(R.id.navigation_history);
                navController.setGraph(navGraph);
            }
        }
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        FirebaseApp.initializeApp(this);
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
                        Log.d(TAG, "FCM Token Initialized " + token);
                        CCFirebaseMessagingService.registerToken(token, getApplicationContext());
                        //Toast.makeText(MainActivity.this, "CleverCam", Toast.LENGTH_SHORT).show();
                    }
                });
        //createNotificationChannel();
    }

    public void alertClick(View view) {
        Log.i(TAG, "cameraHistoryClick On Click =  " + view.getContentDescription());
        FragmentManager fragmentManager = getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager();

        HistoryFragment historyFragment = new HistoryFragment();
        historyFragment._cameraId = view.getContentDescription().toString();
        historyFragment._list_size = 10;
        fragmentManager.beginTransaction()
                .replace(getSupportFragmentManager().getPrimaryNavigationFragment().getId(), historyFragment, "history")
                .setReorderingAllowed(true)
                .addToBackStack("home")
                .commit();
        getSupportActionBar().setTitle(historyFragment._cameraId  + " History ");
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
        historyFragment._list_size = 20;
        fragmentManager.beginTransaction()
                .replace(getSupportFragmentManager().getPrimaryNavigationFragment().getId(), historyFragment, "history")
                .setReorderingAllowed(true)
                .addToBackStack("home")
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
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.default_notification_channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}