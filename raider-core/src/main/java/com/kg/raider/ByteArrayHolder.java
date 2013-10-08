package com.kg.raider;

import com.lmax.disruptor.EventFactory;

/**
 * User: kaveh
 * Date: 8/12/13
 * Time: 10:47 PM
 *
 * Simple java class to wrap the byte[] and serve
 * as an EventFactory for the ring buffer
 */
public final class ByteArrayHolder {

    private byte[] value;

    public byte[] getValue() {
        return value;
    }

    public void setValue(final byte[] value) {
        this.value = value;
    }

    public final static EventFactory<ByteArrayHolder> EVENT_FACTORY = new EventFactory<ByteArrayHolder>() {
        public ByteArrayHolder newInstance() {
            return new ByteArrayHolder();
        }
    };
}
