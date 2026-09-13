package com.tiantian.school.data.remote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 登录态失效的全局通知通道。
 *
 * 背景：服务端规则是「同一账号只允许一个会话，后登录顶掉先登录」，
 * 被顶掉的一侧后续请求会收到 403。此时必须让界面明确感知并回到登录页，
 * 否则会出现「用户名变空、数据全空」却不提示的假死状态。
 *
 * 用法：Repository 在收到 401/403 时写入消息；UI 层（AppNavHost）消费后跳转登录页。
 */
object SessionEvents {

    /** 待处理的提示文案，null 表示无待处理事件。 */
    var pendingMessage by mutableStateOf<String?>(null)
        private set

    fun notifyExpired(message: String) {
        pendingMessage = message
    }

    fun consume() {
        pendingMessage = null
    }
}
