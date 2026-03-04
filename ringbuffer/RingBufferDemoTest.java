package ringbuffer;

import java.util.Optional;

public class RingBufferDemoTest {

    public static void main(String[] args) {
        System.out.println("Running RingBufferDemoTest (no JUnit)...");

        testMultipleReadersIndependentAndOverwriteWorks();
        testSlowReaderGetsLappedAndSkipsForward();

        System.out.println("ALL TESTS PASSED ✅");
    }

    private static void testMultipleReadersIndependentAndOverwriteWorks() {
        RingBuffer<String> rb = new RingBuffer<>(3);

        RingBufferReader<String> r1 = rb.createReader("R1");
        RingBufferReader<String> r2 = rb.createReader("R2");

        rb.write("A");
        rb.write("B");

        assertEquals(Optional.of("A"), r1.read(), "R1 should read A");
        assertEquals(Optional.of("A"), r2.read(), "R2 should read A");

        rb.write("C");
        rb.write("D"); // overwrites A (capacity=3) after 4 total writes

        assertEquals(Optional.of("B"), r1.read(), "R1 should read B");
        assertEquals(Optional.of("C"), r1.read(), "R1 should read C");
        assertEquals(Optional.of("D"), r1.read(), "R1 should read D");
        assertEquals(Optional.empty(), r1.read(), "R1 should have nothing left");

        assertEquals(Optional.of("B"), r2.read(), "R2 should read B");
        assertEquals(Optional.of("C"), r2.read(), "R2 should read C");
        assertEquals(Optional.of("D"), r2.read(), "R2 should read D");
        assertEquals(Optional.empty(), r2.read(), "R2 should have nothing left");
    }

    private static void testSlowReaderGetsLappedAndSkipsForward() {
        RingBuffer<Integer> rb = new RingBuffer<>(2);
        RingBufferReader<Integer> slow = rb.createReader("SLOW");

        rb.write(1);
        rb.write(2);
        rb.write(3); // overwrites 1

        // slow reader target 1 is gone -> it must skip to oldest (2)
        assertEquals(Optional.of(2), slow.read(), "Slow reader should skip to 2");
        assertEquals(Optional.of(3), slow.read(), "Slow reader should read 3");
        assertEquals(Optional.empty(), slow.read(), "Slow reader should have nothing left");
    }

    // ---- minimal assertion helpers (no external libraries) ----

    private static void assertEquals(Object expected, Object actual, String message) {
        if ((expected == null && actual != null) || (expected != null && !expected.equals(actual))) {
            throw new AssertionError(message + "\nExpected: " + expected + "\nActual:   " + actual);
        }
    }
}
