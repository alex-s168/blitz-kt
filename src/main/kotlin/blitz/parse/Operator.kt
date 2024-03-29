package blitz.parse

data class Operator(
    val symbol: Char,
    val precedence: Int = 0,
    val leftAssociative: Boolean = true,
)