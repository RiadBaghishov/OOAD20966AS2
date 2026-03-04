package ringbuffer;

import java.util.Optional;

public class RingBufferDemoTest {

    public static void main(String[] args) {

        System.out.println("Running tests...");

        testMultipleReaders();
        testSlowReader();

        System.out.println("ALL TESTS PASSED");
    }

    private static void testMultipleReaders() {

        RingBuffer<String> rb = new RingBuffer<>(3);

        RingBufferReader<String> r1 = rb.createReader("R1");
        RingBufferReader<String> r2 = rb.createReader("R2");

        rb.write("A");
        rb.write("B");

        assertEquals(Optional.of("A"), r1.read());
        assertEquals(Optional.of("A"), r2.read());

        rb.write("C");
        rb.write("D");

        assertEquals(Optional.of("B"), r1.read());
        assertEquals(Optional.of("C"), r1.read());
        assertEquals(Optional.of("D"), r1.read());

        assertEquals(Optional.of("B"), r2.read());
        assertEquals(Optional.of("C"), r2.read());
        assertEquals(Optional.of("D"), r2.read());
    }

    private static void testSlowReader() {

        RingBuffer<Integer> rb = new RingBuffer<>(2);
        RingBufferReader<Integer> slow = rb.createReader("SLOW");

        rb.write(1);
        rb.write(2);
        rb.write(3);

        assertEquals(Optional.of(2), slow.read());
        assertEquals(Optional.of(3), slow.read());
        assertEquals(Optional.empty(), slow.read());
    }

    private static void assertEquals(Object expected, Object actual) {

        if ((expected == null && actual != null) ||
            (expected != null && !expected.equals(actual))) {

            throw new RuntimeException(
                "Test failed. Expected: " + expected + " but got: " + actual
            );
        }
    }
}

