package blitz.math

infix fun Pair<Int, Int>.lerpi(t: Double): Double =
    (1.0f - t) * first + second * t

infix fun Pair<Double, Double>.lerpd(t: Double): Double =
    (1.0f - t) * first + second * t

infix fun Pair<Float, Float>.lerpf(t: Double): Double =
    (1.0f - t) * first + second * t
