package bluetoothlesmartcharger.ramk.com.blesmartcharger1.UI;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.R;


public class HelpActivity extends AppCompatActivity {

    TextView helpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);



       // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);
        helpText = (TextView)findViewById(R.id.app_use_help_text);

        String help = getString(R.string.app_help_text);

        SpannableString ss1=  new SpannableString(help);

        ss1.setSpan(new RelativeSizeSpan(2f),0,10,0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD),0,10,0);

        ss1.setSpan(new RelativeSizeSpan(1.2f),10,25,0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD),10,25,0);

        ss1.setSpan(new RelativeSizeSpan(1.2f),53,79,0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD),53,79,0);

        ss1.setSpan(new RelativeSizeSpan(1.8f),311,333,0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD),311,333,0);

        ss1.setSpan(new RelativeSizeSpan(1.2f),333,348,0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD),333,348,0);

        ss1.setSpan(new RelativeSizeSpan(1.2f),506,521,0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD),506,521,0);

        ss1.setSpan(new RelativeSizeSpan(1.2f),1281,1295,0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD),1281,1295,0);

        ss1.setSpan(new RelativeSizeSpan(1.2f),1393,1406,0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD),1393,1406,0);

        ss1.setSpan(new RelativeSizeSpan(1.2f),1478,1493,0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD),1478,1493,0);

        ss1.setSpan(new RelativeSizeSpan(1.2f),1528,1542,0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD),1528,1542,0);

        ss1.setSpan(new RelativeSizeSpan(1.2f), 1570, 1584, 0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD), 1570, 1584, 0);

        ss1.setSpan(new RelativeSizeSpan(1.2f), 2034, 2050, 0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD), 2034, 2050, 0);

        ss1.setSpan(new RelativeSizeSpan(1.2f),2075, 2102, 0);
        ss1.setSpan(new StyleSpan(Typeface.BOLD), 2075, 2102, 0);


        ss1.setSpan(new BackgroundColorSpan(Color.CYAN), ss1.length()-18, ss1.length()-1, 0);
        ss1.setSpan(new UnderlineSpan(), ss1.length()-18, ss1.length()-1, 0);


        ss1.setSpan(new BackgroundColorSpan(Color.CYAN), ss1.length()-58, ss1.length()-42, 0);
        ss1.setSpan(new URLSpan("http://www.rabootec.com"), ss1.length()-58, ss1.length()-42, 0);
        //17

        ClickableSpan clickableSpan = new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                // We display a Toast. You could do anything you want here.
                Intent link= new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.rabootec.com"));
                startActivity(link);

            }
        };
        ss1.setSpan(clickableSpan, ss1.length()-58, ss1.length()-42, 0);


        helpText.setText(ss1);
        helpText.setMovementMethod(LinkMovementMethod.getInstance());

        findViewById(R.id.help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

}
