package com.kg.raider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: kaveh
 * Date: 8/12/13
 * Time: 8:58 PM
 *
 * The persistence processor asynchronously works on
 * data submitted to it by the ring buffer
 */
public class PersistenceWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(PersistenceWorker.class);

    /**
     * The internal queue is responsible for buffering
     * all updates received from the ring buffer
     */
    private PersistenceQueue persistenceQueue;

    private boolean processorActive = true;

    public PersistenceWorker(PersistenceQueue persistenceQueue) {
        this.persistenceQueue = persistenceQueue;
    }

    @Override
    public void run() {
        while (processorActive) {
            persistenceQueue.getData();
        }
    }

    public void close() {
        logger.info("closing persistence worker");
        processorActive = false;
    }

}
