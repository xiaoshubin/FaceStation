package com.smallcake.temp.bean

/**
 * errcode
 * 0: 成功
10000: 参数错误
10001: 无查询结果
,纬度,经度,精度半径,地址
 */
data class BaseStationResponse(
    val address: String,//地址
    val errcode: String,//错误码
    val lat: Double,
    val lon: Double,
    val radius: String//基站覆盖范围
){
    override fun toString(): String {
        return "经纬度:[${lat},${lon}] \n地址:$address"
    }
}