package com.mindorks.framework.rsshool2021_android_task_pomodoro_new.stopwatch

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean,
    var period: Long
)
