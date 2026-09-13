package com.tiantian.school.data.repo

/** 统一的调用结果，UI 层只需要判断 Ok / Err。 */
sealed interface ApiResult<out T> {
    data class Ok<T>(val data: T) : ApiResult<T>

    data class Err(
        val message: String,
        val code: String? = null,
        val httpCode: Int = 0,
        /** 401/403：需要清 Token 重新登录 */
        val unauthorized: Boolean = false
    ) : ApiResult<Nothing>
}

/** 业务错误码，对应文档里的 code 字段。 */
object ErrorCode {
    const val NO_LOGIN = "no_login"
    const val NO_COIN = "no_coin"
    const val OWNED = "owned"
    const val NO_ITEM = "no_item"
}
