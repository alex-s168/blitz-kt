package blitz

class Either<A, B> private constructor(
    private val a: Obj<A>?,
    private val b: Obj<B>?
) {
    override fun equals(other: Any?): Boolean =
        other is Either<*, *> && other.a == a && other.b == b

    fun getAOrNull(): A? =
        a?.v

    fun getA(): A =
        (a ?: throw Exception("Value of Either is not of type A!")).v

    fun getAOr(prov: Provider<A>): A =
        getAOrNull() ?: prov()

    fun getBOrNull(): B? =
        b?.v

    fun getB(): B =
        (b ?: throw Exception("Value of Either is not of type B!")).v

    fun getBOr(prov: Provider<B>): B =
        getBOrNull() ?: prov()

    val isA: Boolean =
        a != null

    val isB: Boolean =
        b != null

    fun <R> then(af: (A) -> R, bf: (B) -> R): R =
        if (isA) af(a!!.v) else bf(b!!.v)

    fun <RA> mapA(transform: (A) -> RA): Either<RA, B> =
        Either(a.mapNotNull(transform), b)

    fun <RB> mapB(transform: (B) -> RB): Either<A, RB> =
        Either(a, b.mapNotNull(transform))

    override fun toString(): String =
        if (isA) "Either<A>(${a!!.v})"
        else "Either<B>(${b!!.v})"

    override fun hashCode(): Int {
        var result = a?.hashCode() ?: 0
        result = 31 * result + (b?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun <A, B> ofA(a: A): Either<A, B> =
            Either(Obj.of(a), null)

        fun <A, B> ofB(b: B): Either<A, B> =
            Either(null, Obj.of(b))
    }
}

fun <A, B, R> Either<A, B>.flatten(): R where A: R, B: R =
    getAOrNull() ?: getB()

fun <A, A2, B> Either<A, Either<A2, B>>.partiallyFlattenB(): Either<A2, B> where A: A2 =
    mapA<Either<A2, B>> { Either.ofA(it) }.flatten()

fun <A, B, B2> Either<Either<A, B2>, B>.partiallyFlattenA(): Either<A, B2> where B: B2 =
    mapB<Either<A, B2>> { Either.ofB(it) }.flatten()

fun <A, BA, BB, BAN> Either<A, Either<BA, BB>>.mapBA(fn: (BA) -> BAN): Either<A, Either<BAN, BB>> =
    mapB { it.mapA(fn) }

fun <A, BA, BB, BBN> Either<A, Either<BA, BB>>.mapBB(fn: (BB) -> BBN): Either<A, Either<BA, BBN>> =
    mapB { it.mapB(fn) }

fun <AA, AB, B, AAN> Either<Either<AA, AB>, B>.mapAA(fn: (AA) -> AAN): Either<Either<AAN, AB>, B> =
    mapA { it.mapA(fn) }

fun <AA, AB, B, ABN> Either<Either<AA, AB>, B>.mapAB(fn: (AB) -> ABN): Either<Either<AA, ABN>, B> =
    mapA { it.mapB(fn) }

fun <AA, AB, B> Either<Either<AA, AB>, B>.getAAOrNull(): AA? =
    getAOrNull()?.getAOrNull()

fun <AA, AB, B> Either<Either<AA, AB>, B>.getABOrNull(): AB? =
    getAOrNull()?.getBOrNull()

fun <A, BA, BB> Either<A, Either<BA, BB>>.getBAOrNull(): BA? =
    getBOrNull()?.getAOrNull()

fun <A, BA, BB> Either<A, Either<BA, BB>>.getBBOrNull(): BB? =
    getBOrNull()?.getBOrNull()