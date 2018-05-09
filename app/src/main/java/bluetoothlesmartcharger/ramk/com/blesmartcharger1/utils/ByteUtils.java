package bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils;

/**
 * Created by Administrator on 2017/8/7 0007.
 */

public class ByteUtils {
    public static String aaa(String a){
        String aa = null;
        int xx =  Integer.parseInt(a, 16) ^ 0xa3;
        if (xx<16){
            aa ="0"+ Integer.toHexString(Integer.parseInt(a, 16) ^ 0xa3).toUpperCase();
        }else {
            aa = Integer.toHexString(Integer.parseInt(a, 16) ^ 0xa3).toUpperCase();
        }
        return aa;
    }

    public static byte[] intToByte(String p){
        int i = Integer.parseInt(p);
        byte[] b = new byte[3];
        b[0] = (byte)((i >> 16) & 0xFF);
        b[1] = (byte)((i >> 8) & 0xFF);
        b[2] = (byte)(i & 0xFF);
        return b;
    }

    /**
     * 高位在前
     * @param i
     * @return
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    /**
     * 十进制转16进制
     * @param data
     * @return
     */
    public static String byteTo16String(byte data){
        int d = data & 0xff;
        if (d>=10){
            return "0x"+ Integer.toHexString((data & 0xff));
        }else {
            return "0x0"+ Integer.toHexString(d);
        }
    }
    public static String byteArrayTo16String(byte[] data){
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i=0; i<data.length;i++){
            builder.append(byteTo16String(data[i]));
            if (i<data.length-1)
              builder.append(",");
        }
         builder.append("]");
        return builder.toString();
    }


}
