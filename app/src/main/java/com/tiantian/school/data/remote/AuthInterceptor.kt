package com.tiantian.school.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 自动附加 `Authorization: Bearer <token>`。
 * 使用 lambda 读取 Token，保证登录/登出后不用重建 Retrofit 实例。
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                header("Authorization", "Bearer $token")
            }
            header("Accept", "application/json")
        }.build()
        return chain.proceed(request)
    }
}
