package com.kg.raider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: kaveh
 * Date: 8/13/13
 * Time: 9:01 AM
 *
 * The notification processor asynchronously works on
 * data submitted to it by the ring buffer
 */
public class NotificationWorker implements Runnable {

    private int number_of_events_processed = 0;

    public int getNumber_of_events_processed() {
        return number_of_events_processed;
    }

    public NotificationSocket getNotificationSocket() {
        return notificationSocket;
    }

    private Logger logger = LoggerFactory.getLogger(NotificationWorker.class);

    /**
     * The internal queue is responsible for buffering
     * all updates received from the ring buffer
     */
    private NotificationQueue notificationQueue;

    /**
     * The notification socket is the outbound gateway
     * for clients that have subscribed to metric events
     */
    private NotificationSocket notificationSocket;

    private boolean processorActive = true;

    public NotificationWorker(NotificationQueue notificationQueue) {
        this.notificationQueue = notificationQueue;
        this.notificationSocket = new NotificationSocket();
    }

    @Override
    public void run() {

        /*
         * This loop will continuously take data from the notification
         * queue and publish to the outgoing socket in case there are
         * any subscribers interested in updates
         */
        while(processorActive) {
            notificationSocket.publish(notificationQueue.getData());
            number_of_events_processed++;
        }
    }

    public void close() {
        logger.info("closing notification worker");
        processorActive = false;
        notificationSocket.close();
        notificationQueue.close();
    }
}
