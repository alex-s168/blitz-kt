package blitz.term

object Terminal {
    object COLORS {
        val BLACK = Color(AnsiiMode(30), AnsiiMode(40))
        val RED = Color(AnsiiMode(31), AnsiiMode(41))
        val GREEN = Color(AnsiiMode(32), AnsiiMode(42))
        val YELLOW = Color(AnsiiMode(33), AnsiiMode(43))
        val BLUE = Color(AnsiiMode(34), AnsiiMode(44))
        val MAGENTA = Color(AnsiiMode(35), AnsiiMode(45))
        val CYAN = Color(AnsiiMode(36), AnsiiMode(46))
        val WHITE = Color(AnsiiMode(37), AnsiiMode(47))
    }

    object STYLES {
        val BOLD = AnsiiMode(1)
        val DIM = AnsiiMode(2)
        val UNDERLINE = AnsiiMode(4)
        val BLINK = AnsiiMode(5)
        val OVERLINE = AnsiiMode(53)
        val FRAMED = AnsiiMode(51)
        val ENCIRCLED = AnsiiMode(52)
    }

    class Color(
        val fg: AnsiiMode,
        val bg: AnsiiMode,
        brighterIn: Color? = null,
        darkerIn: Color? = null
    ) {
        private fun ch(mode: AnsiiMode): Int =
            mode.values[0]

        private fun brighterChannel(va: Int): Int =
            if (va <= 50) va + 60 else va

        private fun darkerChannel(va: Int): Int =
            if (va >= 50) va - 60 else va

        val brighter by lazy { Color(AnsiiMode(brighterChannel(ch(fg))), AnsiiMode(brighterChannel(ch(bg)))) }
        val darker by lazy { Color(AnsiiMode(darkerChannel(ch(fg))), AnsiiMode(darkerChannel(ch(bg)))) }
    }

    /** Escape-sequence safe string length */
    fun len(str: String): Int {
        var len = 0
        var ansii = false
        str.forEach {
            if (ansii) {
                if (it == 'm')
                    ansii = false
            } else if (it.code == 27) {
                ansii = true
            } else {
                len ++
            }
        }
        return len
    }

    fun encodeString(str: String, vararg modes: AnsiiMode) =
        ansiiStr(str, *modes)

    fun print(str: String, vararg modes: AnsiiMode) {
        kotlin.io.print(encodeString(str, *modes))
    }

    fun println(str: String, vararg modes: AnsiiMode) {
        kotlin.io.println(encodeString(str, *modes))
    }

    fun err(str: String, vararg modes: AnsiiMode) {
        System.err.print(encodeString(str, *modes))
    }

    fun errln(str: String, vararg modes: AnsiiMode) {
        System.err.println(encodeString(str, *modes))
    }

    @Deprecated(
        "Use errln instead!",
        ReplaceWith(
            "errln(str, *modes)",
            "blitz.term.Terminal.errln"
        )
    )
    fun warn(str: String, vararg modes: AnsiiMode) {
        errln(str, *modes)
    }

    object Cursor {
        fun savePos() {
            print(escape + "7")
        }

        fun restorePos() {
            print(escape + "8")
        }

        fun goto(line: Int, col: Int) {
            print("$escape[${line};${col}H")
        }

        fun gotoZero() {
            print("$escape[H")
        }

        fun gotoBeginOfPrevLine() {
            print("$escape[F")
        }

        fun gotoBeginOfLine() {
            print((0x0D).toChar())
        }
    }

    fun save() {
        print("$escape[?47h")
    }

    fun restore() {
        print("$escape[?47l")
    }

    fun clear() {
        print("$escape[2J")
    }

    fun clearLine() {
        print("$escape[2K")
    }
}