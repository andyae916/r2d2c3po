package com.vokevr.simpleviewer.helpers;

public class SensorReadingStats {
    private static final String TAG = SensorReadingStats.class.getSimpleName();
    private int sampleBufSize;
    private int numAxes;
    private float[][] sampleBuf;
    private int writePos;
    private int samplesAdded;

    public SensorReadingStats(int sampleBufSize, int numAxes) {
        this.sampleBufSize = sampleBufSize;
        this.numAxes = numAxes;
        if(sampleBufSize < 1) {
            throw new IllegalArgumentException("sampleBufSize is invalid.");
        } else if(numAxes < 1) {
            throw new IllegalArgumentException("numAxes is invalid.");
        } else {
            this.sampleBuf = new float[sampleBufSize][numAxes];
        }
    }

    public void addSample(float[] values) {
        if(values.length < this.numAxes) {
            throw new IllegalArgumentException("values.length is less than # of axes.");
        } else {
            this.writePos = (this.writePos + 1) % this.sampleBufSize;

            for(int i = 0; i < this.numAxes; ++i) {
                this.sampleBuf[this.writePos][i] = values[i];
            }

            ++this.samplesAdded;
        }
    }

    public void reset() {
        this.samplesAdded = 0;
        this.writePos = 0;
    }

    public boolean statsAvailable() {
        return this.samplesAdded >= this.sampleBufSize;
    }

    float getAverage(int axis) {
        if(!this.statsAvailable()) {
            throw new IllegalStateException("Average not available. Not enough samples.");
        } else if(axis >= 0 && axis < this.numAxes) {
            float var4 = 0.0F;

            for(int i = 0; i < this.sampleBufSize; ++i) {
                var4 += this.sampleBuf[i][axis];
            }

            return var4 / (float)this.sampleBufSize;
        } else {
            int sum = this.numAxes - 1;
            throw new IllegalStateException((new StringBuilder(38)).append("axis must be between 0 and ").append(sum).toString());
        }
    }

    float getMaxAbsoluteDeviation(int axis) {
        if(axis >= 0 && axis < this.numAxes) {
            float var5 = this.getAverage(axis);
            float maxAbsDev = 0.0F;

            for(int i = 0; i < this.sampleBufSize; ++i) {
                maxAbsDev = Math.max(Math.abs(this.sampleBuf[i][axis] - var5), maxAbsDev);
            }

            return maxAbsDev;
        } else {
            int avg = this.numAxes - 1;
            throw new IllegalStateException((new StringBuilder(38)).append("axis must be between 0 and ").append(avg).toString());
        }
    }

    public float getMaxAbsoluteDeviation() {
        float maxAbsDev = 0.0F;

        for(int i = 0; i < this.numAxes; ++i) {
            maxAbsDev = Math.max(maxAbsDev, this.getMaxAbsoluteDeviation(i));
        }

        return maxAbsDev;
    }
}