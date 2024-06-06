package blitz

import blitz.async.Lock

interface Obj<T> {
    val v: T

    companion object {
        fun <T> of(v: T): Obj<T> =
            object : Obj<T> {
                override val v: T = v

                override fun toString(): String =
                    "Obj($v)"

                override fun equals(other: Any?): Boolean =
                    (other is Obj<*>) && v == other.v

                override fun hashCode(): Int = v.hashCode()
            }
    }
}

fun <I, O> Obj<I>?.mapNotNull(transform: (I) -> O): Obj<O>? =
    this?.v?.let { Obj.of(transform(it)) }

fun <I, O> Obj<I>.map(transform: (I) -> O): Obj<O> =
    Obj.of(transform(v))

interface MutObj<T> {
    var v: T

    companion object {
        fun <T> of(v: T): MutObj<T> =
            object : MutObj<T> {
                override var v: T = v

                override fun toString(): String =
                    "MutObj($v)"

                override fun equals(other: Any?): Boolean =
                    (other is Obj<*>) && v == other.v

                override fun hashCode(): Int = v.hashCode()
            }

        fun <T> mutex(v: T): MutObj<T> =
            object : MutObj<T> {
                val lock = Lock()
                override var v: T = v
                    get() = lock.use { field }
                    set(inp) = lock.use { field = inp }

                override fun toString(): String =
                    "MutMutexObj($v)"

                override fun equals(other: Any?): Boolean =
                    (other is Obj<*>) && v == other.v

                override fun hashCode(): Int = v.hashCode()
            }
    }
}