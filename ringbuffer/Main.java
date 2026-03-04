package ringbuffer;

import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        RingBuffer<String> buffer = new RingBuffer<>(3);

        RingBufferReader<String> r1 = buffer.createReader("R1");
        RingBufferReader<String> r2 = buffer.createReader("R2");

        System.out.println("=== DEMO: Single Writer / Multiple Readers ===");
        System.out.println("Capacity = " + buffer.capacity());

        System.out.println("\nWriter writes: A, B");
        buffer.write("A");
        buffer.write("B");

        printRead(r1);
        printRead(r2);

        System.out.println("\nWriter writes: C, D (D overwrites A because capacity=3)");
        buffer.write("C");
        buffer.write("D");

        // r1 continues independently
        printRead(r1); // B
        printRead(r1); // C
        printRead(r1); // D
        printRead(r1); // empty

        // r2 continues independently
        printRead(r2); // B
        printRead(r2); // C
        printRead(r2); // D
        printRead(r2); // empty

        System.out.println("\n=== DEMO: Slow reader lapped ===");
        RingBuffer<Integer> rb2 = new RingBuffer<>(2);
        RingBufferReader<Integer> slow = rb2.createReader("SLOW");

        System.out.println("Writer writes: 1, 2, 3 (3 overwrites 1 because capacity=2)");
        rb2.write(1);
        rb2.write(2);
        rb2.write(3);

        System.out.println("SLOW reads (should skip 1 and start at 2):");
        System.out.println("SLOW -> " + slow.read().orElse(null));
        System.out.println("SLOW -> " + slow.read().orElse(null));
        System.out.println("SLOW -> " + slow.read().orElse(null));
    }

    private static void printRead(RingBufferReader<String> reader) {
        Optional<String> v = reader.read();
        System.out.println(reader.name() + " reads: " + v.orElse("Nothing"));
    }
}
