package com.example.sugarsync;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class GlucoseEntry implements Parcelable {
    private long timestamp;
    private float glucoseLevel;

    public GlucoseEntry(long timestamp, float glucoseLevel) {
        this.timestamp = timestamp;
        this.glucoseLevel = glucoseLevel;
    }

    protected GlucoseEntry(Parcel in) {
        timestamp = in.readLong();
        glucoseLevel = in.readFloat();
    }

    public static final Creator<GlucoseEntry> CREATOR = new Creator<GlucoseEntry>() {
        @Override
        public GlucoseEntry createFromParcel(Parcel in) {
            return new GlucoseEntry(in);
        }

        @Override
        public GlucoseEntry[] newArray(int size) {
            return new GlucoseEntry[size];
        }
    };

    public long getTimestamp() {
        return timestamp;
    }

    public float getGlucoseLevel() {
        return glucoseLevel;
    }

    public void setGlucoseLevel(float glucoseLevel) {
        this.glucoseLevel = glucoseLevel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeFloat(glucoseLevel);
    }
}

