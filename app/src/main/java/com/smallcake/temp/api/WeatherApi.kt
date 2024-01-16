package com.smallcake.temp.api

import com.smallcake.temp.base.Constant
import com.smallcake.temp.bean.BaseStationResponse
import com.smallcake.temp.bean.FaceRequest
import com.smallcake.temp.bean.FaceResponse
import com.smallcake.temp.http.im
import io.reactivex.Observable
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.http.*
import java.net.URL


interface WeatherApi {
    @GET("cell/")
    fun query(@Query("mcc")mcc:String?, @Query("mnc")mnc:String?,@Query("lac")lac:String?,@Query("ci")ci:String?,@Query("coord")coord:String,@Query("output")output:String,): Observable<BaseStationResponse>
}

@KoinApiExtension
class WeatherImpl:WeatherApi, KoinComponent {
    private val api: WeatherApi by inject()
    override fun query(
        mcc: String?,
        mnc: String?,
        lac: String?,
        ci: String?,
        coord: String,
        output: String,
    ): Observable<BaseStationResponse> {
       return  api.query(mcc,mnc,lac,ci,coord,output).im()
    }


}