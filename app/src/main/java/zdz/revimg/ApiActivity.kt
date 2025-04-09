package zdz.revimg

import android.app.Activity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.launch
import zdz.revimg.api.RevImgApi
import zdz.revimg.utils.processImageUri
import zdz.revimg.utils.scope
import zdz.revimg.utils.toastAndFinish
import zdz.revimg.utils.upload
import zdz.revimg.utils.viewContent
import androidx.core.net.toUri
import zdz.revimg.utils.serializableExtra
import zdz.revimg.utils.uri

/**
 * API接口Activity，用于处理其他应用发送过来的请求
 * 支持以下操作：
 * 1. ACTION_SEARCH_IMAGE - 接收图片并根据指定的搜索引擎进行搜索
 * 2. ACTION_GET_SEARCH_ENGINES - 获取支持的搜索引擎列表
 */
class ApiActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scope.launch {
            when (intent.action) {
                RevImgApi.Actions.SEARCH_IMAGE -> handleSearchImage()
                else -> {
                    // 不支持的action，直接结束
                    Log.w("ApiActivity", "不支持的Action: ${intent.action}")
                    toastAndFinish("不支持的请求类型")
                }
            }
        }
    }

    /**
     * 处理搜索图片请求
     * 接收图片URI和搜索引擎参数，然后使用指定的搜索引擎进行搜索
     */
    private suspend fun handleSearchImage() {
        val engine = intent.serializableExtra<RevImgApi.Engine>(RevImgApi.Extras.SEARCH_ENGINE)
        val queryUrl = engine?.source ?: return toastAndFinish("不支持的搜索引擎")

        if (intent.type?.startsWith("image/") != true) intent.uri?.toString() else {
            intent.uri?.let { processImageUri(it) }?.let {
                try {
                    upload(it)
                } catch (e: Exception) {
                    Log.e("ShareReceiver", "上传失败", e)
                    return toastAndFinish("上传失败: ${e.message}")
                }
            }
        }?.let { imageUrl ->
            startActivity(viewContent(queryUrl.replace("%s", imageUrl).toUri()))
            toastAndFinish("已使用${engine}搜索图片")
        } ?: toastAndFinish("无法处理图片")
    }
}
