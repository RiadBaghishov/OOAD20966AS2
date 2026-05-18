package ringbuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CircularStoreTest {

    @FunctionalInterface
    private interface TestCase {
        void run() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        List<String> failures = new ArrayList<>();

        run("constructor rejects non-positive capacity", CircularStoreTest::constructorRejectsNonPositiveCapacity, failures);
        run("capacity returns configured size", CircularStoreTest::capacityReturnsConfiguredSize, failures);
        run("publish rejects null values", CircularStoreTest::publishRejectsNullValues, failures);
        run("openReader validates required arguments", CircularStoreTest::openReaderValidatesArguments, failures);
        run("NOW reader starts at current write position", CircularStoreTest::nowReaderStartsAtCurrentWritePosition, failures);
        run("OLDEST_AVAILABLE reader reads existing items in order", CircularStoreTest::oldestAvailableReaderReadsExistingItemsInOrder, failures);
        run("oldest reader only sees retained items after overwrite", CircularStoreTest::oldestReaderOnlySeesRetainedItemsAfterOverwrite, failures);
        run("reader offset is applied and clamped", CircularStoreTest::readerOffsetIsAppliedAndClamped, failures);
        run("readers are independent", CircularStoreTest::readersAreIndependent, failures);
        run("slow reader skips overwritten items", CircularStoreTest::slowReaderSkipsOverwrittenItems, failures);
        run("reader continues correctly across wraparound", CircularStoreTest::readerContinuesCorrectlyAcrossWraparound, failures);
        run("ringIndex follows the next read sequence", CircularStoreTest::ringIndexFollowsNextReadSequence, failures);

        if (!failures.isEmpty()) {
            System.err.println("\nFAILED TESTS:");
            for (String failure : failures) {
                System.err.println(" - " + failure);
            }
            System.exit(1);
        }

        System.out.println("\nAll unit tests passed.");
    }

    private static void run(String name, TestCase test, List<String> failures) {
        try {
            test.run();
            System.out.println("PASS: " + name);
        } catch (Throwable error) {
            failures.add(name + " -> " + error.getClass().getSimpleName() + ": " + error.getMessage());
            System.err.println("FAIL: " + name);
            error.printStackTrace(System.err);
        }
    }

    private static void constructorRejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new CircularStore<Integer>(0));
        assertThrows(IllegalArgumentException.class, () -> new CircularStore<Integer>(-1));
    }

    private static void capacityReturnsConfiguredSize() {
        CircularStore<String> store = new CircularStore<>(7);
        assertEquals(7, store.capacity(), "capacity should match constructor argument");
    }

    private static void publishRejectsNullValues() {
        CircularStore<String> store = new CircularStore<>(3);
        assertThrows(NullPointerException.class, () -> store.publish(null));
    }

    private static void openReaderValidatesArguments() {
        CircularStore<Integer> store = new CircularStore<>(3);

        assertThrows(NullPointerException.class, () -> store.openReader(null, "reader", 0, 0));
        assertThrows(NullPointerException.class, () -> store.openReader(StartMode.NOW, null, 0, 0));
    }

    private static void nowReaderStartsAtCurrentWritePosition() {
        CircularStore<String> store = new CircularStore<>(4);
        store.publish("first");
        store.publish("second");

        ReadHandle<String> reader = store.openReader(StartMode.NOW, "now", 0, 0);
        assertEmpty(reader.poll(), "NOW reader should not read older values");

        store.publish("third");
        assertPresentEquals("third", reader.poll(), "NOW reader should read the first value published after opening");
        assertEmpty(reader.poll(), "reader should be empty after consuming latest value");
    }

    private static void oldestAvailableReaderReadsExistingItemsInOrder() {
        CircularStore<Integer> store = new CircularStore<>(5);
        store.publish(10);
        store.publish(20);
        store.publish(30);

        ReadHandle<Integer> reader = store.openReader(StartMode.OLDEST_AVAILABLE, "oldest", 25, 0);

        assertEquals("oldest", reader.name(), "reader name should be preserved");
        assertEquals(25L, reader.delayMs(), "reader delay should be preserved");
        assertPresentEquals(10, reader.poll(), "first available value should be read first");
        assertPresentEquals(20, reader.poll(), "second available value should be read second");
        assertPresentEquals(30, reader.poll(), "third available value should be read third");
        assertEmpty(reader.poll(), "reader should be empty after all values are consumed");
    }

    private static void oldestReaderOnlySeesRetainedItemsAfterOverwrite() {
        CircularStore<Integer> store = new CircularStore<>(3);
        for (int i = 1; i <= 5; i++) {
            store.publish(i);
        }

        ReadHandle<Integer> reader = store.openReader(StartMode.OLDEST_AVAILABLE, "after-overwrite", 0, 0);

        assertPresentEquals(3, reader.poll(), "oldest retained value should be 3");
        assertPresentEquals(4, reader.poll(), "next retained value should be 4");
        assertPresentEquals(5, reader.poll(), "newest retained value should be 5");
        assertEmpty(reader.poll(), "reader should not see overwritten values 1 and 2");
    }

    private static void readerOffsetIsAppliedAndClamped() {
        CircularStore<Integer> store = new CircularStore<>(5);
        for (int i = 1; i <= 4; i++) {
            store.publish(i);
        }

        ReadHandle<Integer> offsetReader = store.openReader(StartMode.OLDEST_AVAILABLE, "offset", 0, 2);
        assertPresentEquals(3, offsetReader.poll(), "offset of 2 should skip the first two available items");
        assertPresentEquals(4, offsetReader.poll(), "offset reader should then read remaining existing item");
        assertEmpty(offsetReader.poll(), "offset reader should be empty after existing values are consumed");

        ReadHandle<Integer> clampedReader = store.openReader(StartMode.OLDEST_AVAILABLE, "clamped", 0, 99);
        assertEmpty(clampedReader.poll(), "offset beyond write sequence should clamp to current write position");
        store.publish(5);
        assertPresentEquals(5, clampedReader.poll(), "clamped reader should read future values normally");
    }

    private static void readersAreIndependent() {
        CircularStore<String> store = new CircularStore<>(4);
        store.publish("A");
        store.publish("B");

        ReadHandle<String> firstReader = store.openReader(StartMode.OLDEST_AVAILABLE, "first", 0, 0);
        ReadHandle<String> secondReader = store.openReader(StartMode.OLDEST_AVAILABLE, "second", 0, 0);

        assertPresentEquals("A", firstReader.poll(), "first reader should read A");
        assertPresentEquals("B", firstReader.poll(), "first reader should read B");
        assertEmpty(firstReader.poll(), "first reader should be caught up");

        assertPresentEquals("A", secondReader.poll(), "second reader should still independently read A");
        assertPresentEquals("B", secondReader.poll(), "second reader should still independently read B");
        assertEmpty(secondReader.poll(), "second reader should be caught up");

        store.publish("C");
        assertPresentEquals("C", firstReader.poll(), "first reader should read new value C");
        assertPresentEquals("C", secondReader.poll(), "second reader should also read new value C");
    }

    private static void slowReaderSkipsOverwrittenItems() {
        CircularStore<Integer> store = new CircularStore<>(3);
        ReadHandle<Integer> slowReader = store.openReader(StartMode.OLDEST_AVAILABLE, "slow", 0, 0);

        store.publish(1);
        store.publish(2);
        assertPresentEquals(1, slowReader.poll(), "slow reader initially reads value 1");

        store.publish(3);
        store.publish(4);
        store.publish(5);
        store.publish(6);

        assertPresentEquals(4, slowReader.poll(), "reader should skip overwritten values 2 and 3");
        assertPresentEquals(5, slowReader.poll(), "reader should continue with retained value 5");
        assertPresentEquals(6, slowReader.poll(), "reader should continue with retained value 6");
        assertEmpty(slowReader.poll(), "reader should be caught up after retained values");
    }

    private static void readerContinuesCorrectlyAcrossWraparound() {
        CircularStore<Integer> store = new CircularStore<>(2);
        ReadHandle<Integer> reader = store.openReader(StartMode.NOW, "wrap", 0, 0);

        store.publish(1);
        store.publish(2);
        assertPresentEquals(1, reader.poll(), "reader should read first value before wraparound");
        assertPresentEquals(2, reader.poll(), "reader should read second value before wraparound");

        store.publish(3);
        store.publish(4);
        assertPresentEquals(3, reader.poll(), "reader should read value from reused slot after wraparound");
        assertPresentEquals(4, reader.poll(), "reader should read next value from reused slot after wraparound");
        assertEmpty(reader.poll(), "reader should be caught up after wraparound values");
    }

    private static void ringIndexFollowsNextReadSequence() {
        CircularStore<String> store = new CircularStore<>(3);
        ReadHandle<String> reader = store.openReader(StartMode.NOW, "index", 0, 0);

        assertEquals(0, reader.ringIndex(), "initial next read index should be 0");

        store.publish("A");
        assertPresentEquals("A", reader.poll(), "reader should consume A");
        assertEquals(1, reader.ringIndex(), "next read index should advance to 1");

        store.publish("B");
        assertPresentEquals("B", reader.poll(), "reader should consume B");
        assertEquals(2, reader.ringIndex(), "next read index should advance to 2");

        store.publish("C");
        assertPresentEquals("C", reader.poll(), "reader should consume C");
        assertEquals(0, reader.ringIndex(), "next read index should wrap back to 0");
    }

    private static <T> void assertPresentEquals(T expected, Optional<T> actual, String message) {
        if (actual.isEmpty()) {
            throw new AssertionError(message + " | expected Optional containing <" + expected + "> but was empty");
        }
        assertEquals(expected, actual.get(), message);
    }

    private static void assertEmpty(Optional<?> actual, String message) {
        if (actual.isPresent()) {
            throw new AssertionError(message + " | expected Optional.empty but was <" + actual.get() + ">");
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " | expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static <T extends Throwable> void assertThrows(Class<T> expectedType, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable error) {
            if (expectedType.isInstance(error)) {
                return;
            }
            throw new AssertionError("expected " + expectedType.getSimpleName()
                    + " but got " + error.getClass().getSimpleName(), error);
        }
        throw new AssertionError("expected " + expectedType.getSimpleName() + " but no exception was thrown");
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
