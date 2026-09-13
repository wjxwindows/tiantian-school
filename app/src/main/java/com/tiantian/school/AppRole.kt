package com.tiantian.school

/**
 * 当前 APK 的角色。
 * 由 product flavor 注入：child = 孩子端，parent = 家长端。
 */
object AppRole {
    const val CHILD = "child"
    const val PARENT = "parent"

    val current: String get() = BuildConfig.APP_ROLE
    val isChild: Boolean get() = current == CHILD
    val isParent: Boolean get() = current == PARENT

    val displayName: String get() = if (isChild) "孩子端" else "家长端"
}
