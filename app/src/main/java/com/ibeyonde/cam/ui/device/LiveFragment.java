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
import com.ibeyonde.cam.databinding.FragmentLiveBinding;

public class LiveFragment extends Fragment {
    private static final String TAG= LiveFragment.class.getCanonicalName();

    public static String _cameraId;

    private LiveViewModel liveViewModel;
    private FragmentLiveBinding binding;
    Mjpeg _mjpeg;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        liveViewModel =
                new ViewModelProvider(this).get(LiveViewModel.class);
        binding = FragmentLiveBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.cameraLabel.setText(_cameraId);
        liveViewModel.getLiveUrl(getContext(), _cameraId);

        _mjpeg = Mjpeg.newInstance();
        liveViewModel._url.observe(this.getActivity(), new Observer<String>() {
            public void onChanged(@Nullable String url) {
                Log.d(TAG, "Live URL = " + url);
                try {
                    _mjpeg
                            .open(url)
                            .subscribe(inputStream -> {
                                binding.cameraLive.setSource(inputStream);
                                binding.cameraLive.setDisplayMode(DisplayMode.BEST_FIT);
                                //mjpegView.showFps(true);
                            });
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _mjpeg.sendConnectionCloseHeader();
    }

}