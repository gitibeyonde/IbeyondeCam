package com.ibeyonde.cam.ui.device.history;

import android.content.Context;
import android.os.Bundle;

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
import com.ibeyonde.cam.databinding.FragmentHistoryItemListBinding;
import com.ibeyonde.cam.utils.History;

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
                HistoryMotionContent.initialize(h.getHistory());
                // Set the adapter
                if (view instanceof RecyclerView) {
                    Context context = view.getContext();
                    RecyclerView recyclerView = (RecyclerView) view;
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    recyclerView.setAdapter(new HistoryRecyclerViewAdapter(HistoryMotionContent._item_list));
                }
                //binding.progressBarHistory.setVisibility(View.GONE);
            }
        });

        getActivity().setTitle( _cameraId + " History ");
        return view;
    }
}