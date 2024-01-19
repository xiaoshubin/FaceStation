package com.smallcake.temp.bean

data class BaseDataBean (
    var cellId : String?=null ,// 基站编号
    var lac : String?=null ,// 位置区域码
    var mcc : String?=null, // 移动国家代码（中国的为460）
    var mnc : String?=null,// 移动网络号码（移动为0，联通为1，电信为2）
    var dbm : String?=null,//手机主卡信号强度单位
    var signalStrength : String?=null,//信号强度
    var tag : String?=null,//来源
){
    override fun toString(): String {
        //411,2490431,460,00,-82
//        return "基站编号:$cellId 位置区域码:$lac 移动国家代码:$mcc 移动网络号码:$mnc 信号强度:$signalStrength"
        /**
         * 5G基站信息
         * 基站信息1[基站编号:411,位置区域码:2490431,移动国家代码:460,移动网络号码:00,信号强度:-82]"
         * 基站信息2[基站编号:490,位置区域码:2147483647,移动国家代码:460,移动网络号码:00,信号强度:-104]"
         * 基站信息3[基站编号:491,位置区域码:2147483647,移动国家代码:460,移动网络号码:00,信号强度:-97]"
         * 基站信息4[基站编号:489,位置区域码:2147483647,移动国家代码:460,移动网络号码:00,信号强度:-99]"
         */

        return "${tag}基站信息[$cellId,$lac,$mcc,$mnc,$signalStrength]"
    }
}