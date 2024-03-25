package blitz

import blitz.collections.selfInitializingSequence

class OperationChain<I, O> private constructor(
    private val impl: Impl = Impl()
) {
    private var until = 0

    private class Impl {
        val seqe = mutableListOf<(Sequence<Any?>) -> Sequence<Any?>>()

        var finalized = false

        fun add(op: Operator<*, *>) {
            seqe += { seq: Sequence<Any?> ->
                seq.map(op as Operator<Any?, Any?>)
            }
        }

        fun addFlat(op: Operator<*, Sequence<*>>) {
            seqe += { seq: Sequence<Any?> ->
                seq.flatMap(op as Operator<Any?, Sequence<Any?>>)
            }
        }
    }

    fun <NO> map(op: Operator<O, NO>): OperationChain<I, NO> =
        OperationChain<I, NO>(impl.also { it.add(op) })
            .also { it.until = this.until + 1 }

    fun <NO> flatMap(op: Operator<O, Sequence<NO>>): OperationChain<I, NO> =
        OperationChain<I, NO>(impl.also { it.addFlat(op) })
            .also { it.until = this.until + 1 }

    fun <NO> map(op: OperationChain<O, NO>): OperationChain<I, NO> {
        if (!op.impl.finalized)
            throw Exception("Can not map un-finalized operation chain onto operation chain!")
        return flatMap(op::process)
    }

    fun <NO> modifier(op: Operator<Sequence<O>, Sequence<NO>>): OperationChain<I, NO> =
        OperationChain<I, NO>(impl.also { it.seqe.add(op as (Sequence<Any?>) -> Sequence<Any?>) })
            .also { it.until = this.until + 1 }

    fun finalize(): OperationChain<I, O> {
        if (impl.finalized)
            throw Exception("Can't finalize a finalized OperationChain!")
        impl.finalized = true
        return this
    }

    fun process(v: I): Sequence<O> =
        selfInitializingSequence {
            var seq = sequenceOf<Any?>(v)
            impl.seqe
                .asSequence()
                .take(until)
                .forEach { op ->
                    seq = op(seq)
                }
            seq as Sequence<O>
        }

    fun processAll(v: Sequence<I>): Sequence<O> =
        v.flatMap { process(it) }

    companion object {
        internal fun <I> create(): OperationChain<I, I> =
            OperationChain()
    }
}

fun <T, O> Sequence<T>.map(chain: OperationChain<T, O>): Sequence<O> =
    chain.processAll(this)

fun <I> chain(): OperationChain<I, I> =
    OperationChain.create()

fun <I, O> OperationChain<I, O>.chunked(size: Int): OperationChain<I, List<O>> =
    modifier { it.chunked(size) }

fun <I, O, R> OperationChain<I, O>.chunked(size: Int, transform: (List<O>) -> R): OperationChain<I, R> =
    modifier { it.chunked(size, transform) }

fun <I, O> OperationChain<I, O>.filter(predicate: (O) -> Boolean): OperationChain<I, O> =
    modifier { it.filter(predicate) }