package com.qhping.kotlin.app

import android.app.Application
import android.util.Log

class MainApplication : Application(), Thread.UncaughtExceptionHandler {
    val TAG = "崩溃异常！！！！"
    override fun uncaughtException(p0: Thread, p1: Throwable) {
        Log.e(TAG, "uncaughtException: $p1")
    }

}