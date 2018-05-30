package bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Background.SampleGattAttributes;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.BleComm;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.BroadcastDevice;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.Const;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.ConvertHelper;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.SharedPreferencesUtil;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.R;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.SendCommand;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.UI.MainActivity;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.ble.BleDeviceListener;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.ble.Peripheral;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.broad.KeepLiveReceiver;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.ByteUtils;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.L;

/**
 * This Service is Persistent Service. Do some what you want to do here.<br/>
 * 守护进程
 * 把蓝牙连接写到这里面,
 * 所有蓝牙通讯回调通过广播发出去
 */
public class Service1 extends Service {
    private static final String TAG = "Service1";
    public final static String ACTION_START_SCAN =
            "com.example.bluetooth.le.ACTION_START_SCAN";
    public final static String ACTION_STOP_SCAN =
            "com.example.bluetooth.le.ACTION_STOP_SCAN";
    public final static String ACTION_SCAN_RESULT =
            "com.example.bluetooth.le.ACTION_SCAN_RESULT";
    public final static String ACTION_BATTERY_STATUS =
            "com.example.bluetooth.le.ACTION_BATTERY_STATUS";
    //充电广播
    public final static String ACTION_BATTERY_DATA_DISCHARGING =
            "com.example.bluetooth.le.ACTION_BATTERY_DATA_DISCHARGING";
    public final static String ACTION_BATTERY_DATA_CHARGING =
            "com.example.bluetooth.le.ACTION_BATTERY_DATA_CHARGING";
    public final static String ACTION_BATTERY_DATA_DEFAULT =
            "com.example.bluetooth.le.ACTION_BATTERY_DEFAULT";

    //监听充电广播
    public final static String ACTION_BATTERY_CONNECTED =
            "android.intent.action.ACTION_POWER_CONNECTED";
    public final static String ACTION_BATTERY_DISCONNECTED =
            "android.intent.action.ACTION_POWER_DISCONNECTED";

    public final static String EXTRA_DATA_BYTE =
            "com.example.bluetooth.le.EXTRA_DATA_BYTE";

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_RSSI =
            "com.example.bluetooth.le.ACTION_DATA_RSSI";
    public final static String ACTION_DATA_WRITE =
            "com.example.bluetooth.le.ACTION_DATA_WRITE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String AOTO =
            "aoto";

    //    public final static UUID UUID_HM_RX_TX =
//            UUID.fromString(SampleGattAttributes.HM_RX_TX);
    private UUID SERVICE_UUID = UUID.fromString(SampleGattAttributes.HM_10_CONF);

    public final static UUID UUID_HM_SERVICE =
            UUID.fromString(SampleGattAttributes.HM_10_CONF);
    public final static UUID UUID_HM_RX_TX =
            UUID.fromString(SampleGattAttributes.HM_RX_TX);
    public final static UUID UUID_HM_RN_TX =
            UUID.fromString(SampleGattAttributes.HM_RN_TX);

    //    BleComm bleComm;
    private KeepLiveReceiver keepLiveReceiver;
    BleBroadcastReceiver mBleReceiver;
    boolean isEnabled = false; //蓝牙状态
    BluetoothLeScanner scanner;
    private BluetoothAdapter mBluetoothAdapter;
    BluetoothAdapter.LeScanCallback leScanCallback;
    ScanCallback scanCallback;

    private boolean isScaning = false; //是否正在扫描
    private boolean autoConnect = false; //是否自动连接
    private String connectAddress, mBluetoothDeviceName;
    Peripheral mPeripheral;
    private BluetoothDevice remoteDevice;
    PowerManager pManager;
    PowerManager.WakeLock mWakeLock;

    private NotificationManager mNotificationManager;
//    Timer timer;

    private boolean check = true;
    int count = 0;
    private Thread checkThread;

    private int charging = 80;
    private int discharging = 100;
    private boolean chargingDefault;

    private int currentStart = 0;
    boolean isSmartCharger;
    private ExecutorService cachedThreadPool;

