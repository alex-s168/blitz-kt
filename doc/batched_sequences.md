# Batched sequences
## Source
You should make all your sources return `BatchSequence<T>`
and then you can use the `.batched(count: Int)` function
to drastically decrease the amount of single reads in the original source.

Example:
```kt
File("text.txt")  // File
  .openRead()     // BatchSequence<Byte>
  .batched(64)    // BatchSequence<Byte>
```

## Sink
You should make all your sinks take `BatchSequence<T>`
and then you can use the `.asBatch()` function to allow
the sink to get multiple bytes at once.

Example:
```kt
val data = myData  // Sequence<Byte>
  .asBatch()       // BatchSequence<Byte>

File("text.txt")
  .write(data)
```