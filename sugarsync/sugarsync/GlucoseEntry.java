package com.example.sugarsync;

public class GlucoseEntry {
    private long timestamp;
    private float glucoseLevel;

    public GlucoseEntry(long timestamp, float glucoseLevel) {
        this.timestamp = timestamp;
        this.glucoseLevel = glucoseLevel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getGlucoseLevel() {
        return glucoseLevel;
    }
}

