package com.kg.raider;

/**
 * User: kaveh
 * Date: 8/9/13
 * Time: 9:36 PM
 */
public interface RaiderMBean {

    int getNumberAddedToQueue();
    int getNumberRemovedFromQueue();
    int getNumberProcessedByWorker();
    int getNumberOfMessagesPublished();

    /**
     * allows us to monitor the queue size using jconsole/jvisualvm
     *
     * if the queue sizes grow continuously then that would be a sign
     * that the rate of incoming requests is overwhelming our ability
     * to process the data
     *
     * @return current size of the persistence queue
     */
    int getPersistenceQueueSize();

    /**
     * allows us to monitor the queue size using jconsole/jvisualvm
     *
     * if the queue sizes grow continuously then that would be a sign
     * that the rate of incoming requests is overwhelming our ability
     * to process the data
     *
     * @return current size of the notification queue
     */
    int getNotificationQueueSize();

    void shutdown() throws Exception;
}
