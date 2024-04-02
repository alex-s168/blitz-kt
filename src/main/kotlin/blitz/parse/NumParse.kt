package blitz.parse

import blitz.parse.comb.*

object NumParse {
    private val intBase = parser { it.require("0b")?.to(2) } or
            parser { it.require("0x")?.to(16) } or
            parser { it.require("0o")?.to(8) } or
            constantParser(10)

    private val sign = parser { it.require("+")?.to(1) } or
            parser { it.require("-")?.to(-1) } or
            constantParser(1)

    val int = parser { s ->
        s.map(sign)?.map(intBase)?.map { str, (sign, base) ->
            val chars = when (base) {
                2 -> "01"
                8 -> "01234567"
                10 -> "0123456789"
                16 -> "0123456789abcdefABCDEF"
                else -> error("wtf")
            }
            str.asLongAs(*chars.toCharArray()) {
                it.toLongOrNull(base)?.times(sign)
            }
        }
    }

    val float = parser { s ->
        s.map(sign)?.map { str, sign ->
            str.asLongAs('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.') {
                it.toDoubleOrNull()?.times(sign)
            }
        }
    }
}

fun parseInt(str: String): Long? =
    NumParse.int(Parsable(str))?.second

fun parseDouble(str: String): Double? =
    NumParse.float(Parsable(str))?.second