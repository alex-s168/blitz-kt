package blitz.parse.comb2

import blitz.collections.RefVec
import blitz.str.charsToString
import kotlin.math.absoluteValue
import kotlin.math.sign

private fun isWhitespace(it: Char) =
    it == ' ' || it == '\n' || it == '\t' || it == '\r' || it == '\b'
val whitespaces: Parser<Char, Unit> =
    repeatedNoSave(filter("expected whitespace", ::isWhitespace))

val digit: Parser<Char, Char> =
    filter("expected digit") { it >= '0' && it <= '9' }

val uintLit: Parser<Char, RefVec<Char>> =
    verifyValue(repeated(digit))
    { if (it.size == 0) "need digits after sign in num lit" else null }

val intLit: Parser<Char, Long> =
    mapValue(then(choose<Char, Int> {
        it(mapValue(just('+')) { +1 })
        it(mapValue(just('-')) { -1 })
        it(value(+1))
     }, uintLit))
    { (sign, v) -> sign * (v.charsToString().toLongOrNull() ?: Long.MAX_VALUE) }

val floatLit: Parser<Char, Double> =
    mapValue(
            then(
                intLit,
                orElseVal(
                    thenOverwrite(just('.'), uintLit),
                    RefVec.of('0'))))
    { (pre, post) ->
        var p = post.charsToString().toDouble()
        while (p.absoluteValue >= 1) {
            p *= 0.1
        }

        (pre.toDouble().absoluteValue + p) * pre.toDouble().sign
    }

val escapeChar: Parser<Char, Char> =
    thenOverwrite(just('\\'),
        mapErrors(choose {
            it(just('"'))
            it(just('\''))
            it(just('\\'))
            it(mapValue(just('n')) { '\n' })
            it(mapValue(just('r')) { '\r' })
            it(mapValue(just('b')) { '\b' })
            it(mapValue(just('t')) { '\t' })
        })
        { ParseError(it.loc, "invalid escape sequence") }
    )

val stringLit: Parser<Char, String> =
    mapValue(thenIgnore(then(just('"'),
        repeated(choose<Char,Char>{
            it(escapeChar)
            it(filter("a") { it != '"' })
        })),
        just('"')))
    { (_, str) -> str.charsToString() }

inline fun <I, O: Any, T: Any> delimitedBy(crossinline self: Parser<I, O>, crossinline delim: Parser<I, T>): Parser<I, RefVec<O>> =
    orElse(mapValue(then(repeated(thenIgnore(self, delim)), self))
        { (a, b) -> a.pushBack(b); a },
    value(RefVec.of()))