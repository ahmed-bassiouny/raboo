package bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Background.SampleGattAttributes;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.BleComm;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.BluetoothService.BluetoothLeService;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.R;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.SendCommand;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.UI.MainActivity;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon.Service1;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.L;

/**
 * Created by Rana Abdul Manan on 11/11/2015.
 */
public class BluetoothConnection {
    private final String TAG = "BluetoothConnection";
    private BluetoothGattCharacteristic characteristicTXBattery;
    private BluetoothGattCharacteristic characteristicRXBattery;
    private BluetoothGattCharacteristic characteristicTxName;
    private BluetoothGattCharacteristic characteristicRxName;
    //    private BluetoothLeService mBluetoothLeService;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private boolean mConnected = false;
    private String deviceAddress = "null";
    private static BluetoothConnection _instance = null;
    private boolean isSmartCharger = false;
    private Context _context = null;
    private int state = 0;
    private String lastMsg = "l";
    public static boolean runInBackground = false;
    //
    public static boolean isFirstGet61Battery = true;
    private Intent gattServiceIntent;


    private BluetoothConnection() {
        _instance = this;
    }

    public static BluetoothConnection get_instance() {
        if (_instance == null) {
            _instance = new BluetoothConnection();
        }
        return _instance;
    }

    private SendCommand sendCommand;
    private final ServiceConnection aidlConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            log("aidl onServiceConnected... ");
            //客户端获取代理对象
            sendCommand = SendCommand.Stub.asInterface(service);
            try {
                sendCommand.connect(Const.HOST_DEVICE_ADDRESS);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
//            createNotification();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            log("aidl onServiceDisconnected... ");
            sendCommand = null;
            gattServiceIntent = new Intent(_context, Service1.class);
            _context.bindService(gattServiceIntent, aidlConnection, Context.BIND_AUTO_CREATE);
        }
    };


