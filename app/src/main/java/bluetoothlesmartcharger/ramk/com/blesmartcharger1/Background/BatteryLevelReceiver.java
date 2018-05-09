package bluetoothlesmartcharger.ramk.com.blesmartcharger1.Background;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;


import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.BluetoothConnection;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.Const;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.R;

/**
 * Created by Rana Abdul Manan on 9/9/2015.
 */
public class BatteryLevelReceiver extends BroadcastReceiver {

    //private static DeviceConnector connector;
    private int tryConnection = 0;

    private boolean checkAndSetHostAddress(Context context){
        if(Const.HOST_DEVICE_ADDRESS.equalsIgnoreCase("NULL")){

            SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_prefs_name), 1);
            final String host_mac = prefs.getString(context.getString(R.string.mac_address), "Not");

            if(!host_mac.equalsIgnoreCase("Not")){
                Const.HOST_DEVICE_ADDRESS = host_mac;
                return true;
            }else{
                return false;
            }
        }
        return true;
    }
    private void TryConnection(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(BatteryLevelService.get_instance()==null){
                    if(tryConnection<4){
                        TryConnection();
                    }
                    tryConnection++;
                    Log.e("Receiver","Try Connection times : "+tryConnection);
                }else{
                    BatteryLevelService.get_instance().setFromBackground(true);
                }
            }
        },500);
    }
    @Override
    public void onReceive(final Context context, Intent batteryStatus) {

//        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_prefs_name), 1);

       // if(prefs.getBoolean("runInBackground",true)){



        if (BatteryLevelService.get_instance() != null) {
            BatteryLevelService.get_instance().stopSelf();
            BluetoothConnection.get_instance().stopConnection();
            if(BluetoothAdapter.getDefaultAdapter().isEnabled()){
                BluetoothAdapter.getDefaultAdapter().disable();
            }
            try {
                Thread.sleep(500);
            }catch (Exception ex){

            }
        }

         Intent serviceIntent = new Intent(context, BatteryLevelService.class);
         context.startService(serviceIntent);
         TryConnection();







        //}




        /*


        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = (int) ((level / (float) scale) * 100);

        String chargingMsg = "Battery Level : "+batteryPct+"%\r\n IsCharging : "+isCharging+"\r\n Charged PluggedIn : "+chargePlug+"\r\n usbCharging : "+usbCharge+"\r\n AcCharging : "+acCharge;

        Toast.makeText(context, chargingMsg, Toast.LENGTH_LONG).show();

        if(DeviceControlActivity.get_instace()!=null){
            DeviceControlActivity.get_instace().setBatteryInfo(isCharging,batteryPct);
        }

        if (!isConnected()) {
            SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_prefs_name), 1);
            String address = prefs.getString(context.getString(R.string.mac_address), "null");
            if (!address.equalsIgnoreCase("null")) {
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                if (btAdapter!=null) {
                    if (!btAdapter.isEnabled())
                        btAdapter.enable();

                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
                    setupConnector(device, context);
                }
            }
        }

        if(isConnected()) {
            if (batteryPct < 81) {
                sendCommand("a", context);
            } else if (batteryPct == 100) {
                sendCommand("b", context);
            }
        }
        stopConnection();*/

    }
    /*
    private void setupConnector(BluetoothDevice connectedDevice, Context context) {
        stopConnection();
        try {
            String emptyName = context.getString(R.string.empty_device_name);
            DeviceData data = new DeviceData(connectedDevice, emptyName);
            connector = new DeviceConnector(data, new BluetoothResponseHandler(context));
            connector.connect();
        } catch (IllegalArgumentException e) {
            Utils.log("setupConnector failed: " + e.getMessage());
        }
    }
    public void sendCommand(String commandString,Context context){

        final String mode = Utils.getPrefence(context, context.getString(R.string.pref_commands_mode));
        boolean hexMode = mode.equals("HEX");
        String command_ending = getCommandEnding(context);
        if(commandString!=null && commandString.trim().length()>0){
            commandString = commandString+"\r\n";
            byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
            if (command_ending != null) command = Utils.concat(command, command_ending.getBytes());
            if (isConnected()) {
                connector.write(command);
            }

        }

    }
    private boolean isConnected() {
        return (connector != null) && (connector.getState() == DeviceConnector.STATE_CONNECTED);
    }

    private void stopConnection() {
        if (connector != null) {
            connector.stop();
            connector = null;

        }
    }

    private String getCommandEnding(Context context) {
        String result = Utils.getPrefence(context, context.getString(R.string.pref_commands_ending));
        if (result.equals("\\r\\n")) result = "\r\n";
        else if (result.equals("\\n")) result = "\n";
        else if (result.equals("\\r")) result = "\r";
        else result = "";
        return result;
    }

    private static class BluetoothResponseHandler extends Handler {
        private WeakReference<Context> mActivity;

        public BluetoothResponseHandler(Context context) {
            mActivity = new WeakReference<Context>(context);
        }

        public void setTarget(Context target) {
            mActivity.clear();
            mActivity = new WeakReference<Context>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            Context activity = mActivity.get();
            if (activity != null) {

            }
        }
    }*/
}


