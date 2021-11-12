package com.ibeyonde.cam.ui.device.history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ibeyonde.cam.MainActivity;
import com.ibeyonde.cam.R;
import com.ibeyonde.cam.databinding.FragmentHistoryListBinding;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.History;

/**
 * A fragment representing a list of Items.
 */
public class HistoryFragment extends Fragment {
    private static final String TAG= HistoryFragment.class.getCanonicalName();

    public static volatile String _cameraId;
    public static volatile int _list_size=20;
    private HistoryViewModel historyViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_list, container, false);

        historyViewModel =
                new ViewModelProvider(this).get(HistoryViewModel.class);
        FragmentHistoryListBinding binding = FragmentHistoryListBinding.inflate(inflater, container, false);

        historyViewModel.getHistoryOn(getActivity().getApplicationContext(), _cameraId, "", 0, _list_size);

        historyViewModel._history.observe(this.getActivity(), new Observer<Short>() {
            @Override
            public void onChanged(Short s) {
                if (s == 1) {
                    Log.i(TAG, "Device List = " + historyViewModel._history_list._total);
                    HistoryMotionContent.initialize(historyViewModel._history_list.getHistory());
                    // Set the adapter
                    if (view instanceof RecyclerView) {
                        Context context = view.getContext();
                        RecyclerView recyclerView = (RecyclerView) view;
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        recyclerView.setAdapter(new HistoryRecyclerViewAdapter(HistoryMotionContent._item_list));
                    }
                    Camera c = DeviceViewModel.getCamera(_cameraId);
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(c._name + " History ");
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


        /**OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "Back button pressed");
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);**/


        return view;
    }
}