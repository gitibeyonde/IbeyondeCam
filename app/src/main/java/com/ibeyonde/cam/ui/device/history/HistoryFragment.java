package com.ibeyonde.cam.ui.device.history;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ibeyonde.cam.R;
import com.ibeyonde.cam.databinding.FragmentHistoryListBinding;
import com.ibeyonde.cam.utils.History;

/**
 * A fragment representing a list of Items.
 */
public class HistoryFragment extends Fragment {
    private static final String TAG= HistoryFragment.class.getCanonicalName();

    public static String _cameraId;
    public static int _list_size=20;
    private HistoryViewModel historyViewModel;

    public HistoryFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_list, container, false);

        historyViewModel =
                new ViewModelProvider(this).get(HistoryViewModel.class);
        FragmentHistoryListBinding binding = FragmentHistoryListBinding.inflate(inflater, container, false);

        historyViewModel.getHistoryOn(getActivity().getApplicationContext(), _cameraId, "", 0, _list_size);

        historyViewModel._history.observe(this.getActivity(), new Observer<History>() {
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
            }
        });

        getActivity().setTitle( _cameraId + " History ");

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