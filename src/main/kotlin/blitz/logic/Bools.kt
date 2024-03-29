package blitz.logic

/**
 * Execute a block if the boolean is true.
 */
fun Boolean.then(block: () -> Unit): Boolean {
    if (this) {
        block()
    }

    return this
}

/**
 * Execute a block if the boolean is false.
 */
fun Boolean.otherwise(block: () -> Unit): Boolean {
    if (!this) {
        block()
    }

    return this
}

/**
 * Execute a block if the boolean is true, otherwise execute another block.
 */
fun Boolean.then(block: () -> Unit, otherwise: () -> Unit): Boolean {
    if (this) {
        block()
    } else {
        otherwise()
    }

    return this
}