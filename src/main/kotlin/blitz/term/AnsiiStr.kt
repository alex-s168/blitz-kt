package blitz.term

class AnsiiMode(internal val values: MutableList<Int>) {
    constructor(mo: Int): this(mutableListOf(mo))

    operator fun plus(other: AnsiiMode): AnsiiMode =
        AnsiiMode((values + other.values).toMutableList())
}

private val escape = (27).toChar()

fun ansiiStr(str: String, vararg modes: AnsiiMode) =
    if (modes.isEmpty())
        str
    else
        "$escape[${modes.flatMap { it.values }.joinToString(separator = ";")}m$str$escape[0m"