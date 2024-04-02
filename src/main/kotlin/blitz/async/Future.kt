package blitz.async

import blitz.Obj
import java.util.concurrent.atomic.AtomicBoolean

open class Future<T> {
    protected lateinit var value: Obj<T>
    protected val then = mutableListOf<(T) -> Unit>()
    protected val presentAtomic = AtomicBoolean(false)

    val present get() = presentAtomic.get()

    fun done(va: T) {
        if (presentAtomic.get())
            error("Can not set future twice!")
        value = Obj.of(va)
        presentAtomic.set(true)
        then.forEach {
            it(va)
        }
    }

    fun then(fn: (T) -> Unit): Future<T> {
        if (presentAtomic.get())
            fn(value.v)
        else
            then.add(fn)
        return this
    }

    fun <R> map(fn: (T) -> R): Future<R> {
        val new = Future<R>()
        then {
            new.done(fn(it))
        }
        return new
    }

    // TODO: can do better
    open fun await(iterSleepMS: Long = 10): T {
        val thread = Thread {
            while (true) Thread.sleep(iterSleepMS)
        }
        thread.start()
        then { thread.interrupt() }
        kotlin.runCatching { thread.join() }
        return value.v
    }

    override fun toString(): String =
        if (presentAtomic.get()) "Future(${value.v})" else "Future(?)"
}

class ThreadWaitingFuture<T>(
    private val on: Thread
): Future<T>() {
    override fun await(iterSleepMS: Long): T {
        on.join()
        return value.v
    }
}