package com.ibeyonde.cam.ui.device.history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ibeyonde.cam.MainActivity;
import com.ibeyonde.cam.R;
import com.ibeyonde.cam.databinding.FragmentHistoryListBinding;
import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.utils.Camera;

/**
 * A fragment representing a list of Items.
 */
public class HistoryFragment extends Fragment {
    private static final String TAG= HistoryFragment.class.getCanonicalName();

    private HistoryViewModel historyViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyViewModel =  new ViewModelProvider(this).get(HistoryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_list, container, false);
        FragmentHistoryListBinding binding = FragmentHistoryListBinding.inflate(inflater, container, false);


        HistoryMotionContent.initialize(historyViewModel._history_list.getHistory());
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new HistoryRecyclerViewAdapter(HistoryMotionContent._item_list));
        }

        Camera c = DeviceViewModel.getCamera(HistoryProgressFragment._cameraId);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(c._name + " History ");

        return view;
    }
}