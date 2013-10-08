package com.kg.raider;

import com.lmax.disruptor.EventHandler;

/**
 * User: kaveh
 * Date: 8/9/13
 * Time: 8:58 PM
 *
 * The notification event handler is connected to the
 * disruptor ring buffer and receives events in a pre-determined
 * sequence (compared to other event handlers)
 */
public class NotificationEventHandler implements EventHandler<ByteArrayHolder> {

    private NotificationQueue queue;

    /**
     * The handler leverages a queue to communicate the events
     * it receives for downstream processing
     *
     * @param queue events will be published onto this queue
     */
    public NotificationEventHandler(NotificationQueue queue) {
        this.queue = queue;
    }

    /**
     * This method is invoked by the ring buffer for processing
     * the next event. It is on the critical path and any additional
     * work/cycles here will impact overall throughput
     *
     * @param metric     binary representation of the metric submitted by a client
     * @param sequence   sequence number that was used by the ring buffer
     * @param endOfBatch indicate whether this is the end of a batch
     * @throws Exception
     */
    @Override
    public void onEvent(ByteArrayHolder metric, long sequence, boolean endOfBatch) throws Exception {
        queue.addData(metric);
    }
}
