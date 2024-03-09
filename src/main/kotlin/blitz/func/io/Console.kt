package blitz.func.io

import blitz.func.*

fun Monad<String>.print() =
    bind { print(it) }

fun readIn() =
    Monad { generateSequence { readln() } }