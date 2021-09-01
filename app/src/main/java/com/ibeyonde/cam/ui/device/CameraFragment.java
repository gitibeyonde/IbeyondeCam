package com.ibeyonde.cam.ui.device;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.niqdev.mjpeg.DisplayMode;
import com.github.niqdev.mjpeg.Mjpeg;
import com.ibeyonde.cam.databinding.FragmentCameraBinding;

public class CameraFragment extends Fragment {
    private static final String TAG= CameraFragment.class.getCanonicalName();

    public static String _cameraId;

    private CameraViewModel cameraViewModel;
    private FragmentCameraBinding binding;

    int TIMEOUT = 5; //seconds

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        cameraViewModel =
                new ViewModelProvider(this).get(CameraViewModel.class);
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.cameraLabel.setText(_cameraId);
        cameraViewModel.getLiveUrl(getContext(), _cameraId);


        cameraViewModel._url.observe(this.getActivity(), new Observer<String>() {
            public void onChanged(@Nullable String url) {
                Log.d(TAG, "Live URL = " + url);
                Mjpeg.newInstance()
                        .open(url, TIMEOUT)
                        .subscribe(inputStream -> {
                            binding.cameraLive.setSource(inputStream);
                            binding.cameraLive.setDisplayMode(DisplayMode.BEST_FIT);
                            //mjpegView.showFps(true);
                        });
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}