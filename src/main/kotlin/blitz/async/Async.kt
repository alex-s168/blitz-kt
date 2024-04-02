package blitz.async

import kotlin.random.Random

// TODO: use coroutines?
fun <R> async(fn: () -> R): Future<R> {
    lateinit var future: ThreadWaitingFuture<R>
    val thread = Thread {
        future.done(fn())
    }
    future = ThreadWaitingFuture(thread)
    thread.start()
    return future
}