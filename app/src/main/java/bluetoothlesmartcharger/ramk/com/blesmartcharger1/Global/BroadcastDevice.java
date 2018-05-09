package bluetoothlesmartcharger.ramk.com.blesmartcharger1.Global;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/9/10.
 */

public class BroadcastDevice implements Parcelable {
    private String name;
    private String address;
    private int rssi;
    private byte[] scanRecord;

    public BroadcastDevice(String name, String address, int rssi, byte[] scanRecord) {
        this.name = name;
        this.address = address;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
    }

    protected BroadcastDevice(Parcel in) {
        name = in.readString();
        address = in.readString();
        rssi = in.readInt();
        scanRecord = in.createByteArray();
    }

    public static final Creator<BroadcastDevice> CREATOR = new Creator<BroadcastDevice>() {
        @Override
        public BroadcastDevice createFromParcel(Parcel in) {
            return new BroadcastDevice(in);
        }

        @Override
        public BroadcastDevice[] newArray(int size) {
            return new BroadcastDevice[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeInt(rssi);
        dest.writeByteArray(scanRecord);
    }
}
