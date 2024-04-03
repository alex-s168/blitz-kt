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
        it.stringWithEscape()
            ?.mapSecond { Str(it) }
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

    val jsonNull = parser { it.require("null")?.to(Nul()) }

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
        jsonElement = (jsonArray or jsonNum or jsonString or jsonObj or jsonBool or jsonNull).trim()
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

    fun parse(string: String): Element? =
        jsonElement(Parsable(string))?.second
}