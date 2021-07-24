package com.mindorks.framework.rsshool2021_android_task_pomodoro_new.stopwatch

interface StopwatchListener {
    fun start(id: Int)
    fun stop(id: Int, currentMs: Long)
    fun delete(id: Int)
    object customValue{
        var value: Long = 0
    }
}