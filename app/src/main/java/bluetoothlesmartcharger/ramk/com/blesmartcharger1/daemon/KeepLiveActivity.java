package bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.L;

/**
 * Created by Administrator on 2017/9/4 0004.
 * 监控手机锁屏解锁事件，在屏幕锁屏时启动1个像素的 Activity，
 * 在用户解锁时将 Activity 销毁掉。注意该 Activity 需设计成用户无感知。
 * 通过该方案，可以使进程的优先级在屏幕锁屏时间由4提升为最高优先级1，不过好像没啥卵用o(∩_∩)o 哈哈。
 */

public class KeepLiveActivity extends Activity {
    private static final String TAG = "KeepLiveActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.w(TAG,"keepLive ->onCreate");
        AppManager.getAppManager().addActivity(this);
        Window window = getWindow();
        window.setGravity(Gravity.LEFT| Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);

        Intent intent = new Intent(KeepLiveActivity.this,bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon.Service1.class);
        startService(intent);
        bindService(intent,aidlConnection,Context.BIND_AUTO_CREATE);
    }
    private final ServiceConnection aidlConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            L.i("aidl onServiceConnected... ");
            //客户端获取代理对象

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            L.i("aidl onServiceDisconnected... ");

        }
    };
    @Override
    protected void onDestroy() {
        L.w(TAG,"keepLive ->onDestroy");
//        Intent intent = new Intent(KeepLiveActivity.this,bluetoothlesmartcharger.ramk.com.blesmartcharger.daemon.Service1.class);
//        startService(intent);
        unbindService(aidlConnection);
        super.onDestroy();
    }
}
