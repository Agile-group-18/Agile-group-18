package org.grupp18.sortsmart

import android.app.Application
import org.grupp18.sortsmart.data.api.AuthRetrofitClient

class SortSmartApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AuthRetrofitClient.init(this)
    }
}