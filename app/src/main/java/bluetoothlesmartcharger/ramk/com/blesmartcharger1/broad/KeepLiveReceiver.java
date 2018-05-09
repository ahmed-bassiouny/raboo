package bluetoothlesmartcharger.ramk.com.blesmartcharger1.broad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon.AppManager;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon.KeepLiveActivity;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon.Service1;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.L;

/**
 * Created by Administrator on 2017/9/3 0003.
 */

public class KeepLiveReceiver extends BroadcastReceiver {
    private static final String TAG = "KeepLiveReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        L.i(TAG,"onReceive  " + action);
        if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            L.w(TAG,"关闭屏幕 off， " + action);
            Intent intent1 = new Intent(context, KeepLiveActivity.class);
//            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            context.startActivity(intent1);
        } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
            L.w(TAG,"PRESENT  解锁 " + action);
            AppManager.getAppManager().finishActivity();
        }else if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
            L.w(TAG,"ACTION_BOOT_COMPLETED   开机自启动 " + action);
            Intent service = new Intent(context, Service1.class);
            context.startService(service);
        }else if (action.equals(Intent.ACTION_SCREEN_ON)){
            L.w(TAG,"ACTION_SCREEN_ON  亮屏幕 "+action);
            AppManager.getAppManager().finishActivity();
        }
    }
}
