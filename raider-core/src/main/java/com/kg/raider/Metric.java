package com.kg.raider;

/**
 * User: kaveh
 * Date: 8/9/13
 * Time: 12:01 AM
 *
 * POJO representing a metric event
 */
public class Metric {

    /**
     * Any string can serve as a key. Would be
     * more intuitive if the key has a sensible
     * hierarchy which can be used for client
     * notifications
     *
     * For Example: "continent.country.state.metric"
     */
    private String key;
    private long date;
    private float value;

    public Metric(String key, long date, float value) {
        this.key = key;
        this.date = date;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public long getDate() {
        return date;
    }

    public float getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Metric metric = (Metric) o;

        if (date != metric.date) return false;
        if (Float.compare(metric.value, value) != 0) return false;
        if (!key.equals(metric.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (int) (date ^ (date >>> 32));
        result = 31 * result + (value != +0.0f ? Float.floatToIntBits(value) : 0);
        return result;
    }
}
