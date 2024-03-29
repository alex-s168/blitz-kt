package blitz.collections

import kotlin.math.max

object Search {
    /**
     * Find a element by comparing.
     * Only works on sorted lists
     */
    fun <T, C: Comparable<C>> findElementComparing(
        list: List<T>,
        element: T,
        comparer: (T) -> C,
    ): Int {
        var rev = max(0, list.size / 2 - 1)
        var prev = 0

        while (true) {
            // outside of array:
            if (rev < 0)
                break

            // outside of array:
            if (rev >= list.size)
                break

            val cmp = comparer(element)
                .compareTo(comparer(list[rev]))

            // found:
            if (cmp == 0)
                return rev

            // in between of two existing values:
            if (cmp > 0 && prev < 0)
                break

            // in between of two existing values:
            if (cmp < 0 && prev > 0)
                break

            if (cmp < 0)
                rev --

            else if (cmp > 0)
                rev ++

            prev = cmp
        }

        return -1
    }
}