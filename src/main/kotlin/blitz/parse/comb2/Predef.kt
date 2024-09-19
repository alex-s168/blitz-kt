package blitz.parse.comb2

import blitz.collections.RefVec
import blitz.str.charsToString
import kotlin.math.absoluteValue
import kotlin.math.sign

fun whitespaces(): Parser<Char, Unit> =
    repeatedNoSave(oneOf("\n\t\r\b ".toList()))

fun digit(): Parser<Char, Char> =
    oneOf("0123456789".toList())

fun uintLit(): Parser<Char, RefVec<Char>> =
    verifyValueWithSpan(withSpan(repeated(digit())))
             { if (it.size == 0) "need digits after sign in num lit" else null }

fun intLit(): Parser<Char, Int> =
    mapValue(then(choose(mapValue(just('+')) { +1 },
               mapValue(just('-')) { -1 },
               value(+1)),
            uintLit()))
    { (sign, v) -> sign * v.charsToString().toInt() }

fun floatLit(): Parser<Char, Double> =
    mapValue(
            then(
                thenIgnore(
                    intLit(),
                    just('.')),
                orElseVal(uintLit(), RefVec.of('0'))))
    { (pre, post) ->
        var p = post.charsToString().toDouble()
        while (p.absoluteValue >= 1) {
            p *= 0.1
        }

        (pre.toDouble().absoluteValue + p) * pre.toDouble().sign
    }

fun escapeChar(): Parser<Char, Char> =
    thenOverwrite(just('\\'),
        mapErrors(choose(just('"'),
               just('\''),
               just('\\'),
               mapValue(just('n')) { '\n' },
               mapValue(just('r')) { '\r' },
               mapValue(just('b')) { '\b' },
               mapValue(just('t')) { '\t' }))
        { RefVec.of(ParseError(it[0].loc, "invalid escape sequence")) }
    )

fun stringLit(): Parser<Char, String> =
    mapValue(thenIgnore(then(just('"'),
        repeated(choose(escapeChar(),
            filter("a") { it != '"' }))),
        just('"')))
    { (_, str) -> str.charsToString() }

inline fun <I, O: Any, T: Any> delimitedBy(crossinline self: Parser<I, O>, crossinline delim: Parser<I, T>): Parser<I, RefVec<O>> =
    orElse(mapValue(then(repeated(thenIgnore(self, delim)), self))
        { (a, b) -> a.pushBack(b); a },
    value(RefVec.of()))