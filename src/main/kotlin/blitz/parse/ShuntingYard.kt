package blitz.parse

fun Iterator<Token>.shuntingYard(): Iterator<Token> {
    val iter = this
    val opStack = mutableListOf<Token>()
    return sequence {
        while (iter.hasNext()) {
            val tk0 = iter.next()
            when (tk0.type) {
                Token.Type.NUMBER -> yield(tk0)
                Token.Type.IDENT -> opStack.add(tk0)
                Token.Type.OPERATOR -> {
                    while (true) {
                        if (opStack.isEmpty())
                            break
                        val oo = opStack.last()
                        if (oo.type == Token.Type.PAREN_OPEN)
                            break
                        if (!((oo.op!!.precedence > tk0.op!!.precedence) || (tk0.op.precedence == oo.op.precedence && tk0.op.leftAssociative)))
                            break
                        opStack.removeLast()
                        yield(oo)
                    }
                    opStack.add(tk0)
                }
                Token.Type.COMMA -> {
                    while (true) {
                        if (opStack.isEmpty())
                            break
                        val oo = opStack.last()
                        if (oo.type == Token.Type.PAREN_OPEN)
                            break
                        opStack.removeLast()
                    }
                }
                Token.Type.PAREN_OPEN -> opStack.add(tk0)
                Token.Type.PAREN_CLOSE -> {
                    while (true) {
                        if (opStack.isEmpty())
                            throw Exception("Unexpected closing parenthesis!")
                        val oo = opStack.last()
                        if (oo.type == Token.Type.PAREN_OPEN)
                            break
                        opStack.removeLast()
                        yield(oo)
                    }
                    if (opStack.removeLastOrNull()?.type != Token.Type.PAREN_OPEN)
                        throw Exception("Unexpected closing parenthesis!")
                    if (opStack.lastOrNull()?.type == Token.Type.IDENT)
                        yield(opStack.removeLast())
                }
                Token.Type.SEPARATOR -> continue
            }
        }
        while (opStack.isNotEmpty()) {
            val oo = opStack.removeLast()
            if (oo.type in listOf(Token.Type.PAREN_OPEN, Token.Type.PAREN_CLOSE))
                throw Exception("Mismatched parenthesis")
            yield(oo)
        }
    }.iterator()
}