package bluetoothlesmartcharger.ramk.com.blesmartcharger1.UI;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.BuildConfig;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.BluetoothConnection;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.Const;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.SharedPreferencesUtil;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.R;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.WebViewActivity;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.L;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.SpUtils;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.Utils;

import static bluetoothlesmartcharger.ramk.com.blesmartcharger1.R.string.app_name;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;

    //    ScheduledExecutorService executorService;
    Intent batteryStatus;

    private static MainActivity _instance = null;

    private ImageView batteryImage;

    private ImageView bluetooth;

    private TextView title, subtitle;
    private TextView batteryPercentage;
    private TextView chargingDischarging;
    private int batterTotalHeight=0;
    int batteryPct = 0;
    private boolean connectionTryStart;

    private int lastLevel = 0;
    private int batteryIv_width = 0;
    private int batteryIv_height = 0;


    private static final int PERMISSON_REQUESTCODE = 100;
    //    private TextView logTextView;
//    private EditText commandEditText;
    //private Button changeStart, changeStop;
    boolean isStart = false;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    boolean ischaring;
                    int value = msg.arg1;
                    if (value == 1) {
                        ischaring = true;
//                        Toast.makeText(MainActivity.this, "充电状态", Toast.LENGTH_SHORT).show();
                    } else {
                        ischaring = false;
//                        Toast.makeText(MainActivity.this, "非充电状态", Toast.LENGTH_SHORT).show();
                    }
                    int batterPac = msg.arg2;
                    Log.e( "handleMessage: ", "aaaaaaaaaaaaaaaaaaaaaa");
                    setBatteryInfo(ischaring, batterPac);
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //方法一
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        setContentView(R.layout.activity_home);
        title = (TextView) findViewById(R.id.title);
        subtitle = (TextView) findViewById(R.id.subtitle);
        bluetooth = (ImageView) findViewById(R.id.bluetooth);
        String version = getResources().getString(R.string.app_name);
        title.setText(version);
        L.i(TAG, "onCreate");
        initExecutor();
        isLocationPermissionGranted();

        batteryIv_width = Utils.dip2px(MainActivity.this, 150f);
        batteryIv_height = Utils.dip2px(MainActivity.this, 300f);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        _instance = this;
        BluetoothConnection.get_instance().setContext(getApplicationContext());
        SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("runInBackground", true);
        editor.commit();
        if (prefs.getBoolean("default_setting", true)) {

            Const.charging = 80;
            Const.discharging = 100;

        } else {
            int charge_value = prefs.getInt("charging", 80);
            int discharge_value = prefs.getInt("discharging", 100);

            Const.charging = charge_value;
            Const.discharging = discharge_value;
        }
        String mac = prefs.getString(getString(R.string.mac_address), "Not");
        if (!mac.equalsIgnoreCase("Not")) {
            Const.HOST_DEVICE_ADDRESS = mac;
        }
//        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
//            BluetoothAdapter.getDefaultAdapter().enable();
//        }
        initializeUI();
        BluetoothConnection.get_instance().setContext(getApplicationContext());

        /*if(!BluetoothConnection.get_instance().isBluetoothConnected())
            createConnectionWithBlueooth();
        */
        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (BluetoothConnection.get_instance().isBluetoothConnected()) {
                    if (BatteryLevelService.get_instance() == null) {
                        Intent serviceIntent = new Intent(MainActivity.this, BatteryLevelService.class);
                        startService(serviceIntent);
                    } else {
                        BatteryLevelService.get_instance().checkBatteryStatus();
                    }
                }

            }
        }, 7000);*/
        BluetoothConnection.runInBackground = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Const.HOST_DEVICE_ADDRESS.equalsIgnoreCase("NULL"))
                    subtitle.setText("Not Connected");
                else {
                    if (BluetoothConnection.get_instance().isBluetoothConnected()) {
                        if (BluetoothConnection.get_instance().isSmartCharger())
                            subtitle.setText("Connected with Smart Charger");
                    } else {
                        subtitle.setText("Not Connected");
                        L.i(TAG, "一进主界面就先不开始连接");
                        BluetoothConnection.get_instance().createConnection();
                    }
                }
            }
        }, 3000);
//        if (!SpUtils.getBoolean(MainActivity.this,SpUtils.SETTING,false))
//            showMissingDialog();
        checkPermissions(needPermissions);
