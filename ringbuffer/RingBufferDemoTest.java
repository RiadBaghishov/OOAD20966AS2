package com.example.ringbuffer;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RingBufferDemoTest {

    @Test
    void multipleReadersIndependentAndOverwriteWorks() {
        RingBuffer<String> rb = new RingBuffer<>(3);

        RingBufferReader<String> r1 = rb.createReader("R1");
        RingBufferReader<String> r2 = rb.createReader("R2");

        rb.write("A");
        rb.write("B");

        assertEquals(Optional.of("A"), r1.read());
        assertEquals(Optional.of("A"), r2.read());

        rb.write("C");
        rb.write("D"); // overwrites "A" (capacity=3)

        // r1 already read A, next should be B then C then D
        assertEquals(Optional.of("B"), r1.read());
        assertEquals(Optional.of("C"), r1.read());
        assertEquals(Optional.of("D"), r1.read());
        assertEquals(Optional.empty(), r1.read());

        // r2 read only A, next is B, but depending on timing it can still be present.
        // After 4 writes total (A,B,C,D) with capacity 3, oldest is B.
        assertEquals(Optional.of("B"), r2.read());
        assertEquals(Optional.of("C"), r2.read());
        assertEquals(Optional.of("D"), r2.read());
        assertEquals(Optional.empty(), r2.read());
    }

    @Test
    void slowReaderGetsLappedAndSkipsForward() {
        RingBuffer<Integer> rb = new RingBuffer<>(2);
        RingBufferReader<Integer> slow = rb.createReader("SLOW");

        rb.write(1);
        rb.write(2);
        rb.write(3); // overwrites 1 (capacity=2)

        // slow reader started at oldest, but 1 is gone now; it should skip to 2
        assertEquals(Optional.of(2), slow.read());
        assertEquals(Optional.of(3), slow.read());
        assertEquals(Optional.empty(), slow.read());
    }
}

