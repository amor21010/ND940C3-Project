package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding

sealed class DownloadStatus(val value: Int) {
    object SUCCESS : DownloadStatus(1)
    object FAIL : DownloadStatus(0)
}

const val DOWNLOAD_STATUS_KEY = "status"
const val FILE_NAME_KEY = "file_name"

class MainActivity : AppCompatActivity() {
    companion object {
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "download_channel_name"
        private const val NOTIFICATION_ID = 0
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var downloadManager: DownloadManager
    private lateinit var downloadStatus: DownloadStatus


    private var url: String = ""

    private var fileName: String = ""

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.content.radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.glide -> {
                    url = "https://github.com/bumptech/glide"
                    fileName = getString(R.string.glide)
                }
                R.id.loadapp -> {
                    url =
                        "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
                    fileName = getString(R.string.loadapp)
                }
                R.id.retrofit -> {
                    url = "https://github.com/square/retrofit"
                    fileName = getString(R.string.retrofit)
                }
            }
        }

        binding.content.downloadButton.setOnClickListener {
            if (url.isEmpty()) {
                Toast.makeText(
                    this, "Please select a file to download",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            binding.content.downloadButton.buttonState = ButtonState.Clicked
            download(url)
            binding.content.downloadButton.buttonState = ButtonState.Loading
        }
        notificationManager = getSystemService(NotificationManager::class.java)
        createChannel()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID) {
                binding.content.downloadButton.buttonState = ButtonState.Completed

                val query = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))

                if (query.moveToFirst()) {
                    val statusIndex = query.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    when (query.getInt(statusIndex)) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloadStatus = DownloadStatus.SUCCESS
                        }
                        DownloadManager.STATUS_FAILED -> {
                            downloadStatus = DownloadStatus.FAIL
                        }
                    }
                    downloadNotify()
                }
            }
        }
    }

    private fun downloadNotify() {
        notificationManager.sendNotification(getText(R.string.notification_description).toString())
        Toast.makeText(this, "Download finished!", Toast.LENGTH_SHORT).show()
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun createChannel() {

        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.description = "Download finished"
        notificationManager.createNotificationChannel(
            notificationChannel
        )

    }

    private fun NotificationManager.sendNotification(body: String) {

        val detailIntent = Intent(applicationContext, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = ACTION_VIEW
        }
        detailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        detailIntent.putExtra(DOWNLOAD_STATUS_KEY, downloadStatus.value)
        detailIntent.putExtra(FILE_NAME_KEY, fileName)
        pendingIntent = PendingIntent.getActivity(
            applicationContext, NOTIFICATION_ID, detailIntent, FLAG_IMMUTABLE
        )

        action = NotificationCompat.Action.Builder(
            R.drawable.ic_assistant_black_24dp,
            applicationContext.getString(R.string.notification_button),
            pendingIntent
        ).build()

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .addAction(action)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notify(NOTIFICATION_ID, builder.build())
    }

}
