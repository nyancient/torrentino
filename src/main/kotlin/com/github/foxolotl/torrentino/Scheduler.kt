package com.github.foxolotl.torrentino

import java.time.Duration
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Schedules tasks for periodic execution, with a minimum period of one second.
 */
class Scheduler private constructor(
    private val executor: ScheduledThreadPoolExecutor
) {
    sealed interface Task
    data class TaskImpl(val future: ScheduledFuture<*>) : Task

    /**
     * Schedule a function for periodic execution at the given interval.
     * The function should be thread safe, and not take longer to execute than the given interval.
     */
    fun schedule(waitTime: Duration, runImmediately: Boolean = true, task: () -> Unit): Task {
        val period = max(1, waitTime.toSeconds())
        val initialDelay = if (runImmediately) 0 else period
        val future = executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS)
        return TaskImpl(future)
    }

    /**
     * Remove the given task from the scheduler.
     * If the task is currently executing, it will be allowed to finish.
     */
    fun cancel(task: Task) {
        (task as TaskImpl).future.cancel(false)
    }

    /**
     * Cancel all tasks and stop the scheduler.
     * Any tasks currently executing will be allowed to finish.
     */
    fun stop() {
        executor.shutdown()
    }

    companion object {
        /**
         * Create a new scheduler.
         * The scheduler will always use a single thread.
         */
        fun create(): Scheduler {
            val executor = ScheduledThreadPoolExecutor(1)
            executor.removeOnCancelPolicy = true
            return Scheduler(executor)
        }
    }
}
