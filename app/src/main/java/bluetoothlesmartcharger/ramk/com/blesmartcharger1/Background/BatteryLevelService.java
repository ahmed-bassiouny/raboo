package bluetoothlesmartcharger.ramk.com.blesmartcharger1.Background;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.BluetoothConnection;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.Const;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.R;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.UI.MainActivity;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.L;


/**
 * Created by Rana Abdul Manan on 9/27/2015.
 */
public class BatteryLevelService extends Service {

    private static final String TAG = "Background Service";

    private Handler notiHandler;

    private static BatteryLevelService _instance = null;

    private static int CURRENT_ALARM = 0;

    private Timer timer = null;
    private TimerTask updateProfile = null;
    private boolean D = true;
    //private static FragmentActivity activity = null;
    private boolean isTryToConnected = false;
    private static boolean _isServiceRunning;
    private Context _context;
    private boolean serviceContext = false;

    ExecutorService executorService;

    public BatteryLevelService() {
        _isServiceRunning = false;



        Log.i("Alram Service", "Constructed");
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _context = getApplicationContext();




        Log.i("Alram Service", "Start Command");

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        if (timer == null)
            timer = new Timer();

        _instance = this;

//        StartServiceJob();

        _context = getApplicationContext();

        Log.i("Alram Service", "Created");

        super.onCreate();
    }

    public void setFromBackground(boolean val) {
        serviceContext = val;
    }

    public static BatteryLevelService get_instance() {
        return _instance;
    }

    @Override
    public void onDestroy() {
        _instance = null;
        super.onDestroy();

        // Do not forget to unregister the receiver!!!

    }

    @Override
    public void onStart(Intent intent, int startId) {

//        StartServiceJob();

        Log.i("Alram Service", "Started");
        super.onStart(intent, startId);
    }

    public void StartServiceJob() {
        stopSelf();
        _context = getBaseContext();
        notiHandler = new Handler();
        try {
            if (timer != null)
                timer.cancel();

            timer = new Timer();

            updateProfile = new NotificationSchedular(_context);
            timer.scheduleAtFixedRate(updateProfile, 0, 10000);
            //}
        } catch (Exception ex) {
            Log.e("Starting Service Error", ex.getMessage());
        }
        _isServiceRunning = true;
    }

    public static boolean isServiceRunning() {
        return _isServiceRunning;
    }


