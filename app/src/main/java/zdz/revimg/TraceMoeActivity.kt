package zdz.revimg

import android.app.Activity
import android.os.Bundle
import kotlinx.coroutines.launch
import zdz.revimg.utils.handleIntent
import zdz.revimg.utils.scope

class TraceMoeActivity : Activity() {
    companion object {
        const val QUERY_URL = "https://trace.moe/?auto&url=%s"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scope.launch {
            handleIntent(QUERY_URL)
        }
    }
}