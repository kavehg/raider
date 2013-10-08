package com.kg.raider;

import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

/**
 * User: kaveh
 * Date: 7/8/13
 * Time: 9:26 PM
 *
 * The socket service exposes a zeroMQ socket in PULL mode
 */
public class ReceiveSocket extends Thread {

    Logger logger = LoggerFactory.getLogger(ReceiveSocket.class);

    private RingBuffer<ByteArrayHolder> ringBuffer;

    private static final int PORT = 9999;
    private static boolean socketListening;

    private ZContext context;
    private Socket serverSocket;

    /**
     * The socket service exposes a zeromq socket and puts
     * everything that it receives from clients on a ring
     * buffer for further processing
     *
     * @param ringBuffer is the destination of all events received
     */
    public ReceiveSocket(RingBuffer<ByteArrayHolder> ringBuffer) {
        this.ringBuffer = ringBuffer;

        context = new ZContext();
        serverSocket = context.createSocket(ZMQ.PULL);
        serverSocket.bind("tcp://*:" + PORT);

        socketListening = true;

        logger.debug(
                String.format(  "zeromq service initialized. zeromq version string: " +
                                "%s, and version int: %d",
                                ZMQ.getVersionString(), ZMQ.getFullVersion())
        );
    }

    /**
     * Push every message received on to the ring buffer
     * until signaled to stop
     *
     * Critical Path! Any additional work here will impact
     * total throughput!
     */
    @Override
    public void run() {
        logger.debug("starting server socket");
        while(socketListening) {
            // receive bytes representing a metric to be saved
            byte[] receivedBytes = null;
            try {
                receivedBytes = serverSocket.recv(0);
            } catch (ZMQException e) {
                logger.error(e.getMessage());
                close();
            }

            // publish metric to ring buffer
            long seq = ringBuffer.next();
            ByteArrayHolder metricHolder = ringBuffer.get(seq);
            metricHolder.setValue(receivedBytes);
            ringBuffer.publish(seq);
        }

        logger.debug("zeromq listening socket exiting");
    }

    /**
     * clean up before exiting
     *
     * @throws Exception
     */
    public void close()  {
        logger.debug("received request to stop server socket");

        // try to interrupt running thread
        socketListening = false;
        serverSocket.close();

        // clean up
        context.close();
        context.destroy();

        logger.debug("server socket stopped");
    }
}
