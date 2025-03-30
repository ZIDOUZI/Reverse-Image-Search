package zdz.revimg

import android.app.Activity
import android.os.Bundle
import kotlinx.coroutines.launch
import zdz.revimg.utils.handleIntent
import zdz.revimg.utils.scope

class Ascii2dActivity : Activity() {
    companion object {
        const val QUERY_URL = "https://ascii2d.net/search/url/%s"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scope.launch {
            handleIntent(QUERY_URL)
        }
    }
}