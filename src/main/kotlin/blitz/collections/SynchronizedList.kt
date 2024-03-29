package blitz.collections

import blitz.async.Lock

/**
 * Wraps any MutableList.
 * Concurrent by synchronizing.
 */
class SynchronizedList<T, L: MutableList<T>>(
    private val innerList: L
): MutableList<T>, RandomAccess {
    private val innerLock = Lock()

    fun <R> access(block: (L) -> R): R =
        innerLock.use { block(innerList) }

    override val size: Int
        get() = innerList.size

    override fun clear() =
        access { it.clear() }

    override fun addAll(elements: Collection<T>): Boolean =
        access { it.addAll(elements) }

    override fun addAll(index: Int, elements: Collection<T>): Boolean =
        access { it.addAll(index, elements) }

    override fun add(index: Int, element: T) =
        access { it.add(index, element) }

    override fun add(element: T): Boolean =
        access { it.add(element) }

    override fun get(index: Int): T =
        access { it.get(index) }

    override fun isEmpty(): Boolean =
        access { it.isEmpty() }

    override fun iterator(): MutableIterator<T> =
        access { it.iterator() }

    override fun listIterator(): MutableListIterator<T> =
        access { it.listIterator() }

    override fun listIterator(index: Int): MutableListIterator<T> =
        access { it.listIterator(index) }

    override fun removeAt(index: Int): T =
        access { it.removeAt(index) }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> =
        access { it.subList(fromIndex, toIndex) }

    override fun set(index: Int, element: T): T =
        access { it.set(index, element) }

    override fun retainAll(elements: Collection<T>): Boolean =
        access { it.retainAll(elements) }

    override fun removeAll(elements: Collection<T>): Boolean =
        access { it.removeAll(elements) }

    override fun remove(element: T): Boolean =
        access { it.remove(element) }

    override fun lastIndexOf(element: T): Int =
        access { it.lastIndexOf(element) }

    override fun indexOf(element: T): Int =
        access { it.indexOf(element) }

    override fun containsAll(elements: Collection<T>): Boolean =
        access { it.containsAll(elements) }

    override fun contains(element: T): Boolean =
        access { it.contains(element) }
}