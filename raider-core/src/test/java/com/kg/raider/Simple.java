package com.kg.raider;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.kg.raider.pb.MetricPB;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.junit.Test;

public class Simple {

    private NotificationEventHandler notificationHandler;
    private PersistenceEventHandler persistenceHandler;

    @SuppressWarnings("unchecked")
    @Test
    public void testDisruptor() {
        ExecutorService exec = Executors.newCachedThreadPool();
        // Preallocate RingBuffer with 1024 MetricEvents
        Disruptor<ByteArrayHolder> disruptor = new Disruptor<ByteArrayHolder>(ByteArrayHolder.EVENT_FACTORY, 1024, exec);
        final EventHandler<ByteArrayHolder> handler = new EventHandler<ByteArrayHolder>() {
            // event will eventually be recycled by the Disruptor after it wraps
            public void onEvent(final ByteArrayHolder event, final long sequence, final boolean endOfBatch) throws Exception {
                System.out.println("Sequence: " + sequence);
                System.out.println("MetricEvent: " + event.getValue());
            }
        };
        // Build dependency graph
        disruptor.handleEventsWith(handler);
        RingBuffer<ByteArrayHolder> ringBuffer = disruptor.start();

        for (long i = 10; i < 2000; i++) {

            MetricPB metricToReport = MetricPB.newBuilder()
                    .setKey("test.metric")
                    .setTimestamp(123456789l)
                    .setValue(1.93f)
                    .build();

            String uuid = UUID.randomUUID().toString();
            // Two phase commit. Grab one of the 1024 slots
            long seq = ringBuffer.next();
            ByteArrayHolder valueEvent = ringBuffer.get(seq);
            byte[] bytes = new byte[8];
            //valueEvent.setValue(uuid);
            valueEvent.setValue(metricToReport.toByteArray());
            ringBuffer.publish(seq);
        }
        disruptor.shutdown();
        exec.shutdown();
    }
}