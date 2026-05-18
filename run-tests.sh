set -euo pipefail

BUILD_DIR="out-test"

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

javac -d "$BUILD_DIR" ringbuffer/*.java test/ringbuffer/*.java
java -cp "$BUILD_DIR" ringbuffer.CircularStoreTest
