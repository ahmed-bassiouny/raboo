package bluetoothlesmartcharger.ramk.com.blesmartcharger1;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.Const;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.SharedPreferencesUtil;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.UI.MainActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i;
                boolean isNew = SharedPreferencesUtil.getBoolean(SplashActivity.this, "is_new", true);
                if(isNew){
                    i = new Intent(SplashActivity.this, ImageSliderActivity.class);
                }else {
                    i = new Intent(SplashActivity.this, MainActivity.class);
                }
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
