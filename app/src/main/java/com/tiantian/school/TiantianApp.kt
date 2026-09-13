package com.tiantian.school

import android.app.Application
import com.tiantian.school.data.local.AppPrefs

class TiantianApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 任何页面访问 AppPrefs 之前先完成初始化
        AppPrefs.init(this)
    }
}
