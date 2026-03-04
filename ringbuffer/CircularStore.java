package ringbuffer;

import java.util.Arrays;
import java.util.Objects;

/**
 * Single-writer / multi-reader fixed-size circular store.
 * - Non-destructive reads (per-reader cursor)
 * - Writer overwrites when full
 * - Slow readers may miss items (auto-skip to oldest available)
 */
public final class CircularStore<T> {

    private final int capacity;
    private final Object[] slots;

    // Monotonic sequence for writes (exclusive upper bound)
    private long writeSeq = 0;

    // Used only for debugging output (next physical slot index)
    private int writePos = 0;

    // Shared monitor for writer + all readers
    final Object monitor = new Object();

    public CircularStore(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        this.slots = new Object[capacity];
    }

    public int capacity() {
        return capacity;
    }

    /**
     * Single writer method: stores item and advances sequence. Overwrites when full.
     */
    public void publish(T item) {
        Objects.requireNonNull(item, "item must not be null");

        synchronized (monitor) {
            slots[writePos] = item;
            writePos = (writePos + 1) % capacity;
            writeSeq++;
        }
    }

    /**
     * Opens an independent reader with its own cursor sequence.
     */
    public ReadHandle<T> openReader(StartMode mode, String name, long delayMs, long offsetItems) {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(name, "name");

        synchronized (monitor) {
            long base = (mode == StartMode.NOW) ? writeSeq : oldestAvailableSeqUnsafe();
            long start = base + offsetItems;

            // Don't start beyond what exists yet
            if (start > writeSeq) start = writeSeq;

            return new ReadHandle<>(this, name, delayMs, start);
        }
    }

    long writeSeqUnsafe() {
        return writeSeq;
    }

    long oldestAvailableSeqUnsafe() {
        // oldest sequence kept is (writeSeq - capacity), but never below 0
        long oldest = writeSeq - capacity;
        return Math.max(0, oldest);
    }

    @SuppressWarnings("unchecked")
    T loadBySeqUnsafe(long seq) {
        int idx = (int) (seq % capacity);
        return (T) slots[idx];
    }

    public String debugState() {
        synchronized (monitor) {
            return Arrays.toString(slots) + " {writePos=" + writePos + ", writeSeq=" + writeSeq + "}";
        }
    }
}
