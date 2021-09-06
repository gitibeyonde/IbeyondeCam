package com.ibeyonde.cam.ui.device;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ibeyonde.cam.R;
import com.ibeyonde.cam.databinding.FragmentDeviceBinding;
import com.ibeyonde.cam.databinding.FragmentHistoryItemBinding;
import com.ibeyonde.cam.databinding.FragmentHistoryItemListBinding;
import com.ibeyonde.cam.ui.device.placeholder.PlaceholderContent;
import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.History;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class HistoryFragment extends Fragment {
    private static final String TAG= HistoryFragment.class.getCanonicalName();

    public static String _cameraId;
    private HistoryViewModel historyViewModel;

    public HistoryFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_item_list, container, false);

        historyViewModel =
                new ViewModelProvider(this).get(HistoryViewModel.class);
        com.ibeyonde.cam.databinding.FragmentHistoryItemListBinding binding = FragmentHistoryItemListBinding.inflate(inflater, container, false);

        historyViewModel.getHistoryOn(getActivity().getApplicationContext(), _cameraId, "", 0);

        Log.d(TAG, "onCreateView");

        historyViewModel._historyList.observe(this.getActivity(), new Observer<History>() {
            @Override
            public void onChanged(History h) {
                Log.i(TAG, "Device List = " + h._total);
                // Set the adapter
                if (view instanceof RecyclerView) {
                    Context context = view.getContext();
                    RecyclerView recyclerView = (RecyclerView) view;
                    if (  h._total <= 1) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    } else {
                        recyclerView.setLayoutManager(new GridLayoutManager(context,   h._total));
                    }
                    recyclerView.setAdapter(new HistoryRecyclerViewAdapter(h.getHistory()));
                }
                binding.progressBarHistory.setVisibility(View.GONE);
            }
        });

        getActivity().setTitle( _cameraId + " History ");
        return view;
    }
}