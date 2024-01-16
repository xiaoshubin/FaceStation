package com.smallcake.temp.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.telephony.*
import android.telephony.cdma.CdmaCellLocation
import android.telephony.gsm.GsmCellLocation
import android.util.Log
import android.view.View
import com.baidu.mapapi.model.LatLng
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.smallcake.temp.MyApplication
import com.smallcake.temp.R
import com.smallcake.temp.base.BaseBindFragment
import com.smallcake.temp.base.Constant
import com.smallcake.temp.bean.BaseDataBean
import com.smallcake.temp.bean.FaceRequest
import com.smallcake.temp.bean.FaceRequestItem
import com.smallcake.temp.bean.FaceResponse
import com.smallcake.temp.databinding.FragmentHomeBinding
import com.smallcake.temp.utils.*
import com.yx.jiading.utils.sizeNull
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class HomeFragment: BaseBindFragment<FragmentHomeBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //基础照片存在就加载照片
        val externalCacheDir = MyApplication.instance.externalCacheDir
        val file = File(externalCacheDir, "base_pic.jpg")
        if (file.exists()){
            Glide.with(this).load(file).into(bind.ivBasePic)
        }
        bind.btnUpBaseFace.setOnClickListener {
            SelectImgUtils.getPic(this@HomeFragment.activity as Activity){
                val file = File(it)
                L.e("file:$file")
                savePic(file)
            }
        }
        bind.btnOtherPic.setOnClickListener {
            if (file.exists()) {
                SelectImgUtils.getPic(this@HomeFragment.activity as Activity) {
                    val file = File(it)
                    Glide.with(this).load(file).into(bind.ivOtherPic)

                    val fileBase = File(externalCacheDir, "base_pic.jpg")
                    val base64img1  = fileToBase64(fileBase)
                    val base64img2  = fileToBase64(file)
                    val face1 = FaceRequestItem(base64img1,"BASE64","LIVE","LOW","HIGH")
                    val face2 = FaceRequestItem(base64img2,"BASE64","LIVE","LOW","HIGH")
                    val request = FaceRequest()
                    request.add(face1)
                    request.add(face2)
                   faceMatch(request)

                }
            }else{
                showToast("请先设置基础照片")
            }
        }
        //获取基站信息
        bind.btnGetBaseStation.setOnClickListener {

            XXPermissions.with(this)
                .permission(arrayListOf(Permission.ACCESS_FINE_LOCATION))
                .request(object : OnPermissionCallback {
                    override fun onGranted(p0: MutableList<String>, all: Boolean) {
                        if (!all)return
                        getBaseData(this@HomeFragment.context!!){list->
                            val buffer = StringBuffer()
                            list.forEachIndexed { index, baseDataBean ->
                                buffer.append("基站$index:${baseDataBean.toString()}\n")
                            }
                            bind.tvBaseStationInfo.text = ""
                            val bean = list.first()
                            val mcc = bean.mcc
                            val mnc= bean.mnc
                            val lac= bean.lac
                            val ci =  bean.cellId
                            val output="json"

                        }
                    }

                })

        }
    }

    fun getBaseData(mContext: Context,cb:(list:ArrayList<BaseDataBean>)->Unit) {
        // lac连接基站位置区域码 cellid连接基站编码 mcc MCC国家码 mnc MNC网号
        // signalstrength连接基站信号强度
        val list: ArrayList<BaseDataBean> = ArrayList<BaseDataBean>()
        val beans = BaseDataBean()
        val telephonyManager = mContext
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val operator = telephonyManager.networkOperator
        beans.mcc = operator.substring(0, 3)
        beans.mnc = operator.substring(3)
        if (telephonyManager.phoneType == TelephonyManager.PHONE_TYPE_CDMA) { // 这是电信的
            val cdmaCellLocation = telephonyManager.cellLocation as CdmaCellLocation
            beans.cellId = cdmaCellLocation.baseStationId.toString() + ""
            beans.lac = cdmaCellLocation.networkId.toString() + ""
        } else { // 这是移动和联通的
            val gsmCellLocation = telephonyManager.cellLocation as GsmCellLocation?
            beans.cellId = gsmCellLocation?.cid.toString() + ""
            beans.lac = gsmCellLocation?.lac.toString() + ""
        }
        beans.signalStrength = "0"
        list.add(beans)
        //获取邻区基站信息
        val infoLists = telephonyManager.allCellInfo
        if (infoLists.size != 0) {
            for (info in infoLists) {
                /** 1、GSM是通用的移动联通电信2G的基站。
                 * 2、CDMA是3G的基站。
                 * 3、LTE，则证明支持4G的基站。 */
                val bean = BaseDataBean()
                if (info.toString().contains("CellInfoLte")) {//4g网络的基站数据
                    val cellInfoLte = info as CellInfoLte
                    val cellIdentityLte = cellInfoLte
                        .cellIdentity
                    val cellSignalStrengthLte = cellInfoLte
                        .cellSignalStrength
                    bean.signalStrength = cellSignalStrengthLte.dbm.toString() + ""
                    bean.cellId = cellIdentityLte.ci.toString() + ""
                    bean.lac = cellIdentityLte.tac.toString() + ""
                    bean.mcc = cellIdentityLte.mcc.toString() + ""
                    bean.mnc = cellIdentityLte.mnc.toString() + ""
                } else if (info.toString().contains("CellInfoGsm")) {// 通用的移动联通电信2G的基站数据
                    val cellInfoGsm = info as CellInfoGsm
                    val cellIdentityGsm = cellInfoGsm
                        .cellIdentity
                    val cellSignalStrengthGsm = cellInfoGsm
                        .cellSignalStrength
                    bean.signalStrength = cellSignalStrengthGsm.dbm.toString() + ""
                    bean.cellId = cellIdentityGsm.cid.toString() + ""
                    bean.lac = cellIdentityGsm.lac.toString() + ""
                    bean.mcc = cellIdentityGsm.mcc.toString() + ""
                    bean.mnc = cellIdentityGsm.mnc.toString() + ""
                } else if (info.toString().contains("CellInfoCdma")) {//电信3G的基站数据
                    val cellInfoCdma = info as CellInfoCdma
                    val cellIdentityCdma = cellInfoCdma
                        .cellIdentity
                    val cellSignalStrengthCdma = cellInfoCdma
                        .cellSignalStrength
                    bean.cellId = cellIdentityCdma.basestationId.toString() + ""
                    bean.signalStrength = cellSignalStrengthCdma.cdmaDbm
                        .toString() + ""
                    /**因为待会我要把这个list转成gson，所以这个对象的所有属性我都赋一下值，不必理会这里 */
                    bean.lac = "0"
                    bean.mcc = "0"
                    bean.mnc = "0"
                }
                list.add(bean)
            }

            list.forEach{
                Log.e("基站位置",it.toString())
            }
            cb(if (list.sizeNull()>3)list.take(3) as ArrayList<BaseDataBean> else list)
        }
    }

    fun faceMatch(map: FaceRequest) {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/face/v3/match"

        try {
            val param: String = GsonUtils.toJson(map)
            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            Thread{
                val  result = HttpUtil.post(url, Constant.access_token, "application/json", param)
                println(result)
                val faceResponse = GsonUtils.fromJson<FaceResponse>(result,FaceResponse::class.java)
                val msg = mHandler.obtainMessage()
                msg.what=1
                msg.obj = faceResponse
                mHandler.sendMessage(msg)
            }.start()


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private val mHandler = Handler {
        when(it.what){
            1->{
                val faceResponse:FaceResponse = it.obj as FaceResponse
                bind.tvFaceResult.text= "相似度:${faceResponse.result.score.toInt()}%"
            }
        }
        false
    }

    fun savePic(fileGet: File) {
        val externalCacheDir = MyApplication.instance.externalCacheDir
        val fileTarget = File(externalCacheDir, "base_pic.jpg")
        fileGet.renameTo(fileTarget)
        Thread{ this@HomeFragment.context?.let { Glide.get(it).clearDiskCache() }}.start()
        Handler().postDelayed({
            val requestOptions =  RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
            Glide.with(this).load(fileTarget).apply(requestOptions).into(bind.ivBasePic)
        },300)

    }

    private fun fileToBase64(file: File):String{
        val byteArray = readBytesFromFile(file)
        return encodeToBase64(byteArray)
    }

    private fun readBytesFromFile(file: File): ByteArray {
        val inputStream: InputStream = FileInputStream(file)
        val outputStream = ByteArrayOutputStream()
        val bufferSize = 1024 * 8
        val buffer = ByteArray(bufferSize)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        return outputStream.toByteArray()
    }

    private fun encodeToBase64(data: ByteArray): String {
        return android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT)
    }
}