//        hasPermission();


        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, Const.REQUEST_CONNECT_DEVICE);
            }
        });

        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.shop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        isStart = true;
    }


    @Override
    protected void onStop() {
        super.onStop();
//        isStart = false;
    }

    public void setIsStart(boolean b) {
        L.i(" 是否在后台 " + b);
        isStart = b;
        BluetoothConnection.get_instance().setIsStart(b);
    }

    public boolean getIsStart() {
        return this.isStart;
    }


    private void initExecutor() {
        final Context mContext = getApplicationContext();
//        if (executorService != null) {
//            L.i(TAG,"结束executorService");
//            executorService.shutdown();
//            executorService = null;
//        }
//         executorService = Executors.newScheduledThreadPool(1);
//        executorService.scheduleAtFixedRate(new Runnable() {
//
//            @Override
//            public void run() {
//                getBattery(mContext);
//            }
//        }, 0, 2, TimeUnit.SECONDS);
    }


    private void getBattery(Context context) {
        L.w("Main ", " getBattery.........................");
        L.i(TAG, "获取电量，初始充电值=" + SharedPreferencesUtil.getInt(MainActivity.this, Const.START_CHARGE_VALUE, 100));
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isChargingCurrentStatus = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        batteryPct = (int) ((level / (float) scale) * 100);
        Const.current_charging = batteryPct;
        //说明断开充电
        if (!isChargingCurrentStatus) {
            BluetoothConnection.isFirstGet61Battery = true;
            SharedPreferencesUtil.saveInt(MainActivity.this, Const.START_CHARGE_VALUE, 100);
        }

        if (MainActivity.get_instance() != null) {

            Const.current_level = batteryPct;

            if (Const.isCharging == null) {
                Const.isCharging = isChargingCurrentStatus;
            }

        }

        Message msg = Message.obtain();
        msg.what = 1;
        if (isChargingCurrentStatus) {
            msg.arg1 = 1;
        } else {
            msg.arg1 = 0;
        }

        msg.arg2 = batteryPct;
        mHandler.sendMessage(msg);

        L.i(TAG, "充电区间: [" + Const.charging + "-" + Const.discharging + "],当前电量=" + batteryPct);

        if (batteryPct <= Const.charging) {

            L.i(TAG, "手机当前电量小于设置本机最小电量，发送61");
            if (BluetoothConnection.get_instance().isBluetoothConnected() && BluetoothConnection.get_instance().isSmartCharger()) {
                BluetoothConnection.get_instance().sendData(new byte[]{(byte) 0x61});
            }
        } else if (batteryPct < Const.discharging && batteryPct > Const.charging) {
            //区间状态
            int startChargePct = SharedPreferencesUtil.getInt(MainActivity.this, Const.START_CHARGE_VALUE, 100);
            if (startChargePct <= Const.charging) {
                //一直是从15%充电上来的,发充电的命令
                BluetoothConnection.get_instance().sendData(new byte[]{(byte) 0x61});
            } else {
                //一直不是从15%充电上来的，发不充电的命令
                BluetoothConnection.get_instance().sendData(new byte[]{(byte) 0x62});
            }

        } else if (batteryPct >= Const.discharging) {
            L.i(TAG, "充电区间中，手机当前电量大于设置本机电量，发送62");
            if (BluetoothConnection.get_instance().isBluetoothConnected() && BluetoothConnection.get_instance().isSmartCharger()) {
                BluetoothConnection.get_instance().sendData(new byte[]{(byte) 0x62});
            }
        }


        Const.isCharging = isChargingCurrentStatus;

    }

    private void isLocationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("MainActivity", "Permission is granted");
            } else {

                Log.v("MainActivity", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("MainActivity", "Permission is granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 1) {
            Log.v("MainActivity", "Permission is granted");
        }
    }

    public void enableDisable(boolean val) {
        //changeStart.setEnabled(val);
    }

    public void startBackgroundService() {
//        if (BatteryLevelService.get_instance() == null) {
//            Intent serviceIntent = new Intent(MainActivity.this, BatteryLevelService.class);
//            startService(serviceIntent);
//        } else {
//            BatteryLevelService.get_instance().checkBatteryStatus();
//        }
    }

    private void initializeUI() {
        //changeStart = (Button) findViewById(R.id.change_start);
        //changeStop = (Button) findViewById(R.id.change_stop);
        //chargingImage = (ImageView) findViewById(R.id.charging_battery);
        batteryImage = (ImageView) findViewById(R.id.battery_level_image);

        batteryPercentage = (TextView) findViewById(R.id.battery_percentage);
        chargingDischarging = (TextView) findViewById(R.id.charging_discharging);

        setUpChargingInfo();

        /*changeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothConnection.get_instance().sendData(new byte[]{(byte) 0x61});
            }
        });

        changeStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothConnection.get_instance().sendData(new byte[]{(byte) 0x62});
            }
        });*/


    }

    /**
     * 手动点击的连接，但是并没有被调用
     */
    private void createConnectionWithBlueooth() {

        if (!BluetoothConnection.get_instance().isBluetoothConnected()) {
            if (Const.HOST_DEVICE_ADDRESS.equalsIgnoreCase("NULL")) {
                SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);
                final String host_mac = prefs.getString(getString(R.string.mac_address), "Not");
                if (!host_mac.equalsIgnoreCase("Not")) {
                    Const.HOST_DEVICE_ADDRESS = host_mac;
                    Const.DEVICE_NAME = prefs.getString(getString(R.string.device_name), "Bluetooth");

                    BluetoothConnection.get_instance().setContext(getApplicationContext());
                    BluetoothConnection.get_instance().createConnection();
                } else {
                    Toast.makeText(MainActivity.this, "Select Bluetooth device", Toast.LENGTH_LONG).show();
                }
            } else {

                BluetoothConnection.get_instance().createConnection();
            }
        } else {
            Toast.makeText(MainActivity.this, "Already Connected with smart charger", Toast.LENGTH_LONG).show();
        }


    }

    private void setUpChargingInfo() {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);


        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        //int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        //boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        //boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = (int) ((level / (float) scale) * 100);

        //String chargingMsg = "Battery Level : "+batteryPct+"% IsCharging : "+isCharging+"\r\n Charged PluggedIn : "+chargePlug+"\r\n usbCharging : "+usbCharge+"\r\n AcCharging : "+acCharge;
        setBatteryInfo(isCharging, batteryPct);
        startBatteryMonitorService();

    }

    public static MainActivity get_instance() {
        return _instance;
    }

    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            moveTaskToBack(true); //退到后台
