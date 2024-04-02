package blitz.parse

import blitz.parse.comb.*

object JSON {
    lateinit var jsonElement: Parser<Element>

    val jsonNum = parser {
        it.map(NumParse.float)?.mapSecond { n ->
            Number(n)
        }
    }

    val jsonString = parser {
        it.require("\"")
            ?.untilRequire("\"") { str -> Str(str) }
    }

    val jsonArray = parser {
        it.require("[")
            ?.array(",") { elem ->
                elem.whitespaces()
                    .map(jsonElement)
                    ?.whitespaces()
            }
            ?.require("]")
            ?.mapSecond { x -> Array(x) }
    }

    val jsonBool = parser { it.require("true")?.to(Bool(true)) } or
            parser { it.require("false")?.to(Bool(false)) }

    val jsonObj = parser {
        it.require("{")
            ?.array(",") { elem ->
                elem.whitespaces()
                    .map(jsonString)
                    ?.mapSecond { it.str }
                    ?.whitespaces()
                    ?.require(":")
                    ?.whitespaces()
                    ?.map(jsonElement)
                    ?.whitespaces()
            }
            ?.require("}")
            ?.mapSecond { x -> Obj(x.toMap()) }
    }

    init {
        jsonElement = (jsonArray or jsonNum or jsonString or jsonObj or jsonBool).trim()
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

    fun parse(string: String): Element? =
        jsonElement(Parsable(string))?.second
}