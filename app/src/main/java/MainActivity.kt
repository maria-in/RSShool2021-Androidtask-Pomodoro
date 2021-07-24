package com.mindorks.framework.rsshool2021_android_task_pomodoro_new

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindorks.framework.rsshool2021_android_task_pomodoro_new.databinding.ActivityMainBinding
import com.mindorks.framework.rsshool2021_android_task_pomodoro_new.foregroundservice.ForegroundService
import com.mindorks.framework.rsshool2021_android_task_pomodoro_new.stopwatch.Stopwatch
import com.mindorks.framework.rsshool2021_android_task_pomodoro_new.stopwatch.StopwatchAdapter
import com.mindorks.framework.rsshool2021_android_task_pomodoro_new.stopwatch.StopwatchListener
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding
    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    private var timeMs = 0L
    private var currentMs = StopwatchListener.customValue.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding =  ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {
            if(!binding.editTime.text.isEmpty()) {
                try {
                    if ((binding.editTime.text.toString().toLong() * 1).toString().equals("0")) {
                        Toast.makeText(this, "Понадобится явно больше времени, введите другое значение", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        timeMs = binding.editTime.text.toString().toLong() * 1000 * 60
                        if(timeMs > 86400000L)
                            Toast.makeText(this, "Понадобится явно меньше времени, введите другое значение", Toast.LENGTH_SHORT).show()
                        else{
                            stopwatches.add(Stopwatch(nextId++, timeMs, false, timeMs))
                            stopwatchAdapter.submitList(stopwatches.toList())
                        }
                    }
                    binding.editTime.text.clear()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Введено слишком большое значение, необходимо задать меньшее", Toast.LENGTH_SHORT).show()
                    binding.editTime.text.clear()
                }
            } else {
                Toast.makeText(this, "Введите время!", Toast.LENGTH_SHORT).show()
                binding.editTime.text.clear()
            }
        }
    }

    override fun start(id: Int) {
        changeStopwatch(id, null, true)
        for (i in 0..stopwatches.size - 1)
            if (stopwatches[i].id != id && stopwatches[i].isStarted)
                changeStopwatch(stopwatches[i].id, stopwatches[i].currentMs, false)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, currentMs ?: it.currentMs, isStarted, it.period))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, currentMs)
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)

    }

    private companion object {
        const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME"
    }
}