    public void checkBatteryStatus() {
        L.w("Battery "," checkBatteryStatus.........................");
        try {
//            Logger.t(TAG).i("每10秒查看一次手机电量");

            if (MainActivity.get_instance() == null && !serviceContext) {
                BluetoothConnection.get_instance().setContext(getApplicationContext());
                serviceContext = true;

            }


            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = _instance.registerReceiver(null, ifilter);

            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            final boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            final int batteryPct = (int) ((level / (float) scale) * 100);

            Logger.t(TAG).i("当前电量：" + batteryPct + "%");
//            Logger.t(TAG).i("当前充电方式(Plug)：" + chargePlug);
//            Logger.t(TAG).i("当前充电方式(USB)：" + usbCharge);
//            Logger.t(TAG).i("当前充电方式(AC)：" + acCharge);
            // Toast.makeText(_instance, chargingMsg, Toast.LENGTH_LONG).show();
            if (Const.isCharging == null) {
                Const.isCharging = !isCharging;
            }

            //更新主界面
            if (MainActivity.get_instance() != null) {
                Const.current_level = batteryPct;

                if (Const.isCharging == null) {
                    Const.isCharging = isCharging;

                    if (MainActivity.get_instance() != null) {
                        MainActivity.get_instance().setBatteryInfo(isCharging, batteryPct);
                    }
                }
                if (MainActivity.get_instance() != null) {
                    MainActivity.get_instance().setBatteryInfo(isCharging, batteryPct);
                }
            }
//            Logger.t(TAG).i("电量等级：" + batteryPct);

            Logger.t(TAG).i("充电区间: [" + Const.charging + "-" + Const.discharging + "],当前电量=" + batteryPct);
//            Logger.t(TAG).i("是否在充电：" + isCharging);
//            Logger.t(TAG).i("是不是这个设备：" + BluetoothConnection.get_instance().isSmartCharger());
//            Logger.t(TAG).i("是不是连接状态：" + BluetoothConnection.get_instance().isBluetoothConnected());

            if (batteryPct < Const.charging + 1) {
                Logger.t(TAG).i("手机当前电量小于设置本机电量");
                if (!isCharging) {
                    Logger.t(TAG).i("开始充电");
                    if (BluetoothConnection.get_instance().isBluetoothConnected() && BluetoothConnection.get_instance().isSmartCharger()) {
                        BluetoothConnection.get_instance().sendData("a\r");

                    }
                }
            }
            /*
            if (batteryPct < Const.charging + 1) {
                if (!isCharging && (BluetoothConnection.get_instance().getLastMsg().equalsIgnoreCase("l") || BluetoothConnection.get_instance().getLastMsg().equalsIgnoreCase("h"))) {
                    if(checkAndSetHostAddress()) {
                        if (!BluetoothConnection.get_instance().isBluetoothConnected() && BluetoothConnection.get_instance().getState() != 1) {
                            BluetoothConnection.get_instance().createConnection();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"First Select smart charger bluetooth",Toast.LENGTH_LONG).show();
                        stopSelf();
                    }
                    if (BluetoothConnection.get_instance().isBluetoothConnected() && BluetoothConnection.get_instance().isSmartCharger()) {

                        BluetoothConnection.get_instance().sendData("a\r");
                        try{
                            Thread.sleep(500);
                        }catch (Exception ex){}
                        if(BluetoothConnection.get_instance().getLastMsg().equalsIgnoreCase("a")) {
                            BluetoothConnection.get_instance().sendData("g\r");
                        }
                    }
                }
                else{
                    if(!BluetoothConnection.get_instance().getLastMsg().equalsIgnoreCase("g")) {
                        if(!BluetoothConnection.get_instance().isBluetoothConnected()){
                            if(checkAndSetHostAddress()) {
                                Toast.makeText(getApplicationContext(),"Address : "+Const.HOST_DEVICE_ADDRESS,Toast.LENGTH_LONG).show();
                                if (BluetoothConnection.get_instance().getState() != 1) {
                                    BluetoothConnection.get_instance().createConnection();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(),"First Select smart charger bluetooth",Toast.LENGTH_LONG).show();
                                stopSelf();
                            }
                        }
                        if (BluetoothConnection.get_instance().isBluetoothConnected() && BluetoothConnection.get_instance().isSmartCharger())
                            BluetoothConnection.get_instance().sendData("g\r");
                    }
                }
            }*/
            else if (batteryPct >= Const.discharging) {
                Logger.t(TAG).i("手机当前电量大于设置本机电量");
                if (isCharging) {
                    Logger.t(TAG).i("充电状态，结果本次充电");
                    if (BluetoothConnection.get_instance().isBluetoothConnected() && BluetoothConnection.get_instance().isSmartCharger()) {
                        BluetoothConnection.get_instance().sendData("b\r");
                    }
                }
            }


            Const.isCharging = isCharging;
        } catch (Exception ex) {
            //Log.e("Error",ex.getMessage());
        }


    }


    public boolean checkAndSetHostAddress() {
        if (Const.HOST_DEVICE_ADDRESS.equalsIgnoreCase("NULL")) {
            SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_prefs_name), 1);
            final String host_mac = prefs.getString(getString(R.string.mac_address), "Not");
            if (!host_mac.equalsIgnoreCase("Not")) {
                Const.HOST_DEVICE_ADDRESS = host_mac;
                return true;
            } else {
                return false;
            }

        }
        return true;

    }

    public class NotificationSchedular extends TimerTask {


        private Context context;
        private android.os.Handler mHandler = new android.os.Handler();

        public NotificationSchedular(Context con) {
            this.context = con;
        }


        @Override
        public void run() {
            new Thread(new Runnable() {

                public void run() {

                    mHandler.post(new Runnable() {
                        public void run() {

                            checkBatteryStatus();

                        }

                    });
                }
            }).start();

        }


    }

}