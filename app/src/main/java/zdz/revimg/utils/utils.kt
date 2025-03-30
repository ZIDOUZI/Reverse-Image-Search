package zdz.revimg.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.net.toUri
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import androidx.core.graphics.scale
import kotlinx.coroutines.CoroutineScope
import java.net.URLEncoder

val scope = CoroutineScope(Dispatchers.Default)

suspend fun Activity.handleIntent(queryURL: String) {
    val uriString = when (intent.action) {
        Intent.ACTION_PROCESS_TEXT -> intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
        Intent.ACTION_VIEW, Intent.ACTION_SEND -> run {
//            Debug.waitForDebugger()
            if (intent.type?.startsWith("image/") == true) {
                val imgUrl = intent.data
                    ?: intent.clipData?.getItemAt(0)?.uri
                    ?: intent.getExtra<Uri>(Intent.EXTRA_STREAM)
                imgUrl?.let { processImageUri(it) }?.let {
                    try {
                        return@run upload(it)
                    } catch (e: Exception) {
                        Log.e("ShareReceiver", "上传失败", e)
                        return toastAndFinish("上传失败: ${e.message}")
                    }
                }
            }
            null
        } ?: intent.dataString ?: intent.getStringExtra(Intent.EXTRA_TEXT)

        else -> null
    }

    if (uriString == null) return toastAndFinish("分享类型错误 | 分享内容不包含URI")

    if (!uriString.isValidUrl()) {
        Log.e("UriProcess", "无效的URL: $uriString")
        return toastAndFinish("分享内容不包含有效的URL")
    }

    try {
        withContext(Dispatchers.Main) {
            startActivity(Intent.ACTION_VIEW) {
                data = queryURL.replace("%s", URLEncoder.encode(uriString, "UTF-8")).toUri()
                flagNewTask()
            }
            toastAndFinish("已打开")
        }
    } catch (e: Exception) {
        Log.e("UriProcess", "打开搜索结果失败", e)
        toastAndFinish("打开搜索结果失败: ${e.message}")
    }
}

private fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()

private inline fun <reified T : Parcelable> Intent.getExtra(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra<T>(name)
    }

fun Activity.toastAndFinish(msg: String) {
    runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    finishAndRemoveTask()
}

fun Activity.processImageUri(uri: Uri): Bitmap? {
    try {
        return contentResolver.openInputStream(uri)
            ?.use { BitmapFactory.decodeStream(it) }
            ?.scaleBitmap()
    } catch (e: Exception) {
        Log.e("ShareReceiver", "处理文件失败", e)
        return null
    }
}

// 缩放图片到合适的尺寸
private fun Bitmap.scaleBitmap(maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap {
    // 获取原始尺寸
    val width = width
    val height = height

    // 如果图片已经小于目标尺寸，直接返回
    if (width <= maxWidth && height <= maxHeight) return this

    val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)

    // 计算新尺寸
    val newWidth = (width * ratio).toInt()
    val newHeight = (height * ratio).toInt()

    // 缩放图片
    return scale(newWidth, newHeight).also {
        // 如果创建了新的Bitmap，释放原始Bitmap
        if (it != this) recycle()
    }
}

suspend fun upload(img: Bitmap): String = withContext(Dispatchers.IO) {
    // 将Bitmap转换为字节数组
    val imageBytes = ByteArrayOutputStream().use {
        img.compress(Bitmap.CompressFormat.JPEG, 90, it)
        it.toByteArray()
    }

    // 创建一个唯一的文件名
    val fileName = "image_${System.currentTimeMillis()}.jpg"

    // 准备multipart请求
    val boundary = "----WebKitFormBoundary${System.currentTimeMillis()}"
    val lineEnd = "\r\n"
    val twoHyphens = "--"

    // 创建URL连接
    val url = URL("https://api.resmush.it/?qlty=60")
    val connection = (url.openConnection() as HttpURLConnection).apply {
        doInput = true
        doOutput = true
        useCaches = false
        requestMethod = "POST"
        connectTimeout = 15000
        readTimeout = 15000
        setRequestProperty("Connection", "Keep-Alive")
        setRequestProperty("User-Agent", "ReverseImageSearch/1.0")
        setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
    }

    // 写入请求体
    DataOutputStream(connection.outputStream).use {
        it.writeBytes("$twoHyphens$boundary$lineEnd")
        it.writeBytes("Content-Disposition: form-data; name=\"files\"; filename=\"$fileName\"$lineEnd")
        it.writeBytes("Content-Type: image/jpeg$lineEnd$lineEnd")

        it.write(imageBytes)
        it.writeBytes(lineEnd)

        it.writeBytes("$twoHyphens$boundary$lineEnd")
        it.flush()
    }

    // 获取响应
    val responseCode = connection.responseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        // 解析JSON响应获取图像URL
        val jsonResponse = JSONObject(response)
        val destinationUrl = jsonResponse.optString("dest")

        if (destinationUrl.isNotEmpty()) return@withContext destinationUrl
    }

    throw Error("上传失败，错误码: $responseCode")
}