package blitz.parse

import blitz.collections.RefVec
import blitz.collections.contents
import blitz.parse.comb2.*
import blitz.unreachable

object JSON {

    val jsonBool: Parser<Char, Element> = choose {
        it(mapValue(seq("true".toList())) { Element.newBool(true) })
        it(mapValue(seq("false".toList())) { Element.newBool(false) })
    }

    val jsonNull: Parser<Char, Element> =
        mapValue(seq("null".toList())) { Element.newNull() }

    val jsonNum: Parser<Char, Element> =
        mapValue(floatLit, Element::newNum)

    val jsonString: Parser<Char, Element> =
        mapValue(stringLit, Element::newStr)

    val jsonElement = futureRec { jsonElement: Parser<Char, Element> ->

        val jsonArray: Parser<Char, Element> =
            thenIgnore(
                thenIgnore(
                    thenOverwrite(
                        thenIgnore(just('['), whitespaces),
                        mapValue(delimitedBy(jsonElement,
                            chain(whitespaces, ignoreSeq(","), whitespaces)), Element::newArr)),
                whitespaces),
            just(']')
            )

        val jsonObj: Parser<Char, Element> =
            mapValue(thenIgnore(thenIgnore(thenOverwrite(
                just('{'),
                delimitedBy(
                    then(
                        thenIgnore(
                            thenIgnore(
                                thenOverwrite(
                                    whitespaces,
                                    stringLit),
                                whitespaces),
                            just(':')),
                        jsonElement),
                    just(','))),
                whitespaces),
                just('}'))) { Element.newObj(it.toMap()) }

        thenIgnore(thenOverwrite(
            whitespaces,
            choose {
                it(jsonArray)
                it(jsonNum)
                it(jsonString)
                it(jsonObj)
                it(jsonBool)
                it(jsonNull)
            }),
            whitespaces)

    }

    class Element(
        @JvmField val kind: Int,
        @JvmField val _boxed: Any? = null,
        @JvmField val _num: Double = 0.0,
        @JvmField val _bool: Boolean = false,
    ) {
        companion object {
            const val NUM = 0
            const val BOOL = 1
            const val NULL = 2
            const val ARR = 3
            const val STR = 4
            const val OBJ = 5

            inline fun newNum(v: Double): Element =
                Element(NUM, _num = v)
            inline fun newBool(v: Boolean): Element =
                Element(BOOL, _bool = v)
            inline fun newNull(): Element =
                Element(NULL)
            inline fun newArr(v: RefVec<Element>): Element =
                Element(ARR, _boxed = v)
            inline fun newStr(v: String): Element =
                Element(STR, _boxed = v)
            inline fun newObj(v: Map<String, Element>): Element =
                Element(OBJ, _boxed = v)
        }

        override fun toString(): String =
            when (kind) {
                NUM -> uncheckedAsNum().toString()
                BOOL -> uncheckedAsBool().toString()
                NULL -> "null"
                ARR -> uncheckedAsArr().contents.toString()
                STR -> "\"${uncheckedAsStr()}\""
                OBJ -> uncheckedAsObj().map { "${it.key}: ${it.value}" }.joinToString(prefix = "{", postfix = "}")
                else -> unreachable()
            }
    }
    
    inline fun Element.uncheckedAsNum(): Double =
        _num
    inline fun Element.uncheckedAsBool(): Boolean =
        _bool
    inline fun Element.uncheckedAsArr(): RefVec<Element> =
        _boxed as RefVec<Element>
    inline fun Element.uncheckedAsStr(): String =
        _boxed as String
    inline fun Element.uncheckedAsObj(): Map<String, Element> =
        _boxed as Map<String, Element>

    inline fun Element.asNum(): Double {
        require(kind == Element.NUM) { "Element is not a Number" }
        return _num
    }

    inline fun Element.asBool(): Boolean {
        require(kind == Element.BOOL) { "Element is not a Boolean" }
        return _bool
    }

    inline fun Element.asArr(): RefVec<Element> {
        require(kind == Element.ARR) { "Element is not an Array" }
        return _boxed as RefVec<Element>
    }

    inline fun Element.asStr(): String {
        require(kind == Element.STR) { "Element is not a String" }
        return _boxed as String
    }

    inline fun Element.asObj(): Map<String, Element> {
        require(kind == Element.OBJ) { "Element is not an Object" }
        return _boxed as Map<String, Element>
    }

    fun parse(string: String): ParseResult<Element> =
        jsonElement.run(string.toList())
}