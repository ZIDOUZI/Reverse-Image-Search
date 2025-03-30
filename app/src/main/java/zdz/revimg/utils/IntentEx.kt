@file:Suppress("NOTHING_TO_INLINE", "UNUSED", "FunctionName", "HasPlatformType")

package zdz.revimg.utils

import android.app.Activity
import android.content.Context
import android.content.Intent as SIntent
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

inline fun Intent(action: String? = null, handler: SIntent.() -> Unit): SIntent =
    SIntent(action).apply(handler)

inline fun <reified T : Activity> Intent(
    context: Context,
    handler: SIntent.() -> Unit
): SIntent = SIntent(context, T::class.java).apply(handler)

inline fun Context.startActivity(action: String, handler: SIntent.() -> Unit) =
    startActivity(Intent(action, handler))

inline fun <reified T : Activity> Context.startActivity(handler: SIntent.() -> Unit) =
    startActivity(Intent<T>(this, handler))

inline fun Activity.startActivityForResult(
    requestCode: Int,
    action: String? = null,
    handler: SIntent.() -> Unit
) = startActivityForResult(Intent(action, handler), requestCode)

fun SIntent.createChooser(title: String = "选择应用：", confirm: Boolean = true) =
    if (confirm) SIntent.createChooser(this, title) else this

fun viewContent(uri: Uri, mimeType: String? = null) = Intent(SIntent.ACTION_VIEW) {
    setDataAndTypeAndNormalize(uri, mimeType)
}

fun viewImage(image: File, choose: Boolean = true) =
    viewContent(image.toUri(), "image/*")

fun viewImage(image: Uri, choose: Boolean = true) =
    viewContent(image, "image/*")

fun viewUri(uri: Uri, choose: Boolean = true) = viewContent(uri)

inline fun sendContent(mimeType: String? = null, putExtra: SIntent.() -> Unit) =
    Intent(SIntent.ACTION_SEND) {
        type = mimeType
        putExtra()
    }


fun sendText(text: String, mimeType: String = "text/*") =
    sendContent(mimeType) { putExtra(SIntent.EXTRA_TEXT, text) }

fun sendImage(image: File) = sendContent("image/*") {
    putExtra(SIntent.EXTRA_STREAM, image.toUri())
}

fun sendImage(image: Uri) = sendContent("image/*") {
    putExtra(SIntent.EXTRA_STREAM, image)
}

fun SIntent.flagNewTask() = this.apply {
    flags = SIntent.FLAG_ACTIVITY_NEW_TASK or
            SIntent.FLAG_ACTIVITY_CLEAR_TASK or
            SIntent.FLAG_GRANT_READ_URI_PERMISSION
}