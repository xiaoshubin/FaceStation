package com.smallcake.temp

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.telephony.*
import android.telephony.cdma.CdmaCellLocation
import android.telephony.gsm.GsmCellLocation
import android.util.Log
import com.baidu.location.BDLocation
import com.baidu.mapapi.NetworkUtil
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.utils.DistanceUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.smallcake.smallutils.text.NavigationBar
import com.smallcake.temp.base.BaseBindActivity
import com.smallcake.temp.base.Constant
import com.smallcake.temp.base.addFragment
import com.smallcake.temp.bean.BaseDataBean
import com.smallcake.temp.bean.FaceRequest
import com.smallcake.temp.bean.FaceRequestItem
import com.smallcake.temp.bean.FaceResponse
import com.smallcake.temp.databinding.ActivityMainBinding
import com.smallcake.temp.fragment.HomeFragment
import com.smallcake.temp.fragment.ListFragment
import com.smallcake.temp.fragment.MineFragment
import com.smallcake.temp.utils.*
import com.tencent.bugly.crashreport.CrashReport
import com.yx.jiading.utils.sizeNull
import org.koin.core.component.bind
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.NullPointerException
import java.util.*

/**
 *如何用registerForActivityResult替代onActivityResult
 * 发起并接收
private val register = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
if (activityResult.resultCode == Activity.RESULT_OK) {
val address = activityResult.data?.getStringExtra("address")
bind.etAddress.setText(address)
}
}
val intent = Intent()
intent.putExtra("address",address)
setResult(RESULT_OK, intent)
finish()

bind.recyclerView.apply {
addItemDecoration(GridItemDecoration())
layoutManager = LinearLayoutManager(context)
adapter  = mAdapter
}


mAdapter.apply {
setEmptyId()
loadMoreModule.setOnLoadMoreListener {
page++
loadData()
}
setOnItemClickListener{adapter,_,postion->
val item = adapter.getItem(postion) as AnnouncementList
goActivity(NoticeInfoActivity::class.java,item.announcementId)
}
}
数据加载更多模板
val list = it.data
mAdapter.apply {
if (list.sizeNull() > 0) {
if (page == 1) setList(list) else addData(list!!)
loadMoreModule.loadMoreComplete()
} else {
if (page == 1) setList(list)
loadMoreModule.loadMoreEnd()
}
}

 车辆固定位置:
[29.542206,106.568574]
 */
class MainActivity : BaseBindActivity<ActivityMainBinding>() {

