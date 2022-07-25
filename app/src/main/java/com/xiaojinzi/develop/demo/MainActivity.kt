package com.xiaojinzi.develop.demo

import android.app.ActivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.xiaojinzi.develop.DevelopHelper
import com.xiaojinzi.develop.demo.R
import com.xiaojinzi.serverlog.bean.MessageBean
import com.xiaojinzi.serverlog.bean.NetWorkLogInfoBean
import com.xiaojinzi.serverlog.bean.UserBehaviorBean
import com.xiaojinzi.serverlog.impl.ServerLog
import java.lang.NullPointerException

class MainActivity : AppCompatActivity() {

    val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.v(tag, "onCreate")
    }

    fun tipDevelopState(view: View) {
        Log.d(tag, "tipDevelopState")
        Toast.makeText(this, "developState = " + DevelopHelper.isDevelop(), Toast.LENGTH_SHORT)
            .show()
    }

    fun userBehaviorTest(view: View) {
        Log.i(tag, "userBehaviorTest")
        var userBehaviorBean =
            UserBehaviorBean("xiaojinzi", mapOf(Pair("name", "xiaojinzi"), Pair("pass", "123")))

        val messageBean = MessageBean<UserBehaviorBean>()
        messageBean.type = MessageBean.TYPE_USER_BEHAVIOR
        messageBean.data = userBehaviorBean
        ServerLog.send(messageBean)
    }

    fun networkTest(view: View) {

        Log.w(tag, "networkTest")

        var netWorkLogInfoBean = NetWorkLogInfoBean()
        netWorkLogInfoBean.req_url = "www.xiaojinzi.com"
        netWorkLogInfoBean.req_method = "POST"
        netWorkLogInfoBean.req_body = "{'name':'xiaojinzi','pass':'123'}"
        netWorkLogInfoBean.req_headers = listOf("req_header1: value1", "req_header2: value2")

        netWorkLogInfoBean.res_message = "ok"
        netWorkLogInfoBean.res_code = 200
        netWorkLogInfoBean.res_body = "{'res1':'value1','res2':'value2'}"
        netWorkLogInfoBean.res_headers = listOf("res_header1: value1", "res_header2: value2")

        val messageBean = MessageBean<NetWorkLogInfoBean>()
        messageBean.type = MessageBean.TYPE_NETWORK
        messageBean.data = netWorkLogInfoBean
        ServerLog.send(messageBean)

    }

    fun createCrash(view: View) {
        Log.e(tag, "createCrash")
        throw NullPointerException()
    }

    fun developOpen(view: View) {
        Log.e(tag, "developOpen")
        DevelopHelper.tryOpen()
    }

    override fun finish() {
        super.finish()
        (getSystemService(ACTIVITY_SERVICE) as ActivityManager).appTasks?.forEach {
            it.finishAndRemoveTask()
        }
    }

}