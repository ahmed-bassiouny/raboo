package bluetoothlesmartcharger.ramk.com.blesmartcharger1.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * Created by Administrator on 2017/6/28 0028.
 */

public interface BleDeviceListener {
    public void onConnected(String address);
    public void onDisConnected(String address);
    public void onServicesDiscovered(BluetoothGatt gatt, int status);
    public void onServicesFailed(BluetoothGatt gatt, int status);
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status);
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

}
