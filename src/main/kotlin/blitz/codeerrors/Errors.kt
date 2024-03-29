package blitz.codeerrors

import blitz.str.MutMultiLineString
import blitz.str.MutString
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
            Error.Level.INFO to Terminal.COLORS.WHITE.fg,
            Error.Level.WARN to Terminal.COLORS.YELLOW.fg,
            Error.Level.ERROR to Terminal.COLORS.RED.fg
        ),
        val levelStr: (Error.Level) -> String = {
            when (it) {
                Error.Level.INFO -> "note"
                Error.Level.WARN -> "warning"
                Error.Level.ERROR -> "error"
            }
        },
        val underlineString: (len: Int) -> String = {
            if (it == 0) "" else "^" + "~".repeat(it - 1)
        },
    )

    fun print(config: PrintConfig, errors: Iterable<Error>) {
        val bySources = errors.groupBy { it.loc.source }
        bySources.forEach { (source, errors) ->
            val worst = errors
                .map { it.level }
                .reduce { acc, level -> acc + level }
            Terminal.errln("File: \"${source.file}\"", config.styles[worst]!!)
            Terminal.errln("================================================================================", config.styles[worst]!!)

            val perLines = errors
                .groupBy { it.loc.line }
                .entries
                .sortedBy { it.key }

            perLines.forEach { (line, errors) ->
                errors.asSequence().filterNot { it.isHint }.forEach { err ->
                    Terminal.err(config.levelStr(err.level), config.styles[err.level]!!, Terminal.STYLES.BOLD)
                    Terminal.errln(": ${err.message}", Terminal.STYLES.BOLD)
                }

                val msg = MutMultiLineString(' ')
                val lineStr = line.toString()
                msg[1, 2] = lineStr
                var nextCol = 3 + lineStr.length
                msg[0, nextCol] = '|' // TODO: print above and below source but dimmed?
                msg[1, nextCol] = '|'
                msg[2, nextCol] = '|'
                nextCol += 2
                if (line > 0)
                    msg[0, nextCol] = source.content[line - 1]
                msg[1, nextCol] = source.content[line]

                // TODO: underline

                // TODO: hints

                Terminal.errln(msg.toString())
            }

            Terminal.errln("================================================================================", config.styles[worst]!!)
        }
    }
}

fun main() {
    val source = Errors.Source("main.kt", MutMultiLineString.from("""
        fn main() {
            return 1
        }
    """.trimIndent(), ' '))

    val errors = listOf(
        Errors.Error(
            "Cannot return integer from function with return type void",
            Errors.Error.Level.ERROR,
            Errors.Location(source, 1, 11, 1)
        )
    )

    val config = Errors.PrintConfig()

    Errors.print(config, errors)
}