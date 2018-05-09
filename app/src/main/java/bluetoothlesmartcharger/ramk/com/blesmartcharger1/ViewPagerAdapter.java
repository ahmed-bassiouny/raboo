package bluetoothlesmartcharger.ramk.com.blesmartcharger1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global.SharedPreferencesUtil;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.UI.MainActivity;

/**
 * Created by bassiouny on 07/05/18.
 */

public class ViewPagerAdapter extends PagerAdapter {
    private Activity context;
    private LayoutInflater layoutInflater;
    private Integer[] images = {R.drawable.tutorial_1,R.drawable.tutorial_2,R.drawable.tutorial_3};

    public ViewPagerAdapter(Activity context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.custom_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesUtil.saveBoolean(context,"is_new",false);
                context.startActivity(new Intent(context, MainActivity.class));
                context.finish();
            }
        });
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageResource(images[position]);
        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}

