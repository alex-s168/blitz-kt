package blitz.func.io

import blitz.*
import blitz.func.*
import blitz.io.*

fun Monad<String>.asPath(): Monad<Path> =
    bind { Path.of(it) }

fun Monad<Path>.read(): Monad<ByteBatchSequence> =
    bind { it.getFile().read() }

fun Monad<Path>.write(seq: Monad<ByteBatchSequence>): Monad<Unit> =
    bind { it.getFile().write(seq.impure()) }

fun Monad<Path>.append(seq: Monad<ByteBatchSequence>): Monad<Unit> =
    bind { it.getFile().append(seq.impure()) }

fun Monad<ByteBatchSequence>.writeTo(path: Monad<Path>): Monad<Unit> =
    path.write(this)

fun Monad<ByteBatchSequence>.appendTo(path: Monad<Path>): Monad<Unit> =
    path.append(this)

fun Monad<Path>.exists(): Monad<Boolean> =
    bind { it.exists() }

fun Monad<Path>.isDir(): Monad<Boolean> =
    bind { it.isDir() }

fun Monad<Path>.isFile(): Monad<Boolean> =
    bind { it.isFile() }