    var scanLatLng = LatLng(29.542206,106.568574)
    override fun onCreate(savedInstanceState: Bundle?, bar: NavigationBar) {
        bind.bmapView.map.isMyLocationEnabled = true
        bar.hide()
        val model = SystemUtils.systemVersion+SystemUtils.model
        CrashReport.setDeviceModel(this,model)
        //基础照片存在就加载照片
        val externalCacheDir = MyApplication.instance.externalCacheDir
        val file = File(externalCacheDir, "base_pic.jpg")
        if (file.exists()){
            Glide.with(this).load(file).into(bind.ivBasePic)
        }
        bind.btnUpBaseFace.setOnClickListener {
            SelectImgUtils.getPic(this@MainActivity as Activity){
                val file = File(it)
                L.e("file:$file")
                savePic(file)
            }
        }
        bind.btnOtherPic.setOnClickListener {
            if (file.exists()) {
                SelectImgUtils.getPic(this@MainActivity as Activity) {
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
            //检查网络
            if (!NetUtils.isNetworkConnected(this)){
                showToast("请先开启网络")
                return@setOnClickListener
            }
            XXPermissions.with(this)
                .permission(arrayListOf(Permission.ACCESS_FINE_LOCATION,Permission.ACCESS_COARSE_LOCATION))
                .request(object : OnPermissionCallback {
                    override fun onGranted(p0: MutableList<String>, all: Boolean) {
                        if (!all)return
                        getBaseData(this@MainActivity!!){list->
                            bind.tvBaseStationInfo.text = ""
                            val buffer = StringBuffer()
                            list.forEachIndexed { index, baseDataBean ->
                                buffer.append("$index:${baseDataBean}\n")
                            }
                            bind.tvBaseStationInfo.text = buffer.toString()
                            val newList = list.filter { it.cellId!="0"&&it.mcc=="460" } as ArrayList<BaseDataBean>
                            if (newList.isEmpty()){
                                showToast("基站信息不正确")
                                return@getBaseData
                            }

                            val bean = newList.first()
                            val mcc = bean.mcc
                            val mnc= bean.mnc
                            val lac= bean.lac
                            val ci =  bean.cellId
                            val coord="bd09"
                            val output="json"
                            dataProvider.weather.query(mcc,mnc,lac,ci,coord,output).subscribe {
                                if (it.lat==0.0&&it.lon==0.0)return@subscribe
                                bind.tvBaseStationInfo.text = buffer.toString()+"\n↓\n"+it.toString()
                                val latlng = LatLng(it.lat,it.lon)
                                val distance =  DistanceUtil.getDistance(scanLatLng, latlng)
                                bind.tvDistance.text = "基站定位<-${distance.toInt()}米->车辆位置"
                                BmapHelper.drawLines(bind.bmapView.map, listOf(scanLatLng, latlng))
                                bind.bmapView.addMarker(latlng, R.mipmap.ic_base_station)
                                bind.bmapView.toCenter(latlng)
                            }
                        }
                    }

                })

        }

//            BmapHelper.onceLocation(this){location->
//                L.e("经纬度:[${location.latitude},${location.longitude}]${location.address.address}")
//                BmapHelper.toCenterMyLocation(bind.bmapView,location)
//                bind.bmapView.toCenter(location.toLatLng())
//            }
        bind.bmapView.addMarker(scanLatLng)
        bind.bmapView.toCenter(scanLatLng)


    }

    override fun onDestroy() {
        bind.bmapView.map.isMyLocationEnabled = false
        super.onDestroy()
    }

    fun getBaseData(mContext: Context, cb:(list:ArrayList<BaseDataBean>)->Unit) {
        // lac连接基站位置区域码 cellid连接基站编码 mcc MCC国家码 mnc MNC网号
        // signalstrength连接基站信号强度
        val list: ArrayList<BaseDataBean> = ArrayList<BaseDataBean>()
//        val beans = BaseDataBean()
        val telephonyManager = mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        val operator = telephonyManager.networkOperator
//        beans.mcc = operator.substring(0, 3)
//        beans.mnc = operator.substring(3)
//        if (telephonyManager.phoneType == TelephonyManager.PHONE_TYPE_CDMA) { // 这是电信的
//            val cdmaCellLocation = telephonyManager.cellLocation as CdmaCellLocation
//            beans.cellId = cdmaCellLocation.baseStationId.toString() + ""
//            beans.lac = cdmaCellLocation.networkId.toString() + ""
//        } else { // 这是移动和联通的
//            val gsmCellLocation = telephonyManager.cellLocation as GsmCellLocation?
//            beans.cellId = gsmCellLocation?.cid.toString() + ""
//            beans.lac = gsmCellLocation?.lac.toString() + ""
//        }
//        beans.signalStrength = "0"
//        list.add(beans)
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
                    val cellIdentityLte = cellInfoLte.cellIdentity
                    val cellSignalStrengthLte = cellInfoLte.cellSignalStrength
                    bean.signalStrength = cellSignalStrengthLte.rssi.toString() + ""
                    bean.cellId = cellIdentityLte.ci.toString() + ""
                    bean.lac = cellIdentityLte.tac.toString() + ""
                    bean.mcc = cellIdentityLte.mcc.toString() + ""
                    bean.mnc = cellIdentityLte.mnc.toString() + ""
                    bean.dbm = cellSignalStrengthLte.dbm.toString() + ""
                    bean.tag = "Lte"
                } else if (info.toString().contains("CellInfoGsm")) {// 通用的移动联通电信2G的基站数据
                    val cellInfoGsm = info as CellInfoGsm
                    val cellIdentityGsm = cellInfoGsm.cellIdentity
                    val cellSignalStrengthGsm = cellInfoGsm.cellSignalStrength
                    bean.signalStrength = cellSignalStrengthGsm.dbm.toString() + ""
                    bean.cellId = cellIdentityGsm.cid.toString() + ""
                    bean.lac = cellIdentityGsm.lac.toString() + ""
                    bean.mcc = cellIdentityGsm.mcc.toString() + ""
                    bean.mnc = cellIdentityGsm.mnc.toString() + ""
                    bean.dbm = cellSignalStrengthGsm.dbm.toString() + ""
                    bean.tag = "Gsm"

                } else if (info.toString().contains("CellInfoCdma")) {//电信3G的基站数据
                    val cellInfoCdma = info as CellInfoCdma
                    val cellIdentityCdma = cellInfoCdma.cellIdentity
                    val cellSignalStrengthCdma = cellInfoCdma.cellSignalStrength
                    bean.cellId = cellIdentityCdma.basestationId.toString() + ""
                    bean.signalStrength = cellSignalStrengthCdma.cdmaDbm.toString() + ""
                    /**因为待会我要把这个list转成gson，所以这个对象的所有属性我都赋一下值，不必理会这里 */
                    bean.lac =  "0"
                    bean.mcc =  "0"
                    bean.mnc =  "0"
                    bean.dbm = cellSignalStrengthCdma.dbm.toString()
                    bean.tag = "Cdma"
                    //可以得到经纬度
                } else if (info.toString().contains("CellInfoNr")){//5G网络
                    val cellInfoNr = info as CellInfoNr
                    val cellIdentityNr:CellIdentityNr = cellInfoNr.cellIdentity as CellIdentityNr
                    val cellSignalStrengthNr = cellInfoNr.cellSignalStrength
                    bean.signalStrength = cellSignalStrengthNr.dbm.toString() + ""
                    bean.cellId = cellIdentityNr.pci.toString()
                    bean.lac = cellIdentityNr.tac.toString()
                    bean.mcc = cellIdentityNr.mccString
                    bean.mnc = cellIdentityNr.mncString
                    bean.dbm = cellSignalStrengthNr.dbm.toString() + ""
                    bean.tag = "5G"
                }else{//其他基站信息
                    val cellInfo = info
                    bean.signalStrength = cellInfo.cellSignalStrength.dbm.toString()
                    val cellIdentity = cellInfo.cellIdentity
                    bean.cellId = "-"
                    bean.lac = "-"
                    bean.mcc = "-"
                    bean.mnc = "-"
                    bean.dbm = cellInfo.cellSignalStrength.dbm.toString()
                    bean.tag = "其他"
                }
                list.add(bean)
            }

            list.forEach{
                Log.e("基站位置",it.toString())
            }
            cb(list.distinctBy{it.cellId to it.signalStrength} as ArrayList<BaseDataBean>)
//            cb(list.filter { it.cellId!="0"&&it.mcc=="460" } as ArrayList<BaseDataBean>)
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
        Thread{ this@MainActivity?.let { Glide.get(it).clearDiskCache() }}.start()
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


