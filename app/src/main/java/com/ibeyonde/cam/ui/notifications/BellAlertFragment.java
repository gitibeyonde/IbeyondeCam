package com.ibeyonde.cam.ui.notifications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ibeyonde.cam.databinding.FragmentBellAlertBinding;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.ui.device.live.LiveViewModel;
import com.ibeyonde.cam.ui.device.live.MjpegRunner;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.AlertDetails;
import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.ImageLoadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BellAlertFragment extends Fragment {
    private static final String TAG= BellAlertFragment.class.getCanonicalName();

    public static volatile String _cameraId;
    public static volatile String _dateTime;
    private LoginViewModel loginViewModel;
    private DeviceViewModel deviceViewModel;
    private NotificationViewModel notificationViewModel;
    private LiveViewModel liveViewModel;
    private FragmentBellAlertBinding binding;

    private Handler handler;
    private MjpegRunner rc;

    public static BellAlertFragment newInstance() {
        return new BellAlertFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        liveViewModel = new ViewModelProvider(this).get(LiveViewModel.class);

        Log.d(TAG, "onCreate");
        liveViewModel._url_updated.observe(this.getActivity(), new Observer<Boolean>() {
            public void onChanged(@Nullable Boolean url_updated) {
                String url = liveViewModel._url;
                Log.i(TAG, "Live URL = " + url);
                try {
                    rc = new MjpegRunner(handler, binding.cameraLive, new URL(url));
                    Thread t = new Thread(rc);
                    t.start();
                } catch (Exception e) {
                    if (rc != null) rc.stop();
                    Log.i(TAG, "Error in starting live = ", e);
                }
                binding.progressBar.setVisibility(View.GONE);

                Camera c = DeviceViewModel.getCamera(_cameraId);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(c._name + " @ " + _dateTime);
            }
        });
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        binding = FragmentBellAlertBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        handler = new Handler(getContext().getMainLooper());
        Timer t = new Timer();

        //LOGIN
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        File file = new File(getActivity().getApplicationContext().getFilesDir(), ".cred");
        Log.i(TAG, "Getting creds in " + file.getAbsoluteFile());

        // read cred from file
        try (BufferedReader fo = new BufferedReader(new FileReader(file))) {
            String cred = fo.readLine();
            if (cred != null && cred.contains("%%")) {
                String[] cv = cred.split("%%");
                binding.progressBar.setVisibility(View.VISIBLE);
                loginViewModel._username = cv[0];
                loginViewModel._pass = cv[1];
            }
            else {
                Log.i(TAG, "Failed reading cred file, no data ");
                Toast.makeText(getActivity().getApplicationContext(), "User not logged in, live boot failed", Toast.LENGTH_SHORT).show();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            Log.i(TAG, "Failed reading cred file " + e.getMessage());
        }
        notificationViewModel.getBellAlertDetails(getContext(), _cameraId, _dateTime);
        //GET DEVICE LIST
        liveViewModel.getLiveUrl(getContext(), _cameraId);

        View.OnClickListener navClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton tb = getActivity().findViewById(v.getId());
                Bitmap bitmap = ((BitmapDrawable)tb.getDrawable()).getBitmap();
                binding.historyImage.setImageBitmap(bitmap);
                t.cancel();
            }
        };
        ImageButton navButtons[] = { binding.histNav0, binding.histNav1, binding.histNav2, binding.histNav3, binding.histNav4, binding.histNav5,
                binding.histNav6, binding.histNav7, binding.histNav8, binding.histNav9};

        notificationViewModel._Ralert_details.observe(this.getActivity(), new Observer<Short>() {
            public void onChanged(@Nullable Short s) {
                if (s == 1) {
                    binding.progressBar.setVisibility(View.GONE);
                    AlertDetails ad = notificationViewModel._alert_details;
                    if (ad == null) return;

                    new ImageLoadTask(ad.getCurrentURL(), binding.historyImage).execute();

                    ArrayList<JSONObject> adlist = ad._alert_details;
                    for (int i = 0; i < adlist.size() && i < 10; i++) {
                        try {
                            new ImageLoadTask(adlist.get(i).getString("url"), navButtons[i]).execute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "Bell alert url loading " + ad.getCurrentURL());

                    TimerTask imgRefresh = new TimerTask() {
                        @Override
                        public void run() {
                            new ImageLoadTask(ad.getCurrentURL(), binding.historyImage).execute();
                        }
                    };
                    t.scheduleAtFixedRate(imgRefresh, 0, 1000);


                    for (int i = 0; i < 10; i++) {
                        navButtons[i].setOnClickListener(navClickListener);
                    }
                }
                else {
                    Toast toast = Toast.makeText(getContext(), "Alert Failed to load, retry !", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 20, 500);
                    toast.show();
                }
            }
        });

        Log.i(TAG, "on create view ");
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "on resume ");
        if (rc != null)rc.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "on pause ");
        if (rc != null)rc.pause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "on onDestroyView ");
        if (rc != null)rc.stop();
        super.onDestroyView();
    }


}