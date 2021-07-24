package com.mindorks.framework.rsshool2021_android_task_pomodoro_new.stopwatch

import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.mindorks.framework.rsshool2021_android_task_pomodoro_new.R
import com.mindorks.framework.rsshool2021_android_task_pomodoro_new.databinding.StopwatchItemBinding
import com.mindorks.framework.rsshool2021_android_task_pomodoro_new.displayTime

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener
) : RecyclerView.ViewHolder(binding.root), LifecycleObserver {

    private var timer: CountDownTimer? = null
    var startTime = 0L

    fun bind(stopwatch: Stopwatch) {
        startTime = System.currentTimeMillis()
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.customViewOne.setPeriod(stopwatch.period)
        binding.customViewOne.setCurrent(stopwatch.currentMs)
        binding.commandButton.isEnabled = true
        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }
        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.commandButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }
        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun startTimer(stopwatch: Stopwatch) {

        binding.commandButton.text = "Pause"

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        if (stopwatch.currentMs < UNIT_TEN_MS) {
            binding.commandButton.isEnabled = false
            binding.customViewOne.setCurrent(0)
        }
        binding.commandButton.text = "Start"
        timer?.cancel()
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        binding.customViewOne.setPeriod(stopwatch.period)
        return object : CountDownTimer(stopwatch.currentMs, UNIT_TEN_MS) {
            override fun onTick(millisUntilFinished: Long) {
                if (stopwatch.currentMs > UNIT_TEN_MS) {
                    binding.stopwatchTimer.text = millisUntilFinished.displayTime()
                    StopwatchListener.customValue.value = millisUntilFinished
                    stopwatch.currentMs = millisUntilFinished
                    binding.customViewOne.setCurrent(millisUntilFinished)
                }else{stopTimer(stopwatch)}
            }

            override fun onFinish() {
                stopTimer(stopwatch)
                //binding.customViewOne.setCurrent(0)
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                Toast.makeText(itemView.context, "Пора приступать к новым делам!", Toast.LENGTH_SHORT).show()
                MediaPlayer.create(itemView.context, R.raw.inflicted).start()

            }
        }
    }

    private companion object {
        private const val UNIT_TEN_MS = 100L
    }
}