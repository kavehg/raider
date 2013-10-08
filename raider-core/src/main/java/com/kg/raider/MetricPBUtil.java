package com.kg.raider;

import com.kg.raider.pb.MetricPB;

/**
 * User: kaveh
 * Date: 8/14/13
 * Time: 8:50 PM
 */
public class MetricPBUtil {

    /**
     * convert a metric PB to a pojo metric
     *
     * @param metric protocol buffer version of metric
     * @return metric pojo
     */
    public static Metric fromProto(MetricPB metric) {
        String key = metric.getKey();
        long timestamp = metric.getTimestamp();
        float value = metric.getValue();

        return new Metric(key, timestamp, value);
    }

    /**
     * convert pojo metric to PB metric
     *
     * @param metric pojo
     * @return protocol buffer pojo
     */
    public static MetricPB toProto(Metric metric) {
        return MetricPB.newBuilder()
                .setKey(metric.getKey())
                .setTimestamp(metric.getDate())
                .setValue(metric.getValue())
                .build();
    }

}
