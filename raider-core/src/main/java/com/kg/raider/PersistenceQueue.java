package com.kg.raider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * User: kaveh
 * Date: 8/9/13
 * Time: 10:53 PM
 */
public class PersistenceQueue {

    private Logger logger = LoggerFactory.getLogger(PersistenceQueue.class);

    /*
     * Getting 400K req/sec throughput with single thread
     */
    private static final Integer THREAD_COUNT = 1;

    /*
     * Using a blocking queue so that we can more
     * gracefully handle situations where the queue
     * might get overwhelmed with incoming data
     */
    private LinkedBlockingDeque<byte[]> queue;
    private ExecutorService executorService;
    private PersistenceWorker persistenceWorker;

    /**
     * Creates a persistence queue by instantiating the
     * thread pool, the internal queue, and the processor
     */
    public PersistenceQueue() {
        executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        queue = new LinkedBlockingDeque<>();
        persistenceWorker = new PersistenceWorker(this);
    }

    /**
     * Uses takeLast() to take an item from the end of the queue (FIFO).
     * takeLast() will block if the queue is empty
     *
     * @return next metric event to be processed
     */
    public byte[] getData() {
        byte[] nextItem = null;
        try {
            nextItem = queue.takeLast();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return nextItem;
    }

    /**
     * signals the start of processing, which means
     * taking entries from the deque
     */
    public void activateProcessor() {
        executorService.execute(persistenceWorker);
    }

    /**
     * Will add the byte[] representation of the incoming
     * metric to the front of the persistence queue.
     *
     * putFirst() will block if the queue is full which
     * in turn will slow down the ring buffer
     *
     * @param incomingMetrics is the byte[] representing a metric event
     */
    public void addData(ByteArrayHolder incomingMetrics) {
        try {
            queue.putFirst(incomingMetrics.getValue());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * If this number grows then the single threaded processor is
     * falling behind and consuming messages at a slower rate than
     * the ring buffer (i.e. clients) are producing
     *
     * @return current number of unprocessed metric events on the queue
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Stops the processor thread and shuts down the
     * thread pool
     */
    public void close() {
        logger.info("closing persistence queue");
        persistenceWorker.close();
        executorService.shutdown();
    }
}
