package bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global;
/**
 *
 * @author DIM
 * @date 2015/06/03
 * @version V1.0
 */
public class ConvertHelper {

	/**
	 * byte 数组转换为十六进制的字符串
	 *
	 * @param b
	 *            输入需要转换的byte数组
	 * @return 返回十六进制 字符串
	 */
	public final static String byte2Hex(byte[] b) {
		char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] newChar = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			newChar[2 * i] = hex[(b[i] & 0xf0) >> 4];
			newChar[2 * i + 1] = hex[b[i] & 0xf];

		}
		String str = new String(newChar);
		return str;
	}

	/**
	 * byte 数组转换为十六进制的字符串
	 *
	 * @param b
	 *            输入需要转换的byte数组
	 * @return 返回十六进制 字符串 101020203030--->10,10,20,20,30,30
	 */
	public final static String byte2HexForShow(byte[] b) {
		char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] newChar = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			newChar[2 * i] = hex[(b[i] & 0xf0) >> 4];
			newChar[2 * i + 1] = hex[b[i] & 0xf];

		}
		String str = spilt2wordstr(new String(newChar), ",");
		return str;
	}

	/**
	 * @param b
	 * @return 101020203030[全部20个字节]--->10 10 20 20 30 30
	 */
	public final static String byte2HexForHardware(byte[] b) {
		char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] newChar = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			newChar[2 * i] = hex[(b[i] & 0xf0) >> 4];
			newChar[2 * i + 1] = hex[b[i] & 0xf];

		}
		String str = spilt2wordstr(new String(newChar), " ");
		return str;
	}

	/**
	 * 用于显示16位的ADC的实时波形
	 *
	 * @param b
	 * @return 101020203030-->int[4112,8224,12336]
	 */
	public final static int[] byte2HexForAdcTranslateIntArr(byte[] b) {
		int rateViewArr[] = new int[8];
		String strArr[] = ConvertHelper.byte2HexToStrArr(b);
		for (int i = 0; i < rateViewArr.length; i++) {
			// 低位在前，高位在后
			String s = strArr[i * 2 + 1] + strArr[i * 2];
			rateViewArr[i] = Integer.valueOf(s, 16);
		}
		return rateViewArr;
	}

	/**
	 * @param b
	 * @return 101020203030--->0x10,0x10,0x20,0x20,0x30,0x30
	 */
	public final static String byte2HexForHardwareDebug(byte[] b) {
		char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] newChar = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			newChar[2 * i] = hex[(b[i] & 0xf0) >> 4];
			newChar[2 * i + 1] = hex[b[i] & 0xf];

		}
		String str = spilt2wordstr(new String(newChar), ",", "0x");
		return str;
	}

	public final static String byte2HexForHardwareDebugDaily(byte[] b) {
		char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] newChar = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			newChar[2 * i] = hex[(b[i] & 0xf0) >> 4];
			newChar[2 * i + 1] = hex[b[i] & 0xf];

		}
		String str = spilt2wordDaily(new String(newChar), ",", "0x");
		return str;
	}

	public final static int hexStrToInt(String str) {
		int value = Integer.valueOf(str, 16);
		return value;
	}

	/**
	 * byte 数组转换为十进制的字符串
	 *
	 * @param value
	 *            输入需要转换的byte数组
	 * @return 返回十进制 字符串
	 */
	public final static String byte2HexToIntForShow(byte[] value) {
		String[] str = byte2HexToStrArr(value);
		int data[] = new int[str.length];

		for (int i = 0; i < data.length; i++) {
			data[i] = Integer.valueOf(str[i], 16);
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			sb.append(data[i] + ",");
		}
		return sb.toString();
	}

	/**
	 * byte 数组转换为int的数组
	 *
	 * @param value
	 *            输入需要转换的byte数组
	 * @return 返回十六进制 字符串
	 */
	public final static int[] byte2HexToIntArr(byte[] value) {
		String[] str = byte2HexToStrArr(value);
		int data[] = new int[str.length];

		for (int i = 0; i < data.length; i++) {
			data[i] = Integer.valueOf(str[i], 16);
		}
		return data;
	}

	/**
	 * 16进制 数组转换为int的数组
	 *
	 * @param str
	 *            输入需要转换的byte数组
	 * @return 返回十六进制 字符串
	 */
	public final static int[] HexStrArrToIntArr(String[] str) {
		int data[] = new int[str.length];

		for (int i = 0; i < data.length; i++) {
			data[i] = Integer.valueOf(str[i], 16);
		}
		return data;
	}

	/**
	 * byte 数组转换为十六进制的字符串数组
	 *
	 * @param b
	 *            输入需要转换的byte数组
	 * @return 返回十六进制 字符串
	 */
	public final static String[] byte2HexToStrArr(byte[] b) {
		char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] newChar = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			newChar[2 * i] = hex[(b[i] & 0xf0) >> 4];
			newChar[2 * i + 1] = hex[b[i] & 0xf];

		}
		String str[] = spilt2word(new String(newChar));
		return str;
	}

	/**
	 *
	 * @param hexString
	 * @return 将十六进制转换为字节数组
	 */
	public static byte[] HexStringToBinary(String hexString) {
		if (hexString != null) {
			byte[] result = new byte[hexString.length() / 2];
			for (int i = 0; i < hexString.length() / 2; ++i)
				result[i] = (byte) (Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16) & 0xff);
			return result;
		} else
			return null;
	}

	/**
	 * 将字符串两两分割
	 *
	 * @param str
	 * @return
	 */
	public static String[] spilt2word(String str) {
		int m = str.length() / 2;
		if (m * 2 < str.length()) {
			m++;
		}
		String[] strs = new String[m];
		int j = 0;
		for (int i = 0; i < str.length(); i++) {
			if (i % 2 == 0) {
				strs[j] = str.charAt(i) + "";
			} else {
				strs[j] = strs[j] + str.charAt(i);
				j++;
			}
		}
		return strs;
	}

	/**
	 * 将字符串两两分割
	 *
	 * @param str
	 * @return
	 */
	public static String spilt2wordstr(String str, String rex) {
		int m = str.length() / 2;
		if (m * 2 < str.length()) {
			m++;
		}
		String[] strs = new String[m];
		int j = 0;
		for (int i = 0; i < str.length(); i++) {
			if (i % 2 == 0) {
				strs[j] = str.charAt(i) + "";
			} else {
				strs[j] = strs[j] + str.charAt(i);
				j++;
			}
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			sb.append(strs[i] + rex);
		}
		return (new String(sb.toString()));
	}

	/**
	 * 将字符串两两分割
	 *
	 * @param str
	 * @return
	 */
	public static String spilt2wordDaily(String str, String rex, String head) {
		int m = str.length() / 2;
		if (m * 2 < str.length()) {
			m++;
		}
		String[] strs = new String[m];
		int j = 0;
		for (int i = 0; i < str.length(); i++) {
			if (i % 2 == 0) {
				strs[j] = str.charAt(i) + "";
			} else {
				strs[j] = strs[j] + str.charAt(i);
				j++;
			}
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 3; i < strs.length - 1; i++) {
			sb.append(head + strs[i] + rex);
		}
		return (new String(sb.toString()));
	}

	/**
	 * 将字符串两两分割
	 *
	 * @param str
	 * @return
	 */
	public static String spilt2wordstr(String str, String rex, String head) {
		int m = str.length() / 2;
		if (m * 2 < str.length()) {
			m++;
		}
		String[] strs = new String[m];
		int j = 0;
		for (int i = 0; i < str.length(); i++) {
			if (i % 2 == 0) {
				strs[j] = str.charAt(i) + "";
			} else {
				strs[j] = strs[j] + str.charAt(i);
				j++;
			}
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			sb.append(head + strs[i] + rex);
		}
		return (new String(sb.toString()));
	}



	// 1234转为0x00,0x00,0x04,0xD2
	public static byte[] intToBytes(int res) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (res >>> (24 - i * 8));
		}

		return b;
	}

	public static byte loUint16(short v) {
		return (byte) (v & 0xFF);
	}

	public static byte hiUint16(short v) {
		return (byte) (v >> 8);
	}

}
