package de.rki.coronawarnapp.notification

import de.rki.coronawarnapp.R

/**
 * The notification constants are used inside the NotificationHelper
 *
 * @see NotificationHelper
 */
object NotificationConstants {

    const val NOTIFICATION_REQUEST_CODE_ID = "NOTIFICATION_REQUEST_CODE_ID"

    const val POSITIVE_RESULT_NOTIFICATION_REQUEST_CODE = 100

    /**
     * Notification channel id String.xml path
     */
    const val NOTIFICATION_CHANNEL_ID = R.string.notification_channel_id

    /**
     * Notification small icon String.xml path
     */
    const val NOTIFICATION_SMALL_ICON = R.drawable.ic_splash_logo

    /**
     * Notification channel name String.xml path
     */
    const val CHANNEL_NAME = R.string.notification_name

    /**
     * Notification channel description String.xml path
     */
    const val CHANNEL_DESCRIPTION = R.string.notification_description

    /**
     * Risk changed notification title String.xml path
     */
    const val NOTIFICATION_CONTENT_TITLE_RISK_CHANGED = R.string.notification_headline

    /**
     * Risk changed notification content text String.xml path
     */
    const val NOTIFICATION_CONTENT_TEXT_RISK_CHANGED = R.string.notification_body
}
