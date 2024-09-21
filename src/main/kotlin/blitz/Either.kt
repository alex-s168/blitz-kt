package blitz

class Either<A: Any, B: Any> private constructor(
    aIn: A?, bIn: B?
) {
    /** DO NOT SET MANUALLY!!! */
    @JvmField
    var a: A? = aIn

    /** DO NOT SET MANUALLY!!! */
    @JvmField
    var b: B? = bIn

    override fun equals(other: Any?): Boolean =
        other is Either<*, *> && other.a == a && other.b == b

    fun assertA(): A =
        (a ?: throw Exception("Value of Either is not of type A!"))

    fun assertB(): B =
        (b ?: throw Exception("Value of Either is not of type B!"))

    override fun toString(): String =
        if (isA()) "Either<A>(${a!!})"
        else "Either<B>(${b!!})"

    override fun hashCode(): Int {
        var result = a?.hashCode() ?: 0
        result = 31 * result + (b?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun <A: Any, B: Any> unsafeCreate(a: A?, b: B?, pool: StupidObjPool<Either<*,*>>? = null): Either<A, B> =
            Either(a, b)

        inline fun <A: Any, B: Any> ofA(a: A, pool: StupidObjPool<Either<*,*>>? = null): Either<A, B> =
            unsafeCreate(a, null, pool)

        inline fun <A: Any, B: Any> ofB(b: B, pool: StupidObjPool<Either<*,*>>? = null): Either<A, B> =
            unsafeCreate(null, b, pool)
    }
}

inline fun <A: Any, B: Any> Either<A, B>.isA() = a != null
inline fun <A: Any, B: Any> Either<A, B>.isB() = b != null

inline fun <A: Any, B: Any> Either<A, B>.getAOr(prov: Provider<A>): A =
    a ?: prov()

inline fun <A: Any, B: Any> Either<A, B>.getBOr(prov: Provider<B>): B =
    b ?: prov()

inline fun <A: Any, B: Any, R> Either<A, B>.then(af: (A) -> R, bf: (B) -> R): R =
    if (isA()) af(a!!) else bf(b!!)

inline fun <A: Any, B: Any, RA: Any> Either<A, B>.mapA(transform: (A) -> RA): Either<RA, B> =
    Either.unsafeCreate(a?.let(transform), b)

inline fun <A: Any, B: Any> Either<A, B>.flatMapA(transform: (A) -> Either<A, B>): Either<A, B> =
    if (a != null) {
        transform(a!!)
    } else this

inline fun <A: Any, B: Any> Either<A, B>.flatMapB(transform: (B) -> Either<A, B>): Either<A, B> =
    if (b != null) {
        transform(b!!)
    } else this

@JvmName("flatMapA_changeType")
inline fun <A: Any, B: Any, RA: Any> Either<A, B>.flatMapA(transform: (A) -> Either<RA, B>): Either<RA, B> =
    if (a != null) {
        transform(a!!)
    } else Either.ofB(b!!)

@JvmName("flatMapB_changeType")
inline fun <A: Any, B: Any, RB: Any> Either<A, B>.flatMapB(transform: (B) -> Either<A, RB>): Either<A, RB> =
    if (b != null) {
        transform(b!!)
    } else Either.ofA(a!!)

inline fun <A: Any, B: Any, RB: Any> Either<A, B>.mapB(transform: (B) -> RB): Either<A, RB> =
    Either.unsafeCreate(a, b?.let(transform))

fun <A, B, R: Any> Either<A, B>.flatten(): R where A: R, B: R =
    a ?: assertB()

fun <A, A2: Any, B: Any> Either<A, Either<A2, B>>.partiallyFlattenB(): Either<A2, B> where A: A2 =
    mapA { Either.ofA<A2, B>(it) }.flatten()

fun <A: Any, B, B2: Any> Either<Either<A, B2>, B>.partiallyFlattenA(): Either<A, B2> where B: B2 =
    mapB { Either.ofB<A, B2>(it) }.flatten()

inline fun <A: Any, BA: Any, BB: Any, BAN: Any> Either<A, Either<BA, BB>>.mapBA(fn: (BA) -> BAN): Either<A, Either<BAN, BB>> =
    mapB { it.mapA(fn) }

inline fun <A: Any, BA: Any, BB: Any, BBN: Any> Either<A, Either<BA, BB>>.mapBB(fn: (BB) -> BBN): Either<A, Either<BA, BBN>> =
    mapB { it.mapB(fn) }

inline fun <AA: Any, AB: Any, B: Any, AAN: Any> Either<Either<AA, AB>, B>.mapAA(fn: (AA) -> AAN): Either<Either<AAN, AB>, B> =
    mapA { it.mapA(fn) }

inline fun <AA: Any, AB: Any, B: Any, ABN: Any> Either<Either<AA, AB>, B>.mapAB(fn: (AB) -> ABN): Either<Either<AA, ABN>, B> =
    mapA { it.mapB(fn) }

fun <AA: Any, AB: Any, B: Any> Either<Either<AA, AB>, B>.getAAOrNull(): AA? =
    a?.a

fun <AA: Any, AB: Any, B: Any> Either<Either<AA, AB>, B>.getABOrNull(): AB? =
    a?.b

fun <A: Any, BA: Any, BB: Any> Either<A, Either<BA, BB>>.getBAOrNull(): BA? =
    b?.a

fun <A: Any, BA: Any, BB: Any> Either<A, Either<BA, BB>>.getBBOrNull(): BB? =
    b?.b