package me.alex_s168.kreflect

fun main(args: Array<String>) {
    // pureCat(args)
    //     .impure()
    val inp = sequenceOf("AAA", "BBB", "AAA", "AAA", "AAA", "BBB")
    val out = inp.easyMappingSequence { i, s, m ->
        if (s(i-1) == m(i)) null
        else m(i)
    }
    println(out.contents)
}

fun pureCat(args: Array<String>): Monad<Unit> =
    args
    .ifEmpty { arrayOf("-") }
    .map {
        if (it == "-") readIn()
        else unit(it)
            .asPath()
            .read()
            .stringify()
    }
    .rewrap()
    .flatten()
    .map { unit(it).print() }
    .combine()