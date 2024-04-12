package blitz.collections

import kotlin.math.min

fun Sequence<Int>.movingAverage(factor: Double): Sequence<Double> {
    var avg = 0.0
    return mapIndexed { index, v->
        val count = index + 1
        avg += (v - avg) / min(count.toDouble(), factor)
        avg
    }
}