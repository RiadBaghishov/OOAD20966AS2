package ringbuffer;

public final class App {

    public static void main(String[] args) {
        int capacity = (args.length > 0) ? Integer.parseInt(args[0]) : 5;

        CircularStore<Integer> store = new CircularStore<>(capacity);

        // Readers start from oldest available item
        ReadHandle<Integer> a = store.openReader(StartMode.OLDEST_AVAILABLE, "A", 150, 0);
        ReadHandle<Integer> b = store.openReader(StartMode.OLDEST_AVAILABLE, "B", 450, 0);

        Thread producer = new Thread(new Producer(store, 120), "producer");
        Thread readerA  = new Thread(new Consumer(a), "reader-A");
        Thread readerB  = new Thread(new Consumer(b), "reader-B");

        producer.start();
        readerA.start();
        readerB.start();
    }
}
