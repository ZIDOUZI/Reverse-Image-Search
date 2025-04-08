@file:Suppress("UNUSED")

package zdz.revimg.api

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
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
        
        /**
         * 搜索图片URL的Action
         */
        const val SEARCH_IMAGE_URL = "zdz.revimg.action.SEARCH_IMAGE_URL"
        
        /**
         * 获取搜索引擎列表的Action
         */
        const val GET_SEARCH_ENGINES = "zdz.revimg.action.GET_SEARCH_ENGINES"
    }
    
    /**
     * 额外参数
     */
    object Extras {
        /**
         * 指定搜索引擎的参数名
         */
        const val SEARCH_ENGINE = "search_engine"
        
        /**
         * 搜索引擎列表的参数名
         */
        const val SEARCH_ENGINES = "search_engines"
        
        /**
         * 图片URL的参数名
         */
        const val IMAGE_URL = "image_url"
    }
    
    /**
     * 支持的搜索引擎
     */
    object Engines {
        const val GOOGLE = "google"
        const val SAUCENAO = "saucenao"
        const val YANDEX = "yandex"
        const val IQDB = "iqdb"
        const val ASCII2D = "ascii2d"
        const val TRACEMOE = "tracemoe"
        
        /**
         * 获取所有支持的搜索引擎
         */
        val ALL = listOf(GOOGLE, SAUCENAO, YANDEX, IQDB, ASCII2D, TRACEMOE)
    }
    
    /**
     * 检查RevImg应用是否已安装
     * @param context 上下文
     * @return 是否已安装
     */
    fun isInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(PACKAGE_NAME, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取支持的搜索引擎列表
     * @param context 上下文
     * @param resultLauncher 用于接收结果的ActivityResultLauncher
     * @return 是否成功发送请求
     */
    fun getSearchEngines(context: Context, resultLauncher: ActivityResultLauncher<Intent>): Boolean {
        if (!isInstalled(context)) return false
        
        val intent = Intent(Actions.GET_SEARCH_ENGINES).apply {
            setPackage(PACKAGE_NAME)
        }
        
        return try {
            resultLauncher.launch(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 使用指定的搜索引擎搜索图片
     * @param context 上下文
     * @param bitmap 要搜索的图片
     * @param engine 搜索引擎名称，如Engines.GOOGLE, Engines.SAUCENAO等
     * @return 是否成功发送请求
     */
    fun searchImage(context: Context, bitmap: Bitmap, engine: String = Engines.GOOGLE): Boolean {
        if (!isInstalled(context)) return false
        
        // 将Bitmap保存到临时文件
        val file = File(context.cacheDir, "shared_image_${System.currentTimeMillis()}.jpg")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            // 获取文件的Uri
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // 创建Intent
            val intent = Intent(Actions.SEARCH_IMAGE).apply {
                setPackage(PACKAGE_NAME)
                putExtra(Extras.SEARCH_ENGINE, engine)
                setDataAndType(uri, "image/jpeg")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // 启动Activity
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * 使用指定的搜索引擎搜索图片
     * @param context 上下文
     * @param imageUri 图片的Uri
     * @param engine 搜索引擎名称，如Engines.GOOGLE, Engines.SAUCENAO等
     * @return 是否成功发送请求
     */
    fun searchImage(context: Context, imageUri: Uri, engine: String = Engines.GOOGLE): Boolean {
        if (!isInstalled(context)) return false
        
        return try {
            // 创建Intent
            val intent = Intent(Actions.SEARCH_IMAGE).apply {
                setPackage(PACKAGE_NAME)
                putExtra(Extras.SEARCH_ENGINE, engine)
                setDataAndType(imageUri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // 启动Activity
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 从ActivityResult中解析搜索引擎列表
     * @param data Intent数据
     * @return 搜索引擎列表，如果解析失败则返回null
     */
    fun parseSearchEnginesResult(data: Intent?): List<String>? {
        return data?.getStringArrayListExtra(Extras.SEARCH_ENGINES)
    }
    
    /**
     * 使用指定的搜索引擎搜索图片URL
     * @param context 上下文
     * @param imageUrl 要搜索的图片URL
     * @param engine 搜索引擎名称，如Engines.GOOGLE, Engines.SAUCENAO等
     * @return 是否成功发送请求
     */
    fun searchImageUrl(context: Context, imageUrl: String, engine: String = Engines.GOOGLE): Boolean {
        if (!isInstalled(context)) return false
        
        return try {
            // 创建Intent
            val intent = Intent(Actions.SEARCH_IMAGE_URL).apply {
                setPackage(PACKAGE_NAME)
                putExtra(Extras.SEARCH_ENGINE, engine)
                putExtra(Extras.IMAGE_URL, imageUrl)
            }
            
            // 启动Activity
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
