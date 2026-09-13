package com.tiantian.school.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tiantian.school.BuildConfig

/**
 * 本地配置与登录态存储。
 *
 * 优先使用 EncryptedSharedPreferences 加密保存 Token；
 * 若设备/系统不支持（少数定制 ROM 会抛异常），自动降级为普通 SharedPreferences，
 * 保证 App 不会因为存储初始化失败而崩溃。
 */
object AppPrefs {

    private const val FILE_SECURE = "tiantian_secure"
    private const val FILE_FALLBACK = "tiantian_prefs"

    private const val KEY_TOKEN = "token"
    private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
    private const val KEY_USERNAME = "username"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_REMEMBER = "remember_me"
    private const val KEY_LAST_ACCOUNT = "last_account"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_SCHOOL_URL = "school_url"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_DEVICE_NAME = "device_name"

    /**
     * 默认地址来自 BuildConfig：
     * - debug   → http://192.168.1.32:3000/（当前开发电脑的局域网地址）
     * - release → 正式 HTTPS 域名
     * 地址变化时可在「设置」里修改，无需重新编译。
     */
    val DEFAULT_BASE_URL: String = BuildConfig.DEFAULT_BASE_URL
    val DEFAULT_SCHOOL_URL: String = BuildConfig.DEFAULT_SCHOOL_URL

    private lateinit var sp: SharedPreferences

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val app = context.applicationContext
            sp = try {
                val masterKey = MasterKey.Builder(app)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    app,
                    FILE_SECURE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (t: Throwable) {
                app.getSharedPreferences(FILE_FALLBACK, Context.MODE_PRIVATE)
            }
            migrateLegacyDebugUrls()
            initialized = true
        }
    }

    private fun prefs(): SharedPreferences = sp

    /**
     * 旧版本默认使用 10.0.2.2。部分模拟器网络模式无法访问该地址，
     * 升级 APK 后自动迁移到当前开发服务器地址，避免沿用旧缓存。
     */
    private fun migrateLegacyDebugUrls() {
        if (!BuildConfig.DEBUG) return

        val currentBase = sp.getString(KEY_BASE_URL, null)?.trimEnd('/')
        if (currentBase == "http://10.0.2.2:3000") {
            sp.edit().putString(KEY_BASE_URL, DEFAULT_BASE_URL).apply()
        }

        val currentSchool = sp.getString(KEY_SCHOOL_URL, null)?.trimEnd('/')
        if (currentSchool == "http://10.0.2.2:3000/school") {
            sp.edit().putString(KEY_SCHOOL_URL, DEFAULT_SCHOOL_URL).apply()
        }
    }
    // ---------- 登录态 ----------

    var token: String?
        get() = prefs().getString(KEY_TOKEN, null)
        set(value) = prefs().edit().putString(KEY_TOKEN, value).apply()

    /** Token 到期时间戳（毫秒）。 */
    var tokenExpiresAt: Long
        get() = prefs().getLong(KEY_TOKEN_EXPIRES_AT, 0L)
        set(value) = prefs().edit().putLong(KEY_TOKEN_EXPIRES_AT, value).apply()

    var username: String?
        get() = prefs().getString(KEY_USERNAME, null)
        set(value) = prefs().edit().putString(KEY_USERNAME, value).apply()

    var nickname: String?
        get() = prefs().getString(KEY_NICKNAME, null)
        set(value) = prefs().edit().putString(KEY_NICKNAME, value).apply()

    var rememberMe: Boolean
        get() = prefs().getBoolean(KEY_REMEMBER, true)
        set(value) = prefs().edit().putBoolean(KEY_REMEMBER, value).apply()

    var lastAccount: String
        get() = prefs().getString(KEY_LAST_ACCOUNT, "").orEmpty()
        set(value) = prefs().edit().putString(KEY_LAST_ACCOUNT, value).apply()

    fun saveSession(token: String, expiresInSeconds: Long, user: String?, nick: String?) {
        val expiresAt = System.currentTimeMillis() + expiresInSeconds * 1000L
        prefs().edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_TOKEN_EXPIRES_AT, expiresAt)
            .putString(KEY_USERNAME, user)
            .putString(KEY_NICKNAME, nick)
            .apply()
    }

    fun clearSession() {
        prefs().edit()
            .remove(KEY_TOKEN)
            .remove(KEY_TOKEN_EXPIRES_AT)
            .remove(KEY_USERNAME)
            .remove(KEY_NICKNAME)
            .apply()
    }

    fun isLoggedIn(): Boolean = !token.isNullOrBlank() && !isTokenExpired()

    fun isTokenExpired(): Boolean {
        val exp = tokenExpiresAt
        if (exp <= 0L) return false
        return System.currentTimeMillis() > exp
    }

    // ---------- 服务器地址 ----------

    var baseUrl: String
        get() = prefs().getString(KEY_BASE_URL, DEFAULT_BASE_URL).orEmpty().ifBlank { DEFAULT_BASE_URL }
        set(value) = prefs().edit().putString(KEY_BASE_URL, value).apply()

    var schoolUrl: String
        get() = prefs().getString(KEY_SCHOOL_URL, DEFAULT_SCHOOL_URL).orEmpty().ifBlank { DEFAULT_SCHOOL_URL }
        set(value) = prefs().edit().putString(KEY_SCHOOL_URL, value).apply()

    /**
     * 设备唯一标识，首次访问时生成并持久化。
     * 多端登录接入后，登录/心跳请求会带上它，便于服务端区分不同设备。
     */
    val deviceId: String
        get() {
            val cached = prefs().getString(KEY_DEVICE_ID, null)
            if (!cached.isNullOrBlank()) return cached
            val generated = java.util.UUID.randomUUID().toString()
            prefs().edit().putString(KEY_DEVICE_ID, generated).apply()
            return generated
        }

    /** 若服务端返回了不同的 deviceId，以服务端为准，避免产生「幽灵设备」。 */
    fun overrideDeviceId(value: String?) {
        if (value.isNullOrBlank()) return
        if (value == prefs().getString(KEY_DEVICE_ID, null)) return
        prefs().edit().putString(KEY_DEVICE_ID, value).apply()
    }

    /**
     * 设备名称，显示在「登录设备管理」中。
     * 首次访问时按机型生成，用户在设备管理页重命名后会覆盖。
     */
    var deviceName: String
        get() {
            val cached = prefs().getString(KEY_DEVICE_NAME, null)
            if (!cached.isNullOrBlank()) return cached
            val generated = generateDeviceName()
            prefs().edit().putString(KEY_DEVICE_NAME, generated).apply()
            return generated
        }
        set(value) = prefs().edit().putString(KEY_DEVICE_NAME, value).apply()

    private fun generateDeviceName(): String {
        val manufacturer = android.os.Build.MANUFACTURER.orEmpty().trim()
        val model = android.os.Build.MODEL.orEmpty().trim()
        val combined = listOf(manufacturer, model)
            .filter { it.isNotBlank() && !it.equals("unknown", ignoreCase = true) }
            .joinToString(" ")
        return combined.ifBlank { "我的安卓设备" }
    }
}
