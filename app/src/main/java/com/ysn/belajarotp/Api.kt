package com.ysn.belajarotp

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {

    @POST("otp/send")
    fun sendOtp(@Query("phoneNumber", encoded = true) phoneNumber: String): Observable<ResponseBody>

    @POST("otp/update")
    fun updateOtp(@Query("codeOtp") codeOtp: String): Observable<ResponseBody>

}