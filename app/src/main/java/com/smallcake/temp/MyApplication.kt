package com.smallcake.temp

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.baidu.mapapi.SDKInitializer
import com.lsxiao.apollo.core.Apollo
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.smallcake.smallutils.SmallUtils
import com.smallcake.temp.module.httpModule
import com.smallcake.temp.module.mapModule
import com.smallcake.temp.utils.SystemUtils
import com.smallcake.temp.utils.SystemUtils.model
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import io.reactivex.android.schedulers.AndroidSchedulers
import org.koin.core.context.startKoin


/**
 * Date: 2020/1/4
 * author: SmallCake
 */
class MyApplication : Application() {
    companion object{
       lateinit var instance:MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        //日志打印
        Logger.addLogAdapter(AndroidLogAdapter())
        //模块注入
        startKoin{
            modules(httpModule, mapModule)
        }
        //事件通知
        Apollo.init(AndroidSchedulers.mainThread(), this)
        //数据存储
//        MMKV.initialize(this)
        //小工具初始化
        SmallUtils.init(this)
        //百度地图初始化
        SDKInitializer.initialize(this)
        CrashReport.initCrashReport(this, "14af038634", false)


    }


    //方法数量过多，合并
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}