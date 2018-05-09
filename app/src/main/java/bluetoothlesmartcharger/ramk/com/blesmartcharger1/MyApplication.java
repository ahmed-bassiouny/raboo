package bluetoothlesmartcharger.ramk.com.blesmartcharger1;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.marswin89.marsdaemon.DaemonClient;
import com.marswin89.marsdaemon.DaemonConfigurations;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

import java.util.List;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.CustomLogAdapter;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.UI.MainActivity;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon.Receiver1;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon.Receiver2;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon.Service1;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.daemon.Service2;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.L;

/**
 * Created by TimAimee on 2017/5/19.
 */

public class MyApplication extends Application {
    private DaemonClient mDaemonClient;

    @Override
    public void onCreate() {
        super.onCreate();
        initLog();
//        getRunApp();
        registerActivity();
    }

    private void initLog() {
        Logger
                .init("timaimee")       //全局Tag  .init("applicationName")
                .methodCount(0)                 // 方法条数的显示 默认 2
                .hideThreadInfo()               // 线程信息的显示 默认 显示
                .logLevel(LogLevel.FULL)        // 调 试/发布 Loglevel.FULL/Loglevel.NONE 默认 Loglevel.FULL
                // 方法偏移的大小 默认 0(不是很明白)
                .logAdapter(new CustomLogAdapter()); //Log的适配器 默认 AndroidLogAdapter，可以继承LogAdapter自定义
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mDaemonClient = new DaemonClient(createDaemonConfigurations());
        mDaemonClient.onAttachBaseContext(base);
    }
    private DaemonConfigurations createDaemonConfigurations(){
        DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
                "bluetoothlesmartcharger.ramk.com.blesmartcharger1:process1",
                Service1.class.getCanonicalName(),
                Receiver1.class.getCanonicalName());
        DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
                "bluetoothlesmartcharger.ramk.com.blesmartcharger1:process2",
                Service2.class.getCanonicalName(),
                Receiver2.class.getCanonicalName());
        DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
        //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
        return new DaemonConfigurations(configuration1, configuration2, listener);
    }

    class MyDaemonListener implements DaemonConfigurations.DaemonListener{
        @Override
        public void onPersistentStart(Context context) {
        }

        @Override
        public void onDaemonAssistantStart(Context context) {
        }

        @Override
        public void onWatchDaemonDaed() {
        }
    }
    //当后台程序已经终止资源还匮乏时会调用这个方法,这个时候就可以去干掉其他应用了，o(∩_∩)o 哈哈
    @Override
    public void onLowMemory() {



        super.onLowMemory();
    }

    private void getRunApp(){
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info :infos ){
            L.i("APP",info.processName + " ; " + info.uid + " ; " + info.pid );
        }

    }
    //检测用户是否对本app开启了“Apps with usage access”权限
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private int count = 0;
    private void registerActivity() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (count == 0) {
                    L.v("Master", ">>>>>>切到前台 " + count + " , " + activity.getClass().getName());
                    if (activity instanceof MainActivity){
                        ((MainActivity) activity).setIsStart(true);
                    }
                }
                count++;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                count--;
                if (count == 0) {
                    L.v("Master", ">>>>>>>切到后台 " + count + " , " + activity.getClass().getName());
                    if (activity instanceof MainActivity){
                        ((MainActivity) activity).setIsStart(false);
                    }
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }


            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }


}
