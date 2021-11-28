package com.ibeyonde.cam;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.ibeyonde.cam.bt.BluetoothFragment;
import com.ibeyonde.cam.databinding.ActivityMainBinding;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceFragment;
import com.ibeyonde.cam.ui.device.live.LiveFragment;
import com.ibeyonde.cam.ui.device.history.HistoryFragment;
import com.ibeyonde.cam.ui.device.setting.DeviceSettingFragment;
import com.ibeyonde.cam.ui.notifications.BellAlertFragment;
import com.ibeyonde.cam.ui.notifications.NotificationFragment;
import com.ibeyonde.cam.ui.setting.SettingFragment;
import com.ibeyonde.cam.utils.CCFirebaseMessagingService;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private static final String TAG= MainActivity.class.getCanonicalName();

    private ActivityMainBinding binding;

    final Fragment _device = new DeviceFragment();
    final Fragment _notification = new NotificationFragment();
    final Fragment _bluetooth = new BluetoothFragment();
    final Fragment _settings = new SettingFragment();
    final LiveFragment _live = new LiveFragment();
    final HistoryFragment _history = new HistoryFragment();
    final DeviceSettingFragment _deviceSetting = new DeviceSettingFragment();
    final BellAlertFragment _bellAlert = new BellAlertFragment();
    final FragmentManager _fm = getSupportFragmentManager();
    final Fragment _active[] = { _device };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNavigation = findViewById(R.id.nav_view);
        bottomNavigation.setOnItemSelectedListener( new BottomNavigationView.OnItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_device:
                        _fm.beginTransaction().hide(_active[0]).show(_device).commit();
                        _active[0] = _device;
                        return true;

                    case R.id.navigation_notifications:
                        _fm.beginTransaction().hide(_active[0]).show(_notification).commit();
                        _active[0] = _notification;
                        return true;

                    case R.id.navigation_bluetooth:
                        _fm.beginTransaction().hide(_active[0]).show(_bluetooth).commit();
                        _active[0] = _bluetooth;
                        return true;

                    case R.id.navigation_setting:
                        _fm.beginTransaction().hide(_active[0]).show(_settings).commit();
                        _active[0] = _settings;
                        return true;

                    case R.id.bell_alert:
                        _fm.beginTransaction().hide(_active[0]).show(_bellAlert).commit();
                        _active[0] = _bellAlert;
                        return true;
                }
                return false;
            }

        });

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_device, R.id.navigation_bluetooth, R.id.bell_alert, R.id.navigation_notifications, R.id.navigation_setting)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String uuid = extras.getString("uuid");
            String datetime = extras.getString("datetime");

            if (uuid != null) {
                Log.i(TAG, "Bell Alert=" + uuid + " dt=" + datetime);
                _bellAlert._cameraId = uuid;
                _bellAlert._dateTime = datetime.replace('/', '-'); //"2021/09/28 07:49:47"
                NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.mobile_navigation);
                navGraph.setStartDestination(R.id.bell_alert);
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
        createNotificationChannel();
    }

    public void alertClick(View view) {
        Log.i(TAG, "cameraHistoryClick On Click =  " + view.getContentDescription());
        String desc[] = view.getContentDescription().toString().split("%%");
        _bellAlert._cameraId = desc[0];
        _bellAlert._dateTime = desc[1];
        getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager()
                .beginTransaction().replace(getSupportFragmentManager().getPrimaryNavigationFragment().getId(), _bellAlert, "bellAlert")
                .setReorderingAllowed(true)
                .addToBackStack("home").commit();
        getSupportActionBar().setTitle(_bellAlert._cameraId  + " Bell Alert ");
    }


    public void cameraLiveClick(View view) {
        Log.i(TAG, "cameraLiveClick On Click =  " + view.getContentDescription());
        _live._cameraId = view.getContentDescription().toString();
        getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager()
                .beginTransaction().replace(getSupportFragmentManager().getPrimaryNavigationFragment().getId(), _live, "live")
                .setReorderingAllowed(true)
                .addToBackStack("home").commit();
        getSupportActionBar().setTitle(_live._cameraId + " Live ");
    }

    public void cameraHistoryClick(View view) {
        Log.i(TAG, "cameraHistoryClick On Click =  " + view.getContentDescription());
        _history._cameraId = view.getContentDescription().toString();
        _history._list_size = 20;
        getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager()
                .beginTransaction().replace(getSupportFragmentManager().getPrimaryNavigationFragment().getId(), _history, "history")
                .setReorderingAllowed(true)
                .addToBackStack("home").commit();
        getSupportActionBar().setTitle(_history._cameraId  + " History ");
    }


    public void deviceSettingClick(View view) {
        Log.i(TAG, "deviceSettingClick On Click =  " + view.getContentDescription());
        _deviceSetting._cameraId = view.getContentDescription().toString();
        getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager()
                .beginTransaction().replace(getSupportFragmentManager().getPrimaryNavigationFragment().getId(), _deviceSetting, "settings")
                .setReorderingAllowed(true)
                .addToBackStack("home").commit();

        getSupportActionBar().setTitle(_deviceSetting._cameraId  + " Setting ");
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