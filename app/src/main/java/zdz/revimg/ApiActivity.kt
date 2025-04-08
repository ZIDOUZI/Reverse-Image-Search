package zdz.revimg

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import zdz.revimg.utils.getExtra
import zdz.revimg.utils.startActivity
import zdz.revimg.utils.uri

/**
 * API接口Activity，用于处理其他应用发送过来的请求
 * 支持以下操作：
 * 1. ACTION_SEARCH_IMAGE - 接收图片并根据指定的搜索引擎进行搜索
 * 2. ACTION_GET_SEARCH_ENGINES - 获取支持的搜索引擎列表
 */
class ApiActivity : Activity() {
    companion object {
        // 支持的搜索引擎
        val SUPPORTED_ENGINES = mapOf(
            RevImgApi.Engines.GOOGLE to GoogleLensActivity.QUERY_URL,
            RevImgApi.Engines.SAUCENAO to SauceNaoActivity.QUERY_URL,
            RevImgApi.Engines.YANDEX to YandexActivity.QUERY_URL,
            RevImgApi.Engines.IQDB to IqdbActivity.QUERY_URL,
            RevImgApi.Engines.ASCII2D to Ascii2dActivity.QUERY_URL,
            RevImgApi.Engines.TRACEMOE to TraceMoeActivity.QUERY_URL
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scope.launch {
            when (intent.action) {
                RevImgApi.Actions.SEARCH_IMAGE -> handleSearchImage()
                RevImgApi.Actions.GET_SEARCH_ENGINES -> handleGetSearchEngines()
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
        val engine = intent.getStringExtra(RevImgApi.Extras.SEARCH_ENGINE)
        val queryUrl = SUPPORTED_ENGINES[engine] ?: return toastAndFinish("不支持的搜索引擎")

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

    /**
     * 处理获取搜索引擎列表请求
     * 返回支持的搜索引擎列表
     */
    private fun handleGetSearchEngines() {
        val result = Intent()
        result.putExtra(RevImgApi.Extras.SEARCH_ENGINES, ArrayList(SUPPORTED_ENGINES.keys))
        setResult(RESULT_OK, result)
        finish()
    }
}
