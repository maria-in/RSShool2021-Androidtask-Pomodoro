package com.mindorks.framework.rsshool2021_android_task_pomodoro_new.foregroundservice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mindorks.framework.rsshool2021_android_task_pomodoro_new.*
import com.mindorks.framework.rsshool2021_android_task_pomodoro_new.stopwatch.StopwatchListener
import kotlinx.coroutines.*

class ForegroundService : Service() {

    //определяем запущен ли сервис
    private var isServiceStarted = false
    //обращаемся, когда надо показать нотификацию или обновить её состояние(1 раз в секнду)
    private var notificationManager: NotificationManager? = null
    //для многопоточности
    private var job: Job? = null

    //нужен каждый раз, когда обновляем нотификацию,
    //но некоторые значения неизменны, поэтому изначально создаём с такими параметрами
    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro")
            .setGroup("Timer")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
                //при нажатии на нотификацию вернёмся в мейн активити
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
    }

    //создание экземпляра notificationManager
    override fun onCreate() {
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
    }

    //обрабатываем интент (вызов - когда сервис запускается)
    @DelicateCoroutinesApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //получаем данные из intent, определяем: старт или стоп сервис
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @DelicateCoroutinesApi
    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> commandStart()
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }
    }

    @DelicateCoroutinesApi
    private fun commandStart() {
        if (isServiceStarted) {
            return
        }
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer()
        } finally {
            isServiceStarted = true
        }
    }

    @DelicateCoroutinesApi
    private fun continueTimer() {
        job = GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                notificationManager?.notify(
                        NOTIFICATION_ID,
                        getNotification(
                                StopwatchListener.customValue.value.displayTime()
                        )
                )
                delay(INTERVAL)
            }
        }
    }

    //продолжаем отсчитывать секундомер
    private fun continueTimer(startTime: Long) {
        job = GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(
                        StopwatchListener.customValue.value.displayTime()
                    )
                )
                delay(INTERVAL)
            }
        }
    }

    //останавливаем обновление секундомераjob?.cancel(), убираем сервис
    //из форегроунд стейта stopForeground(true), и останавливаем сервис stopSelf()
    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        try {
            job?.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    //вызов startForegroundService() или startService() в зависимости от api
    //это делаем внутри сервиса потому как будет выдаваить ошибку при отличном от сервиса
    //контексте
    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    //создаем канал, если API >= Android O. Создаем нотификацию и вызываем startForeground()
    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                    CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.action = Intent.ACTION_MAIN
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    private companion object {

        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L
    }
}