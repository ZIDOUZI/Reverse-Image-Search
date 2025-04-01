package zdz.revimg

import android.app.Activity
import android.os.Bundle
import zdz.revimg.utils.handleCreate

class GoogleLensActivity : Activity() {
    companion object {
        const val QUERY_URL = "https://www.google.com/searchbyimage?&image_url=%s"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleCreate(QUERY_URL)
    }
}