package ringbuffer;

import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        RingBuffer<String> buffer = new RingBuffer<>(3);

        RingBufferReader<String> reader1 = buffer.createReader("Reader1");
        RingBufferReader<String> reader2 = buffer.createReader("Reader2");

        System.out.println("Writer writes: A, B");

        buffer.write("A");
        buffer.write("B");

        System.out.println("Reader1 reads: " + reader1.read().orElse("Nothing"));
        System.out.println("Reader2 reads: " + reader2.read().orElse("Nothing"));

        System.out.println("Writer writes: C, D");

        buffer.write("C");
        buffer.write("D");

        printRead("Reader1", reader1);
        printRead("Reader1", reader1);
        printRead("Reader1", reader1);

        printRead("Reader2", reader2);
        printRead("Reader2", reader2);
        printRead("Reader2", reader2);
    }

    private static void printRead(String name, RingBufferReader<String> reader) {
        Optional<String> result = reader.read();
        System.out.println(name + " reads: " + result.orElse("Nothing"));
    }
}
