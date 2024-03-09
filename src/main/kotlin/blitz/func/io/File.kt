package blitz.func.io

import blitz.func.Monad
import blitz.func.bind
import blitz.io.readerSequence
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

fun Monad<String>.asPath() =
    bind { Path(it) }

fun Monad<Path>.read() =
    bind { p -> { SystemFileSystem.source(p) }.readerSequence() }