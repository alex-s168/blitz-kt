package blitz.collections

fun UnGettableIterator<Char>.skipSpaces(): UnGettableIterator<Char> {
    var curr = next()
    while (curr == ' ' || curr == '\n' || curr == '\r') {
        curr = next()
    }
    unGet()
    return this
}