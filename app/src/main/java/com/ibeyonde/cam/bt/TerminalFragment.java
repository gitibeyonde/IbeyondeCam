package com.ibeyonde.cam.bt;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ibeyonde.cam.R;
import com.ibeyonde.cam.databinding.FragmentBtTerminalBinding;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.Utils;

public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {
    private static final String TAG= TerminalFragment.class.getCanonicalName();


    private enum Connected { False, Pending, True }

    private enum BTState { Init, Scanning, SelectWiFi, WiFiPassword, WiFiConnected, UserInited }

    private String deviceAddress;
    private SerialService service;

    private TextView receiveText;
    private TextView sendText;
    private TextUtil.HexWatcher hexWatcher;

    private Connected connected = Connected.False;
    private BTState bt_state = BTState.Init;
    private Integer networks=-1;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;
    Handler handler;

    private FragmentBtTerminalBinding binding;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        deviceAddress = getArguments().getString("device");
        Log.i(TAG, "Terminal frag created " + deviceAddress);
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach create intent");
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "onAttach done");
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView start");
        binding = FragmentBtTerminalBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        receiveText = binding.receiveText;
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        sendText = binding.sendText;
        hexWatcher = new TextUtil.HexWatcher(sendText);
        hexWatcher.enable(hexEnabled);
        sendText.addTextChangedListener(hexWatcher);
        sendText.setHint(hexEnabled ? "HEX mode" : "");

        ImageButton sendBtn = binding.sendBtn;
        sendBtn.setOnClickListener(v -> send(sendText.getText().toString()));

        handler = new Handler(getContext().getMainLooper());
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
        menu.findItem(R.id.hex).setChecked(hexEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.i(TAG, "onOptionsItemSelected");
        if (id == R.id.clear) {
            receiveText.setText("");
            return true;
        } else if (id == R.id.newline) {
            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
            int pos = java.util.Arrays.asList(newlineValues).indexOf(newline);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Newline");
            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
                newline = newlineValues[item1];
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else if (id == R.id.hex) {
            hexEnabled = !hexEnabled;
            sendText.setText("");
            hexWatcher.enable(hexEnabled);
            sendText.setHint(hexEnabled ? "HEX mode" : "");
            item.setChecked(hexEnabled);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, bt_state.name() + " Send text " + str);
        if (bt_state == BTState.SelectWiFi) {
            if (!Utils.isInteger(str, 10)) {
                Log.i(TAG, "Not an integer");
                Toast.makeText(getContext(), "Type an integer between 1 to " + networks, Toast.LENGTH_SHORT).show();
                sendText.setText("");
                return;
            } else if (Integer.parseInt(str) > networks || Integer.parseInt(str) < 1) {
                Log.i(TAG, "Not in range");
                Toast.makeText(getContext(), "Type an integer between 1 to " + networks, Toast.LENGTH_SHORT).show();
                sendText.setText("");
                return;
            }
        }
        else if (bt_state == BTState.WiFiPassword) {
            if (str.length() < 8) {
                Log.i(TAG, "Password length small");
                Toast.makeText(getContext(), "Password size should be at least 8 characters", Toast.LENGTH_SHORT).show();
                sendText.setText("");
                return;
            }
        }
        try {
            String msg;
            byte[] data;
            if(hexEnabled) {
                StringBuilder sb = new StringBuilder();
                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                TextUtil.toHexString(sb, newline.getBytes());
                msg = sb.toString();
                data = TextUtil.fromHexString(msg);
            } else {
                msg = str;
                data = (str + newline).getBytes();
            }
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
        if(hexEnabled) {
            Log.i(TAG, "receive hex " + TextUtil.toHexString(data) );
            receiveText.append(TextUtil.toHexString(data) + '\n');
        } else {
            String msg = new String(data);
            if(newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                // special handling if CR and LF come in separate fragments
                if (pendingNewline && msg.charAt(0) == '\n') {
                    Editable edt = receiveText.getEditableText();
                    if (edt != null && edt.length() > 1)
                        edt.replace(edt.length() - 2, edt.length(), "");
                }
                pendingNewline = msg.charAt(msg.length() - 1) == '\r';

                Log.i(TAG, "Receive >" + msg);

                if (msg.contains("Initializing device...")){
                    send(String.format("%s%%%s", LoginViewModel._username,LoginViewModel._pass));
                }
                else if (msg.contains("Networks found")){
                    String nc = msg.substring(0, msg.indexOf("Networks found")).trim();
                    networks = Integer.parseInt(nc);
                }
                else if (msg.contains("Scanning Wi-Fi")){
                    bt_state = BTState.Scanning;
                }
                else if (msg.contains("enter the Number for")){
                    bt_state = BTState.SelectWiFi;
                }
                else if (msg.contains("enter your Wi-Fi password")){
                    bt_state = BTState.WiFiPassword;
                }
                else if (msg.contains("-Connected-")){
                    bt_state = BTState.WiFiConnected;
                }
                else if (msg.contains("Bluetooth disconnecting")){
                    bt_state = BTState.UserInited;
                }
                Log.i(TAG, bt_state.name());
            }
            sendText.setText("");
            receiveText.append(TextUtil.toCaretString(msg, newline.length() != 0));
        }
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
        String msg = receiveText.getText().toString();
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "Device disconnected successfully.", Toast.LENGTH_SHORT).show();
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Bluetooth Main");
                try {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager();

                    BluetoothFragment bluetoothFragment = new BluetoothFragment();
                    fragmentManager.beginTransaction()
                            .replace(getActivity().getSupportFragmentManager().getPrimaryNavigationFragment().getId(), bluetoothFragment, "bluetooth")
                            .setReorderingAllowed(true)
                            .addToBackStack("home")
                            .commit();
                    //getSupportActionBar().setTitle(settingFragment._cameraId  + " Setting ");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 2000);
    }

}
