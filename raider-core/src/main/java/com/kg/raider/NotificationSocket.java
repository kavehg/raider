package com.kg.raider;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kg.raider.pb.MetricPB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * User: kaveh
 * Date: 8/14/13
 * Time: 10:24 PM
 */
public class NotificationSocket {

    private int number_of_events_published = 0;

    public int getNumber_of_events_published() {
        return number_of_events_published;
    }

    private Logger logger = LoggerFactory.getLogger(NotificationSocket.class);

    /**
     * The port to which the send socket will bind
     */
    private static final Integer PUBLISH_PORT = 6000;

    /**
     * zero-mq context
     */
    private ZContext ctx;

    /**
     * zero-mq outgoing notification/subscription socket
     */
    private ZMQ.Socket publisher;

    /**
     * The notification socket has no external dependencies
     */
    public NotificationSocket() {
        ctx = new ZContext();
        publisher = ctx.createSocket(ZMQ.PUB);

        // bind to port
        publisher.bind("tcp://*:" + PUBLISH_PORT);
    }

    /**
     * Any metrics passed to this method will be published out
     * to subscribing clients - IF the byte array converts to
     * metric PB pojo
     *
     * @param metricToPublish byte array representation of the
     *                        metric to be published
     */
    public void publish(byte[] metricToPublish) {
        try {
            // convert the byte array to PB pojo
            MetricPB metricToReport = MetricPB.parseFrom(metricToPublish);

            // publish the key - used for filtering client subscriptions
            publisher.send(metricToReport.getKey().getBytes(), ZMQ.SNDMORE);

            // publish the body
            publisher.send(metricToReport.toByteArray(), 0);
            number_of_events_published++;
        } catch (InvalidProtocolBufferException e) {
            logger.error(e.getMessage());
        }
    }

    public void close() {
        logger.info("closing notification socket");
        publisher.close();
        ctx.close();
        ctx.destroy();
    }
}
