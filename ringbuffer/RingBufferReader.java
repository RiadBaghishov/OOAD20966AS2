package ringbuffer;

import java.util.Optional;

/**
 * Independent reader: each reader has its own cursor.
 * Reads do NOT remove items from the buffer.
 */
public final class RingBufferReader<T> {

    private final String name;
    private final RingBuffer<T> buffer;
    private final SequenceCursor cursor;

    RingBufferReader(String name, RingBuffer<T> buffer, SequenceCursor cursor) {
        this.name = name;
        this.buffer = buffer;
        this.cursor = cursor;
    }

    public String name() {
        return name;
    }

    /**
     * Reads next item for this reader if available.
     * - If reader is lapped (data overwritten), it skips to oldest available.
     * - If nothing new, returns Optional.empty().
     */
    public Optional<T> read() {
        synchronized (buffer.lock()) {
            long oldest = buffer.oldestSeq();
            long newestExclusive = buffer.nextWriteSeq();

            long seq = cursor.nextReadSeq();

            // Lapped: requested seq is already overwritten
            if (seq < oldest) {
                cursor.setNextReadSeq(oldest);
                seq = oldest;
            }

            // No new data available
            if (seq >= newestExclusive) {
                return Optional.empty();
            }

            T item = buffer.getBySequence(seq);
            cursor.advance();
            return Optional.ofNullable(item);
        }
    }
}
