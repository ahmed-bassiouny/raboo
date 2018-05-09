package bluetoothlesmartcharger.ramk.com.blesmartcharger1;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/9/10 0010.
 */

public class BleComm implements Parcelable {
    private String address;
    private byte[] data;
    private int type;

    public BleComm(String address, byte[] data, int type) {
        this.address = address;
        this.data = data;
        this.type = type;
    }

    protected BleComm(Parcel in) {
        address = in.readString();
        type = in.readInt();
        //如果数组长度大于0，那么就读数组， 所有数组的操作都可以这样。
        data = in.createByteArray();
//        int length = in.readInt();
//        if (length>0){
//            data = new byte[length];
//            in.readByteArray(data);
//        }
    }

    public static final Creator<BleComm> CREATOR = new Creator<BleComm>() {
        @Override
        public BleComm createFromParcel(Parcel in) {
            return new BleComm(in);
        }

        @Override
        public BleComm[] newArray(int size) {
            return new BleComm[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeInt(type);
        dest.writeByteArray(data);
        //这几句话是写数组的,因为数组的长度不确定，所以先确定数组长度，如果为空就不写，但是要把0给发过去
        //让下面的好判断能不能读数组，也就是说下面如果读到的长度是0，那么就不读数组了，否则就创建相同长度的数组去读
//        if (null == data){
//            dest.writeInt(0);
//        }else {
//            dest.writeInt(data.length);
//        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
