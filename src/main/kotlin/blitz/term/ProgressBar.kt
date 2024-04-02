package blitz.term

import kotlin.math.max

/**
 * Single-line progress bar
 */
class ProgressBar(
    var max: Int,
    val style: Style,
    private val preDrawLine: () -> Unit = {},
    private val  postDrawLine: () -> Unit = {}
) {
    fun end() {
        Terminal.Cursor.gotoBeginOfLine()
    }

    fun step(pos: Int) {
        Terminal.Cursor.gotoBeginOfLine()
        preDrawLine()
        print(style.pre)
        val left = (pos.toDouble() / max * style.len).toInt()
        val right = style.len - left
        print(style.segment.toString().repeat(max(0, left - 1)))
        print(style.segmentCursor)
        print(style.segmentEmpty.toString().repeat(right))
        print(style.post)
        postDrawLine()
    }

    data class Style(
        val len: Int,
        val pre: String,
        val segment: Char,
        val segmentCursor: Char,
        val segmentEmpty: Char,
        val post: String
    ) {
        companion object {
            val BASIC = Style(36, "[ ", '=', '=', ' ', " ]")
            val LIGHT = Style(36, "< ", '-', '-', ' ', " >")
            val STARS = Style(36, "( ", '*', '*', ' ', " )")
            val ARROW = Style(36, "[ ", '=', '>', ' ', " ]")
        }
    }
}