//
////            if (BluetoothConnection.get_instance() != null) {
////                BluetoothConnection.get_instance().removeNotification();
////            }
////            super.onBackPressed();
////            _instance = null;
////            finish();
//
//        }


    }

    public void displayConnection(final boolean val) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (val) {
                    subtitle.setText("Connected with " + getString(app_name));
                } else {
                    subtitle.setText("Not Connected");

                }
            }
        });
    }

    //每10秒更新电量的图标
    public void setBatteryInfo(final boolean isCharging, final int percentage) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryPercentage.setText(percentage + "%");
                boolean bluetoothConnected = BluetoothConnection.get_instance().isBluetoothConnected();
                if (isCharging) {
                    // chargingImage.setVisibility(View.VISIBLE);
                    if (bluetoothConnected) {
                        chargingDischarging.setText("smart charging");
                    } else {
                        chargingDischarging.setText("smart charging");

                    }
                } else {
                    // chargingImage.setVisibility(View.GONE);
                    chargingDischarging.setText("SELF DISCHARGING");
                }
                if (bluetoothConnected) {
                    subtitle.setText("Connected with " + "Smart Charger");
                    batteryPercentage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.electric,0,0,0);
                } else {
                    subtitle.setText("Not Connected");
                    batteryPercentage.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                }

                setChargingLevel(percentage);


            }
        });

    }

    private void setChargingLevel(int level) {
        L.i(TAG, " lastLevel = " + lastLevel + " ,level = " + level);
        if (lastLevel == level) {
            return;
        }
        lastLevel = level;

        L.i(TAG, "刷新图片");
        switch (level) {
            case 1: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery01, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }

            case 2: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery02, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery02);

                break;
            }
            case 3: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery03, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery03);

                break;
            }
            case 4: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery04, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery04);

                break;
            }
            case 5: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery05, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery05);

                break;
            }
            case 6: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery06, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery06);

                break;
            }
            case 7: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery07, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery07);

                break;
            }
            case 8: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery08, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery08);

                break;
            }
            case 9: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery09, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery09);

                break;
            }
            case 10: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery10, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery10);

                break;
            }
            case 11: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery11, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery11);

                break;
            }
            case 12: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery12, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery12);

                break;
            }
            case 13: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery13, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery13);

                break;
            }
            case 14: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery14, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery14);

                break;
            }
            case 15: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery15, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery15);

                break;
            }
            case 16: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery16, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery16);

                break;
            }
            case 17: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery17, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery17);

                break;
            }
            case 18: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery18, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery18);

                break;
            }
            case 19: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery19, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery19);

                break;
            }
            case 20: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery20, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery20);

                break;
            }
            case 21: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery21, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery21);

                break;
            }
            case 22: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery22, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery22);

                break;
            }
            case 23: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery23, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery23);

                break;
            }
            case 24: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery24, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery24);

                break;
            }
            case 25: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery25, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery25);

                break;
            }
            case 26: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery26, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery26);

                break;
            }
            case 27: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery27, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery27);

                break;
            }
            case 28: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery28, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery28);

                break;
            }
            case 29: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery29, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery29);

                break;
            }
            case 30: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery30, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery30);

                break;
            }
            case 31: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery31, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery31);

                break;
            }
            case 32: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery32, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery32);

                break;
            }
            case 33: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery33, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery33);

                break;
            }
            case 34: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery34, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery34);

                break;
            }
            case 35: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery35, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery35);

                break;
            }
            case 36: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery36, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery36);

                break;
            }
            case 37: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery37, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery37);

                break;
            }
            case 38: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery38, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery38);

                break;
            }
            case 39: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery39, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery39);

                break;
            }
            case 40: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery40, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery40);

                break;
            }
            case 41: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery41, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery41);

                break;
            }
            case 42: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery42, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery42);

                break;
            }
            case 43: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery43, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery43);

                break;
            }
            case 44: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery44, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery44);

                break;
            }
            case 45: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery45, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery45);

                break;
            }
            case 46: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery46, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery46);

                break;
            }
            case 47: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery47, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery47);

                break;
            }
            case 48: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery48, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery48);

                break;
            }
            case 49: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery49, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery49);

                break;
            }
            case 50: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery50, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery50);

                break;
            }
            case 51: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery51, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery51);

                break;
            }
            case 52: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery52, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery52);

                break;
            }
            case 53: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery53, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 54: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery54, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 55: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery55, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 56: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery56, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 57: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery57, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 58: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery58, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 59: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery59, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 60: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery60, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 61: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery61, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 62: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery62, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 63: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery63, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 64: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery64, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 65: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery65, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 66: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery66, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 67: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery67, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 68: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery68, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 69: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery69, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 70: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery70, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 71: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery71, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 72: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery72, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 73: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery73, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 74: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery74, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 75: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery75, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 76: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery76, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 77: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery77, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 78: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery78, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 79: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery79, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 80: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery80, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 81: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery81, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 82: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery82, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 83: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery83, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 84: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery84, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 85: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery85, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 86: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery86, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 87: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery87, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 88: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery88, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 89: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery89, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 90: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery90, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 91: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery91, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 92: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery92, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 93: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery93, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 94: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery94, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 95: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery95, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 96: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery96, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 97: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery97, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 98: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery98, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 99: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery99, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }
            case 100: {
                Utils.recycleImageViewBitMap(batteryImage);
                Bitmap bitmap = Utils.decodeSampleBitmapFromResource(getResources(), R.drawable.battery100, batteryIv_width, batteryIv_height);
                batteryImage.setImageBitmap(bitmap);
//                batteryImage.setImageResource(R.drawable.battery01);

                break;
            }

        }

    }

    private void startBatteryMonitorService() {
//        if (BatteryLevelService.get_instance() == null) {
//            Intent serviceIntent = new Intent(MainActivity.this, BatteryLevelService.class);
//            startService(serviceIntent);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            startActivityForResult(serverIntent, Const.REQUEST_CONNECT_DEVICE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Const.D)
            Log.d(Const.TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case Const.REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    L.i(TAG, "选中列表后的点击连接=" + Const.HOST_DEVICE_ADDRESS);
                    if (Const.D) {
                        Log.d(Const.TAG, "Select device address stored: " + Const.HOST_DEVICE_ADDRESS + ".  Launching new Intent");
                    }
                    SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putString(getString(R.string.mac_address), Const.HOST_DEVICE_ADDRESS);
                    editor.putString(getString(R.string.device_name), Const.DEVICE_NAME);


                    editor.commit();

                    //Toast.makeText(MainActivity.this,"Connecting with mac : "+Const.HOST_DEVICE_ADDRESS,Toast.LENGTH_LONG).show();

                    BluetoothConnection.get_instance().createConnection();

                }
                break;
            case Const.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a session
                    //TODO might need initialize here, BT is now enabled
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(Const.TAG, "BT not enabled");
                    Toast.makeText(this, "Bluetooth not enabled, Leaving...",
                            Toast.LENGTH_SHORT).show();
                    finish();//TODO Can remove if unwanted
                }
                break;
            case MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS: {
//                if (!hasPermission()) {
//                    //若用户未开启权限，则引导用户开启“Apps with usage access”权限
//                    startActivityForResult(
//                            new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
//                            MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
//                }else {
//                    getTopApp(MainActivity.this);
//                }
//                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.charging_setting) {
            // Handle the camera action
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
//            finish();

        } else if (id == R.id.app_help) {
            final Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
//            finish();

        } else if (id == R.id.exit_app) {
            BluetoothConnection.runInBackground = false;
            if (BluetoothConnection.get_instance() != null) {
                BluetoothConnection.get_instance().removeNotification();
            }
//            if (BatteryLevelService.get_instance() != null) {
//                BatteryLevelService.get_instance().stopSelf();
//
//            }
//            if (mBluetoothAdapter.isEnabled())
//                mBluetoothAdapter.disable();
            if (BluetoothConnection.get_instance().isBluetoothConnected())
                BluetoothConnection.get_instance().stopConnection();

            BluetoothConnection.get_instance().bleServiceStop();
            SharedPreferences prefs = getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("runInBackground", false);
            editor.commit();

            finish();
//            if (mBluetoothAdapter.isEnabled())
//                mBluetoothAdapter.disable();

//            if (BatteryLevelService.get_instance() != null) {
//                BatteryLevelService.get_instance().stopSelf();
//
//            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        BluetoothConnection.get_instance().setLastMsg("l");
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.w(TAG, "onDestroy.... ");
//        if (executorService != null) {
//            L.i(TAG,"结束executorService");
//            executorService.shutdown();
//            executorService = null;
//        }
    }

  /*  private String getAppVersion() {
        Context context = getApplicationContext();
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(
                    context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = info.versionName;

        return version;
    }*/

    private void checkPermissions(String... permissions) {
        List<String> needRequestPermissonList = findDeniedPermissions(permissions);
        if (null != needRequestPermissonList
                && needRequestPermissonList.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    needRequestPermissonList.toArray(
                            new String[needRequestPermissonList.size()]),
                    PERMISSON_REQUESTCODE);
        }
    }

    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WAKE_LOCK,
    };

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, perm)) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }

    private void showMissingDialog() {
        String notifyTitle = "提示";
        String notifyMsg = "为保证本应用功能正常运行，请在设置界面将本应用设为保护应用。";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        title.setText(notifyTitle);
        builder.setMessage(notifyMsg);

        // 拒绝
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
                    }
                });

        builder.setPositiveButton("设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SpUtils.putBoolean(MainActivity.this, SpUtils.SETTING, true);
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    /**
     * 启动应用的设置
     */
    private void startAppSettings() {
        detailintent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        detailintent.setData(Uri.parse("package:" + getPackageName()));
        String sys = Utils.getSystem();
        if (sys.equals(Utils.SYS_EMUI)) {
            gotoHuaweiPermission();
        } else if (sys.equals(Utils.SYS_MIUI)) {
            settingMiui();
        } else if (sys.equals(Utils.SYS_FLYME)) {
            gotoMeizuPermission();
        } else {
            startActivity(detailintent);
        }

    }

    final static int GOD_MODE = 11;

    /**
     * 小米神隐模式
     */
    private void settingMiui() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity"));
        try {
            startActivityForResult(intent, GOD_MODE);
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(detailintent);
        }
    }

    /**
     * 华为的权限管理页面
     */
    private void gotoHuaweiPermission() {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            intent.setComponent(comp);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(detailintent);
        }
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    private void gotoMeizuPermission() {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(detailintent);
        }
    }

    private Intent detailintent;

    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1101;

    private void getTopApp(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager m = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (m != null) {
                long now = System.currentTimeMillis();
                //获取60秒之内的应用数据
                List<UsageStats> stats = m.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 60 * 1000, now);
                Log.i(TAG, "Running app number in last 60 seconds : " + stats.size());

                String topActivity = "";

                //取得最近运行的一个app，即当前运行的app
                if ((stats != null) && (!stats.isEmpty())) {
                    int j = 0;
                    for (int i = 0; i < stats.size(); i++) {
                        if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
                            j = i;
                        }
                    }
                    topActivity = stats.get(j).getPackageName();
                }
                Log.i(TAG, "top running app is : " + topActivity);
            }
        }
    }
}
