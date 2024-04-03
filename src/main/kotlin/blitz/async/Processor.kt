package blitz.async

import blitz.collections.SynchronizedList
import blitz.logic.then

abstract class Processor {
    protected val tasks = SynchronizedList(mutableListOf<Task>())


    fun tick() {
        for (task in tasks) {
            if (task.counter >= task.priority) {
                task.fn()
                task.counter = 0
            } else {
                task.counter ++
            }
        }
    }

    abstract fun add(task: Task)

    abstract fun remove(task: Task)

        /** priority 0 means every tick; 1 means every second tick; 2 means every third tick, ... */
    data class Task(
        internal val priority: Int = 0, // every tick
        internal val fn: () -> Unit
    ) {
        internal var counter: Int = 0
    }

    companion object {
        fun singleThread(): Processor =
            SingleThreadProcessor()

        fun manualTick(): Processor =
            object : Processor() {
                override fun add(task: Task) {
                    tasks.add(task)
                }

                override fun remove(task: Task) {
                    tasks.remove(task)
                }
            }
    }
}

internal class SingleThreadProcessor: Processor() {
    private fun createThread() = Thread { while (true) { tick() } }

    private var thread: Thread? = null

    override fun add(task: Task) {
        tasks.add(task)
        if (thread == null) {
            thread = createThread()
            thread!!.start()
        }
    }

    override fun remove(task: Task) {
        tasks.remove(task)
        tasks.isEmpty().then {
            runCatching { thread?.interrupt() }
            thread = null
        }
    }
}