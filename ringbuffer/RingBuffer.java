package ringbuffer;

import java.util.Objects;

public final class RingBuffer<T> {

    private final Object lock = new Object();
    private final Object[] buffer;
    private final int capacity;

    private long nextWriteSeq = 0;
    private long oldestSeq = 0;

    public RingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        this.capacity = capacity;
        this.buffer = new Object[capacity];
    }

    public int capacity() {
        return capacity;
    }

    public void write(T item) {
        Objects.requireNonNull(item, "item cannot be null");

        synchronized (lock) {
            long seq = nextWriteSeq;
            int index = (int) (seq % capacity);

            buffer[index] = item;
            nextWriteSeq++;

            long minAllowedOldest = nextWriteSeq - capacity;
            if (oldestSeq < minAllowedOldest) {
                oldestSeq = minAllowedOldest;
            }
        }
    }

    public RingBufferReader<T> createReader(String name) {
        synchronized (lock) {
            SequenceCursor cursor = new SequenceCursor(oldestSeq);
            return new RingBufferReader<>(name, this, cursor);
        }
    }

    Object lock() {
        return lock;
    }

    long oldestSeq() {
        return oldestSeq;
    }

    long nextWriteSeq() {
        return nextWriteSeq;
    }

    @SuppressWarnings("unchecked")
    T getBySequence(long seq) {
        int index = (int) (seq % capacity);
        return (T) buffer[index];
    }
}

