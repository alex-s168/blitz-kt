package blitz.parse.comb2

import blitz.Either
import blitz.partiallyFlatten

data class ParseCtx<I>(
    val input: List<I>,
    var idx: Int
) {
    fun loadFrom(old: ParseCtx<I>) {
        idx = old.idx
    }
}

data class ParseError(
    val loc: Int,
    val message: String?,
)

typealias ParseResult<O> = Either<O, List<ParseError>>
typealias Parser<I, O> = (ParseCtx<I>) -> ParseResult<O>

inline fun <I, M, O> Parser<I, M>.mapValue(crossinline fn: (M) -> O): Parser<I, O> =
    { invoke(it).mapA { fn(it) } }

inline fun <I, O> Parser<I, O>.mapErrors(crossinline fn: (List<ParseError>) -> List<ParseError>): Parser<I, O> =
    { invoke(it).mapB { fn(it) } }

fun <I, M, O> Parser<I, M>.then(other: Parser<I, O>): Parser<I, Pair<M, O>> =
    { ctx ->
        invoke(ctx).mapA { first ->
            other.invoke(ctx)
                .mapA { first to it }
        }.partiallyFlatten()
    }

fun <I, O, T> Parser<I, O>.thenIgnore(other: Parser<I, T>): Parser<I, O> =
    { ctx ->
        invoke(ctx).mapA { first ->
            other.invoke(ctx)
                .mapA { first }
        }.partiallyFlatten()
    }

fun <I, O> Parser<I, O>.orElse(other: Parser<I, O>): Parser<I, O> =
    {
        val old = it.copy()
        this(it).mapB { err ->
            it.loadFrom(old)
            other.invoke(it)
                .mapB { err + it }
        }.partiallyFlatten()
    }

fun <I, O> Parser<I, O>.repeated(): Parser<I, List<O>> =
    { ctx ->
        val out = mutableListOf<O>()
        var ret: List<ParseError>? = null
        while (true) {
            val old = ctx.copy()
            val t = invoke(ctx)
            if (t.isA) {
                out += t.getA()
            } else {
                ctx.loadFrom(old)
                ret = t.getB()
                break
            }
        }
        if (ret == null) {
            Either.ofA(out)
        } else Either.ofB(ret)
    }

fun <I, O> Parser<I, O>.delimitedBy(delim: Parser<I, O>): Parser<I, List<O>> =
    thenIgnore(delim)
        .repeated()
        .then(this)
        .mapValue { (a, b) -> a + b }
        .orElse(value(listOf()))

inline fun <I, O> Parser<I, O>.verifyValue(crossinline verif: (O) -> String?): Parser<I, O> =
    { ctx ->
        invoke(ctx).mapA<ParseResult<O>> {
            verif(it)?.let { Either.ofB(listOf(ParseError(ctx.idx, it))) }
                ?: Either.ofA(it)
        }.partiallyFlatten()
    }

inline fun <I, O> Parser<I, Pair<IntRange, O>>.verifyValueWithSpan(crossinline fn: (O) -> String?): Parser<I, O> =
    { ctx ->
        invoke(ctx).mapA<ParseResult<O>> { (span, v) ->
            fn(v)?.let { Either.ofB(listOf(ParseError(span.first, it))) }
                ?: Either.ofA(v)
        }.partiallyFlatten()
    }

fun <I, O: Any?> Parser<I, O?>.errIfNull(msg: String = "parser value was null internally"): Parser<I, O> =
    verifyValue { if (it == null) msg else null }
        .mapValue { it!! }

inline fun <I, O> location(crossinline fn: (Int) -> O): Parser<I, O> =
    { Either.ofA(fn(it.idx)) }

fun <I> location(): Parser<I, Int> =
    location { it }

fun <I, O> withSpan(p: Parser<I, O>): Parser<I, Pair<IntRange, O>> =
    location<I>()
        .then(p)
        .then(location())
        .mapValue { (beginAndV, end) ->
            (beginAndV.first..end) to beginAndV.second
        }

fun <I, O> value(value: O): Parser<I, O> =
    { Either.ofA(value) }

fun whitespaces(): Parser<Char, String> =
    regex("\\s+")

fun <I> just(want: I): Parser<I, I> =
    { ctx ->
        val i = ctx.input[ctx.idx ++]
        if (i == want) Either.ofA(i)
        else Either.ofB(listOf(ParseError(ctx.idx - 1, "expected $want")))
    }

/** group values 0 is the entire match */
fun <O> regex(pattern: Regex, fn: (groups: MatchGroupCollection) -> O): Parser<Char, O> =
    { ctx ->
        pattern.matchAt(ctx.input.toString(), ctx.idx)?.let {
            ctx.idx = it.range.last + 1
            Either.ofA(fn(it.groups))
        } ?: Either.ofB(listOf(
            ParseError(ctx.idx, "regular expression \"$pattern\" does not apply")
        ))
    }

fun regex(pattern: Regex) = regex(pattern) { it[0]!!.value }

/** group values 0 is the entire match */
fun <O> regex(pattern: String, fn: (groups: MatchGroupCollection) -> O): Parser<Char, O> =
    regex(Regex(pattern), fn)

fun regex(pattern: String) = regex(pattern) { it[0]!!.value }