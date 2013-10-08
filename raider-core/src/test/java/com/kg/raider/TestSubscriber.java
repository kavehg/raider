package com.kg.raider;

import org.junit.Test;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZThread;

/**
 * User: kaveh
 * Date: 8/14/13
 * Time: 1:38 PM
 */
public class TestSubscriber {

    private int number_of_msgs_received = 0;

    private ZContext ctx = new ZContext();

    @Test
    public void testSubscription() {
        ZMQ.Socket subscriber = ctx.createSocket(ZMQ.SUB);

        subscriber.connect("tcp://localhost:6000");
        subscriber.subscribe("test.metric".getBytes());

        while (true) {
            // Read envelope with address
            String metricKey = new String(subscriber.recv(0));
            // Read message contents
            byte[] metric = subscriber.recv(0);

            ++number_of_msgs_received;
            if(metricKey.equals("test.metric.FINISHED")) {
                System.out.println("FINISHED [" + number_of_msgs_received + "]");
                break;
            }
        }

    }

}
