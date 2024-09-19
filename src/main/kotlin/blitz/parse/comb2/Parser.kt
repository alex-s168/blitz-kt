package blitz.parse.comb2

import blitz.*
import blitz.collections.RefVec
import blitz.collections.contents
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

typealias ParseResult<O> = Either<O, RefVec<ParseError>>
typealias Parser<I, O> = (ParseCtx<I>) -> ParseResult<O>

inline fun <I, M: Any, O: Any> mapValue(crossinline self: Parser<I, M>, crossinline fn: (M) -> O): Parser<I, O> =
    { self(it).mapA { fn(it) } }

inline fun <I, O: Any> mapErrors(crossinline self: Parser<I, O>, crossinline fn: (RefVec<ParseError>) -> RefVec<ParseError>): Parser<I, O> =
    { self(it).mapB { fn(it) } }

inline fun <I, M: Any, O: Any> then(crossinline self: Parser<I, M>, crossinline other: Parser<I, O>): Parser<I, Pair<M, O>> =
    { ctx ->
        self(ctx).flatMapA<_,_,Pair<M,O>> { first ->
            other.invoke(ctx)
                .mapA { first to it }
        }
    }

inline fun <I, M: Any, O: Any> thenOverwrite(crossinline self: Parser<I, M>, crossinline other: Parser<I, O>): Parser<I, O> =
    { ctx ->
        self(ctx).flatMapA<_,_,O> {
            other.invoke(ctx)
        }
    }

inline fun <I, O: Any, T: Any> thenIgnore(crossinline self: Parser<I, O>, crossinline other: Parser<I, T>): Parser<I, O> =
    { ctx ->
        self(ctx).flatMapA { first ->
            other.invoke(ctx)
                .mapA { first }
        }
    }

inline fun <I, O: Any> orElseVal(crossinline self: Parser<I, O>, value: O): Parser<I, O> =
    orElse(self) { Either.ofA(value) }

inline fun <I, O, R: Any> orElse(crossinline self: Parser<I, O>, crossinline other: Parser<I, R>): Parser<I, R> where O: R =
    {
        val old = it.idx
        self(it).mapB { err ->
            it.idx = old
            other.invoke(it)
                .mapB { err.pushBack(it); err }
        }.partiallyFlattenB()
    }

fun <I, O: Any> choose(possible: Iterable<Parser<I, O>>): Parser<I, O> =
    { ctx ->
        val errors = RefVec<ParseError>(possible.count())
        var res: O? = null
        for (p in possible) {
            val old = ctx.idx
            val t = p.invoke(ctx)
            if (t.isA) {
                res = t.a!!
                break
            } else {
                ctx.idx = old
                errors.pushBack(t.b!!)
            }
        }
        res?.let { Either.ofA(it) }
            ?: Either.ofB(errors)
    }

fun <I, O: Any> choose(vararg possible: Parser<I, O>): Parser<I, O> =
    choose(possible.toList())

inline fun <I, O: Any> repeated(crossinline what: Parser<I, O>): Parser<I, RefVec<O>> =
    { ctx ->
        val out = RefVec<O>(0)
        while (true) {
            val old = ctx.idx
            val t = what(ctx)
            if (t.isA) {
                out.pushBack(t.a!!)
            } else {
                ctx.idx = old
                break
            }
        }
        Either.ofA(out)
    }

inline fun <I, O: Any> repeatedNoSave(crossinline what: Parser<I, O>): Parser<I, Unit> =
    { ctx ->
        while (true) {
            val old = ctx.idx
            val t = what(ctx)
            if (t.isB) {
                ctx.idx = old
                break
            }
        }
        Either.ofA(Unit)
    }

inline fun <I, O: Any> verifyValue(crossinline self: Parser<I, O>, crossinline verif: (O) -> String?): Parser<I, O> =
    { ctx ->
        self(ctx).flatMapA<_,_,_> {
            verif(it)?.let { Either.ofB(RefVec.of(ParseError(ctx.idx, it))) }
                ?: Either.ofA(it)
        }
    }

inline fun <I, O: Any> verifyValueWithSpan(crossinline self: Parser<I, Pair<IntRange, O>>, crossinline fn: (O) -> String?): Parser<I, O> =
    { ctx ->
        self(ctx).flatMapA<_,_,_> { (span, v) ->
            fn(v)?.let { Either.ofB(RefVec.of(ParseError(span.first, it))) }
                ?: Either.ofA(v)
        }
    }

inline fun <I, O: Any> location(crossinline fn: (Int) -> O): Parser<I, O> =
    { Either.ofA(fn(it.idx)) }

inline fun <I> location(): Parser<I, Int> =
    location { it }

inline fun <I, O: Any> withSpan(crossinline p: Parser<I, O>): Parser<I, Pair<IntRange, O>> =
    mapValue(then(then(location(), p), location())) { (beginAndV, end) ->
        (beginAndV.first..end) to beginAndV.second
    }

inline fun <I, O: Any> value(value: O): Parser<I, O> =
    { Either.ofA(value) }

fun <I, O: Any> chain(parsers: List<Parser<I, O>>): Parser<I, RefVec<O>> =
    { ctx ->
        val results = RefVec<O>(parsers.size)
        val errs = RefVec<ParseError>(0)
        for (p in parsers) {
            val r = p.invoke(ctx)
            if (r.isA) {
                results.pushBack(r.a!!)
            } else {
                errs.pushBack(r.b!!)
                break
            }
        }
        if (errs.size != 0) Either.ofB(errs)
        else Either.ofA(results)
    }

inline fun <I: Any> seq(want: List<I>): Parser<I, RefVec<I>> =
    chain(want.map(::just))

inline fun <I: Any> filter(msg: String, crossinline filter: (I) -> Boolean): Parser<I, I> =
    { ctx ->
        if (ctx.idx >= ctx.input.size) {
            Either.ofB(RefVec.of(ParseError(ctx.idx, "unexpected end of file")))
        } else {
            val i = ctx.input[ctx.idx++]
            if (filter(i)) Either.ofA(i)
            else Either.ofB(RefVec.of(ParseError(ctx.idx - 1, msg)))
        }
    }

inline fun <I: Any> just(want: I): Parser<I, I> =
    filter("expected $want") { it == want }

inline fun <I: Any> oneOf(possible: Iterable<I>): Parser<I, I> =
    filter("expected one of ${possible.contents}") { it in possible }

inline fun <I, O: Any> future(crossinline prov: Provider<Parser<I, O>>): Parser<I, O> =
    { prov()(it) }

inline fun <I, O: Any> futureRec(fn: (future: Parser<I, O>) -> Parser<I, O>): Parser<I, O> {
    lateinit var f: Parser<I, O>
    f = fn({ f(it) })
    return f
}

/** group values 0 is the entire match */
fun <O: Any> regex(pattern: Regex, fn: (groups: MatchGroupCollection) -> O): Parser<Char, O> =
    { ctx ->
        pattern.matchAt(ctx.input.charsToString(), ctx.idx)?.let {
            ctx.idx = it.range.last + 1
            Either.ofA(fn(it.groups))
        } ?: Either.ofB(RefVec.of(
            ParseError(ctx.idx, "regular expression \"$pattern\" does not apply")
        ))
    }

fun regex(pattern: Regex) = regex(pattern) { it[0]!!.value }

/** group values 0 is the entire match */
fun <O: Any> regex(pattern: String, fn: (groups: MatchGroupCollection) -> O): Parser<Char, O> =
    regex(Regex(pattern), fn)

fun regex(pattern: String) = regex(pattern) { it[0]!!.value }