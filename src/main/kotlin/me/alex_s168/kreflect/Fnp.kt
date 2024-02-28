package me.alex_s168.kreflect

import kotlinx.io.files.Path

fun main() {

}

fun pureMain(args: Array<String>): Monad<Unit> =
    args.map {
        if (it == "-") readIn()
        else unit(it)
            .asPath()
            .read()
            .bind { it.stringify(64) }
    }