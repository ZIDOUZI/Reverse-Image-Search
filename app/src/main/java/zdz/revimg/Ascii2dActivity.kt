package zdz.revimg

import android.app.Activity
import android.os.Bundle
import zdz.revimg.utils.handleCreate

class Ascii2dActivity : Activity() {
    companion object {
        const val QUERY_URL = "https://ascii2d.net/search/url/%s"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleCreate(QUERY_URL)
    }
}