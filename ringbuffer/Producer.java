package ringbuffer;

public final class Producer implements Runnable {

    private final CircularStore<Integer> store;
    private final long delayMs;
    private int value = 1;

    public Producer(CircularStore<Integer> store, long delayMs) {
        this.store = store;
        this.delayMs = delayMs;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            int v = value++;
            store.publish(v);
            System.out.println("[P] publish=" + v + " | " + store.debugState());
            sleep(delayMs);
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
