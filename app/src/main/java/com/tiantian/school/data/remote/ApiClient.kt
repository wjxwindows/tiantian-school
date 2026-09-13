package com.tiantian.school.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tiantian.school.data.local.AppPrefs
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 单例。
 *
 * 服务器地址可在「设置」里修改，因此这里按 baseUrl 缓存实例，
 * 地址变化时自动重建。
 */
object ApiClient {

    /** 打开后可看到完整请求/响应日志，仅供调试。 */
    var enableLogging: Boolean = true

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .create()

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var builtFor: String? = null

    /** 规范化地址：补协议、补结尾斜杠。 */
    fun normalizeBaseUrl(raw: String): String {
        var url = raw.trim()
        if (url.isEmpty()) url = AppPrefs.DEFAULT_BASE_URL
        if (!url.startsWith("http://", true) && !url.startsWith("https://", true)) {
            url = "http://$url"
        }
        if (!url.endsWith("/")) url = "$url/"
        return url
    }

    @Synchronized
    fun api(): TiantianApi {
        val base = normalizeBaseUrl(AppPrefs.baseUrl)
        val cached = retrofit
        if (cached != null && builtFor == base) {
            return cached.create(TiantianApi::class.java)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (enableLogging) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { AppPrefs.token })
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(150, TimeUnit.SECONDS)   // AI 推理慢，给足时间
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(200, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val instance = Retrofit.Builder()
            .baseUrl(base)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        retrofit = instance
        builtFor = base
        return instance.create(TiantianApi::class.java)
    }

    /** 切换服务器地址后调用，强制下次重建。 */
    @Synchronized
    fun reset() {
        retrofit = null
        builtFor = null
    }

    /**
     * 探测专用：短超时、不缓存。
     * 用于启动阶段快速判断某个地址是不是可用的服务器。
     */
    fun probeApi(rawBase: String): TiantianApi {
        val base = normalizeBaseUrl(rawBase)
        val client = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .callTimeout(4, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
        return Retrofit.Builder()
            .baseUrl(base)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TiantianApi::class.java)
    }

    /** 常见的候选地址，按「最可能命中」排序。 */
    val candidateBaseUrls: List<String> = listOf(
        "http://127.0.0.1:3000/",   // WSA / 真机 adb reverse 后
        "http://10.0.2.2:3000/",    // 标准 AVD 模拟器
        "http://localhost:3000/"
    )
}
