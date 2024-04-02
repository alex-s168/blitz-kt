package blitz.parse.comb

import blitz.str.collectToString
import kotlin.math.max

@JvmInline
value class Parsable(
    val str: String
)

typealias Parser<T> = (Parsable) -> Pair<Parsable, T>?

fun <T> parser(fn: (Parsable) -> Pair<Parsable, T>?): Parser<T> =
    fn

fun <T> Parser<T>.trim(): Parser<T> = parser {
    it.whitespaces()
        .map(this@trim)
        ?.whitespaces()
}

fun <T> constantParser(const: T): Parser<T> = { it to const }

infix fun <T> Parser<T>.or(other: Parser<T>): Parser<T> = {
    this@or(it) ?: other(it)
}

fun Parsable.spaces(): Parsable {
    return Parsable(str.trimStart(' '))
}

fun Parsable.whitespaces(): Parsable {
    return Parsable(str.trimStart())
}

fun Parsable.require(what: String): Parsable? {
    if (str.startsWith(what))
        return Parsable(str.substring(what.length))
    return null
}

fun <T> Parsable.untilRequire(c: String, map: (String) -> T?): Pair<Parsable, T>? {
    return map(str.substringBefore(c))?.let { Parsable(str.substringAfter(c)) to it }
}

fun <T> Parsable.asLongAs(vararg li: Char, map: (String) -> T?): Pair<Parsable, T>? {
    val o = mutableListOf<Char>()
    for (c in str) {
        if (c in li)
            o.add(c)
        else
            break
    }
    val out = str.substring(o.size)
    return map(o.iterator().collectToString())?.let { Parsable(out) to it }
}

fun <T> Parsable.map(parser: Parser<T>): Pair<Parsable, T>? =
    parser(this)

fun <T, R> Pair<Parsable, T>.map(fn: (Parsable, T) -> Pair<Parsable, R>?): Pair<Parsable, R>? =
    fn(first, second)

fun <A, B> Pair<Parsable, A>.map(parser: Parser<B>): Pair<Parsable, Pair<A, B>>? =
    map { parsable, a ->
        parser(parsable)?.let { r ->
            r.first to (a to r.second)
        }
    }

fun <T> Pair<Parsable, T>.mapFirst(fn: (Parsable) -> Parsable): Pair<Parsable, T> =
    fn(first) to second

fun <T> Pair<Parsable, T>.mapFirstNullable(fn: (Parsable) -> Parsable?): Pair<Parsable, T>? =
    fn(first)?.let { it to second }

fun <T, R> Pair<Parsable, T>.mapSecond(fn: (T) -> R): Pair<Parsable, R> =
    first to fn(second)

fun <T> Pair<Parsable, T>.spaces(): Pair<Parsable, T> =
    mapFirst { it.spaces() }

fun <T> Pair<Parsable, T>.whitespaces(): Pair<Parsable, T> =
    mapFirst { it.whitespaces() }

fun <T> Pair<Parsable, T>.require(what: String): Pair<Parsable, T>? =
    mapFirstNullable { it.require(what) }

fun <T> Parsable.array(sep: String, map: (Parsable) -> Pair<Parsable, T>?): Pair<Parsable, List<T>> {
    val out = mutableListOf<T>()

    var curr = str
    fun step() =
        map(Parsable(curr))?.also {
            curr = it.first.str
        }

    while (true) {
        val r = step() ?: break
        out.add(r.second)
        curr = (Parsable(curr).require(sep) ?: break).str
    }

    return Parsable(curr) to out
}