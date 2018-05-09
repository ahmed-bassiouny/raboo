package bluetoothlesmartcharger.ramk.com.blesmartcharger1.ble;

/**
 * Created by Administrator on 2017/6/28 0028.
 */

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import bluetoothlesmartcharger.ramk.com.blesmartcharger1.Background.SampleGattAttributes;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.ByteUtils;
import bluetoothlesmartcharger.ramk.com.blesmartcharger1.utils.L;


/**
 * 低功耗设备，连接，读写
 */
public class Peripheral extends BluetoothGattCallback {
    public final static UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    //    public final static UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
//    public final static UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUIDHelper.uuidFromString("2902");
    public final static UUID UUID_HM_RX_TX =
            UUID.fromString(SampleGattAttributes.HM_RX_TX);
    private static final String TAG = "Peripheral";
    private BluetoothGatt mGatt;
    private BluetoothDevice mDevice;
    private int mRssi;
    private byte[] scanbyte;
    private boolean isConnect = false; //是否连接
    private boolean connecting = false; //正在连接
    private boolean bleProcessing;

    private Context mContext;
    private BleDeviceListener listener;

    private ConcurrentLinkedQueue<BLECommand> commandQueue = new ConcurrentLinkedQueue<BLECommand>();

    public Peripheral(Context context, BluetoothDevice device, int rssi, byte[] scanbyte) {
        this.mDevice = device;
        this.mRssi = rssi;
        this.scanbyte = scanbyte;
        this.mContext = context;
    }

    public void connect() {
        if (isConnect) {
            return;
        }
        connecting = true;
        logv("connect......." + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT <= 23) {
            mGatt = mDevice.connectGatt(mContext, false, this);
        } else {
//            mGatt = mDevice.connectGatt(mContext, false, this);
//            mGatt = mDevice.connectGatt(mContext, false, this);
            mGatt = mDevice.connectGatt(mContext, false, this, BluetoothDevice.TRANSPORT_LE);
        }

    }

    public void disConnect() {
        if ( mGatt !=null) {
            L.i(TAG, "close");
//            mGatt.disconnect();
            mGatt.close();
            isConnect = false;
        }
    }

    public String getAddress() {
        return mDevice.getAddress();
    }

    public void setBleDeviceListener(BleDeviceListener l) {
        if (null != l) {
            this.listener = l;
        }
    }

    public void removeBleDeviceListener() {
        if (null != listener)
            this.listener = null;
    }

    public boolean isConnect() {
        return isConnect;
    }


