package blitz.collections

fun <T, C: Comparable<C>> SortedArrayList(sorter: (T) -> C) =
    SortedList(ArrayList(), sorter)