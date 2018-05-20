/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bluetoothlesmartcharger.ramk.com.blesmartcharger1.UI;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.BleAdvertisedData;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.BleUtil;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.Const;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.R;

import static bluetoothlesmartcharger.ramk.com.blesmartcharger1.Background.SampleGattAttributes.HM_10_CONF;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class DeviceListActivity extends Activity {
    // Debugging
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_DEVICE_NAME = "device_address";
    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private List<BluetoothDevice> pairedDevices;
    private List<BluetoothDevice> newDevices;


    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private ImageView scanButton;
    private ImageView oops;
    private ListView pairedListView,newDevicesListView ;
    private TextView titlePairedDevices,titleNewDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        scanButton = (ImageView) findViewById(R.id.refresh);
        oops = (ImageView) findViewById(R.id.oops);
        titlePairedDevices = (TextView) findViewById(R.id.title_paired_devices);
        titleNewDevices = (TextView) findViewById(R.id.title_new_devices);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                oops.setVisibility(View.GONE);
                titleNewDevices.setVisibility(View.INVISIBLE);
                titlePairedDevices.setVisibility(View.INVISIBLE);
                pairedListView.setVisibility(View.INVISIBLE);
                newDevicesListView.setVisibility(View.INVISIBLE);
                Toast.makeText(DeviceListActivity.this, "Searching", Toast.LENGTH_SHORT).show();
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        pairedDevices = new ArrayList<>();
        newDevices = new ArrayList<>();

        // Find and set up the ListView for paired devices
        pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mPairedDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mNewDeviceClickListener);

        // Register for broadcasts when a device is discovered
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        //filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = bluetoothManager.getAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices1 = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices1.size() > 0) {
            titlePairedDevices.setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices1) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                pairedDevices.add(device);
            }
            oops.setVisibility(View.INVISIBLE);
            titleNewDevices.setVisibility(View.VISIBLE);
            titlePairedDevices.setVisibility(View.VISIBLE);
            pairedListView.setVisibility(View.VISIBLE);
            newDevicesListView.setVisibility(View.VISIBLE);
        } else {
            String noDevices = "No Devices";
            mPairedDevicesArrayAdapter.add(noDevices);
        }
        scanButton.performClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        //this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");
        if (mBtAdapter == null || !mBtAdapter.isEnabled()){
            Log.d(TAG, "isEnabled()");
            Toast.makeText(DeviceListActivity.this,"Please turn on Bluetooth",Toast.LENGTH_SHORT).show();
            return;
        }
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle("Scanning...");

        // Turn on sub-title for new devices
        //titleNewDevices.setVisibility(View.VISIBLE);


        // If we're already discovering, stop it

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        mNewDevicesArrayAdapter.clear();
        // Request discover from BluetoothAdapter
        //mBtAdapter.startDiscovery();
        if (Build.VERSION.SDK_INT >= 23) {

            final ScanCallback mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.e("callbackType", String.valueOf(callbackType));
                    Log.e("result", result.toString());


                    BluetoothDevice btDevice = result.getDevice();
                    List<ParcelUuid> serviceUuids = result.getScanRecord().getServiceUuids();
                    boolean isHaveUUid = isHaveUUID(serviceUuids);
                    if (!newDevices.contains(btDevice) && isHaveUUid) {
                        mNewDevicesArrayAdapter.add(btDevice.getName() + "\n" + btDevice.getAddress());
                        mNewDevicesArrayAdapter.notifyDataSetChanged();
                        oops.setVisibility(View.INVISIBLE);
                        titleNewDevices.setVisibility(View.VISIBLE);
                        titlePairedDevices.setVisibility(View.VISIBLE);
                        pairedListView.setVisibility(View.VISIBLE);
                        newDevicesListView.setVisibility(View.VISIBLE);
                        newDevices.add(btDevice);
                    }
                    //connectToDevice(btDevice);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult sr : results) {
                        Log.i("ScanResult - Results", sr.toString());
                    }
                }
            };

            mLEScanner = mBtAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .build();
            filters = new ArrayList<ScanFilter>();

            mLEScanner.startScan(filters, settings, mScanCallback);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLEScanner.stopScan(mScanCallback);
                    invalidateOptionsMenu();
                    setProgressBarIndeterminateVisibility(false);
                    setTitle("Select a Device");
                    if (mNewDevicesArrayAdapter.getCount() == 0) {
                        String noDevices = "No Devices Found";
                        mNewDevicesArrayAdapter.add(noDevices);
                        oops.setVisibility(View.VISIBLE);
                        titleNewDevices.setVisibility(View.INVISIBLE);
                        titlePairedDevices.setVisibility(View.INVISIBLE);
                        pairedListView.setVisibility(View.INVISIBLE);
                        newDevicesListView.setVisibility(View.INVISIBLE);
                    }
                    scanButton.setVisibility(View.VISIBLE);
                }
            }, 10000);

        } else {
            mBtAdapter.startLeScan(mLeScanCallback);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBtAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                    setProgressBarIndeterminateVisibility(false);
                    setTitle("Select a Device");
                    if (mNewDevicesArrayAdapter.getCount() == 0) {
                        String noDevices = "No Devices Found";
                        mNewDevicesArrayAdapter.add(noDevices);
                        oops.setVisibility(View.VISIBLE);
                        titleNewDevices.setVisibility(View.INVISIBLE);
                        titlePairedDevices.setVisibility(View.INVISIBLE);
                        pairedListView.setVisibility(View.INVISIBLE);
                        newDevicesListView.setVisibility(View.INVISIBLE);
                    }
                    scanButton.setVisibility(View.VISIBLE);
                }
            }, 10000);
        }
    }

