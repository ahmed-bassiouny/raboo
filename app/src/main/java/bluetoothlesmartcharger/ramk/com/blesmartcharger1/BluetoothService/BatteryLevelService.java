package bluetoothlesmartcharger.ramk.com.blesmartcharger1.BluetoothService;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.BluetoothConnection;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.UI.MainActivity;


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

    public BatteryLevelService() {
        _isServiceRunning = false;

        StartServiceJob();

        Log.i("Alram Service", "Constructed");
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _context = getApplicationContext();


        StartServiceJob();

        Log.i("Alram Service", "Start Command");

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        if (timer == null)
            timer = new Timer();

        _instance = this;

        StartServiceJob();

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

        StartServiceJob();

        Log.i("Alram Service", "Started");
        super.onStart(intent, startId);
    }

    public void StartServiceJob() {
        _context = getBaseContext();
        notiHandler = new Handler();
        try {
            if (timer == null)
                timer = new Timer();


            //if (updateProfile==null) {
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

        try {

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

            String chargingMsg = "Battery Level : " + batteryPct + "%\r\n IsCharging : " + isCharging + "\r\n Charged PluggedIn : " + chargePlug + "\r\n usbCharging : " + usbCharge + "\r\n AcCharging : " + acCharge;
        } catch (Exception ex) {
            //Log.e("Error",ex.getMessage());
        }
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

//                            checkBatteryStatus();

                        }

                    });
                }
            }).start();

        }


    }
}
