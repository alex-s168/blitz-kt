# Blitz
Big Kotlin library adding features that the Kotlin standard library just does not have (and might never get)

## How to get
```kotlin
repositories {
    maven {
        name = "alex's repo"
        url = uri("http://207.180.202.42:8080/libs")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    implementation("me.alex_s168:blitz:0.7")
}
```

## Examples
### Fibonacci sequence
```kotlin
val fib = lazySequence(0 to 1) { i, f ->
  f(i-1) + f(i-2)
}

println(fib[10])
```
### Unix `uniq`
```kotlin
val inp = sequenceOf("AAA", "BBB", "AAA", "AAA", "AAA", "BBB")
val out = inp.easyMappingSequence { i, s, m ->
    if (s(i-1) == m(i)) null
    else m(i)
}
println(out.contents)
```
### Reading files
```kotlin
val file = Path.of("test.txt")  // Path
    .getFile()                  // File

val text = file.read()          // ByteBatchSequence
    .stringify()                // Sequence<String>   // (NOT lines!!)
    .flatten()                  // String
```
### Bit fields
```kotlin
class Flags: BitField() {
    var direction by bit(0)
    var moving by bit(1)
    var frontLight by bit(2)
}

val byte = getByteFromSomewhere()
val flags = Flags().decode(byte)
flags.direction = !flags.direction
putByteSomewhere(flags.encode())
```
### Unix `cat` with monads (pure)
```kotlin
fun pureCat(args: Array<String>): Monad<Unit> =
    args
    .ifEmpty { arrayOf("-") }
    .map {
        if (it == "-") readIn()
        else unit(it)
            .asPath()
            .read()
            .stringify()
    }
    .rewrap()
    .flatten()
    .reduce { s -> print(s) }
```
### Numbers to bytes 
```kotlin
val num: Short = 5
val bytes = num.toBytes(Endian.LITTLE)
```
### Caching delegate property
```kotlin
class Label {
    var font = "Arial 11"
    val fontWith by caching(::font) {
        someFunctionToCalculate(it)
    }
}
```
### Contents
```kotlin
val a = listOf(1, 2, 3, 4)
val b = arrayOf(1, 2, 3, 4)
println(a.contents == b.contents) // true
println(b.contents) // [1, 2, 3, 4]
```
### Either
No example yet