/*    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            if(!newDevices.contains(btDevice)) {
                mNewDevicesArrayAdapter.add(btDevice.getName() + "\n" + btDevice.getAddress());
                mNewDevicesArrayAdapter.notifyDataSetChanged();
                newDevices.add(btDevice);
            }
            //connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };*/

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String nm = device.getName();
                            if (TextUtils.isEmpty(nm)) {
                                final BleAdvertisedData badata = BleUtil.parseAdertisedData(scanRecord);
                                nm = badata.getName();
                            }
                            Log.d(TAG, "bluetooth name=" + nm);

                            boolean isHaveUUID = isHaveUUID(scanRecord);
                            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                                if (!newDevices.contains(device) && isHaveUUID) {
                                    Log.d(TAG, "bluetooth ADD");
                                    mNewDevicesArrayAdapter.add(nm + "\n" + device.getAddress());
                                    mNewDevicesArrayAdapter.notifyDataSetChanged();
                                    newDevices.add(device);
                                }
                            }
                        }
                    });
                }
            };

    //SDK<23 UUID 过滤
    private boolean isHaveUUID(byte[] scanRecord) {

        final BleAdvertisedData badata = BleUtil.parseAdertisedData(scanRecord);
        boolean isHaveUUID = false;
        List<UUID> uuids = badata.getUuids();
        for (UUID uuid : uuids) {
            if (uuid.toString().equals(HM_10_CONF)) {
//            if (uuid.toString().equals(V06)) {
                isHaveUUID = true;
                break;
            }
            Log.d(TAG, "<23 uuid=" + uuid.toString());
        }
        return isHaveUUID;
    }

    //SDK>23 UUID 过滤
    private boolean isHaveUUID(List<ParcelUuid> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return false;
        }
        boolean isHaveUUID = false;
        for (ParcelUuid uuid : uuids) {
            if (uuid.getUuid().toString().equals(HM_10_CONF)) {
//            if (uuid.getUuid().toString().equals(V06)) {
                isHaveUUID = true;
                break;
            }
            Log.d(TAG, ">23 uuid=" + uuid.toString());
        }
        return isHaveUUID;
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mNewDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            //String info = ((TextView) v).getText().toString();
            //String address = info.substring(info.length() - 17);
            if (newDevices.size()<=0){
                return;
            }
            BluetoothDevice selectedDevice = newDevices.get(arg2);
            // Create the result Intent and include the MAC address

            String readAddress =
                    Const.HOST_DEVICE_ADDRESS = selectedDevice.getAddress();
            Const.DEVICE_NAME = selectedDevice.getName();
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, readAddress);
            intent.putExtra(EXTRA_DEVICE_NAME, selectedDevice.getName());
            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
    private OnItemClickListener mPairedDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            //String info = ((TextView) v).getText().toString();
            //String address = info.substring(info.length() - 17);
            if (pairedDevices.size()<=0){
                return;
            }
            BluetoothDevice selectedDevice = pairedDevices.get(arg2);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, selectedDevice.getAddress());
            intent.putExtra(EXTRA_DEVICE_NAME, selectedDevice.getName());

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle("Select a Device");
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = "No Devices Found";
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };


}
