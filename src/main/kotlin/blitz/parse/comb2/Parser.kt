package blitz.parse.comb2

import blitz.*
import blitz.collections.RefVec
import blitz.str.charsToString

data class ParseCtx<I>(
    @JvmField val input: List<I>,
    @JvmField var idx: Int
) {
    fun loadFrom(old: ParseCtx<I>) {
        idx = old.idx
    }
}

data class ParseError(
    @JvmField val loc: Int, /** can be -1 */
    @JvmField val message: String?,
)

typealias ParseResult<O> = Either<O, ParseError>
typealias Parser<I, O> = (ParseCtx<I>) -> ParseResult<O>

inline fun <I, M: Any, O: Any> mapValue(crossinline self: Parser<I, M>, crossinline fn: (M) -> O): Parser<I, O> =
    {
        val r = self(it) as Either<Any, ParseError>
        r.a?.let {
            r.a = fn(it as M)
        }
        r as Either<O, ParseError>
    }

inline fun <I, O: Any> mapErrors(crossinline self: Parser<I, O>, crossinline fn: (ParseError) -> ParseError): Parser<I, O> =
    {
        val r = self(it)
        r.b?.let { r.b = fn(it) }
        r
    }

inline fun <I, M: Any, O: Any> then(crossinline self: Parser<I, M>, crossinline other: Parser<I, O>): Parser<I, Pair<M, O>> =
    { ctx ->
        val r0 = self(ctx) as ParseResult<Any>
        r0.a?.let { first ->
            val r1 = other(ctx)
            r1.a?.let { second ->
                (r1 as ParseResult<Any>).a = Pair(first, second)
                (r1 as ParseResult<Pair<M, O>>)
            } ?: (r1 as ParseResult<Pair<M, O>>)
        } ?: (r0 as ParseResult<Pair<M, O>>)
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
        self(it).mapB { _ ->
            it.idx = old
            other.invoke(it)
        }.partiallyFlattenB()
    }

/** Use the other choose that takes a function whenever possible because of perf */
fun <I, O: Any> choose(possible: Iterable<Parser<I, O>>): Parser<I, O> =
    { ctx ->
        var res: O? = null
        for (p in possible) {
            val old = ctx.idx
            val t = p.invoke(ctx)
            if (t.isA()) {
                res = t.a!!
                break
            } else {
                ctx.idx = old
            }
        }
        res?.let { Either.ofA(it) }
            ?: Either.ofB(ParseError(ctx.idx, "none of the possible parsers match"))
    }

inline fun <I, O: Any> choose(crossinline fn: (run: (Parser<I, O>) -> Unit) -> Unit): Parser<I, O> =
    { ctx ->
        var res: O? = null
        fn { p ->
            if (res == null) {
                val old = ctx.idx
                val t = p.invoke(ctx)
                if (t.isA()) {
                    res = t.a!!
                } else {
                    ctx.idx = old
                }
            }
        }
        res?.let { Either.ofA(it) }
            ?: Either.ofB(ParseError(ctx.idx, "none of the possible parsers match"))
    }

/** Use the other choose that takes a function whenever possible because of perf */
inline fun <I, O: Any> choose(vararg possible: Parser<I, O>): Parser<I, O> =
    choose(possible.toList())

inline fun <I, O: Any> repeated(crossinline what: Parser<I, O>): Parser<I, RefVec<O>> =
    { ctx ->
        val out = RefVec<O>(16)
        while (true) {
            val old = ctx.idx
            val t = what(ctx)
            if (t.isA()) {
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
            if (t.isB()) {
                ctx.idx = old
                break
            }
        }
        Either.ofA(Unit)
    }

inline fun <I, O: Any> verifyValue(crossinline self: Parser<I, O>, crossinline verif: (O) -> String?): Parser<I, O> =
    { ctx ->
        self(ctx).flatMapA<_,_,_> {
            verif(it)?.let { Either.ofB(ParseError(ctx.idx, it)) }
                ?: Either.ofA(it)
        }
    }

inline fun <I, O: Any> verifyValueWithSpan(crossinline self: Parser<I, Pair<IntRange, O>>, crossinline fn: (O) -> String?): Parser<I, O> =
    { ctx ->
        self(ctx).flatMapA<_,_,_> { (span, v) ->
            fn(v)?.let { Either.ofB(ParseError(span.first, it)) }
                ?: Either.ofA(v)
        }
    }

inline fun <I, O: Any> location(crossinline fn: (Int) -> O): Parser<I, O> =
    { Either.ofA(fn(it.idx)) }

inline fun <I> location(): Parser<I, Int> =
    location { it }

fun <I, O: Any> withSpan(p: Parser<I, O>): Parser<I, Pair<IntRange, O>> =
    mapValue(then(then(location(), p), location())) { (beginAndV, end) ->
        (beginAndV.first..end) to beginAndV.second
    }

inline fun <I, O: Any> value(value: O): Parser<I, O> =
    { Either.ofA(value) }

fun <I, O: Any> chain(parsers: List<Parser<I, O>>): Parser<I, RefVec<O>> =
    { ctx ->
        val results = RefVec<O>(parsers.size)
        var errs: ParseError? = null
        for (p in parsers) {
            val r = p.invoke(ctx)
            if (r.isA()) {
                results.pushBack(r.a!!)
            } else {
                errs = r.b!!
                break
            }
        }
        if (errs != null) Either.ofB(errs)
        else Either.ofA(results)
    }

inline fun <I: Any> seq(want: List<I>): Parser<I, RefVec<I>> =
    chain(want.map(::just))

inline fun <I: Any> filter(msg: String, crossinline filter: (I) -> Boolean): Parser<I, I> =
    { ctx ->
        if (ctx.idx >= ctx.input.size) {
            Either.ofB(ParseError(ctx.idx, "unexpected end of file"))
        } else {
            val i = ctx.input[ctx.idx++]
            if (filter(i)) Either.ofA(i)
            else Either.ofB(ParseError(ctx.idx - 1, msg))
        }
    }

private class JustParse<I: Any>(wantIn: I): Parser<I, I> {
    @JvmField val want = wantIn
    @JvmField val uef: ParseResult<I> = Either.ofB(ParseError(-1, "unexpected end of file"))
    @JvmField val exdiff: ParseResult<I> = Either.ofB(ParseError(-1,  "expected $wantIn"))
    @JvmField val eitherOfWant: ParseResult<I> = Either.ofA(want)
    override fun invoke(ctx: ParseCtx<I>): ParseResult<I> {
        return if (ctx.idx >= ctx.input.size) uef
        else {
            val i = ctx.input[ctx.idx++]
            if (i == want) eitherOfWant
            else exdiff
        }
    }
}

fun <I: Any> just(wantIn: I): Parser<I, I> =
    JustParse(wantIn)

inline fun <I: Any> oneOf(possible: Iterable<I>): Parser<I, I> =
    filter("expected different") { it in possible }

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
        } ?: Either.ofB(
            ParseError(ctx.idx, "regular expression \"$pattern\" does not apply")
        )
    }

fun regex(pattern: Regex) = regex(pattern) { it[0]!!.value }

/** group values 0 is the entire match */
fun <O: Any> regex(pattern: String, fn: (groups: MatchGroupCollection) -> O): Parser<Char, O> =
    regex(Regex(pattern), fn)

fun regex(pattern: String) = regex(pattern) { it[0]!!.value }