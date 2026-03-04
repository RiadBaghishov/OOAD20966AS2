package ringbuffer;

import java.util.Optional;

public final class Consumer implements Runnable {

    private final ReadHandle<Integer> reader;

    public Consumer(ReadHandle<Integer> reader) {
        this.reader = reader;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Optional<Integer> val = reader.poll();
            if (val.isPresent()) {
                System.out.println("[R-" + reader.name() + "] read=" + val.get() + " | pos=" + reader.ringIndex());
            }
            sleep(reader.delayMs());
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
