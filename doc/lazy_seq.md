# Lazy Sequences
When writing recursive functions like Fibonacci, it is often easier and faster to use
lazy sequences.

Example:
```kt
val fib = lazySequence(0 to 1) { i, f ->
  f(i-1) + f(i-2)
}

println(fib[10])
```
Note: If we call f for any number below 0, it will call f(0) instead.