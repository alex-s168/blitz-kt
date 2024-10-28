package blitz

data class SwitchCase<C, T: Any, R>(
    val cond: (C) -> Pair<Boolean, T?>,
    val then: (T) -> R,
)

inline infix fun <C, T: Any, R> ((C)->Pair<Boolean, T?>).case(noinline then: (T) -> R) =
    SwitchCase(this, then)

infix fun <R> Regex.startsWithCase(then: (MatchResult) -> R): SwitchCase<String, MatchResult, R> =
    { it: String ->
        this.matchAt(it, 0)?.let {
            true to it
        } ?: (false to null)
    } case then

inline fun <T, R> T.switch(vararg cases: SwitchCase<T, *, R>, default: (T) -> R): R {
    cases.forEach { (cond, then) ->
        val (b, v) = cond(this)
        if (b) {
            return (then as (Any) -> R)(v!!)
        }
    }
    return default(this)
}