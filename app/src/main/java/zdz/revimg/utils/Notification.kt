package zdz.revimg.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


const val FLAG = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

fun Context.activityIntent(
    intent: Intent,
    requestCode: Int = 0,
    flags: Int = FLAG
): PendingIntent = PendingIntent.getActivity(this, requestCode, intent, flags)

fun Context.broadcastIntent(
    intent: Intent,
    requestCode: Int = 0,
    flags: Int = FLAG
): PendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, flags)

fun Context.serviceIntent(
    intent: Intent,
    requestCode: Int = 0,
    flags: Int = FLAG
): PendingIntent = PendingIntent.getService(this, requestCode, intent, flags)

inline fun <reified T : BroadcastReceiver> Context.broadcastIntent(
    requestCode: Int = 0,
    action: String? = null,
    flags: Int = FLAG
): PendingIntent = PendingIntent.getBroadcast(this, requestCode, componentIntent<T>(action), flags)

inline fun <reified T : Activity> Context.activityIntent(
    requestCode: Int = 0,
    action: String? = null,
    flags: Int = FLAG
): PendingIntent = PendingIntent.getActivity(this, requestCode, componentIntent<T>(action), flags)

enum class Channels {
    MESSAGE,
    ;

    val id get() = ordinal + 1

    fun Context.register(action: NotificationChannel.() -> Unit = {}) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(name, name, NotificationManager.IMPORTANCE_DEFAULT).apply(action)
                .also { NotificationManagerCompat.from(this).createNotificationChannel(it) }
        } else null

    fun Context.unregister() =
        NotificationManagerCompat.from(this).deleteNotificationChannel(name)

    fun Context.create(builder: NotificationCompat.Builder.() -> Unit) =
        NotificationCompat.Builder(this, name).apply(builder).build()

    @SuppressLint("NotificationPermission")
    fun Context.createAndSend(builder: NotificationCompat.Builder.() -> Unit) =
        create(builder).also {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this)
                    .notify(this@Channels.name, this@Channels.id, it)
            }
        }
}