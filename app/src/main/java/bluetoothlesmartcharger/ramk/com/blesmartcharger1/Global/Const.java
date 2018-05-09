package bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global;

/**
 * Created by Rana Abdul Manan on 10/21/2015.
 */
public class Const {
    public static final String TAG = "SPP_TERMINAL";
    public static int discharging = 100;
    public static int charging = 80;


    public static Boolean isCharging = null;
    public static int current_level = 0;

    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;

    public static String DEVICE_NAME = "device_name";

    public static final String TOAST = "toast";

    public static boolean D = true;

    public static String HOST_DEVICE_ADDRESS = "NULL";
    public static String CHANGEED_DEVICE_NAME = "changeed_device_name";

    public final static String CHANGEED_DEVICE_NAME_BROADCASTER =
            "com.example.bluetooth.le.CHANGEED_DEVICE_NAME";

    public final static String READ_DEVICE_NAME_BROADCASTER =
            "com.example.bluetooth.le.READ_DEVICE_NAME_BROADCASTER";


    public static int current_charging = 0;
    public static String START_CHARGE_VALUE = "start_charge_value";



}
