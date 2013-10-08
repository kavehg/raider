package com.kg.raider;

import com.kg.raider.pb.MetricPB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * User: kaveh
 * Date: 8/9/13
 * Time: 10:09 PM
 */
public class RaiderTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSending() throws Exception {
        ZContext context = new ZContext();
        ZMQ.Socket sendSocket = context.createSocket(ZMQ.PUSH);
        sendSocket.connect("tcp://localhost:9999");

        // give time to connect
        Thread.sleep(5000);

        long start = System.nanoTime();
        for(int i = 0; i < 100000; i++) {
            MetricPB metricToReport = MetricPB.newBuilder()
                    .setKey("test.metric." + i)
                    .setTimestamp(System.nanoTime())
                    .setValue(1.93f + i)
                    .build();

            sendSocket.send(metricToReport.toByteArray(), 0);
        }

        MetricPB metricToReport = MetricPB.newBuilder()
                .setKey("test.metric.FINISHED")
                .setTimestamp(System.nanoTime())
                .setValue(1.93f)
                .build();

        sendSocket.send(metricToReport.toByteArray(), 0);
        long elapsed = System.nanoTime() - start;
        float sec = elapsed / 1000000000.0f;
        System.out.println("Done. [" + sec + " sec]");
    }
}
