package blitz.parse

import blitz.collections.funnyMap

fun Iterator<Char>.tokenize(
    operators: Collection<Operator> = emptyList(),
    ignore: Collection<Char> = emptyList(),
): Iterator<Token> =
    funnyMap {
        val builder = StringBuilder()
        var ident = false
        var num = false
        var paren: Char? = null
        var op: Operator? = null
        var sep = false
        var comma = false
        while (it.hasNext()) {
            when (val c = it.next()) {
                in 'a'..'z',
                in 'A'..'Z',
                '_' -> {
                    if (num) {
                        it.unGet()
                        break
                    } else {
                        builder.append(c)
                        ident = true
                    }
                }
                in '0'..'9' -> {
                    builder.append(c)
                    if (!ident)
                        num = true
                }
                '.' -> {
                    if (num)
                        builder.append(it)
                    else {
                        it.unGet()
                        break
                    }
                }
                '(',
                ')' -> {
                    if (!ident && !num)
                        paren = c
                    else
                        it.unGet()
                    break
                }
                in ignore -> {
                    if (!ident && !num)
                        sep = true
                    else
                        it.unGet()
                    break
                }
                ',' -> {
                    if (!ident && !num)
                        comma = true
                    else
                        it.unGet()
                    break
                }
                else -> {
                    val oo = operators.firstOrNull { o -> o.symbol == c }
                    if (oo != null)
                        op = oo
                    else
                        it.unGet()
                    break
                }
            }
        }
        if (ident)
            Token(Token.Type.IDENT, builder.toString())
        else if (num)
            Token(Token.Type.NUMBER, builder.toString())
        else if (paren == '(')
            Token(Token.Type.PAREN_OPEN)
        else if (paren == ')')
            Token(Token.Type.PAREN_CLOSE)
        else if (op != null)
            Token(Token.Type.OPERATOR, op = op)
        else if (sep)
            Token(Token.Type.SEPARATOR)
        else if (comma)
            Token(Token.Type.COMMA)
        else
            null
    }
