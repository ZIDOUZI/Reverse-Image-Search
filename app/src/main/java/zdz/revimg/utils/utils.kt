package zdz.revimg.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import androidx.core.graphics.scale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import zdz.revimg.BuildConfig
import java.net.URLEncoder
import kotlin.math.sqrt

val scope = CoroutineScope(Dispatchers.Default)

fun Activity.handleCreate(queryURL: String) {
    scope.launch {
        awaitAll(
            async { handleIntent(queryURL) },
            async { checkUpdate()?.let { updateInfo -> notify(updateInfo) } })
    }
}

data class UpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val releaseUrl: String,
    val releaseNotes: String,
    val publishedAt: String
)

suspend fun checkUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
    try {
        // 获取GitHub仓库的最新发布版本
        val url = URL("https://api.github.com/repos/ZIDOUZI/Reverse-Image-Search/releases/latest")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            connectTimeout = 10000
            readTimeout = 10000
        }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(response)

            // 解析GitHub API响应
            val latestVersion = jsonResponse.getString("tag_name").removePrefix("v")
            val releaseUrl = jsonResponse.getString("html_url")
            val releaseNotes = jsonResponse.getString("body")
            val publishedAt = jsonResponse.getString("published_at")

            // 获取当前应用版本
            val currentVersion = BuildConfig.VERSION

            // 比较版本号
            if (isNewerVersion(currentVersion, latestVersion)) {
                return@withContext UpdateInfo(
                    currentVersion = currentVersion,
                    latestVersion = latestVersion,
                    releaseUrl = releaseUrl,
                    releaseNotes = releaseNotes,
                    publishedAt = publishedAt
                )
            }
        }
        return@withContext null
    } catch (e: Exception) {
        Log.e("UpdateChecker", "检查更新失败", e)
        return@withContext null
    }
}

fun Activity.notify(info: UpdateInfo) {
    with(Channels.MESSAGE) {
        register()
        createAndSend {
            setSmallIcon(android.R.drawable.ic_dialog_info)
            setContentTitle("发现新版本: ${info.latestVersion}")
            setContentText("点击打开下载链接")
            setStyle(NotificationCompat.BigTextStyle().bigText(info.releaseNotes))
            setContentIntent(activityIntent(viewContent(info.releaseUrl.toUri(), "text/plain")))
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_DEFAULT
        }
    }
}

private fun isNewerVersion(currentVersion: String, latestVersion: String): Boolean {
    try {
        val current = currentVersion.split(".").map { it.toInt() }
        val latest = latestVersion.split(".").map { it.toInt() }

        // 比较主版本号、次版本号和修订号
        for (i in 0 until minOf(current.size, latest.size)) {
            if (latest[i] > current[i]) return true
            if (latest[i] < current[i]) return false
        }

        // 如果前面的版本号都相同，但最新版本有更多的版本号段
        return latest.size > current.size
    } catch (e: Exception) {
        Log.e("UpdateChecker", "版本比较失败", e)
        return false
    }
}

suspend fun Activity.handleIntent(queryURL: String) {
    val uriString = when (intent.action) {
        Intent.ACTION_PROCESS_TEXT -> intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
        Intent.ACTION_VIEW, Intent.ACTION_SEND -> run {
//            Debug.waitForDebugger()
            if (intent.type?.startsWith("image/") != true) null else {
                intent.uri?.let { processImageUri(it) }?.let {
                    try {
                        upload(it)
                    } catch (e: Exception) {
                        Log.e("ShareReceiver", "上传失败", e)
                        return toastAndFinish("上传失败: ${e.message}")
                    }
                }
            }
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
            viewContent(
                queryURL.replace("%s", URLEncoder.encode(uriString, "UTF-8")).toUri()
            ).let { startActivity(it) }
            toastAndFinish("已打开")
        }
    } catch (e: Exception) {
        Log.e("UriProcess", "打开搜索结果失败", e)
        toastAndFinish("打开搜索结果失败: ${e.message}")
    }
}

private fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()

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

private fun Bitmap.scaleBitmap(totalPixels: Int = 65536): Bitmap {
    if (width * height <= totalPixels) return this

    val ratio = sqrt(totalPixels.toDouble() / (width * height)).toFloat()
    val newWidth = (width * ratio).toInt()
    val newHeight = (height * ratio).toInt()

    return scale(newWidth, newHeight).also { if (it != this) recycle() }
}

/*private fun Bitmap.scaleBitmap(maxWidth: Int = 256, maxHeight: Int = 256): Bitmap {
    if (width <= maxWidth && height <= maxHeight) return this

    val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)

    val newWidth = (width * ratio).toInt()
    val newHeight = (height * ratio).toInt()

    return scale(newWidth, newHeight).also { if (it != this) recycle() }
}*/

suspend fun upload(img: Bitmap): String = withContext(Dispatchers.IO) {
    val imageBytes = ByteArrayOutputStream().use {
        img.compress(Bitmap.CompressFormat.JPEG, 90, it)
        it.toByteArray()
    }

    val fileName = "image_${System.currentTimeMillis()}.jpg"

    // 准备multipart请求
    val boundary = "----WebKitFormBoundary${System.currentTimeMillis()}"
    val lineEnd = "\r\n"
    val twoHyphens = "--"

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

    DataOutputStream(connection.outputStream).use {
        it.writeBytes("$twoHyphens$boundary$lineEnd")
        it.writeBytes("Content-Disposition: form-data; name=\"files\"; filename=\"$fileName\"$lineEnd")
        it.writeBytes("Content-Type: image/jpeg$lineEnd$lineEnd")

        it.write(imageBytes)
        it.writeBytes(lineEnd)

        it.writeBytes("$twoHyphens$boundary$lineEnd")
        it.flush()
    }

    val responseCode = connection.responseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonResponse = JSONObject(response)
        val destinationUrl = jsonResponse.optString("dest")

        if (destinationUrl.isNotEmpty()) return@withContext destinationUrl
    }

    throw Error("上传失败，错误码: $responseCode")
}