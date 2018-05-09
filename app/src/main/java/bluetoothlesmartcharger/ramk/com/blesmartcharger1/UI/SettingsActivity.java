package bluetoothlesmartcharger.ramk.com.blesmartcharger1.UI;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.BluetoothService.BluetoothLeService;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.BluetoothConnection;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.Const;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.SharedPreferencesUtil;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.R;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon.Service1;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.L;


/**
 * Created by sash0k on 29.11.13.
 * Настройки приложения
 */
@SuppressWarnings("deprecation")
public final class SettingsActivity extends AppCompatActivity {
    private final static String TAG = SettingsActivity.class.getSimpleName();
    private LinearLayout default_parent_layout, start_charging_layout, stop_charging_layout;
    private CheckBox default_setting;
    private TextView start_charging, stop_charging, start_charging_heading, stop_charging_heading;
    private Button setting_device_name, reading_device_name;
    private EditText device_name_ed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.charging_setting);

        registerBroadcast();


        default_parent_layout = (LinearLayout) findViewById(R.id.default_setting_parent);
        start_charging_layout = (LinearLayout) findViewById(R.id.start_charging_parent);
        stop_charging_layout = (LinearLayout) findViewById(R.id.stop_charging_parent);


        default_setting = (CheckBox) findViewById(R.id.default_setting);

        start_charging = (TextView) findViewById(R.id.start_charging);
        stop_charging = (TextView) findViewById(R.id.stop_charging);

        start_charging_heading = (TextView) findViewById(R.id.start_charging_heading);
        stop_charging_heading = (TextView) findViewById(R.id.stop_charging_heading);
        setting_device_name = (Button) findViewById(R.id.setting_device_name_but);
        reading_device_name = (Button) findViewById(R.id.setting_device_name_read);
        device_name_ed = (EditText) findViewById(R.id.setting_device_name_ed);


        SharedPreferences prefs = SettingsActivity.this.getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);

        if (prefs.getBoolean("default_setting", true)) {

            start_charging_heading.setEnabled(false);
            stop_charging_heading.setEnabled(false);

            start_charging.setEnabled(false);
            stop_charging.setEnabled(false);

        } else {
            start_charging.setEnabled(true);
            stop_charging.setEnabled(true);
            start_charging_heading.setEnabled(true);
            stop_charging_heading.setEnabled(true);

        }
        default_setting.setChecked(prefs.getBoolean("default_setting", true));
        int charge_value = prefs.getInt("charging", 80);
        int discharge_value = prefs.getInt("discharging", 100);

        start_charging.setText("on " + charge_value + "");
        stop_charging.setText("on " + discharge_value + "");
        callBackListeners();

        String deviceName = SharedPreferencesUtil.getString(SettingsActivity.this, Const.CHANGEED_DEVICE_NAME, "");
        device_name_ed.setText(deviceName);

        setDeviceName();
        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGattUpdateReceiver!= null){
            unregisterReceiver(mGattUpdateReceiver);
        }
    }

    private void registerBroadcast() {

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.CHANGEED_DEVICE_NAME_BROADCASTER);
        intentFilter.addAction(Const.READ_DEVICE_NAME_BROADCASTER);
        return intentFilter;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Const.CHANGEED_DEVICE_NAME_BROADCASTER)) {
                Toast.makeText(SettingsActivity.this, "change device name success", Toast.LENGTH_SHORT).show();
                SharedPreferencesUtil.saveString(SettingsActivity.this, Const.CHANGEED_DEVICE_NAME, device_name_ed.getText().toString());
            } else if (action.equals(Const.READ_DEVICE_NAME_BROADCASTER)) {
                Toast.makeText(SettingsActivity.this, "read device name success", Toast.LENGTH_SHORT).show();
                byte[] byteData = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA_BYTE);
                String lastName = SharedPreferencesUtil.getString(SettingsActivity.this, Const.CHANGEED_DEVICE_NAME, "");
                try {
                    lastName = new String(byteData, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                device_name_ed.setText(lastName);
                SharedPreferencesUtil.saveString(SettingsActivity.this, Const.CHANGEED_DEVICE_NAME, lastName);
            }
        }
    };


    private void callBackListeners() {

        default_setting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = SettingsActivity.this.getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = prefs.edit();
                if (default_setting.isChecked()) {

                    start_charging_heading.setEnabled(false);
                    stop_charging_heading.setEnabled(false);
                    start_charging.setEnabled(false);
                    stop_charging.setEnabled(false);
                    editor.putBoolean("default_setting", true);
                    broadcastBattery(Service1.ACTION_BATTERY_DATA_DEFAULT,true);
                } else {
                    start_charging_heading.setEnabled(true);
                    stop_charging_heading.setEnabled(true);
                    start_charging.setEnabled(true);
                    stop_charging.setEnabled(true);
                    editor.putBoolean("default_setting", false);
                    broadcastBattery(Service1.ACTION_BATTERY_DATA_DEFAULT,false);
                }
                editor.commit();
            }
        });

        //=========================================

        default_parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (default_setting.isChecked())
                    default_setting.setChecked(false);
                else
                    default_setting.setChecked(true);

                SharedPreferences prefs = SettingsActivity.this.getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = prefs.edit();
                if (default_setting.isChecked()) {

                    start_charging_heading.setEnabled(false);
                    stop_charging_heading.setEnabled(false);

                    editor.putBoolean("default_setting", true);
                    broadcastBattery(Service1.ACTION_BATTERY_DATA_DEFAULT,true);
                } else {
                    start_charging_heading.setEnabled(true);
                    stop_charging_heading.setEnabled(true);
                    broadcastBattery(Service1.ACTION_BATTERY_DATA_DEFAULT,false);
                    editor.putBoolean("default_setting", false);
                }
                editor.commit();

            }
        });


        //=========================================


        start_charging_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start_charging_heading.isEnabled())
                    slider_dialog(true);
            }
        });
        start_charging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slider_dialog(true);
            }
        });

        stop_charging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slider_dialog(false);
            }
        });
        stop_charging_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stop_charging_heading.isEnabled())
                    slider_dialog(false);
            }
        });

    }

    // ============================================================================

    private void slider_dialog(final boolean val) {

        LayoutInflater li = LayoutInflater.from(SettingsActivity.this);
        View promptsView = li.inflate(R.layout.slider, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final NumberPicker slider = (NumberPicker) promptsView
                .findViewById(R.id.startStopChargingValue);

        slider.setMaxValue(100);
        slider.setMinValue(0);

        if (val) {

            slider.setValue(Const.charging);

        } else {

            slider.setValue(Const.discharging);

        }

        slider.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (val) {

                    if (newVal >= Const.discharging) {
                        slider.setValue(oldVal);

                        Toast.makeText(SettingsActivity.this, "Start charging must be less than stop charging!!!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    if (newVal <= Const.charging) {
                        slider.setValue(oldVal);

                        Toast.makeText(SettingsActivity.this, "Stop charging must be greater than start charging!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if (val)
            alertDialogBuilder.setTitle("Reconnect");
        else
            alertDialogBuilder.setTitle("Disconnect");
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                SharedPreferences prefs = SettingsActivity.this.getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);

                                SharedPreferences.Editor editor = prefs.edit();
                                if (val) {
                                    Const.charging = slider.getValue();

                                    editor.putInt("charging", Const.charging);
                                    editor.commit();
                                    broadcastBattery(Service1.ACTION_BATTERY_DATA_CHARGING,"charging",Const.charging);
                                    start_charging.setText("on " + Const.charging);


                                } else {
                                    if (slider.getValue() > Const.charging) {
                                        Const.discharging = slider.getValue();

                                        editor.putInt("discharging", Const.discharging);
                                        editor.commit();
                                        broadcastBattery(Service1.ACTION_BATTERY_DATA_DISCHARGING,"discharging",Const.discharging);
                                        stop_charging.setText("on " + Const.discharging);
                                    }

                                }

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    /**
     * 设置设备名称
     */
    private void setDeviceName() {

        L.i(TAG,"修改设备名称");
        setting_device_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceName = device_name_ed.getText().toString();
                if (TextUtils.isEmpty(deviceName)) {
                    Toast.makeText(SettingsActivity.this, "the device name can't be null", Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] deviceNameBytes = deviceName.getBytes();
                if (deviceNameBytes.length > 12) {
                    Toast.makeText(SettingsActivity.this, "the device name too long,please  set it more shorter", Toast.LENGTH_SHORT).show();
                    return;

                }
                L.i(TAG,"deviceName :" + deviceName);
                if (BluetoothConnection.get_instance().isBluetoothConnected() && BluetoothConnection.get_instance().isSmartCharger()) {
                    BluetoothConnection.get_instance().changeDeviceName(deviceName);
                }

            }
        });

        reading_device_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BluetoothConnection.get_instance().isBluetoothConnected() && BluetoothConnection.get_instance().isSmartCharger()) {
                    BluetoothConnection.get_instance().readDeviceName();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                final Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // ============================================================================
    private void broadcastBattery(final String action,String key , int data ) {
        final Intent intent = new Intent(action);
//        charging = intent.getIntExtra("charging", 80);
        intent.putExtra(key,data);
        sendBroadcast(intent);
    }
    private void broadcastBattery(String action,  boolean data ) {
        Intent intent = new Intent(action);
        intent.putExtra("default",data);
        sendBroadcast(intent);
    }
}
