package com.juzistar.m.biz.lbs;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.juzistar.m.R;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.TR;

/**
 * Created by lingminjun on 15/12/2.
 */
public class Location implements Parcelable {
    public String mAddress;
    public String mCity;
    public String mProvince;
    public String mCountry;
    public double mLatitude;
    public double mLongitude;

    public String getSimpleAddress() {
        String str = mAddress;
        if (TextUtils.isEmpty(str)) {return "";}

        int idx = str.lastIndexOf(Res.localized(R.string.district));
        if (idx >= 0 && idx < str.length()) {
            return str.substring(idx+1,str.length());
        }
        return str;
    }

    public Location() {
    }

    public Location(Parcel parcel) {
        // 反序列化 顺序要与序列化时相同
        mAddress = parcel.readString();
        mCity = parcel.readString();
        mProvince = parcel.readString();
        mCountry = parcel.readString();
        mLatitude = parcel.readDouble();
        mLongitude = parcel.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // 序列化
        dest.writeString(TR.string(mAddress));
        dest.writeString(TR.string(mCity));
        dest.writeString(TR.string(mProvince));
        dest.writeString(TR.string(mCountry));
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {

        @Override
        public Location createFromParcel(Parcel source) {
            // 反序列化 顺序要与序列化时相同
            return new Location(source);
        }

        @Override
        public Location[] newArray(int i) {
            return new Location[i];
        }
    };

}
