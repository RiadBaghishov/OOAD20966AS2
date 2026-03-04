package com.example.ringbuffer;

import java.util.Optional;

/**
 * A reader has its own independent cursor.
 * Reading does NOT remove items from the buffer.
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
     * Reads the next available item for this reader, if any.
     *
     * Behavior:
     * - If no new item has been written since this reader's cursor: returns Optional.empty()
     * - If reader fell behind and its target was overwritten: auto-skip to oldest available
     */
    public Optional<T> read() {
        synchronized (buffer.lock()) {
            long oldest = buffer.oldestSeq();
            long newest = buffer.nextWriteSeq(); // exclusive

            long seq = cursor.nextReadSeq();

            // Lapped: reader is behind the oldest available item.
            if (seq < oldest) {
                cursor.setNextReadSeq(oldest);
                seq = oldest;
            }

            // Nothing new available for this reader.
            if (seq >= newest) {
                return Optional.empty();
            }

            T item = buffer.getBySequence(seq);
            cursor.advance();
            return Optional.ofNullable(item);
        }
    }
}

