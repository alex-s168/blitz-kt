package blitz.parse

data class Token(
    val type: Type,
    val value: String? = null,
    val op: Operator? = null,
) {
    enum class Type {
        IDENT,
        NUMBER,
        PAREN_OPEN,
        PAREN_CLOSE,
        OPERATOR,
        SEPARATOR,
        COMMA,
    }
}