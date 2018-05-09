// SendCommand.aidl
package bluetoothlesmartcharger.ramk.com.blesmartcharger1;

// Declare any non-default types here with import statements
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.BleComm;

interface SendCommand {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);
                //连接设备
                void connect(String address);
                //断开设备
                void close();
                //写入数据
                void write(in BleComm command);
                //读取
                void read(in BleComm command);
                //开始扫描
                void startScan(int s);
                //停止扫描
                void stopScan();
                //读取信号强度
                void getRssi();
                void kill();
                //是否在前台
                void isStart(boolean b);

}
