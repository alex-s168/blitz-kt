package blitz.parse.comb2

import blitz.Either
import blitz.Provider
import blitz.collections.contents
import blitz.partiallyFlattenA
import blitz.partiallyFlattenB
import blitz.str.charsToString

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
        }.partiallyFlattenA()
    }

fun <I, O, T> Parser<I, O>.thenIgnore(other: Parser<I, T>): Parser<I, O> =
    { ctx ->
        invoke(ctx).mapA { first ->
            other.invoke(ctx)
                .mapA { first }
        }.partiallyFlattenA()
    }

fun <I, O> Parser<I, O>.orElseVal(value: O): Parser<I, O> =
    orElse { Either.ofA(value) }

fun <I, O: Any> Parser<I, O>.orNot(): Parser<I, O?> =
    orElse { Either.ofA(null) }

fun <I, O, R> Parser<I, O>.orElse(other: Parser<I, R>): Parser<I, R> where O: R =
    {
        val old = it.copy()
        this(it).mapB { err ->
            it.loadFrom(old)
            other.invoke(it)
                .mapB { err + it }
        }.partiallyFlattenB()
    }

fun <I, O> choose(possible: Iterable<Parser<I, O>>): Parser<I, O> =
    { ctx ->
        val errors = mutableListOf<ParseError>()
        var res: O? = null
        for (p in possible) {
            val old = ctx.copy()
            val t = p.invoke(ctx)
            if (t.isA) {
                res = t.getA()
                break
            } else {
                ctx.loadFrom(old)
                errors += t.getB()
            }
        }
        res?.let { Either.ofA(it) }
            ?: Either.ofB(errors)
    }

fun <I, O> choose(vararg possible: Parser<I, O>): Parser<I, O> =
    choose(possible.toList())

fun <I, O> Parser<I, O>.repeated(): Parser<I, List<O>> =
    { ctx ->
        val out = mutableListOf<O>()
        while (true) {
            val old = ctx.copy()
            val t = invoke(ctx)
            if (t.isA) {
                out += t.getA()
            } else {
                ctx.loadFrom(old)
                break
            }
        }
        Either.ofA(out)
    }

inline fun <I, O> Parser<I, O>.verifyValue(crossinline verif: (O) -> String?): Parser<I, O> =
    { ctx ->
        invoke(ctx).mapA<ParseResult<O>> {
            verif(it)?.let { Either.ofB(listOf(ParseError(ctx.idx, it))) }
                ?: Either.ofA(it)
        }.partiallyFlattenA()
    }

inline fun <I, O> Parser<I, Pair<IntRange, O>>.verifyValueWithSpan(crossinline fn: (O) -> String?): Parser<I, O> =
    { ctx ->
        invoke(ctx).mapA<ParseResult<O>> { (span, v) ->
            fn(v)?.let { Either.ofB(listOf(ParseError(span.first, it))) }
                ?: Either.ofA(v)
        }.partiallyFlattenA()
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

fun <I, O> chain(parsers: List<Parser<I, O>>): Parser<I, List<O>> =
    { ctx ->
        val results = mutableListOf<O>()
        val errs = mutableListOf<ParseError>()
        for (p in parsers) {
            val r = p.invoke(ctx)
            if (r.isA) {
                results += r.getA()
            } else {
                errs += r.getB()
                break
            }
        }
        if (errs.isNotEmpty()) Either.ofB(errs)
        else Either.ofA(results)
    }

fun <I> seq(want: List<I>): Parser<I, List<I>> =
    chain(want.map(::just))

inline fun <I> filter(msg: String, crossinline filter: (I) -> Boolean): Parser<I, I> =
    { ctx ->
        if (ctx.idx >= ctx.input.size) {
            Either.ofB(listOf(ParseError(ctx.idx, "unexpected end of file")))
        } else {
            val i = ctx.input[ctx.idx++]
            if (filter(i)) Either.ofA(i)
            else Either.ofB(listOf(ParseError(ctx.idx - 1, msg)))
        }
    }

fun <I> just(want: I): Parser<I, I> =
    filter("expected $want") { it == want }

fun <I> oneOf(possible: Iterable<I>): Parser<I, I> =
    filter("expected one of ${possible.contents}") { it in possible }

fun <I, O> future(prov: Provider<Parser<I, O>>): Parser<I, O> =
    { prov()(it) }

inline fun <I, O> futureRec(crossinline fn: (future: Parser<I, O>) -> Parser<I, O>): Parser<I, O> {
    lateinit var f: Parser<I, O>
    f = fn(future { f })
    return f
}

/** group values 0 is the entire match */
fun <O> regex(pattern: Regex, fn: (groups: MatchGroupCollection) -> O): Parser<Char, O> =
    { ctx ->
        pattern.matchAt(ctx.input.charsToString(), ctx.idx)?.let {
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