package blitz.parse

import blitz.parse.comb2.*

object JSON {
    val jsonElement = futureRec { jsonElement: Parser<Char, Element> ->

        val jsonNum: Parser<Char, Element> = floatLit()
            .mapValue(::Number)

        val jsonString: Parser<Char, Element> = stringLit()
            .mapValue(::Str)

        val jsonArray: Parser<Char, Element> = just('[')
            .then(jsonElement
                .delimitedBy(just(','))
                .mapValue(::Array))
            .thenIgnore(whitespaces())
            .thenIgnore(just(']'))
            .mapValue { it.second }

        val jsonBool: Parser<Char, Element> = choose(
            seq("true".toList()).mapValue { Bool(true) },
            seq("false".toList()).mapValue { Bool(false) },
        )

        val jsonNull: Parser<Char, Element> = seq("null".toList())
            .mapValue { Nul() }

        val jsonObj: Parser<Char, Element> = just('{')
            .then(
                whitespaces()
                .then(stringLit())
                .mapValue { it.second }
                .thenIgnore(whitespaces())
                .thenIgnore(just(':'))
                .then(jsonElement)
                .delimitedBy(just(',')))
            .thenIgnore(whitespaces())
            .thenIgnore(just('}'))
            .mapValue { Obj(it.second.toMap()) }

        whitespaces()
            .then(choose(
                jsonArray,
                jsonNum,
                jsonString,
                jsonObj,
                jsonBool,
                jsonNull
            ))
            .thenIgnore(whitespaces())
            .mapValue { it.second }

    }

    interface Element {
        val arr get() = (this as Array).value
        val num get() = (this as Number).value
        val str get() = (this as Str).value
        val obj get() = (this as Obj).value
        val bool get() = (this as Bool).value

        fun isArr() = this is Array
        fun isNum() = this is Number
        fun isStr() = this is Str
        fun isObj() = this is Obj
        fun isBool() = this is Bool
        fun isNul() = this is Nul
    }

    data class Array(val value: List<Element>): Element {
        override fun toString(): String =
            value.joinToString(separator = ", ", prefix = "[", postfix = "]")
    }

    data class Number(val value: Double): Element {
        override fun toString(): String =
            value.toString()
    }

    data class Str(val value: String): Element {
        override fun toString(): String =
            "\"$value\""
    }

    data class Obj(val value: Map<String, Element>): Element {
        override fun toString(): String =
            value.map { (k, v) -> "\"$k\": $v" }.joinToString(separator = ", ", prefix = "{", postfix = "}")
    }


    data class Bool(val value: Boolean): Element {
        override fun toString(): String =
            value.toString()
    }

    class Nul: Element

    fun parse(string: String): ParseResult<Element> =
        jsonElement(ParseCtx(string.toList(), 0))
}