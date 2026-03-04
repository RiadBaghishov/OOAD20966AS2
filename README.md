# Assignment 2

## Overview

This project implements a **Ring Buffer (Circular Buffer)** in Java that supports:

- One **writer**
- Multiple **independent readers**
- **Fixed capacity**
- **Overwrite behavior** when the buffer becomes full

Each reader maintains its own reading position, so reading by one reader **does not remove data** for other readers.

If a reader is too slow and the writer overwrites data that it has not yet read, the reader will **automatically skip to the oldest available item**.

---

## Design

The solution follows basic **Object-Oriented Design principles** with clear separation of responsibilities.

### Classes

**CircularStore**
- Stores the circular buffer
- Handles writing new data
- Manages buffer capacity and overwrite logic

**ReadHandle**
- Represents an independent reader
- Maintains its own read position
- Allows readers to read without affecting others

**Producer**
- A thread that continuously writes data into the buffer

**Consumer**
- A thread that reads data from the buffer

**StartMode**
- Defines how a reader begins reading (from the current position or from the oldest available data)

**App**
- The main program that initializes the buffer and starts the writer and reader threads

---

## How to Compile

Navigate to the project directory and run:

