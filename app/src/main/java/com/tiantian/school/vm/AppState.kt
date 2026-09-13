package com.tiantian.school.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tiantian.school.data.local.AppPrefs
import com.tiantian.school.data.model.User
import com.tiantian.school.data.remote.ApiClient

/**
 * 应用级会话状态。
 *
 * 用 Compose 的 mutableStateOf 承载，任何页面读取都会自动订阅刷新。
 */
object AppState {

    var user by mutableStateOf<User?>(null)
        private set

    var serverVersion by mutableStateOf<String?>(null)

    val isLoggedIn: Boolean
        get() = AppPrefs.isLoggedIn() && !AppPrefs.username.isNullOrBlank()

    val username: String
        get() = user?.username ?: AppPrefs.username.orEmpty()

    val nickname: String
        get() = user?.nickname?.takeIf { it.isNotBlank() }
            ?: AppPrefs.nickname?.takeIf { it.isNotBlank() }
            ?: username

    val isBanned: Boolean
        get() = user?.isBanned == true

    /**
     * 是否管理员账号。
     *
     * 管理员是系统级账号（不属于家长/孩子），由服务端初始化，不能被封禁，
     * 也不应该出现「账号申诉」这类面向普通用户的入口。
     * 优先用 accountType 判定，老数据没有该字段时回退到「天币无限」这一特征。
     */
    val isAdmin: Boolean
        get() {
            val type = user?.accountType
            if (!type.isNullOrBlank()) {
                return type.equals("admin", ignoreCase = true)
            }
            val coin = user?.tiancoin
            return coin is String && coin.equals("unlimited", ignoreCase = true)
        }

    val equippedBadge: String
        get() = user?.equippedBadge.orEmpty()

    fun updateUser(newUser: User?) {
        user = newUser
        newUser?.let {
            AppPrefs.username = it.username
            AppPrefs.nickname = it.nickname
        }
    }

    /** 只刷新本地缓存的昵称等字段，避免整对象为空时把登录态弄丢。 */
    fun patchUser(transform: (User) -> User) {
        val current = user ?: return
        updateUser(transform(current))
    }

    fun logout() {
        user = null
        AppPrefs.clearSession()
        ApiClient.reset()
    }
}
