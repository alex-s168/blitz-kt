package blitz.str

data class BoxDrawingCharSet(
    val cornerLeftTop: Char,
    val cornerRightTop: Char,
    val cornerLeftBottom: Char,
    val cornerRightBottom: Char,
    val horizontal: Char,
    val vertical: Char,
) {
    constructor(corner: Char, horizontal: Char, vertical: Char): this(corner,corner,corner,corner,horizontal,vertical)
    constructor(corner: Char, line: Char): this(corner, line, line)
    constructor(all: Char): this(all, all)

    /*
      	0 	1 	2 	3 	4 	5 	6 	7 	8 	9 	A 	B 	C 	D 	E 	F
U+250x 	─ 	━ 	│ 	┃ 	┄ 	┅ 	┆ 	┇ 	┈ 	┉ 	┊ 	┋ 	┌ 	┍ 	┎ 	┏

U+251x 	┐ 	┑ 	┒ 	┓ 	└ 	┕ 	┖ 	┗ 	┘ 	┙ 	┚ 	┛ 	├ 	┝ 	┞ 	┟

U+252x 	┠ 	┡ 	┢ 	┣ 	┤ 	┥ 	┦ 	┧ 	┨ 	┩ 	┪ 	┫ 	┬ 	┭ 	┮ 	┯

U+253x 	┰ 	┱ 	┲ 	┳ 	┴ 	┵ 	┶ 	┷ 	┸ 	┹ 	┺ 	┻ 	┼ 	┽ 	┾ 	┿

U+254x 	╀ 	╁ 	╂ 	╃ 	╄ 	╅ 	╆ 	╇ 	╈ 	╉ 	╊ 	╋ 	╌ 	╍ 	╎ 	╏

U+255x 	═ 	║ 	╒ 	╓ 	╔ 	╕ 	╖ 	╗ 	╘ 	╙ 	╚ 	╛ 	╜ 	╝ 	╞ 	╟

U+256x 	╠ 	╡ 	╢ 	╣ 	╤ 	╥ 	╦ 	╧ 	╨ 	╩ 	╪ 	╫ 	╬ 	╭ 	╮ 	╯

U+257x 	╰ 	╱ 	╲ 	╳ 	╴ 	╵ 	╶ 	╷ 	╸ 	╹ 	╺ 	╻ 	╼ 	╽ 	╾ 	╿
     */

    companion object {
        val ASCII =             BoxDrawingCharSet('*', '-', '|')
        val ASCII_BOLD =        BoxDrawingCharSet('#', '=', '|')
        val NORMAL =            BoxDrawingCharSet('┌', '┐', '└', '┘', '─', '│')
        val BOLD =              BoxDrawingCharSet('┏', '┓', '┗', '┛', '━', '┃')
        val BOLD_SPACED =       BoxDrawingCharSet('┏', '┓', '┗', '┛', '╸', '╏')
        val DOUBLE =            BoxDrawingCharSet('╔', '╗', '╚', '╝', '═', '║')
        val ROUND =             BoxDrawingCharSet('╭', '╮', '╰', '╯', '─', '│')
        val DOTTED =            BoxDrawingCharSet('┌', '┐', '└', '┘', '╴', '┆')
        val DOTTED_SMALL =      BoxDrawingCharSet('┌', '┐', '└', '┘', '╴', '┊')
        val BOLD_DOTTED =       BoxDrawingCharSet('┏', '┓', '┗', '┛', '╸', '┇')
        val BOLD_DOTTED_SMALL = BoxDrawingCharSet('┏', '┓', '┗', '┛', '╸', '┋')

        val all = listOf(
            ASCII,
            ASCII_BOLD,
            NORMAL,
            BOLD,
            BOLD_SPACED,
            DOUBLE,
            ROUND,
            DOTTED,
            DOTTED_SMALL,
            BOLD_DOTTED,
            BOLD_DOTTED_SMALL,
        )
    }

    fun draw(start: Pair<Int, Int>, end: Pair<Int, Int>, pixel: (x: Int, y: Int, c: Char) -> Unit) {
        pixel(start.first, start.second, cornerLeftTop)
        pixel(end.first, start.second, cornerRightTop)
        pixel(start.first, end.second, cornerLeftBottom)
        pixel(end.first, end.second, cornerRightBottom)

        fun row(row: Int) {
            for (col in start.first+1..<end.first) {
                pixel(col, row, horizontal)
            }
        }

        fun col(col: Int) {
            for (row in start.second+1..<end.second) {
                pixel(col, row, vertical)
            }
        }

        row(start.second)
        row(end.second)

        col(start.first)
        col(end.first)
    }
}