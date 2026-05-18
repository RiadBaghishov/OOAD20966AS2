# Pull Request: Add unit tests for ring buffer project

## Summary

This PR adds a lightweight Java unit test suite for the existing ring buffer implementation.
No production/source files were modified.

Added files:

- `test/ringbuffer/CircularStoreTest.java` - standard-library test runner and unit tests
- `run-tests.sh` - script to compile source + tests and run the test suite
- `PR_DESCRIPTION.md` - PR notes and testability documentation


```bash
./run-tests.sh
```

The script compiles:

```bash
ringbuffer/*.java
test/ringbuffer/*.java
```

into `out-test/`, then runs:

```bash
java -cp out-test ringbuffer.CircularStoreTest
```

The tests validate:

- constructor validation for invalid capacities
- `capacity()` behavior
- rejecting `null` values in `publish`
- validating required `openReader` arguments
- `StartMode.NOW` behavior
- `StartMode.OLDEST_AVAILABLE` behavior
- reading order for existing data
- overwrite behavior when capacity is exceeded
- independent reader cursors
- slow-reader auto-skip behavior after overwritten data
- reader offset behavior and clamping beyond the current write sequence
- correct reading across physical ring-buffer wraparound
- `ringIndex()` behavior after reads and wraparound

`Producer`, `Consumer`, and `App` are intentionally not directly unit-tested in this PR because they run infinite loops and rely on real thread sleeping/console output. Testing them cleanly would require production-code changes such as injectable loop limits, injectable sleep behavior, or interrupt-aware test hooks. Because the assignment says not to modify source code, this PR documents that limitation instead of changing those classes.

## Source-code changes

None. This PR adds only test-related files and documentation.
