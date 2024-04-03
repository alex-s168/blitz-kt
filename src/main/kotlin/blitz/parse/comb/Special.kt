package blitz.parse.comb

fun Parsable.stringWithEscape(): Pair<Parsable, String>? {
    var escaped = false
    var index = 0
    val out = StringBuilder()
    for (c in str) {
        if (index == 0) {
            if (c != '"')
                return null
        } else {
            if (escaped) {
                escaped = false
                when (c) {
                    '"' -> out.append('"')
                    '\\' -> out.append('\\')
                    'n' -> out.append('\n')
                    'r' -> out.append('\r')
                    'b' -> out.append('\b')
                    't' -> out.append('\t')
                    else -> return null
                }
            } else if (c == '"')
                break
            else if (c == '\\')
                escaped = true
            else {
                out.append(c)
            }
        }
        index ++
    }
    if (escaped)
        return null
    return Parsable(str.substring(index + 1), loc?.plus(index + 1)) to out.toString()
}