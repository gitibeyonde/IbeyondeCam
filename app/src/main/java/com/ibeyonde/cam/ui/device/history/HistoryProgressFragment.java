package com.ibeyonde.cam.ui.device.history;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ibeyonde.cam.MainActivity;
import com.ibeyonde.cam.R;

public class HistoryProgressFragment extends Fragment {
    private static final String TAG= HistoryProgressFragment.class.getCanonicalName();

    public static volatile String _cameraId;
    public static volatile int _list_size=20;

    private HistoryViewModel historyViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        historyViewModel =  new ViewModelProvider(this).get(HistoryViewModel.class);

        historyViewModel._history.observe(this.getActivity(), new Observer<Short>() {
            @Override
            public void onChanged(Short s) {
                if (s == 1) {
                    Log.i(TAG, "Device List = " + historyViewModel._history_list._total);
                    HistoryMotionContent.initialize(historyViewModel._history_list.getHistory());
                    getActivity().getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager()
                            .beginTransaction().replace(getActivity().getSupportFragmentManager().getPrimaryNavigationFragment().getId(), MainActivity._history, "history")
                            .setReorderingAllowed(true)
                            .addToBackStack("home").commit();
                }
                else {
                    Intent i = new Intent(getContext(), MainActivity.class);
                    startActivity(i);
                    Toast toast = Toast.makeText(getContext(), "Failed to get history, retry !", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 20, 500);
                    toast.show();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history_progress, container, false);

        Log.i(TAG, "Get History for = " + _cameraId);
        historyViewModel.getHistoryOn(getActivity().getApplicationContext(), _cameraId, "", 0, _list_size);

        return view;
    }
}