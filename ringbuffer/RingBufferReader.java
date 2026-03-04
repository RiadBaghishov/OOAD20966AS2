package ringbuffer;

import java.util.Optional;

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

    public Optional<T> read() {
        synchronized (buffer.lock()) {

            long oldest = buffer.oldestSeq();
            long newest = buffer.nextWriteSeq();

            long seq = cursor.nextReadSeq();

            if (seq < oldest) {
                cursor.setNextReadSeq(oldest);
                seq = oldest;
            }

            if (seq >= newest) {
                return Optional.empty();
            }

            T item = buffer.getBySequence(seq);
            cursor.advance();

            return Optional.ofNullable(item);
        }
    }
}
