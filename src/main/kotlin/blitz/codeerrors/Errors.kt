package blitz.codeerrors

import blitz.collections.inBounds
import blitz.str.ColoredChar
import blitz.str.MutMultiColoredMultiLineString
import blitz.str.MutMultiLineString
import blitz.term.AnsiiMode
import blitz.term.Terminal

object Errors {
    data class Source(
        val file: String,
        val content: MutMultiLineString
    )

    data class Location(
        val source: Source,
        val line: Int,
        val col: Int,
        val size: Int,
    )

    data class Error(
        val message: String,
        val level: Level,
        val loc: Location,
        val isHint: Boolean = false,
        val isLongDesc: Boolean = false,
    ) {
        enum class Level {
            INFO,
            WARN,
            ERROR,
            ;

            operator fun plus(other: Level): Level =
                when (this) {
                    INFO -> other
                    WARN -> if (other == ERROR) other else this
                    ERROR -> this
                }
        }
    }

    data class PrintConfig(
        val styles: Map<Error.Level, AnsiiMode> = mapOf(
            Error.Level.INFO to Terminal.COLORS.MAGENTA.fg,
            Error.Level.WARN to Terminal.COLORS.YELLOW.fg,
            Error.Level.ERROR to Terminal.COLORS.RED.fg
        ),
        val levelStr: (Error.Level) -> String = {
            when (it) {
                Error.Level.INFO -> "Note"
                Error.Level.WARN -> "Warning"
                Error.Level.ERROR -> "Error"
            }
        },
        val underlineString: (len: Int) -> String = {
            if (it == 0) "" else "^" + "^".repeat(it - 1)
        },
    )

    fun print(config: PrintConfig, errors: Iterable<Error>) {
        val bySources = errors.groupBy { it.loc.source }
        bySources.forEach { (source, errors) ->
            val worst = errors
                .map { it.level }
                .reduce { acc, level -> acc + level }
            Terminal.errln("File: \"${source.file}\"", config.styles[worst]!! + Terminal.STYLES.BOLD)
            Terminal.errln("================================================================================", config.styles[worst]!! + Terminal.STYLES.BOLD)

            val perLinesMap = errors
                .groupBy { it.loc.line }

            val perLines = perLinesMap
                .entries
                .sortedBy { it.key }

            perLines.forEachIndexed { index, (line, errors) ->
                if (index > 0)
                    Terminal.errln("")

                errors.asSequence().filterNot { it.isHint || it.isLongDesc }.forEach { err ->
                    Terminal.err(config.levelStr(err.level), config.styles[err.level]!!, Terminal.STYLES.BOLD)
                    Terminal.errln(": ${err.message}", Terminal.COLORS.WHITE.brighter.fg, Terminal.STYLES.BOLD)
                }

                val printPrev = line > 0 && !perLinesMap.containsKey(line - 1)
                val printNext = source.content.lines.inBounds(line + 1) && !perLinesMap.containsKey(line + 1)

                val worstLine = config.styles[
                    errors
                        .map { it.level }
                        .reduce { acc, level -> acc + level }
                ]!!

                val msg = MutMultiColoredMultiLineString(fill = ColoredChar(' '))

                val lineStr = (line + 1).toString()
                msg.set(1, 2, lineStr, worstLine)

                var nextCol = if (printNext) {
                    val nextLineStr = (line + 2).toString()
                    msg.set(2, 2, nextLineStr, Terminal.COLORS.WHITE.fg)
                    3 + nextLineStr.length
                } else {
                    3 + lineStr.length
                }

                if (printPrev)
                    msg.set(0, 2, line.toString(), Terminal.COLORS.WHITE.fg)

                fun pipe(row: Int, col: Int) =
                    msg.set(row, col, '|', Terminal.COLORS.WHITE.fg + Terminal.STYLES.BOLD)

                pipe(0, nextCol)
                msg.set(1, nextCol, '|', worstLine + Terminal.STYLES.BOLD)
                pipe(2, nextCol)

                nextCol += 2
                if (printPrev)
                    msg.set(0, nextCol, source.content[line - 1].toString(), Terminal.COLORS.WHITE.fg)
                msg.set(1, nextCol, source.content[line].toString(), Terminal.COLORS.WHITE.brighter.fg)
                if (printNext)
                    msg.set(2, nextCol, source.content[line + 1].toString(), Terminal.COLORS.WHITE.fg)

                val byCol = errors.asSequence().sortedBy { it.loc.col }
                byCol.filterNot { it.isHint || it.isLongDesc }.forEach {
                    msg.set(2, it.loc.col + nextCol, config.underlineString(it.loc.size), config.styles[it.level]!! + Terminal.STYLES.BOLD)
                }

                var row = 3
                byCol.filter { it.isHint }.forEach {
                    msg.set(row, it.loc.col + nextCol, config.underlineString(it.loc.size), config.styles[it.level]!! + Terminal.STYLES.BOLD)
                    val end = it.loc.col + nextCol + it.loc.size
                    msg.set(row, end + 1, it.message, config.styles[it.level]!!)
                    pipe(row, nextCol - 2)
                    row ++
                }

                row ++

                byCol.filter { it.isLongDesc }.forEach {
                    val msgLines = MutMultiLineString.from(it.message, fill = ' ')
                    msg[row, nextCol, msgLines] = Terminal.COLORS.WHITE.brighter.fg
                    row ++
                }

                Terminal.errln(msg.toString())
            }

            Terminal.errln("================================================================================", config.styles[worst]!! + Terminal.STYLES.BOLD)
        }
    }
}