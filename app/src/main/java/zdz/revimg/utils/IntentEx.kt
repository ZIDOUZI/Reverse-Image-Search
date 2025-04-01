@file:Suppress("NOTHING_TO_INLINE", "UNUSED", "FunctionName", "HasPlatformType")

package zdz.revimg.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.core.net.toUri
import java.io.File
import android.content.Intent as SIntent

//region Native Constructor enhancement
inline fun Intent(
    action: String? = null,
    data: Uri? = null,
    handler: SIntent.() -> Unit = {}
): SIntent = SIntent(action, data).apply(handler)

inline fun <reified T> Intent(
    context: Context,
    action: String? = null,
    uri: Uri? = null,
    handler: SIntent.() -> Unit = {}
): SIntent = SIntent(action, uri, context, T::class.java).apply(handler)
//endregion

//region Useful Constructor
inline fun <reified T> Context.componentIntent(action: String? = null) =
    Intent<T>(this, action)

inline fun viewContent(
    uri: Uri,
    mimeType: String? = null,
    newTask: Boolean = true
) = Intent(SIntent.ACTION_VIEW) {
    setDataAndTypeAndNormalize(uri, mimeType)
    if (newTask) flagNewTask()
}

fun viewImage(image: File, choose: Boolean = true, newTask: Boolean = true) =
    viewContent(image.toUri(), "image/*", newTask)

fun viewImage(image: Uri, choose: Boolean = true, newTask: Boolean = true) =
    viewContent(image, "image/*", newTask)

fun viewUri(uri: Uri, choose: Boolean = true, newTask: Boolean = true) =
    viewContent(uri, newTask = newTask)


inline fun sendContent(mimeType: String? = null, putExtra: SIntent.() -> Unit) =
    Intent(SIntent.ACTION_SEND) {
        type = mimeType
        putExtra()
    }

fun sendText(text: String, mimeType: String = "text/*") =
    sendContent(mimeType) { putExtra(SIntent.EXTRA_TEXT, text) }

fun sendImage(image: File, mimeType: String = "image/*") =
    sendContent(mimeType) { putExtra(SIntent.EXTRA_STREAM, image.toUri()) }

fun sendImage(image: Uri, mimeType: String = "image/*") =
    sendContent(mimeType) { putExtra(SIntent.EXTRA_STREAM, image) }
//endregion

//region Extend function
fun SIntent.createChooser(
    title: String = "选择应用：",
    sender: IntentSender? = null,
    confirm: Boolean = true
) = if (confirm) SIntent.createChooser(this, title, sender) else this

inline fun <reified T : Parcelable> SIntent.getExtra(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra<T>(name)
    }

private const val FLAG_NEW_TASK = SIntent.FLAG_ACTIVITY_NEW_TASK or
        SIntent.FLAG_ACTIVITY_CLEAR_TASK or
        SIntent.FLAG_GRANT_READ_URI_PERMISSION

fun SIntent.flagNewTask() = apply { flags = FLAG_NEW_TASK }

val SIntent.uri get() = data ?: clipData?.getItemAt(0)?.uri ?: getExtra(SIntent.EXTRA_STREAM)
//endregion

fun ComponentName.mainActivity() = SIntent.makeMainActivity(this)
fun Activity.mainActivity() = SIntent.makeMainActivity(componentName)

fun ComponentName.restartMainActivity() = SIntent.makeRestartActivityTask(this)
fun Activity.restartMainActivity() = SIntent.makeRestartActivityTask(componentName)

inline fun Context.startActivity(
    action: String,
    data: Uri? = null,
    newTask: Boolean = false,
    handler: SIntent.() -> Unit
) = startActivity(Intent(action, data, handler).apply { if (newTask) flagNewTask() })

inline fun <reified T : Activity> Context.startActivity(
    action: String? = null,
    data: Uri? = null,
    handler: SIntent.() -> Unit
) = startActivity(Intent<T>(this, action, data, handler))

inline fun Activity.startActivityForResult(
    requestCode: Int,
    action: String? = null,
    data: Uri? = null,
    handler: SIntent.() -> Unit
) = startActivityForResult(Intent(action, data, handler), requestCode)