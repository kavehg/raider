package com.kg.raider;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: kaveh
 * Date: 8/8/13
 * Time: 9:17 PM
 *
 * Main class holding together all internal raider components
 */
public class Raider implements RaiderMBean {

    Logger logger = LoggerFactory.getLogger(Raider.class);

    /*
     * The zeroMQ socket is the initial point of contact
     * for clients that submit their metric reports. Once
     * the socket receives data it immediately places the
     * received bytes on the ring buffer for further processing
     */
    private ReceiveSocket clientSocket;

    /*
     * The ring buffer is the central coordinator for all
     * incoming metric reports. Once data is placed on the
     * buffer, various handlers start processing the received
     * data (see next section for handlers)
     */
    private ExecutorService executorService;
    private Disruptor<ByteArrayHolder> disruptor;
    private RingBuffer<ByteArrayHolder> ringBuffer;

    /*
     * The handlers do their work in a pre-determined sequence
     * (see disruptor.after(notificationHandler).handleEventsWith(persistenceHandler))
     * in the Raider constructor below.
     *
     * Each handler does minimal work and is merely responsible
     * for removing data from the ring buffer and placing it on
     * a dedicated queue for further processing.
     *
     * There are currently two dedicated queues:
     *
     *  1. Persistence Queue - Any data placed here will be persisted to disk
     *  2. Notification Queue - Any data placed here will be sent out to subscribed clients
     *
     * Given the asynchronous processing of incoming events, no
     * assumptions can be made about sequencing
     */
    private NotificationEventHandler notificationHandler;
    private PersistenceEventHandler persistenceHandler;
    private PersistenceQueue persistenceQueue;
    private NotificationQueue notificationQueue;

    /**
     * assemble internal components
     */
    public Raider() {
        logger.info("starting internal components");

        // initialize the handlers & their queues
        persistenceQueue = new PersistenceQueue();
        notificationQueue = new NotificationQueue();
        notificationHandler = new NotificationEventHandler(notificationQueue);
        persistenceHandler = new PersistenceEventHandler(persistenceQueue);
        notificationQueue.activateProcessor();
        persistenceQueue.activateProcessor();

        // start the ring buffer
        executorService = Executors.newCachedThreadPool();
        disruptor = new Disruptor<>(ByteArrayHolder.EVENT_FACTORY, 1024, executorService);
        disruptor.handleEventsWith(notificationHandler);
        disruptor.after(notificationHandler).handleEventsWith(persistenceHandler);
        ringBuffer = disruptor.start();

        logger.info("starting exposed services");
        clientSocket = new ReceiveSocket(ringBuffer);
        clientSocket.start();
    }

    @Override
    public void shutdown() throws Exception {
        try {
            logger.info("shutting down exposed services");
            clientSocket.close();

            logger.info("shutting down internal components");
            disruptor.shutdown();
            executorService.shutdown();
            persistenceQueue.close();
            notificationQueue.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public int getPersistenceQueueSize() {
        return persistenceQueue.getQueueSize();
    }

    @Override
    public int getNotificationQueueSize() {
        return notificationQueue.getQueueSize();
    }

    @Override
    public int getNumberAddedToQueue() {
        return notificationQueue.getNumber_of_events_added();
    }

    @Override
    public int getNumberRemovedFromQueue() {
        return notificationQueue.getNumber_of_events_removed();
    }

    @Override
    public int getNumberProcessedByWorker() {
        return notificationQueue.getNotificationWorker().getNumber_of_events_processed();
    }

    @Override
    public int getNumberOfMessagesPublished() {
        return notificationQueue.getNotificationWorker().getNotificationSocket().getNumber_of_events_published();
    }
}
