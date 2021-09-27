package com.ibeyonde.cam.ui.notifications;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.ibeyonde.cam.databinding.FragmentBellAlertBinding;
import com.ibeyonde.cam.ui.device.live.LiveViewModel;
import com.ibeyonde.cam.ui.device.live.MjpegRunner;
import com.ibeyonde.cam.utils.AlertDetails;
import com.ibeyonde.cam.utils.ImageLoadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BellAlertFragment extends Fragment {
    private static final String TAG= BellAlertFragment.class.getCanonicalName();

    public static String _cameraId;
    public static String _dateTime;
    private NotificationViewModel notificationViewModel;
    private LiveViewModel liveViewModel;
    FragmentBellAlertBinding binding;

    Handler handler;
    static MjpegRunner rc;

    public static BellAlertFragment newInstance() {
        return new BellAlertFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        liveViewModel = new ViewModelProvider(this).get(LiveViewModel.class);
        FragmentBellAlertBinding binding = FragmentBellAlertBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        notificationViewModel.getBellAlertDetails(getContext(), _cameraId, _dateTime);

        View.OnClickListener navClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton tb = getActivity().findViewById(v.getId());
                Bitmap bitmap = ((BitmapDrawable)tb.getDrawable()).getBitmap();
                binding.historyImage.setImageBitmap(bitmap);
            }
        };
        ImageButton navButtons[] = { binding.histNav0, binding.histNav1, binding.histNav2, binding.histNav3, binding.histNav4, binding.histNav5,
                binding.histNav6, binding.histNav7, binding.histNav8, binding.histNav9};

        notificationViewModel._alert_details.observe(this.getActivity(), new Observer<AlertDetails>() {
            public void onChanged(@Nullable AlertDetails ad) {
                if (ad == null) return;
                ArrayList<JSONObject> adlist = ad._alert_details;
                for (int i=0;i< adlist.size() && i < 10; i++){
                    try {
                        Log.d(TAG, adlist.get(i).getString("url"));
                        new ImageLoadTask(adlist.get(i).getString("url"), navButtons[i]).execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "Bell alert url loading " + ad.getCurrentURL());

                TimerTask imgRefresh = new TimerTask()
                {
                    @Override
                    public void run() {
                        new ImageLoadTask(ad.getCurrentURL(), binding.historyImage).execute();
                    }
                };
                Timer t = new Timer();
                t.scheduleAtFixedRate(imgRefresh, 0, 2000);

                for (int i=0;i< 10; i++){
                    navButtons[i].setOnClickListener(navClickListener);
                }
            }
        });


        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        liveViewModel.getLocalLiveUrl(getContext(), _cameraId);


        liveViewModel._url.observe(this.getActivity(), new Observer<String>() {
            public void onChanged(@Nullable String url) {
                Log.i(TAG, "Live URL = " + url);
                if (url.toString().length() > 10) {
                    try {
                        handler = new Handler(getContext().getMainLooper());
                        rc = new MjpegRunner(new URL(url), handler, binding.cameraLive);
                        Thread t = new Thread(rc);
                        t.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (rc != null)rc.stop();
                    }
                }
                else {
                    if (rc != null)rc.stop();
                }
            }
        });

        getActivity().getActionBar().setTitle(_cameraId  + " Bell Alert ");
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        notificationViewModel._alert_details.postValue(null);
        if (rc != null)rc.stop();
    }

}