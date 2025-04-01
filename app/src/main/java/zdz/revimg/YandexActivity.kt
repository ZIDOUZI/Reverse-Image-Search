package zdz.revimg

import android.app.Activity
import android.os.Bundle
import zdz.revimg.utils.handleCreate

class YandexActivity : Activity() {
    companion object {
        const val QUERY_URL = "https://yandex.com/images/search?url=%s&rpt=imageview"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleCreate(QUERY_URL)
    }
}