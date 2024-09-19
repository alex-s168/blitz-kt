package blitz.parse.comb2

import blitz.str.charsToString
import kotlin.math.absoluteValue
import kotlin.math.sign

fun whitespaces(): Parser<Char, String> =
    oneOf("\n\t\r\b ".toList())
        .repeated()
        .mapValue { it.charsToString() }

fun digit(): Parser<Char, Char> =
    oneOf("0123456789".toList())

fun uintLit(): Parser<Char, UInt> =
    withSpan(digit().repeated())
        .verifyValueWithSpan { if (it.isEmpty()) "need digits after sign in num lit" else null }
        .mapValue { it.charsToString().toUInt() }

fun intLit(): Parser<Char, Int> =
    choose(just('+').mapValue { +1 },
           just('-').mapValue { -1 },
           value(+1))
        .then(uintLit())
        .mapValue { (sign, v) -> sign * v.toInt() }

fun floatLit(): Parser<Char, Double> =
    intLit()
        .then(just('.')
            .then(uintLit())
            .mapValue { it.second }
            .orElseVal(0u))
        .mapValue { (pre, post) ->
            var p = post.toDouble()
            while (p.absoluteValue >= 1) {
                p *= 0.1
            }

            (pre.toDouble().absoluteValue + p) * pre.toDouble().sign
        }

fun escapeChar(): Parser<Char, Char> =
    just('\\').then(
        choose(just('"'),
               just('\''),
               just('\\'),
               just('n').mapValue { '\n' },
               just('r').mapValue { '\r' },
               just('b').mapValue { '\b' },
               just('t').mapValue { '\t' })
            .mapErrors { listOf(ParseError(it.first().loc, "invalid escape sequence")) }
    ).mapValue { it.second }

fun stringLit(): Parser<Char, String> =
    just('"')
        .then(choose(escapeChar(),
                     filter("a") { it != '"' })
            .repeated())
        .thenIgnore(just('"'))
        .mapValue { (_, str) -> str.charsToString() }

fun <I, O, T> Parser<I, O>.delimitedBy(delim: Parser<I, T>): Parser<I, List<O>> =
    thenIgnore(delim)
        .repeated()
        .then(this)
        .mapValue { (a, b) -> a + b }
        .orElse(value(listOf()))