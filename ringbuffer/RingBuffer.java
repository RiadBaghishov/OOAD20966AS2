package ringbuffer;

import java.util.Objects;

/**
 * Fixed-capacity Ring Buffer for:
 * - Single writer
 * - Multiple independent readers
 *
 * Writer overwrites oldest data when buffer is full.
 * Slow readers may miss overwritten items (lapping) and will auto-skip forward.
 */
public final class RingBuffer<T> {

    private final Object lock = new Object();
    private final Object[] data;
    private final int capacity;

    // Sequence numbers (monotonic)
    private long nextWriteSeq = 0; // sequence for next write (exclusive upper bound)
    private long oldestSeq = 0;    // oldest sequence still available

    public RingBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        this.data = new Object[capacity];
    }

    public int capacity() {
        return capacity;
    }

    /**
     * Single-writer write operation. Never blocks. Overwrites oldest when full.
     */
    public void write(T item) {
        Objects.requireNonNull(item, "item cannot be null");

        synchronized (lock) {
            long seq = nextWriteSeq;
            int index = (int) (seq % capacity);

            data[index] = item;
            nextWriteSeq++;

            // keep only last "capacity" items
            long minOldestAllowed = nextWriteSeq - capacity;
            if (oldestSeq < minOldestAllowed) {
                oldestSeq = minOldestAllowed;
            }
        }
    }

    /**
     * Creates a new independent reader starting at the current oldest item.
     */
    public RingBufferReader<T> createReader(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        synchronized (lock) {
            return new RingBufferReader<>(name, this, new SequenceCursor(oldestSeq));
        }
    }

    // Package-private accessors used by RingBufferReader (keeps responsibilities separated)
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
        return (T) data[index];
    }
}
