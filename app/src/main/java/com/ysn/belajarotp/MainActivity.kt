package com.ysn.belajarotp

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), SmsListener {

    private lateinit var api: Api
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkRuntimePermission()
        initRetrofit()
        bindSmsReceiver()
        button_send_activity_main.setOnClickListener { _ ->
            var phoneNumber = edit_text_phone_number.text.toString()
            if (phoneNumber.isBlank() || phoneNumber.isEmpty()) {
                Toast.makeText(this, "Phone number is required", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            phoneNumber = "+62${phoneNumber.substring(1)}"
            showProgressDialog()
            api.sendOtp(phoneNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        hideProgressDialog()
                        Toast.makeText(this, "SMS has been sent", Toast.LENGTH_LONG)
                            .show()
                    },
                    {
                        it.printStackTrace()
                        hideProgressDialog()
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG)
                            .show()
                    }
                )
        }
    }

    private fun checkRuntimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS), 100)
        }
    }

    private fun bindSmsReceiver() {
        SmsReceiver.bindListener(this)
    }

    private fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this@MainActivity)
            progressDialog?.let {
                it.setCancelable(false)
                it.setMessage("Please wait")
            }
        }
        progressDialog!!.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
    }

    private fun initRetrofit() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://server-otp.herokuapp.com/api/")
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(Api::class.java)
    }

    @SuppressLint("CheckResult")
    override fun messageReceived(message: String) {
        Log.d(javaClass.simpleName, "message: $message")
        edit_text_1.setText(message[0].toString())
        edit_text_2.setText(message[1].toString())
        edit_text_3.setText(message[2].toString())
        edit_text_4.setText(message[3].toString())
        showProgressDialog()
        api.updateOtp(message)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    hideProgressDialog()
                    val jsonObjectResponse = JSONObject(it.string())
                    if (jsonObjectResponse.getBoolean("success")) {
                        Toast.makeText(this, "OTP valid", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        Toast.makeText(this, "OTP invalid", Toast.LENGTH_LONG)
                            .show()
                    }
                },
                {
                    it.printStackTrace()
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG)
                        .show()
                }
            )

    }
}