    public BluetoothGatt getBluetoothGatt() {
        return mGatt;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        logi("连接状态 2    status=" + status + " ,newState=" + newState);
        this.mGatt = gatt;
        if (newState == BluetoothGatt.STATE_CONNECTED) {
            isConnect = true;
            if (null != listener) {
                listener.onConnected(getAddress());
            }
            mGatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            isConnect = false;
            if (null != listener) {
                listener.onDisConnected(getAddress());
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        this.mGatt = gatt;
        if (status == BluetoothGatt.GATT_SUCCESS) {
//            L.i(TAG, "发现蓝牙服务成功 " +status);
            isConnect = true;
            if (null != listener) {
                listener.onServicesDiscovered(gatt, status);
            }
//            for (BluetoothGattService service : gatt.getServices() ){
//                L.i(TAG, "service:" + service.getUuid());
//                for (BluetoothGattCharacteristic characteristic :service.getCharacteristics()){
//                    L.i(TAG, "characteristic:" + characteristic.getUuid());
//                    queueRegisterNotifyCallback(service.getUuid(),characteristic.getUuid());
//                 }
//            }

        } else {
            L.i(TAG, "发现蓝牙服务失败 " + status);
            isConnect = false;
            if (null != listener) {
                listener.onServicesFailed(gatt, status);
            }
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        L.w(TAG, "onCharacteristicRead" + Arrays.toString(characteristic.getValue()));
        if (null != listener) {
            listener.onCharacteristicRead(gatt, characteristic, status);
        }
        commandCompleted();
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        L.w(TAG, "onCharacteristicWrite = " + ByteUtils.byteArrayTo16String(characteristic.getValue()));
        if (null != listener) {
            listener.onCharacteristicWrite(gatt, characteristic, status);
        }
        commandCompleted();
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        L.w(TAG, "onCharacteristicChanged2 = " + ByteUtils.byteArrayTo16String(characteristic.getValue()));
        if (null != listener) {
            listener.onCharacteristicChanged(gatt, characteristic);
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        L.w(TAG, "onDescriptorRead" + Arrays.toString(descriptor.getValue()));
        commandCompleted();

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        L.w(TAG, "onDescriptorWrite" + Arrays.toString(descriptor.getValue()) + " UUId = " + descriptor.getUuid());
        if (null != listener) {
            listener.onDescriptorWrite(gatt, descriptor, status);
        }
        commandCompleted();

    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
        L.w(TAG, "onReliableWriteCompleted ,status=" + status);

        commandCompleted();
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        L.i(TAG, "onReadRemoteRssi ,rssi=" + rssi + " ,status = " + status);
        if (null != listener) {
            listener.onReadRemoteRssi(gatt, rssi, status);
        }
        commandCompleted();
    }

    // Update rssi and scanRecord.
    public void update(int rssi, byte[] scanRecord) {
        this.mRssi = rssi;
        this.scanbyte = scanRecord;
    }

    public void updateRssi(int rssi) {
        mRssi = rssi;
    }

    // This seems way too complicated
    private void registerNotifyCallback(UUID serviceUUID, UUID characteristicUUID) {
        boolean success = false;
        if (mGatt == null) {
            logi("BluetoothGatt is null");
            return;
        }
        BluetoothGattService service = mGatt.getService(serviceUUID);
        if( service ==null ){
            disConnect();
            return;
        }
        BluetoothGattCharacteristic characteristic = findNotifyCharacteristic(service, characteristicUUID);
//        String key = generateHashKey(serviceUUID, characteristic);
        if (characteristic != null) {
            int prop = characteristic.getProperties();
            if ((prop & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {  //具有通知属性
//            notificationCallbacks.put(key, callbackContext);
                logi("开启通知特正 ： " + characteristic.getUuid());
                if (mGatt.setCharacteristicNotification(characteristic, true)) {
                    logi("开启通知setCharacteristicNotification  ： " + characteristic.getUuid());
//                    if (UUID_HM_RX_TX.equals(characteristic.getUuid())) {
//                           logi( " UUID_HM_RX_TX = " + UUID_HM_RX_TX);
//                        try {
//                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                            mGatt.writeDescriptor(descriptor);
//                        } catch (Exception ex) {
//                            Log.e("Charactristics Error", ex.getMessage());
//                        }
//                    }

                    // Why doesn't setCharacteristicNotification write the descriptor?
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);
                    if (descriptor != null) {
                        // prefer notify over indicate
                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            L.w(TAG, "Characteristic ENABLE_NOTIFICATION_VALUE   {0x01, 0x00}");
                        } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                            L.w(TAG, "Characteristic ENABLE_INDICATION_VALUE  {0x02, 0x00}"  );
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        } else {
                            L.w(TAG, "Characteristic " + characteristicUUID + " does not have NOTIFY or INDICATE property set");
                        }
                        if (mGatt.writeDescriptor(descriptor)) {
                            success = true;
                            logw("开启服务成功：" + characteristicUUID);
                        } else {
                            logi("Failed to set client characteristic notification for " + characteristicUUID);
                        }

                    } else {
                        logi("Set notification failed for " + characteristicUUID);
                    }

                } else {
                    logi("Failed to register notification for " + characteristicUUID);
                }
            }
        } else {
            logi("Characteristic " + characteristicUUID + " not found");

        }

        if (!success) {
            commandCompleted();
        }
    }

    private void removeNotifyCallback(UUID serviceUUID, UUID characteristicUUID) {
        if (mGatt == null) {
            logi("BluetoothGatt is null");
            return;
        }
        BluetoothGattService service = mGatt.getService(serviceUUID);
        BluetoothGattCharacteristic characteristic = findNotifyCharacteristic(service, characteristicUUID);
//        String key = generateHashKey(serviceUUID, characteristic);

        if (characteristic != null) {
//            notificationCallbacks.remove(key);
            if (mGatt.setCharacteristicNotification(characteristic, false)) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    mGatt.writeDescriptor(descriptor);
                }
//                callbackContext.success();
            } else {
                // TODO we can probably ignore and return success anyway since we removed the notification callback
                logi("Failed to stop notification for " + characteristicUUID);
            }
        } else {
            logi("Characteristic " + characteristicUUID + " not found");
        }
        commandCompleted();
    }

    // Some devices reuse UUIDs across characteristics, so we can't use service.getCharacteristic(characteristicUUID)
    // instead check the UUID and properties for each characteristic in the service until we find the best match
    // This function prefers Notify over Indicate
    private BluetoothGattCharacteristic findNotifyCharacteristic(BluetoothGattService service, UUID characteristicUUID) {
        BluetoothGattCharacteristic characteristic = null;
        // Check for Notify first
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        if (characteristics != null && !characteristics.isEmpty()){
            for (BluetoothGattCharacteristic c : characteristics) {
                if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 && characteristicUUID.equals(c.getUuid())) {
                    characteristic = c;
                    break;
                }
            }
            if (characteristic != null) return characteristic;
            // If there wasn't Notify Characteristic, check for Indicate
            for (BluetoothGattCharacteristic c : characteristics) {
                if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0 && characteristicUUID.equals(c.getUuid())) {
                    characteristic = c;
                    break;
                }
            }

        }


