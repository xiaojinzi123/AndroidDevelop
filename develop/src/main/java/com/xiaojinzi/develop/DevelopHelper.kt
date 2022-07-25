package com.xiaojinzi.develop

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.text.TextUtils
import android.util.Base64
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.xiaojinzi.develop.bean.DevelopAuthVOReq
import com.xiaojinzi.develop.bean.DevelopAuthVORes
import okhttp3.*
import org.json.JSONObject
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.PublicKey

object DevelopHelper {

    // String IP = "10.0.24.86"
    val IP = "192.168.3.80"
    val PORT = 23457
    var DEVELOP_HEADER_NAME = "developLog"
    var DEVELOP_HEADER_VALUE = "true"
    var DEVELOP_URL = "http://$IP:$PORT"
    const val PUBLIC_KEY =
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDLRr+V/3b8SPzMd25wXbZhNodDvfA1trAwchR26DvZS+sMUh5Q2W12pyUJIaBmMaPbRSGwt0DIRX/eWzR+oN9q9Np+ORN5neljej5Qn4SRW6MHBoVtBVcw5Bpa764szddYsCtTxLHSscPDfrmgVU/4L0N6xJoJs+ieDgjpNXQC0wIDAQAB"
    const val DEVELOP_AUTH_FILE_NAME = "developAuth.data"
    const val EXTRA_OPEN_DEVELOP = "openDevelop"

    private val okHttpClient = OkHttpClient()

    private val handler = Handler(Looper.getMainLooper())

    /**
     * [.tryOpen] 尝试的次数, 当尝试超过这个数字就会开启
     */
    var TRY_COUNT = 5

    private val g = Gson()

    private var isDebug = false
    private var isDevelop = false

    private var count = 0

    fun isDebug(): Boolean {
        return isDebug
    }

    fun isDevelop(): Boolean {
        return isDevelop
    }

    private var config: Config? = null

    fun init(config: Config) {
        DevelopHelper.config = config
        isDebug = config.debug
        isDevelop = isDebug
        config.application.registerActivityLifecycleCallbacks(ComponentLifecycleCallback())
    }

    fun tryOpen() {
        if (++count >= TRY_COUNT) {
            try {
                Thread {
                    realOpen()
                }.start()
            } catch (ignore: java.lang.Exception) {
                // ignore
            } finally {
                count = 0
            }
        }
    }

    @WorkerThread
    fun realOpen(): Boolean {
        return realOpen(null)
    }

