package com.ibeyonde.cam.ui.notifications;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.ibeyonde.cam.databinding.FragmentBellAlertBinding;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.ui.device.live.LiveViewModel;
import com.ibeyonde.cam.ui.device.live.MjpegRunner;
import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.History;
import com.ibeyonde.cam.utils.ImageLoadTask;

import org.json.JSONException;

import java.net.URL;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class BellAlertFragment extends Fragment {
    private static final String TAG= BellAlertFragment.class.getCanonicalName();

    public static String _cameraId;
    private DeviceViewModel deviceViewModel;
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
        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        liveViewModel = new ViewModelProvider(this).get(LiveViewModel.class);
        FragmentBellAlertBinding binding = FragmentBellAlertBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        deviceViewModel.getHistory(getContext(), _cameraId);

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

        deviceViewModel._update.observe(this.getActivity(), new Observer<Short>() {
            public void onChanged(@Nullable Short s) {
                Hashtable<String, Camera> ch = deviceViewModel._deviceList.getValue();
                if (ch != null) {
                    Camera c = ch.get(_cameraId);
                    History h = c._history;
                    for (int i=0;i< 10; i++){
                        try {
                            new ImageLoadTask(h._history.get(i).getString(0), navButtons[i]).execute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "Bell alert url loading " + h.getCurrentURL());

                    TimerTask imgRefresh = new TimerTask()
                    {
                        @Override
                        public void run() {
                            new ImageLoadTask(h.getCurrentURL(), binding.historyImage).execute();
                        }
                    };
                    Timer t = new Timer();
                    t.scheduleAtFixedRate(imgRefresh, 0, 2000);

                    for (int i=0;i< 10; i++){
                        navButtons[i].setOnClickListener(navClickListener);
                    }

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

        return root;
    }

}