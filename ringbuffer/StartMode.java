package ringbuffer;

public enum StartMode {
    NOW,              // start from current write position
    OLDEST_AVAILABLE  // start from oldest still available (skips overwritten history)
}
