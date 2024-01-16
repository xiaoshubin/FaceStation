package com.smallcake.temp.utils

import android.content.Context
import android.net.ConnectivityManager


object NetUtils {
    /**
     * 判断是否有网络连接
     * @param context
     * @return
     */
    fun isNetworkConnected(context: Context?): Boolean { //true是链接，false是没链接
        if (context != null) {
            val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mNetworkInfo = mConnectivityManager.activeNetworkInfo
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable
            }
        }
        return false
    }
}