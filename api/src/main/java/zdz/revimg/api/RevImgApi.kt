@file:Suppress("UNUSED")

package zdz.revimg.api

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import zdz.revimg.utils.sendImage
import java.io.File
import java.io.FileOutputStream

/**
 * RevImg API类，提供与RevImg应用交互的方法
 * 支持以下功能：
 * 1. 检查RevImg应用是否已安装
 * 2. 获取支持的搜索引擎列表
 * 3. 使用指定的搜索引擎搜索图片
 */
object RevImgApi {
    /**
     * RevImg应用的包名
     */
    const val PACKAGE_NAME = "zdz.revimg"

    /**
     * 自定义Action
     */
    object Actions {
        /**
         * 搜索图片的Action
         */
        const val SEARCH_IMAGE = "zdz.revimg.action.SEARCH_IMAGE"
    }

    /**
     * 额外参数
     */
    object Extras {
        /**
         * 指定搜索引擎的参数名
         */
        const val SEARCH_ENGINE = "search_engine"
    }


    enum class Engine(
        val label: String,
        val iconResId: Int,
        val description: String,
        val source: String
    ) {
        GoogleLens("GoogleLens", R.drawable.googlelens, "Google的图像识别服务，适合识别现实世界的物体和场",
            "https://www.google.com/searchbyimage?&image_url=%s&client=firefox-bd"),
        SauceNAO("SauceNAO", R.drawable.saucenao, "适合搜索动漫、插画和漫画，支持多个图库",
            "https://saucenao.com/search.php?url=%s"),
        Yandex("Yandex", R.drawable.yandex, "俄罗斯搜索引擎，图像搜索功能强大",
            "https://yandex.com/images/search?url=%s&rpt=imageview"),
        IQDB("IQDB", R.drawable.iqdb, "专注于动漫图像搜索，适合查找原始动漫图片",
            "https://iqdb.org/?url=%s"),
        Ascii2d("Ascii2d", R.drawable.ascii2d, "日本的动漫图像搜索引擎，适合查找插画和漫画",
            "https://ascii2d.net/search/url/%s"),
        TraceMoe("TraceMoe", R.drawable.tracemoe, "专注于动画截图搜索，可以找到动画片段",
            "https://trace.moe/?auto&url=%s");
    }

    /**
     * 检查RevImg应用是否已安装
     * @param context 上下文
     * @return 是否已安装
     */
    fun isInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(PACKAGE_NAME, 0)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * 获取支持的搜索引擎列表
     * @param context 上下文
     * @param resultLauncher 用于接收结果的ActivityResultLauncher
     * @return 是否成功发送请求
     */
    fun getSearchEngines() = Engine.entries

    /**
     * 使用指定的搜索引擎搜索图片
     * @param context 上下文
     * @param bitmap 要搜索的图片
     * @param engine 搜索引擎名称，参见[Engine]
     * @return 是否成功发送请求
     */
    fun searchImage(context: Context, bitmap: Bitmap, engine: Engine = Engine.GoogleLens): Boolean =
        if (!isInstalled(context)) false else {
            val file = File(context.cacheDir, "revimg/${System.currentTimeMillis()}.jpg")
            val uri = try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                // 获取文件的Uri
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileProvider",
                    file
                )
            } catch (e: Exception) {
                return false
            }
            searchImage(context, uri, engine)
        }

    /**
     * 使用指定的搜索引擎搜索图片
     * @param context 上下文
     * @param imageUri 图片的Uri
     * @param engine 搜索引擎名称，参见[Engine]
     * @return 是否成功发送请求
     */
    fun searchImage(context: Context, imageUri: Uri, engine: Engine = Engine.GoogleLens): Boolean =
        if (!isInstalled(context)) false else try {
            sendImage(imageUri, "image/*").apply { action = Actions.SEARCH_IMAGE }.let {
                context.startActivity(it)
            }
            true
        } catch (e: Exception) {
            false
        }

    /**
     * 使用指定的搜索引擎搜索图片
     * @param context 上下文
     * @param imageUrl 图片的Url
     * @param engine 搜索引擎名称，参见[Engine]
     * @return 是否成功发送请求
     */
    fun searchImage(context: Context, imageUrl: String, engine: Engine = Engine.GoogleLens) =
        if (!isInstalled(context)) false else try {
            sendImage(imageUrl.toUri(), "text/plain").apply { action = Actions.SEARCH_IMAGE }.let {
                context.startActivity(it)
            }
            true
        } catch (e: Exception) {
            false
        }
}