        // As a last resort, try and find ANY characteristic with this UUID, even if it doesn't have the correct properties
        if (characteristic == null) {
            characteristic = service.getCharacteristic(characteristicUUID);
        }
        return characteristic;
    }

    private void readCharacteristic(UUID serviceUUID, UUID characteristicUUID) {
        boolean success = false;
        if (mGatt == null) {
            logi("BluetoothGatt is null");
            return;
        }
        BluetoothGattService service = mGatt.getService(serviceUUID);
        BluetoothGattCharacteristic characteristic = findReadableCharacteristic(service, characteristicUUID);
        if (characteristic == null) {
            logi("Characteristic " + characteristicUUID + " not found.");
        } else {
            if (mGatt.readCharacteristic(characteristic)) {
                success = true;
            } else {
                logi("Read failed");
            }
        }
        if (!success) {
            commandCompleted();
        }
    }

    private void readRSSI() {
        boolean success = false;
        if (mGatt == null) {
            logi("BluetoothGatt is null");
            return;
        }
        if (mGatt.readRemoteRssi()) {
            success = true;
        } else {
            logi("Read RSSI failed");
        }
        if (!success) {
            commandCompleted();
        }
    }

    // Some peripherals re-use UUIDs for multiple characteristics so we need to check the properties
    // and UUID of all characteristics instead of using service.getCharacteristic(characteristicUUID)
    private BluetoothGattCharacteristic findReadableCharacteristic(BluetoothGattService service, UUID characteristicUUID) {
        BluetoothGattCharacteristic characteristic = null;
        int read = BluetoothGattCharacteristic.PROPERTY_READ;
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        if (characteristics !=null && !characteristics.isEmpty()){
            for (BluetoothGattCharacteristic c : characteristics) {
                if ((c.getProperties() & read) != 0 && characteristicUUID.equals(c.getUuid())) {
                    characteristic = c;
                    break;
                }
            }
        }
        // As a last resort, try and find ANY characteristic with this UUID, even if it doesn't have the correct properties
        if (characteristic == null) {
            characteristic = service.getCharacteristic(characteristicUUID);
        }
        return characteristic;
    }

    private void writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] data, int writeType) {
        boolean success = false;
        if (mGatt == null) {
            logi("BluetoothGatt is null");
            return;
        }
        BluetoothGattService service = mGatt.getService(serviceUUID);
        if (service == null) {
            logi("service = null ");
            disConnect();
            return;
        }
        BluetoothGattCharacteristic characteristic = findWritableCharacteristic(service, characteristicUUID, writeType);
//        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            logw("Characteristic " + characteristicUUID + " not found.");
            disConnect();
        } else {
            characteristic.setValue(data);
            characteristic.setWriteType(writeType);
            if (mGatt.writeCharacteristic(characteristic)) {
                success = true;
            } else {
                logw("Write failed");
            }
        }
        logw("写入是否成功 " + success);
        if (!success) {
            commandCompleted();
        }
    }

    // Some peripherals re-use UUIDs for multiple characteristics so we need to check the properties
    // and UUID of all characteristics instead of using service.getCharacteristic(characteristicUUID)
    private BluetoothGattCharacteristic findWritableCharacteristic(BluetoothGattService service, UUID characteristicUUID, int writeType) {
        BluetoothGattCharacteristic characteristic = null;

        // get write property
        int writeProperty = BluetoothGattCharacteristic.PROPERTY_WRITE;
        if (writeType == BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) {
            writeProperty = BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
        }

        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        if (characteristics != null && !characteristics.isEmpty()) {
//            if (characteristic == null) {
//                characteristic = service.getCharacteristic(characteristicUUID);
//            }
//            return characteristic;
            for (BluetoothGattCharacteristic c : characteristics) {
                if ((c.getProperties() & writeProperty) != 0 && characteristicUUID.equals(c.getUuid())) {
                    characteristic = c;
                    break;
                }
            }
        }


        // As a last resort, try and find ANY characteristic with this UUID, even if it doesn't have the correct properties
        if (characteristic == null) {
            characteristic = service.getCharacteristic(characteristicUUID);
        }
        return characteristic;
    }

    public void queueRead(UUID serviceUUID, UUID characteristicUUID) {
        BLECommand command = new BLECommand(serviceUUID, characteristicUUID, BLECommand.READ);
        queueCommand(command);
    }

    public void queueWrite(UUID serviceUUID, UUID characteristicUUID, byte[] data, int writeType) {
        BLECommand command = new BLECommand(serviceUUID, characteristicUUID, data, writeType);
        queueCommand(command);
    }

    public void queueRegisterNotifyCallback(UUID serviceUUID, UUID characteristicUUID) {
        BLECommand command = new BLECommand(serviceUUID, characteristicUUID, BLECommand.REGISTER_NOTIFY);
        queueCommand(command);
    }

    public void queueRemoveNotifyCallback(UUID serviceUUID, UUID characteristicUUID) {
        BLECommand command = new BLECommand(serviceUUID, characteristicUUID, BLECommand.REMOVE_NOTIFY);
        queueCommand(command);
    }

    public void queueReadRSSI() {
        BLECommand command = new BLECommand(null, null, BLECommand.READ_RSSI);
        queueCommand(command);
    }

    // add a new command to the queue
    private void queueCommand(BLECommand command) {
        L.d(TAG, "Queuing Command " + command.getType());
        commandQueue.add(command);
        if (!bleProcessing) {
            processCommands();
        }
    }

    // command finished, queue the next command
    private void commandCompleted() {
        L.d(TAG, "Processing Complete");
        bleProcessing = false;
        processCommands();
    }

    // process the queue
    private void processCommands() {
        L.d(TAG, "Processing Commands");
        if (bleProcessing) {
            return;
        }
        BLECommand command = commandQueue.poll();
        if (command != null) {
            if (command.getType() == BLECommand.READ) {
                L.d(TAG, "Read " + command.getCharacteristicUUID());
                bleProcessing = true;
                readCharacteristic(command.getServiceUUID(), command.getCharacteristicUUID());
            } else if (command.getType() == BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) {
                L.d(TAG, "Write " + command.getCharacteristicUUID());
                bleProcessing = true;
                writeCharacteristic(command.getServiceUUID(), command.getCharacteristicUUID(), command.getData(), command.getType());
            } else if (command.getType() == BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) {
                L.d(TAG, "Write No Response " + command.getCharacteristicUUID());
                bleProcessing = true;
                writeCharacteristic(command.getServiceUUID(), command.getCharacteristicUUID(), command.getData(), command.getType());
            } else if (command.getType() == BLECommand.REGISTER_NOTIFY) {
                L.d(TAG, "Register Notify " + command.getCharacteristicUUID());
                bleProcessing = true;
                registerNotifyCallback(command.getServiceUUID(), command.getCharacteristicUUID());
            } else if (command.getType() == BLECommand.REMOVE_NOTIFY) {
                L.d(TAG, "Remove Notify " + command.getCharacteristicUUID());
                bleProcessing = true;
                removeNotifyCallback(command.getServiceUUID(), command.getCharacteristicUUID());
            } else if (command.getType() == BLECommand.READ_RSSI) {
                L.d(TAG, "Read RSSI");
                bleProcessing = true;
                readRSSI();
            } else {
                // this shouldn't happen
                throw new RuntimeException("Unexpected BLE Command type " + command.getType());
            }
        } else {
            L.d(TAG, "Command Queue is empty.");
        }
    }

    private String generateHashKey(BluetoothGattCharacteristic characteristic) {
        return generateHashKey(characteristic.getService().getUuid(), characteristic);
    }

    private String generateHashKey(UUID serviceUUID, BluetoothGattCharacteristic characteristic) {
        return String.valueOf(serviceUUID) + "|" + characteristic.getUuid() + "|" + characteristic.getInstanceId();
    }

    private void logi(String str) {
        L.i(TAG, str);
    }

    private void logw(String str) {
        L.w(TAG, str);
    }

    private void logv(String str) {
        L.v(TAG, str);
    }

}
