package com.smallcake.temp.http

import com.smallcake.temp.api.WeatherImpl
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * 网络数据提供者
 */
@KoinApiExtension
class DataProvider :KoinComponent {
    val weather: WeatherImpl = get()
}