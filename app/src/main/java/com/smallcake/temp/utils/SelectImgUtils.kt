package com.smallcake.temp.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.FileUtils
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectMimeType.ofImage
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import java.io.File
import java.io.FileOutputStream
import java.util.*


/**
 * Date:2021/7/14 13:38
 * Author:SmallCake
 * Desc:用于选择多张图片
 * 1.可删除选择的图片
 * 2.限制最大图片选择数量
 * 3.点击已选图片查看大图
 **/
object SelectImgUtils {
    private const val TAG = "SelectImgUtils"

    fun getPic(activity: Activity,cb:(String)->Unit) {
        PictureSelector.create(activity)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setMaxSelectNum(1)
            .setImageSpanCount(3)
            .isDisplayCamera(true)// 是否显示拍照按钮
            .isPreviewImage(false)//不能预览，避免本来想选中，
            .setCompressEngine(LuBanCompressEngine())
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    for (media in result) {
                        val filePath = if (media.isCompressed)media.compressPath else media.realPath
                        cb(filePath)
                    }
                }

                override fun onCancel() {
                    showToast("取消了图片选择")
                }
            })
    }

}



/**
 * 图片选择类
 * @property path String 选择图片后的文件路径
 * @property isAdd Boolean 是否是添加图片
 */
data class ImgSelectBean(val path: String = "", var isAdd: Boolean = false)