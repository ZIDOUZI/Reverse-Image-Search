package zdz.revimg.api

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileOutputStream

/**
 * RevImgApi的单元测试
 */
@RunWith(MockitoJUnitRunner::class)
class RevImgApiTest {

    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockPackageManager: PackageManager
    
    @Mock
    private lateinit var mockResultLauncher: ActivityResultLauncher<Intent>
    
    @Mock
    private lateinit var mockBitmap: Bitmap
    
    @Mock
    private lateinit var mockUri: Uri
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.packageManager).thenReturn(mockPackageManager)
    }
    
    @Test
    fun isInstalled_whenPackageExists_returnsTrue() {
        // 模拟包存在的情况
        `when`(mockPackageManager.getPackageInfo(eq(RevImgApi.PACKAGE_NAME), anyInt())).thenReturn(mock())
        
        // 调用测试方法
        val result = RevImgApi.isInstalled(mockContext)
        
        // 验证结果
        assertTrue(result)
    }
    
    @Test
    fun isInstalled_whenPackageDoesNotExist_returnsFalse() {
        // 模拟包不存在的情况
        `when`(mockPackageManager.getPackageInfo(eq(RevImgApi.PACKAGE_NAME), anyInt()))
            .thenThrow(PackageManager.NameNotFoundException())
        
        // 调用测试方法
        val result = RevImgApi.isInstalled(mockContext)
        
        // 验证结果
        assertFalse(result)
    }
    
//    @Test
    fun getSearchEngines_whenAppInstalled_launchesIntent() {
        // 模拟包存在的情况
        `when`(mockPackageManager.getPackageInfo(eq(RevImgApi.PACKAGE_NAME), anyInt())).thenReturn(mock())
        
        // 调用测试方法
        // NotMocked. Do not test.
        val result = RevImgApi.getSearchEngines(mockContext, mockResultLauncher)
        
        // 验证结果
        assertTrue(result)
        
        // 验证启动了正确的Intent
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        verify(mockResultLauncher).launch(intentCaptor.capture())
        
        val capturedIntent = intentCaptor.value
        assertEquals(RevImgApi.Actions.GET_SEARCH_ENGINES, capturedIntent.action)
        assertEquals(RevImgApi.PACKAGE_NAME, capturedIntent.`package`)
    }
    
    @Test
    fun getSearchEngines_whenAppNotInstalled_returnsFalse() {
        // 模拟包不存在的情况
        `when`(mockPackageManager.getPackageInfo(eq(RevImgApi.PACKAGE_NAME), anyInt()))
            .thenThrow(PackageManager.NameNotFoundException())
        
        // 调用测试方法
        val result = RevImgApi.getSearchEngines(mockContext, mockResultLauncher)
        
        // 验证结果
        assertFalse(result)
    }
    
    @Test
    fun parseSearchEnginesResult_withValidData_returnsEnginesList() {
        // 准备测试数据
        val expectedEngines = arrayListOf("google", "saucenao", "yandex")
        val mockIntent = mock(Intent::class.java)
        `when`(mockIntent.getStringArrayListExtra(RevImgApi.Extras.SEARCH_ENGINES)).thenReturn(expectedEngines)
        
        // 调用测试方法
        val result = RevImgApi.parseSearchEnginesResult(mockIntent)
        
        // 验证结果
        assertEquals(expectedEngines, result)
    }
    
    @Test
    fun parseSearchEnginesResult_withNullData_returnsNull() {
        // 调用测试方法
        val result = RevImgApi.parseSearchEnginesResult(null)
        
        // 验证结果
        assertEquals(null, result)
    }
}
