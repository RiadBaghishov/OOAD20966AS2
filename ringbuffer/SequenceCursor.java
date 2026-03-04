package com.example.ringbuffer;

/**
 * Holds a reader's current sequence position (next sequence to read).
 * Separated as its own class to keep responsibilities clean.
 */

    SequenceCursor(long startSeq) {
        this.nextReadSeq = startSeq;
    }

    long nextReadSeq() {
        return nextReadSeq;
    }

    void setNextReadSeq(long value) {
        this.nextReadSeq = value;
    }

    void advance() {
        this.nextReadSeq++;
    }
}


