package com.smallcake.temp.bean

data class BaseDataBean (
    var cellId : String?=null ,// 基站编号
    var lac : String?=null ,// 位置区域码
    var mcc : String?=null, // 移动国家代码（中国的为460）
    var mnc : String?=null,// 基站编号（移动为0，联通为1，电信为2）
    var dbm : String?=null,//手机主卡信号强度单位
    var signalStrength : String?=null,//信号强度
)