    private int sleep_time = 3;
    private boolean isSendBroadcast = true;
    boolean isKill = false;
    boolean lu = false;
    boolean isCanSend ;
    @Override
    public void onCreate() {
        super.onCreate();
        //TODO do some thing what you want..
        Log.i(TAG, "onCreate");
        initBroadCastReceiver();
        //createNotification();
        init();

        cachedThreadPool = Executors.newCachedThreadPool();
        checkThread = new CheckThread();
        cachedThreadPool.submit(checkThread);

//        timer = new Timer();
//        timer.schedule(new MyTask(),10*1000,15*1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand  " + isConnect());
        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);
        String mac = prefs.getString(getString(R.string.mac_address), "Not");
        if (!mac.equals("Not") && !mac.equals("NULL")) {
            autoConnect = true;
            connectAddress = mac;
        }
        log(mac);
        initialize();
        SharedPreferences batt = this.getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);
        charging = batt.getInt("charging", 80);
        discharging = batt.getInt("discharging", 100);
        chargingDefault = batt.getBoolean("default_setting", false);
        log("charging =" + charging + " discharging = " + discharging + " chargingDefault = " + chargingDefault);
        if (chargingDefault) {
            charging = 80;
            discharging = 100;
        }
        check = true;
//        updateNotification("xxxxxxx");
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return stub;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (!isKill) {
            Intent intent = new Intent(this, Service1.class);
            startService(intent);

        }
        if (null != mWakeLock) {
            mWakeLock.release();
        }
        removeNotification();
        unregisterReceiver(keepLiveReceiver);
        unregisterReceiver(mBleReceiver);
        super.onDestroy();
    }

    public void init() {
        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        enableBle();
        isScaning = false;
    }

    public void initialize() {
        if (null == mWakeLock) {
            pManager = ((PowerManager) getSystemService(POWER_SERVICE));
            mWakeLock = pManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, TAG);
        }

        mWakeLock.acquire();
    }

    /**
     * 开启蓝牙
     */
    public void enableBle() {
        if (null != mBluetoothAdapter) {
            if (!mBluetoothAdapter.enable()) {
                Log.w(TAG," 启动蓝牙....");
                isEnabled = false;
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Log.w(TAG," 蓝牙已经启动....");
                isEnabled = true;
                lu = true;
//                leStartScan(10);
            }
            if (Build.VERSION.SDK_INT >= 23) {
                scanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
            initScanCallback();
            isScaning = false;
        } else {
            Toast.makeText(this, "Bluetooth 4.0 is not supported", Toast.LENGTH_SHORT).show();
        }
    }

    //    class MyTask extends TimerTask {
//
//        @Override
//        public void run() {
//
//        }
//
//    }
    private void initScanCallback() {
        if (Build.VERSION.SDK_INT >= 23) {
//            if (scanCallback == null)
                scanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, final ScanResult result) {
                        super.onScanResult(callbackType, result);

                        if (Build.VERSION.SDK_INT >= 21) {
                            BluetoothDevice device = result.getDevice();
//                                log(device.getName() + "  " + device.getAddress() + " Rssi = " + result.getRssi() + Arrays.toString(result.getScanRecord().getBytes()));
                            onScanResults(device, result.getRssi(), result.getScanRecord().getBytes());
                        }

                    }

                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        super.onBatchScanResults(results);
                        log("onBatchScanResults");
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        log("onScanFailed  " + errorCode);
                    }
                };
        } else {
//            if (leScanCallback == null)
                leScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(final BluetoothDevice devices, final int rssi, final byte[] scanRecord) {
                        onScanResults(devices, rssi, scanRecord);
                    }
                };
        }
    }

    private void onScanResults(BluetoothDevice device, int rssi, byte[] sb) {
//        if (!TextUtils.isEmpty(stopAddress)){
//            if (stopAddress.equals(device.getAddress())){
//                stopScan();
//                log("找到指定连接的设备："+stopAddress);
//            }
//        }
        BroadcastDevice broadcastDevice = new BroadcastDevice(device.getName(), device.getAddress(), rssi, sb);
        broadcastScanResult(ACTION_SCAN_RESULT, broadcastDevice);
        if (autoConnect) {
            if (device.getAddress().equals(connectAddress))
                connectDevice(device.getAddress());
        }
    }

    /**
     * second <=0  一直扫描
     *
     * @param second 扫描时长
     */
    public synchronized void leStartScan(final int second) {

        if (isScaning && null == mBluetoothAdapter && isEnabled) {
            return;
        }
        broadcastUpdateScan(ACTION_START_SCAN);

//        cachedThreadPool.submit(new StartScanThread());

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        log("开始扫描。。。");
        if (Build.VERSION.SDK_INT >= 23 && null != scanner) {
            scanner.startScan(scanCallback);
        } else {
            mBluetoothAdapter.startLeScan(leScanCallback);
        }
//
//            }
//        }).start();
        if (second > 0) {
            cachedThreadPool.submit(new StopScanThread(second));
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(second * 1000);
//                        leStopScan();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();

        }
        isScaning = true;
    }

    class StartScanThread extends Thread {
        public StartScanThread() {
        }

        @Override
        public void run() {
            super.run();
            log("开始扫描。。。");
            if (Build.VERSION.SDK_INT >= 23 && null != scanner) {
                scanner.startScan(scanCallback);
            } else {
                mBluetoothAdapter.startLeScan(leScanCallback);
            }
        }
    }

    class StopScanThread extends Thread {
        int scondTime = 0;

        public StopScanThread(int s) {
            scondTime = s;
        }

        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(scondTime * 1000);
                leStopScan();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    public void leStopScan() {
        log("停止扫描。。。");
        if (!isScaning) {
            return;
        }
        isScaning = false;
        broadcastUpdateScan(ACTION_STOP_SCAN);
        if (Build.VERSION.SDK_INT >= 21 && null != scanner) {
            log("停止扫描-------------");
            scanner.stopScan(scanCallback);
        } else {
            log("停止扫描-------------");
            mBluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    private void broadcastUpdateScan(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastScanResult(String action, BroadcastDevice device) {
        Intent intent = new Intent(action);
        intent.putExtra("scanResult", device);
        sendBroadcast(intent);
    }

    private void connectDevice(String address) {
        if (TextUtils.isEmpty(address) || address.equals("NULL")) {
            return;
        }
        if (isScaning)
            leStopScan();
        if (null == mBluetoothAdapter && !isEnabled) {
            return;
        }
        remoteDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (null == remoteDevice) {
            return;
        }

        connectAddress = remoteDevice.getAddress();
        mBluetoothDeviceName = remoteDevice.getName();
        autoConnect = true;
        SharedPreferencesUtil.saveString(Service1.this, Const.CHANGEED_DEVICE_NAME, mBluetoothDeviceName);
        if (lu)
            disConnect();
        lu = true;
        mPeripheral = new Peripheral(this, remoteDevice, 0, null);
        mPeripheral.setBleDeviceListener(listener);
        if (null != mPeripheral) {
            L.i(TAG, "connect.....");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    lu = true;
                    mPeripheral.connect();
                }
            }).start();
        }
    }

    private void disConnect() {
        isCanSend = false;
        if (null != mPeripheral) {
            mPeripheral.disConnect();
            mPeripheral = null;
        }
    }

    private boolean isConnect() {
        if (null != mPeripheral && mPeripheral.isConnect()) {
            return true;
        }
        return false;
    }

    private void readRssi() {
        if (isConnect()) {
            mPeripheral.queueReadRSSI();
        }
    }

    private BleDeviceListener listener = new BleDeviceListener() {
        @Override
        public void onConnected(String address) {
            broadcastUpdate(ACTION_GATT_CONNECTED);
            updateNotification(getString(R.string.app_name) + " connected");
        }

        @Override
        public void onDisConnected(String address) {
            lu = true;
            broadcastUpdate(ACTION_GATT_DISCONNECTED);
            isSmartCharger = false;
            updateNotification(getString(R.string.app_name) + " disConnected");
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
           new Thread(new Runnable() {
               @Override
               public void run() {
                   try {
                       Thread.sleep(1000);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
                   if (isEnabled && null != gatt) {
//                       for ( BluetoothGattService service : gatt.getServices()) {
//                           log("onServicesDiscovered service:" + service.getUuid());
//                           for ( BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
//                               if (characteristic.getUuid().equals(UUID_HM_RX_TX)) {
//                                   setCharacteristicNotification(service.getUuid(), characteristic.getUuid());
//                               }
//                           }
//                       }
                       setCharacteristicNotification(SERVICE_UUID, UUID_HM_RX_TX);
                   }
                   broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
               }
           }).start();

        }

        @Override
        public void onServicesFailed(BluetoothGatt gatt, int status) {
//            connectDevice(remoteDevice.getAddress());
            lu = true;
            disConnect();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            log("onCharacteristicChanged : " + Arrays.toString(characteristic.getValue()));
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.getUuid().toString().equalsIgnoreCase(SampleGattAttributes.HM_RN_TX)) {
                    Intent intent = new Intent(Const.READ_DEVICE_NAME_BROADCASTER);
                    intent.putExtra(EXTRA_DATA_BYTE, characteristic.getValue());
                    sendBroadcast(intent);
//                    LocalBroadcastManager.getInstance(Service1.this).sendBroadcast(intent);
                    L.i(TAG, "read = " + Arrays.toString(ConvertHelper.byte2HexToStrArr(characteristic.getValue())));
                }
//                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            log("onCharacteristicWrite............................ ");
            isCanSend = true;
            broadcastUpdateData(ACTION_DATA_WRITE, characteristic.getValue());
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//            ACTION_DATA_RSSI
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_RSSI, "rssi", rssi);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            L.e(TAG, "onDescriptorWrite  " + Arrays.toString(descriptor.getValue()));
            byte[] b = {0x58};
            isCanSend = true;
            queueWrite(SERVICE_UUID, UUID_HM_RX_TX, b, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdateData(final String action, byte[] data) {
        final Intent intent = new Intent(action);
        intent.putExtra("data", data);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, String key, int data) {
        final Intent intent = new Intent(action);
        intent.putExtra(key, data);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        if (!isSendBroadcast)
            return;

        final Intent intent = new Intent(action);

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        Log.i(TAG, "data" + Arrays.toString(characteristic.getValue()));

        if (data != null && data.length > 0) {
            if (data[0] == 0x58) {
                isSmartCharger = true;
            }
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            Log.d(TAG, String.format("%s", new String(data)));
            // getting cut off when longer, need to push on new line, 0A
            intent.putExtra(EXTRA_DATA, String.format("%s", new String(data)));
            intent.putExtra(EXTRA_DATA_BYTE, data);
        }
//        intent.putExtra("data",data);
        sendBroadcast(intent);
    }

    /**
     * 开启设备通知
     */
    public void setCharacteristicNotification(UUID serviceUUID, UUID characteristicUUID) {
        if (null != mPeripheral && mPeripheral.isConnect()) {
            mPeripheral.queueRegisterNotifyCallback(serviceUUID, characteristicUUID);
        }
    }

    public void removeNotify(UUID serviceUUID, UUID characteristicUUID) {
        if (null != mPeripheral && mPeripheral.isConnect()) {
            mPeripheral.queueRemoveNotifyCallback(serviceUUID, characteristicUUID);
        }
    }

    public void updateRssi() {
        if (null != mPeripheral && mPeripheral.isConnect()) {
            mPeripheral.queueReadRSSI();
        }
    }

    public void queueWrite(UUID serviceUUID, UUID characteristicUUID, byte[] data, int writeType) {
        log(" write = " + ByteUtils.byteArrayTo16String(data));
        if (null != mPeripheral && mPeripheral.isConnect()) {
            mPeripheral.queueWrite(serviceUUID, characteristicUUID, data, writeType);
        }
    }

    public void queueRead(UUID serviceUUID, UUID characteristicUUID) {
        if (null != mPeripheral && mPeripheral.isConnect()) {
            mPeripheral.queueRead(serviceUUID, characteristicUUID);
        }
    }

    public void createNotification() {
        mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getApplicationContext());
        builder.setTicker(getString(R.string.app_name));
        builder.setContentTitle(getString(R.string.app_name));
        //通知内容
        builder.setContentText("");
        builder.setSmallIcon(R.drawable.app_icon);
        //设置为不可清除模式
//        builder.setOngoing(false);
        Intent intentNotification = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity
                (this, 0, intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent pendingIntent = PendingIntent.getActivity
//                (this, 0, intentNotification, 0);
        builder.setContentIntent(pendingIntent);
//        builder.setWhen()
        Notification notification = builder.build();
        //显示通知，id必须不重复，否则新的通知会覆盖旧的通知（利用这一特性，可以对通知进行更新）
//        mNotificationManager.notify(1, notification);
        //启动到前台
        L.i(TAG, "startForeground.............. ");
        startForeground(1, notification);
    }

    public void updateNotification(String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getApplicationContext());
        builder.setTicker(getString(R.string.app_name));
        builder.setContentTitle(getString(R.string.app_name));
        //通知内容
        builder.setContentText(text);
        builder.setSmallIcon(R.drawable.app_icon);
        //设置为不可清除模式
//        builder.setOngoing(false);
        Intent intentNotification = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity
                (this, 0, intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        mNotificationManager.notify(1, notification);
    }

    public void removeNotification() {
        if (mNotificationManager != null)
            mNotificationManager.cancel(1);
    }

    /**
     * 初始化广播
     **/
    private void initBroadCastReceiver() {
        keepLiveReceiver = new KeepLiveReceiver();
        IntentFilter recevierFilter = new IntentFilter();
        recevierFilter.addAction(Intent.ACTION_SCREEN_ON);
        recevierFilter.addAction(Intent.ACTION_SCREEN_OFF);
        recevierFilter.addAction(Intent.ACTION_USER_PRESENT);
        recevierFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(keepLiveReceiver, recevierFilter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Service1.ACTION_BATTERY_DATA_CHARGING);
        filter.addAction(Service1.ACTION_BATTERY_DATA_DISCHARGING);
        filter.addAction(Service1.ACTION_BATTERY_DATA_DEFAULT);

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        filter.addAction(ACTION_BATTERY_CONNECTED);
        filter.addAction(ACTION_BATTERY_DISCONNECTED);

//        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //蓝牙连接上的广播
//        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //蓝牙断开的广播
        mBleReceiver = new BleBroadcastReceiver();
        registerReceiver(mBleReceiver, filter);
    }

    class BleBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("aaa", "STATE_OFF 手机蓝牙关闭");
                        lu = true;
//                        enableBle();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("aaa", "STATE_TURNING_OFF 手机蓝牙正在关闭");
                        isEnabled = false;
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("aaa", "STATE_ON 手机蓝牙开启");
                        isEnabled = true;
                        lu = false;
                        if (Build.VERSION.SDK_INT >= 23) {
                            scanner = mBluetoothAdapter.getBluetoothLeScanner();
                        }
                        initScanCallback();
                        if (autoConnect && !isConnect())
                            leStartScan(0);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("aaa", "STATE_TURNING_ON 手机蓝牙正在开启");
                        break;
                }
            } else if (action.equals(Service1.ACTION_BATTERY_DATA_CHARGING)) {
                charging = intent.getIntExtra("charging", 80);
                Log.d("charging", "charging = " + charging);
                SharedPreferences prefs = Service1.this.getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("charging", charging);
                editor.commit();
            } else if (action.equals(Service1.ACTION_BATTERY_DATA_DISCHARGING)) {
                discharging = intent.getIntExtra("discharging", 100);
                Log.d("discharging", "discharging = " + discharging);
                SharedPreferences prefs = Service1.this.getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("discharging", discharging);
                editor.commit();
            } else if (action.equals(Service1.ACTION_BATTERY_DATA_DEFAULT)) {

                chargingDefault = intent.getBooleanExtra("default", false);
                if (chargingDefault) {
                    charging = 80;
                    discharging = 100;
                } else {
                    SharedPreferences batt = context.getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);
                    charging = batt.getInt("charging", 80);
                    discharging = batt.getInt("discharging", 100);
                }

                Log.d("DEFAULT", "chargingDefault = " + chargingDefault + "charging = " + charging + " , discharging=" + discharging);
            } else if (action.equals(ACTION_BATTERY_CONNECTED)) {

            } else if (action.equals(ACTION_BATTERY_DISCONNECTED)) {

            }
        }
    }


    private final SendCommand.Stub stub = new SendCommand.Stub() {

        @Override
        public void connect(String address) throws RemoteException {
            connectDevice(address);
        }

        @Override
        public void close() throws RemoteException {
            disConnect();
        }

        @Override
        public void write(BleComm command) throws RemoteException {
            int type = command.getType();
            log(" command type = " + type);
            if (type == 0) {
                queueWrite(SERVICE_UUID, UUID_HM_RX_TX, command.getData(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            } else {
                queueWrite(SERVICE_UUID, UUID_HM_RX_TX, command.getData(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }


        }

        @Override
        public void read(BleComm command) throws RemoteException {
            queueRead(SERVICE_UUID, UUID_HM_RN_TX);
        }

        @Override
        public void startScan(int s) throws RemoteException {
            leStartScan(s);
        }

        @Override
        public void stopScan() throws RemoteException {
            leStopScan();
        }

        @Override
        public void getRssi() throws RemoteException {
            readRssi();
        }

        @Override
        public void kill() throws RemoteException {
            killProcess();
        }

        @Override
        public void isStart(boolean b) throws RemoteException {
            log(" 是否在前台：" + b);
            if (b) {
                sleep_time = 2;
                isSendBroadcast = true;
            } else {
                sleep_time = 2;
                isSendBroadcast = false;
            }
        }
    };

    public void killProcess() {
        isKill = true;
        check = false;
        removeNotification();
//        interruptCheckThread();

//        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void log(String str) {
        L.i(TAG, str);
    }


    private void checkBatteryStatus() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = (Service1.this).registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        final boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        final int batteryPct = (int) ((level / (float) scale) * 100);
        if (!isCharging) {
            currentStart = batteryPct;
        }
        boolean bb = false;
//        log("充电起点：" + currentStart + "  当前电量：" + batteryPct);
//        log("充电区间: [" + charging + " , " + discharging + "]  , isSmartCharger = " + isSmartCharger);
        if (isCanSend) {
            if (batteryPct < charging) {
                byte[] a = {(byte) 0x61};
                queueWrite(SERVICE_UUID, UUID_HM_RX_TX, a, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                currentStart = batteryPct;
                bb = true;
            } else if (batteryPct >= charging && batteryPct < discharging) {  //当前电量在区间
                if (currentStart == 0) { //一直着充电之后打开应用，电量在区间，不需要充电
                    bb = false;
                    byte[] d = {(byte) 0x62};
                    queueWrite(SERVICE_UUID, UUID_HM_RX_TX, d, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                } else {  //先打开应用，再接上充电线一直充电 ,电量在区间，
                    if (currentStart > charging) {
                        bb = false;
                        byte[] d = {(byte) 0x62};
                        queueWrite(SERVICE_UUID, UUID_HM_RX_TX, d, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    } else {
                        bb = true;
                        byte[] a = {(byte) 0x61};
                        queueWrite(SERVICE_UUID, UUID_HM_RX_TX, a, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    }
                }
            } else if (batteryPct >= discharging) {
                bb = false;
                byte[] d = {(byte) 0x62};
                queueWrite(SERVICE_UUID, UUID_HM_RX_TX, d, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }
        }
        L.i(TAG, " 是否充电： " + bb);
        if (isSendBroadcast)
            broadcastBatteryStatus(isCharging, batteryPct);
    }

    private void broadcastBatteryStatus(boolean isCharging, int data) {
        final Intent intent = new Intent(ACTION_BATTERY_STATUS);
        intent.putExtra("isCharging", isCharging);
        intent.putExtra("batteryPct", data);
        intent.putExtra("isConnected", isSmartCharger);
        sendBroadcast(intent);
    }

    public void interruptCheckThread() {
        check = false;
        if (checkThread != null) {
            checkThread.interrupt();
        }
    }

    class CheckThread extends Thread {

        @Override
        public void run() {
            while (check) {
                try {
                    Thread.sleep(sleep_time * 1000); //2秒检查一次电量
                    checkBatteryStatus();
                    if (count != 0 && count % 8 == 0) {
                        L.w("Check", " CheckThread.........................");
                        if (autoConnect && mBluetoothAdapter != null && isEnabled && !isConnect()) {
//                            log("定时器" + isConnect());
//                            disConnect();
                            leStartScan(6);
                        }
                        count = 0;
                    }
                    count++;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
