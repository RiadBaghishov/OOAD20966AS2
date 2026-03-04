package ringbuffer;

import java.util.Optional;

/**
 * Independent reader state (cursor) + read operation.
 * Kept separate from the store to keep responsibilities clean.
 */
public final class ReadHandle<T> {

    private final CircularStore<T> store;
    private final String name;
    private final long delayMs;

    private long nextSeq; // next sequence this reader wants to read

    ReadHandle(CircularStore<T> store, String name, long delayMs, long startSeq) {
        this.store = store;
        this.name = name;
        this.delayMs = delayMs;
        this.nextSeq = startSeq;
    }

    public String name() {
        return name;
    }

    public long delayMs() {
        return delayMs;
    }

    /**
     * Read next available item for this reader.
     * - If lapped: auto-skip to oldest available
     * - If nothing new: Optional.empty()
     */
    public Optional<T> poll() {
        synchronized (store.monitor) {
            long oldest = store.oldestAvailableSeqUnsafe();
            long newestExclusive = store.writeSeqUnsafe();

            if (nextSeq < oldest) {
                nextSeq = oldest; // lapped: skip missed items
            }

            if (nextSeq >= newestExclusive) {
                return Optional.empty();
            }

            T value = store.loadBySeqUnsafe(nextSeq);
            nextSeq++;
            return Optional.ofNullable(value);
        }
    }

    public int ringIndex() {
        synchronized (store.monitor) {
            return (int) (nextSeq % store.capacity());
        }
    }
}
