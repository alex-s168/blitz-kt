package me.alex_s168.kotlin_bits

object Ansi {
    const val ESC = (0x1B).toChar()

    object Cursor {
        fun home() =
            print("$ESC[H")

        fun goto(line: Int, col: Int) =
            print("$ESC[${line};${col}H")

        fun up(lines: Int) =
            print("$ESC[${lines}A")

        fun down(lines: Int) =
            print("$ESC[${lines}B")

        fun right(cols: Int) =
            print("$ESC[${cols}C")

        fun left(cols: Int) =
            print("$ESC[${cols}D")
    }

    fun printAndBack(text: String) {
        print(text)
        Cursor.left(text.length)
    }
}

fun main() {
    Ansi.printAndBack("001")
    print("002")
}