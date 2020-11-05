package de.rki.coronawarnapp.notification

import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.notification.NotificationConstants.NOTIFICATION_REQUEST_CODE_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_REQUEST_CODE
import de.rki.coronawarnapp.ui.main.MainActivity
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.Interval
import timber.log.Timber
import kotlin.random.Random

/**
 * Singleton class for notification handling
 * Notifications should only be sent when the app is not in foreground.
 * The helper uses externalised constants for readability.
 *
 * @see NotificationConstants
 */
object NotificationHelper {

    private val TAG: String? = NotificationHelper::class.simpleName

    /**
     * Notification channel id
     *
     * @see NotificationConstants.NOTIFICATION_CHANNEL_ID
     */
    private val channelId =
        CoronaWarnApplication.getAppContext()
            .getString(NotificationConstants.NOTIFICATION_CHANNEL_ID)

    /**
     * Notification manager
     */
    private val notificationManager =
        CoronaWarnApplication.getAppContext()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Notification channel audio attributes
     */
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    /**
     * Create notification channel
     * Notification channel is only needed for API version >= 26.
     * Safe to be called repeatedly.
     *
     * @see NotificationConstants.CHANNEL_NAME
     * @see NotificationConstants.CHANNEL_DESCRIPTION
     * @see audioAttributes
     * @see notificationManager
     * @see channelId
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName =
                CoronaWarnApplication.getAppContext().getString(NotificationConstants.CHANNEL_NAME)

            val notificationRingtone =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)

            channel.description =
                CoronaWarnApplication.getAppContext()
                    .getString(NotificationConstants.CHANNEL_DESCRIPTION)
            channel.setSound(notificationRingtone, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleRepeatingNotification(initialTime: Instant, interval: Duration, requestCode: Int) {
        val intent = Intent(CoronaWarnApplication.getAppContext(), NotificationReceiver::class.java)
        intent.putExtra(NOTIFICATION_REQUEST_CODE_ID, requestCode)
        val pendingIntent = PendingIntent.getBroadcast(
                CoronaWarnApplication.getAppContext(),
                requestCode,
                intent,
                FLAG_UPDATE_CURRENT)

        val manager = CoronaWarnApplication.getAppContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setInexactRepeating(AlarmManager.RTC, initialTime.millis, interval.millis, pendingIntent)
    }

    fun cancelNotification(requestCode: Int) {
        val pendingIntent = PendingIntent.getBroadcast(
                CoronaWarnApplication.getAppContext(),
                requestCode,
                Intent(),
                FLAG_UPDATE_CURRENT)
        val manager = CoronaWarnApplication.getAppContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pendingIntent)
    }

    /**
     * Build notification
     * Create notification with defined title, content text and visibility.
     *
     * @param title: String
     * @param content: String
     * @param visibility: Int
     *
     * @return Notification?
     *
     * @see NotificationCompat.VISIBILITY_PUBLIC
     */
    private fun buildNotification(
        title: String,
        content: String,
        visibility: Int,
        expandableLongText: Boolean = false,
        pendingIntent: PendingIntent = createPendingIntentToMainActivity()
    ): Notification? {
        val builder = NotificationCompat.Builder(CoronaWarnApplication.getAppContext(), channelId)
            .setSmallIcon(NotificationConstants.NOTIFICATION_SMALL_ICON)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(visibility)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (expandableLongText) {
            builder
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(content)
                )
        }

        if (title.isNotEmpty()) {
            builder.setContentTitle(title)
        }

        if (visibility == NotificationCompat.VISIBILITY_PRIVATE) {
            builder.setPublicVersion(
                buildNotification(
                    title,
                    content,
                    NotificationCompat.VISIBILITY_PUBLIC
                )
            )
        } else if (visibility == NotificationCompat.VISIBILITY_PUBLIC) {
            builder.setContentText(content)
        }

        return builder.build().also { logNotificationBuild(it) }
    }

    /**
     * Create pending intent to main activity
     *
     * @return PendingIntent
     */
    private fun createPendingIntentToMainActivity() =
        PendingIntent.getActivity(
            CoronaWarnApplication.getAppContext(),
            0,
            Intent(CoronaWarnApplication.getAppContext(), MainActivity::class.java),
            0
        )

    /**
     * Send notification
     * Build and send notification with predefined title, content and visibility.
     *
     * @param title: String
     * @param content: String
     * @param visibility: Int
     */
    fun sendNotification(
        title: String,
        content: String,
        visibility: Int,
        expandableLongText: Boolean = false,
        pendingIntent: PendingIntent = createPendingIntentToMainActivity()
    ) {
        val notification =
            buildNotification(title, content, visibility, expandableLongText, pendingIntent) ?: return
        with(NotificationManagerCompat.from(CoronaWarnApplication.getAppContext())) {
            notify(Random.nextInt(), notification)
        }
    }

    /**
     * Send notification
     * Build and send notification with content and visibility.
     * Notification is only sent if app is not in foreground.
     *
     * @param content: String
     * @param visibility: Int
     */
    fun sendNotification(content: String, visibility: Int) {
        if (!CoronaWarnApplication.isAppInForeground) {
            sendNotification("", content, visibility, true)
        }
    }

    /**
     * Log notification build
     * Log success or failure of creating new notification
     *
     * @param notification: Notification?
     */
    private fun logNotificationBuild(notification: Notification?) {
        if (BuildConfig.DEBUG) {
            if (notification != null) {
                Timber.d("Notification build successfully.")
            } else {
                Timber.d("Notification build failed.")
            }
        }
    }
}
