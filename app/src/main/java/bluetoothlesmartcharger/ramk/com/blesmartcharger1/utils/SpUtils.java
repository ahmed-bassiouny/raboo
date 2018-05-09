package bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by Administrator on 2017/8/8 0008.
 */

public class SpUtils {
    private final static String name = "raboo";

    public final static String SETTING = "setting";


    private final static int mode = Context.MODE_PRIVATE;
    public final static void putBoolean(Context context, String key, boolean value){
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }
    public final static boolean getContainsKey(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        if (sp.contains(key)) { // 检查是否存在该值，不必每次都通过反射来检查
            return true;
        }
        return false;
    }


    public final static void putString(Context context, String key, String value){
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }
    public final static void putLong(Context context, String key, long l){
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        edit.putLong(key,l);
        edit.commit();
    }
    public final static void putInt(Context context, String key, int l){
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        edit.putInt(key,l);
        edit.commit();
    }
    public final static int getInt(Context context, String key, int defValue){
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp.getInt(key,defValue);
    }
    public final static long getLong(Context context, String key, long defValue){
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp.getLong(key,defValue);
    }
    public final static boolean getBoolean(Context context, String key, boolean defValue){
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp.getBoolean(key, defValue);
    }

    public final static String getString(Context context, String key, String defValue){
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp.getString(key, defValue);
    }


}
