package com.example.ringbuffer;

import java.util.Objects;

/**
 * RingBuffer<T> is a fixed-capacity circular buffer supporting:
 * - Single Writer (write)
 * - Multiple independent Readers (createReader)
 *
 * Notes:
 * - Reads are non-destructive (data stays until overwritten).
 * - Writer never blocks: if full, it overwrites the oldest element.
 * - Slow readers may miss items (lapping). They auto-skip to the oldest available.
 */
public final class RingBuffer<T> {

    private final Object lock = new Object();
    private final Object[] buffer;
    private final int capacity;

    // Monotonic sequence numbers:
    // nextWriteSeq: sequence to assign to the next written item
    // oldestSeq: oldest sequence still available (due to overwrites)
    private long nextWriteSeq = 0;
    private long oldestSeq = 0;

    public RingBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        this.buffer = new Object[capacity];
    }

    public int capacity() {
        return capacity;
    }

    /**
     * Single-writer write. Overwrites oldest when full.
     */
    public void write(T item) {
        Objects.requireNonNull(item, "item cannot be null");
        synchronized (lock) {
            long seq = nextWriteSeq;
            int index = (int) (seq % capacity);
            buffer[index] = item;

            nextWriteSeq++;

            // If writer advanced more than capacity items ahead of oldest, move oldest forward.
            long minAllowedOldest = nextWriteSeq - capacity;
            if (oldestSeq < minAllowedOldest) {
                oldestSeq = minAllowedOldest;
            }
        }
    }

    /**
     * Create a new independent reader (each has its own read position).
     * The reader starts at the current oldest available item.
     */
    public RingBufferReader<T> createReader(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        synchronized (lock) {
            SequenceCursor cursor = new SequenceCursor(oldestSeq);
            return new RingBufferReader<>(name, this, cursor);
        }
    }

    // ---- package-private methods used by RingBufferReader ----

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