//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//            if (!mBluetoothLeService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
//
//            }
//            // Automatically connects to the device upon successful start-up initialization.
//            if (!Const.HOST_DEVICE_ADDRESS.equalsIgnoreCase("NULL")) {
//                Logger.t(TAG).i("蓝牙服务开启,先不开始连接");
//                mBluetoothLeService.connect(Const.HOST_DEVICE_ADDRESS);
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mBluetoothLeService = null;
//        }
//    };

    public void setContext(Context cont) {
//        if (_context != null) {
//            _context.unbindService(mServiceConnection);
//            _context.unregisterReceiver(mGattUpdateReceiver);
//        }
        if (cont != null) {
            this._context = cont;
            gattServiceIntent = new Intent(_context, Service1.class);
            _context.startService(gattServiceIntent);
            _context.bindService(gattServiceIntent, aidlConnection, Context.BIND_AUTO_CREATE);
//            Intent gattServiceIntent = new Intent(_context, BluetoothLeService.class);
//            _context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            _context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        }


    }

    public Context getContext() {
        return this._context;
    }

    public boolean isSmartCharger() {
        return isSmartCharger;
    }

    //有关蓝牙的连接，发现服务，数据返回的监听都在这里
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Service1.ACTION_GATT_CONNECTED.equals(action)) {
                Log.w("Connection", "Bluetooth CONNECTED");

            } else if (Service1.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.e("Connection", "Bluetooth disconnected");
                mConnected = false;
                state = 0;
                deviceAddress = "null";
                isSmartCharger = false;
                // Update UI after losing connection


                if (MainActivity.get_instance() != null) {
                    MainActivity.get_instance().displayConnection(false);
                }
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {

                }
                boolean val = runInBackground;
                Log.e("Run in background", "Run in background " + runInBackground);

//                if (val) {
//                    Logger.t(TAG).i("后台运行，断开连接，暂不重连");
//                    createConnection();
//                }

            } else if (Service1.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                if (mConnected)
                    return;
                Log.e("Connection", "Bluetooth Connected");

                mConnected = true;

//                sendData("X\r");
//                sendData(new byte[]{(byte) 0x58});
                // Update UI after connection making
            } else if (Service1.ACTION_DATA_AVAILABLE.equals(action)) {
                // Get receive data from here
                String data = intent.getStringExtra(Service1.EXTRA_DATA);
                byte[] dataByte = intent.getByteArrayExtra(Service1.EXTRA_DATA_BYTE);
                Log.e("Data received", data.charAt(0) + " received");
                L.i(TAG, "接收数据=" + Arrays.toString(ConvertHelper.byte2HexToStrArr(dataByte)));
//                Toast.makeText(context,"接收数据=" + Arrays.toString(ConvertHelper.byte2HexToStrArr(dataByte)),600).show();
                if (dataByte[0] == (byte) 0x58) {
                    Log.e("Data received", data.charAt(0) + " generating notifications");
//                    createNotification();

                    if (MainActivity.get_instance() != null) {
                        MainActivity.get_instance().enableDisable(true);
                        if (MainActivity.get_instance() != null) {
                            MainActivity.get_instance().displayConnection(true);
                        }
//                        MainActivity.get_instance().startBackgroundService();
                    }
                    isSmartCharger = true;
//                    Toast.makeText(context, "对接上好智能充电设备", Toast.LENGTH_SHORT).show();
                } else if (dataByte[0] == (byte) 0x61) {
                    if (isFirstGet61Battery) {
//                        SharedPreferencesUtil.saveInt(getContext(), Const.START_CHARGE_VALUE, Const.current_charging);
                        isFirstGet61Battery = false;
                    }
//                    Toast.makeText(context, "开始充电", Toast.LENGTH_SHORT).show();
                } else if (dataByte[0] == (byte) 0x62) {
                    BluetoothConnection.isFirstGet61Battery = true;
//                    SharedPreferencesUtil.saveInt(getContext(), Const.START_CHARGE_VALUE, 100);
//                    Toast.makeText(context, "结束充电", Toast.LENGTH_SHORT).show();

                }
                if (data.charAt(0) != 'X') {
                    lastMsg = data.charAt(0) + "";
                }
                //displayData(intent.getStringExtra(mBluetoothLeService.EXTRA_DATA));
            } else if (action.equals(Service1.ACTION_BATTERY_STATUS)) {

                boolean isCharging = intent.getBooleanExtra("isCharging", false);
                int batteryPct = intent.getIntExtra("batteryPct", 0);
                boolean isConnected = intent.getBooleanExtra("isConnected", false);
//                Toast.makeText(context,"isCharging=" + isCharging, 1000).show();
                if (MainActivity.get_instance() != null && MainActivity.get_instance().getIsStart()) {
                    MainActivity.get_instance().setBatteryInfo(isCharging, batteryPct);
                    log("isCharging = " + isCharging + " , batteryPct =" + batteryPct + " , isConnected=" + isConnected);
                }


            }
        }
    };

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String msg) {
        lastMsg = msg;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "Unknown service";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));

            // If the service exists for HM 10 Serial, say so.
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            // get characteristic when UUID matches RX/TX UUID 电量
            characteristicTXBattery = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);//ffel
            characteristicRXBattery = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);//ffel 读写用的都是一个uuid

            // get characteristic when UUID matches RN/TX UUID 修改名字
            characteristicTxName = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RN_TX);//ffe2
            characteristicRxName = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RN_TX);//ffe2

        }

    }

    public boolean isBluetoothConnected() {
        return mConnected;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Service1.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Service1.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Service1.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Service1.ACTION_DATA_AVAILABLE);

        intentFilter.addAction(Service1.ACTION_BATTERY_STATUS);

        return intentFilter;
    }

    public void createConnection() {

        if (state == 1 || isSmartCharger)
            return;

        if (mConnected)
            stopConnection();

//        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
//            BluetoothAdapter.getDefaultAdapter().enable();
//            try {
//                Thread.sleep(500);
//            } catch (Exception ex) {
//            }
//        }
        if (_context != null) {

            state = 1;

            deviceAddress = Const.HOST_DEVICE_ADDRESS;
            L.i(TAG, "调用连接方法");
//            final boolean result = mBluetoothLeService.connect(Const.HOST_DEVICE_ADDRESS);
            if (null != sendCommand)
                try {
                    sendCommand.connect(Const.HOST_DEVICE_ADDRESS);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                state = 0;
            }
        }, 2000);

    }

    private void createNotification() {

        //if(_context!=null) {

        Intent notificationIntent = new Intent(_context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(_context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context);
        Notification notification = builder.setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setTicker(_context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.notifi_icon)
                .setContentTitle(_context.getString(R.string.app_name))
                .setContentText(_context.getString(R.string.app_name) + " connected").build();
        // notification.flags |= Notification.FLAG_AUTO_CANCEL;
        ((NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notification);
        //}
    }

    public void removeNotification() {
//        ((NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
    }

    public int getState() {
        return state;
    }

    public void setIsStart(boolean b) {
        if (sendCommand != null) {
            try {
                sendCommand.isStart(b);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void bleServiceStop() {
        if (sendCommand != null)
            try {
                sendCommand.kill();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        if (aidlConnection != null)
            _context.unbindService(aidlConnection);
        if (gattServiceIntent!=null)
        _context.stopService(gattServiceIntent);
    }

    public void stopConnection() {
//        if (mConnected)
//            mBluetoothLeService.disconnect();
        if (mConnected) {
            try {
                sendCommand.close();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    public void sendData(final String data) {
        Log.e("Data Sent", data + " is sending connection : " + mConnected);
        final byte[] tx = data.getBytes();
        try {
            Thread.sleep(200);
        } catch (Exception ex) {
            Log.e("Error in Thread sleep", ex.getMessage());
        }
        if (mConnected) {

//            if (characteristicTXBattery == null || mBluetoothLeService == null) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                            sendData(data);
//
//                    }
//                }, 2000);
//            } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    L.i(TAG, "发送数据=" + data);
                    BleComm bleComm = new BleComm("", tx, 0);
                    try {
                        sendCommand.write(bleComm);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
//                        Logger.t(TAG).i("发送数据=" + data);
//                        characteristicTXBattery.setValue(tx);
//                        mBluetoothLeService.writeCharacteristic(characteristicTXBattery);
                }
            }, 1000);

//            }
        }
    }

    public void sendData(final byte[] tx) {
        L.i(TAG, "sendData:" + Arrays.toString(ConvertHelper.byte2HexToStrArr(tx)));
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
            Log.e("Error in Thread sleep", ex.getMessage());
        }
        if (mConnected) {

            BleComm bleComm = new BleComm("", tx, 0);
            try {
                sendCommand.write(bleComm);
            } catch (RemoteException e) {
                e.printStackTrace();
            }


//          if (characteristicTXBattery == null || mBluetoothLeService == null) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (characteristicTXBattery != null || mBluetoothLeService != null) {
//                            sendData(tx);
//                        }
//                    }
//                }, 2000);
//            } else {
//                if (tx[0] == (byte) 0x58) {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            characteristicTXBattery.setValue(tx);
//                            mBluetoothLeService.writeCharacteristic(characteristicTXBattery);
//                        }
//                    }, 1000);
//                } else {
//                    characteristicTXBattery.setValue(tx);
//                    mBluetoothLeService.writeCharacteristic(characteristicTXBattery);
//                }
//
//            }
        }
    }

    public void readDeviceName() {
        if (mConnected) {
            L.i(TAG, "读取=");
            byte[] tx = {0x00,};
            BleComm bleComm = new BleComm("", tx, 0);
            try {
                sendCommand.read(bleComm);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

//            if (characteristicTxName == null || mBluetoothLeService == null) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (characteristicTxName != null || mBluetoothLeService != null) {
//                            readDeviceName();
//                        }
//                    }
//                }, 2000);
//            } else {
//                Logger.t(TAG).i("名字写入 changeDeviceName write cmd");
//                mBluetoothLeService.readCharacteristic(characteristicTxName);
//                Log.e("Data Sent", data + " sent");
//            }
        }
    }

    public void changeDeviceName(final String data) {

        byte[] nameByte = new byte[12];
        L.i(TAG, "changeDeviceName");
        final byte[] tx = data.getBytes();
        System.arraycopy(tx, 0, nameByte, 0, tx.length);
        L.i(TAG, "名字写入 changeDeviceName=" + Arrays.toString(ConvertHelper.byte2HexToStrArr(nameByte)));
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
            Log.e("Error in Thread sleep", ex.getMessage());
        }
        if (mConnected) {
            BleComm bleComm = new BleComm("", nameByte, 1);
            try {
                sendCommand.write(bleComm);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

//            if (characteristicTxName == null || mBluetoothLeService == null) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (characteristicTxName != null || mBluetoothLeService != null) {
//                            changeDeviceName(data);
//                        }
//                    }
//                }, 2000);
//            } else {
//                Logger.t(TAG).i("名字写入 changeDeviceName write cmd");
//                characteristicTxName.setValue(nameByte);
//                mBluetoothLeService.writeCharacteristic(characteristicTxName);
//                Log.e("Data Sent", data + " sent");
//            }
        }
    }

    private Context get_context() {
        return _context;
    }

    private void log(String st) {
        L.i(TAG, st);
    }

}