    @WorkerThread
    fun realOpen(beforeRebootRunnable: Runnable?): Boolean {
        // 声明返回值
        var result = false
        try {
            val developAuthVOReq =
                DevelopAuthVOReq(EXTRA_OPEN_DEVELOP, System.currentTimeMillis(), 10 * 1000)
            val encryptStr = encrypt(g.toJson(developAuthVOReq))
            val formBody = FormBody
                .Builder()
                .add(
                    "content",
                    encryptStr
                )
                .build()

            val request: Request = Request.Builder()
                .url("$DEVELOP_URL/develop/auth")
                .header(DEVELOP_HEADER_NAME, DEVELOP_HEADER_VALUE)
                .post(formBody)
                .build()

            val call = okHttpClient.newCall(request)

            val response = call.execute()
            val json = response.body!!.string()
            val jb = JSONObject(json)
            val errorCode = jb.getInt("errorCode")
            if (errorCode == 0) { // 说明成功了
                // 加密过的 String
                val encryptString = jb.getString("data")
                if (!TextUtils.isEmpty(encryptString)) {
                    val intent = Intent(config!!.developOpenIntentAction)
                    intent.putExtra("mainAppAction", config!!.mainAppIntentAction)
                    intent.putExtra("encryptText", encryptString)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent.resolveActivity(
                            config!!.application.packageManager
                        ) != null
                    ) {
                        beforeRebootRunnable?.run()
                        config!!.application.startActivity(intent)
                        if (config!!.isFinishAllActivityBeforeKillProcess) {
                            ComponentActivityStack.getInstance().destroyAll();
                        }
                        Process.killProcess(Process.myPid())
                        result = true
                    }
                }
            }
        } catch (_: Exception) {
            // ignore
        }
        return result
    }

    @Throws(Exception::class)
    fun encrypt(text: String): String {
        Strings.requireNotEmpty(text)
        val publicKey: PublicKey = RSAUtil.string2PublicKey(PUBLIC_KEY)
        val bytes: ByteArray =
            RSAUtil.publicEncrypt(text.toByteArray(Charsets.UTF_8), publicKey)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    @Throws(Exception::class)
    fun decrypt(text: String): String {
        Strings.requireNotEmpty(text)
        val publicKey: PublicKey = RSAUtil.string2PublicKey(PUBLIC_KEY)
        val bytes: ByteArray = RSAUtil.publicDecrypt(
            Base64.decode(text.toByteArray(charset("UTF-8")), Base64.NO_WRAP),
            publicKey
        )
        return String(bytes, Charsets.UTF_8)
    }

    /**
     * 保存内容到特定的文件中, 这个内容是一个服务器加密的数据. 公钥可以解开, 别人没法伪造
     */
    fun saveEncryptDevelopAuthData(context: Context, value: String) {
        try {
            val file = File(context.filesDir, DEVELOP_AUTH_FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(value.toByteArray(Charsets.UTF_8))
            fileOutputStream.close()
        } catch (ignore: java.lang.Exception) {
        }
    }

    /**
     * 尝试打开开发者工具
     * 1. 对特定的路径下读取文件中的数据
     * 2. 然后对读取到的数据解密.
     * 3. 解密后的数据必须满足两个条件
     * 3-1. 当前的时间不能超过解密后的 endTime, 这个防止文件数据被特别的保存用于持续打开. 也就是在加解密的基础上又加了一层防护
     * 3-2. 解密后的内容必须是特定的字符串
     */
    fun tryOpenDevelop(application: Application) {
        val file = File(application.filesDir, DEVELOP_AUTH_FILE_NAME)
        try {
            if (!file.exists()) {
                return
            }
            val fileInputStream = FileInputStream(file)
            val byteArrayOutputStream = ByteArrayOutputStream()
            val buf = ByteArray(1024)
            var len = -1
            while (fileInputStream.read(buf).also { len = it } != -1) {
                byteArrayOutputStream.write(buf, 0, len)
            }
            fileInputStream.close()
            val jsonStrEncrypt = String(byteArrayOutputStream.toByteArray(), Charsets.UTF_8)
            val jsonStr: String = decrypt(jsonStrEncrypt)
            byteArrayOutputStream.close()
            val developAuthVORes: DevelopAuthVORes = g.fromJson(
                jsonStr,
                DevelopAuthVORes::class.java
            )
            if (System.currentTimeMillis() > developAuthVORes.endTime) {
                return
            }
            if (EXTRA_OPEN_DEVELOP != developAuthVORes.content) {
                return
            }
            isDevelop = true
        } catch (ignore: java.lang.Exception) {
            // ignore
        } finally {
            try {
                file.delete()
            } catch (ignore: java.lang.Exception) {
            }
        }
    }

    fun crashHandle() {
        crashHandle(isDevelop())
    }

    private fun crashHandle(isOpen: Boolean) {
        if (isOpen) {
            Thread.setDefaultUncaughtExceptionHandler { t: Thread?, e: Throwable ->
                try {
                    if (!(config!!.developErrorIntentAction.isNullOrEmpty())) {
                        val sw = StringWriter()
                        var pw = PrintWriter(sw)
                        // 打印到 pw 这个流中, 最终会到 sw 流中
                        e.printStackTrace(pw)
                        try {
                            pw.close()
                            sw.close()
                        } catch (ignore: java.lang.Exception) {
                        }
                        toErrorView(sw.toString())
                    }
                } catch (ignore: java.lang.Exception) {
                    throw ignore
                }
            }
        }
    }

    fun toErrorView(errorMsg: String) {
        val intent = Intent(config!!.developErrorIntentAction)
        intent.putExtra("mainAppAction", config!!.mainAppIntentAction)
        intent.putExtra("errorMsg", errorMsg)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(config!!.application!!.packageManager) != null) {
            config!!.application!!.startActivity(intent)
        }
        if (config!!.isFinishAllActivityBeforeKillProcess) {
            ComponentActivityStack.getInstance().destroyAll();
        }
        Process.killProcess(Process.myPid())
    }

}