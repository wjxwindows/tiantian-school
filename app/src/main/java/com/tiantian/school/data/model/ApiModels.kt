package com.tiantian.school.data.model

/**
 * 天天校园 数据模型。
 *
 * 说明：
 * - 服务端部分字段类型不固定（如 tiancoin 可能是数字，也可能是字符串 "unlimited"），
 *   这里统一用 Any? 接收，再由 [TianCoinValue] 做展示层转换。
 * - 所有字段都给了默认值，避免服务端字段缺失时反序列化崩溃。
 */

data class LoginRequest(
    val account: String,
    val password: String,
    /** 设备唯一标识，同一 deviceId 重复登录只替换该设备自己的会话 */
    val deviceId: String,
    /** 显示在「登录设备管理」里的名称 */
    val deviceName: String,
    val platform: String = "android"
)

data class LoginResponse(
    val success: Boolean = false,
    val token: String? = null,
    val deviceId: String? = null,
    val expiresIn: Long = 0L,
    val user: User? = null,
    val error: String? = null,
    val code: String? = null
)

data class User(
    val username: String = "",
    /** 账号类型：child / parent / admin。由服务端判定，客户端不可篡改 */
    val accountType: String? = null,
    val nickname: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val school: String? = null,
    val gender: String? = null,
    val eduSystem: String? = null,
    val grade: Int? = null,
    /** 监护状态：unbound 未绑定 / pending 待确认 / bound 已绑定 */
    val guardianStatus: String? = null,
    val tiancoin: Any? = null,
    val inventory: List<InventoryItem>? = null,
    val equippedBadge: String? = null,
    val isBanned: Boolean = false,
    val adminToken: String? = null,
    val membershipStatus: MembershipStatus? = null
)

/**
 * GET /api/users/{username} 的返回。
 * 兼容 {success,data:{...}}、{success,user:{...}}、{success,...平铺...} 三种写法。
 */
data class UserResponse(
    val success: Boolean = true,
    val error: String? = null,
    val code: String? = null,
    val data: User? = null,
    val user: User? = null,
    val username: String? = null,
    val nickname: String? = null,
    val gender: String? = null,
    val eduSystem: String? = null,
    val grade: Int? = null,
    val tiancoin: Any? = null,
    val equippedBadge: String? = null,
    val isBanned: Boolean? = null,
    val membershipStatus: MembershipStatus? = null
) {
    fun resolve(): User? {
        data?.let { return it }
        user?.let { return it }
        val name = username ?: return null
        return User(
            username = name,
            nickname = nickname,
            gender = gender,
            eduSystem = eduSystem,
            grade = grade,
            tiancoin = tiancoin,
            equippedBadge = equippedBadge,
            isBanned = isBanned ?: false,
            membershipStatus = membershipStatus
        )
    }
}

data class MembershipStatus(
    val isValid: Boolean = false,
    val level: String = "none",
    val name: String = "普通用户",
    /**
     * 剩余天数。null 表示永久会员（管理员）。
     * 注意：Gson 无法区分「字段缺失」和「显式 null」，这里配合 [isPermanent] 使用。
     */
    val days: Int? = null,
    val isPermanent: Boolean = false
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val phone: String,
    val email: String? = null,
    val nickname: String? = null,
    val gender: String? = null,
    val eduSystem: String? = null,
    val grade: Int? = null
)

data class UpdateUserRequest(
    val nickname: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val school: String? = null,
    val gender: String? = null,
    val eduSystem: String? = null,
    val grade: Int? = null
)

// ---------- 天币 ----------

data class TiancoinInfo(
    val success: Boolean = true,
    val balance: Any? = null,
    val unlimited: Boolean = false,
    val log: List<TiancoinLog> = emptyList()
)

data class TiancoinLog(
    val time: String = "",
    val type: String = "",
    val amount: Int = 0,
    val note: String = ""
)

// ---------- 仓库 / 商城 ----------

data class InventoryItem(
    val itemId: String = "",
    val name: String = "",
    val icon: String = "",
    val desc: String = "",
    val kind: String = "item",
    val boughtAt: String = "",
    val days: Int? = null
)

data class InventoryResponse(
    val success: Boolean = true,
    val inventory: List<InventoryItem> = emptyList()
)

data class StoreItem(
    val id: String = "",
    val name: String = "",
    val price: Int = 0,
    val icon: String = "",
    val desc: String = "",
    val kind: String = "item",
    val days: Int? = null,
    val repeatable: Boolean = false
)

data class StoreItemsResponse(
    val success: Boolean = true,
    val items: List<StoreItem> = emptyList()
)

data class BuyRequest(
    val username: String,
    val itemId: String
)

data class BuyResult(
    val success: Boolean = false,
    val error: String? = null,
    val code: String? = null,
    val balance: Any? = null
)

data class EquipBadgeRequest(
    val username: String,
    val itemId: String
)

// ---------- 会员 ----------

data class MembershipPricing(
    val success: Boolean = true,
    val plans: List<MembershipPlan> = emptyList()
)

data class MembershipPlan(
    val type: String = "",
    val name: String = "",
    val days: Int = 0,
    val price: Int = 0
)

data class MembershipStatusResponse(
    val success: Boolean = true,
    val status: MembershipStatus? = null,
    val days: Int? = null,
    val isPermanent: Boolean = false
)

data class PurchaseMembershipRequest(
    val username: String,
    val planType: String
)

// ---------- 试卷 ----------

data class Paper(
    val code: String = "",
    val title: String = "",
    val subject: String = "",
    val grade: Int? = null,
    val questionCount: Int = 0,
    val duration: Int? = null,
    val createdAt: String? = null,
    val questions: List<Question> = emptyList()
)

data class Question(
    val id: String = "",
    /** single 单选 / multiple 多选 / judge 判断 / fill 填空 / essay 解答 */
    val type: String = "single",
    val stem: String = "",
    val options: List<String> = emptyList(),
    val answer: String? = null,
    val analysis: String? = null,
    val score: Int? = null
)

data class PapersResponse(
    val success: Boolean = true,
    val papers: List<Paper> = emptyList()
)

data class PaperResponse(
    val success: Boolean = true,
    val paper: Paper? = null
)

// ---------- AI 对话 ----------

data class ChatMsg(
    val role: String,
    val content: String
)

data class ChatRequest(
    val username: String,
    val message: String,
    val history: List<ChatMsg> = emptyList(),
    val system: String? = null
)

data class ChatResponse(
    val success: Boolean = false,
    val reply: String? = null,
    val model: String? = null,
    val error: String? = null
)

// ---------- 广告 / 版本 / 通用 ----------

data class Ad(
    val id: String = "",
    val title: String = "",
    val image: String = "",
    val url: String = "",
    val position: String = "home",
    val startAt: String? = null,
    val endAt: String? = null
)

data class AdsResponse(
    val success: Boolean = true,
    val ads: List<Ad> = emptyList()
)

data class VersionInfo(
    val success: Boolean = true,
    val version: String? = null,
    val serverTime: String? = null,
    val name: String? = null
)

data class SimpleResult(
    val success: Boolean = false,
    val error: String? = null,
    val code: String? = null
)

data class AppealRequest(
    val username: String,
    val reason: String
)

data class SchoolMapResponse(
    val success: Boolean = true,
    val exists: Boolean = false,
    val map: Any? = null
)

// ---------- 多设备登录管理 ----------

data class DeviceItem(
    val id: String = "",
    val deviceId: String = "",
    val name: String? = null,
    val deviceName: String? = null,
    val platform: String? = null,
    val ip: String? = null,
    val current: Boolean = false,
    val admin: Boolean = false,
    val createdAt: String? = null,
    val lastSeenAt: String? = null,
    val expiresAt: Long? = null
) {
    /** 展示名：优先用重命名后的 name，其次用登录时上报的 deviceName。 */
    fun displayName(): String =
        name?.takeIf { it.isNotBlank() }
            ?: deviceName?.takeIf { it.isNotBlank() }
            ?: "未知设备"

    /** 稳定标识：接口里 deviceId 与 id 可能只有一个有值。 */
    fun stableId(): String = deviceId.ifBlank { id }
}

data class DeviceListResponse(
    val success: Boolean = true,
    val devices: List<DeviceItem> = emptyList(),
    val currentDeviceId: String? = null
)

data class RenameDeviceRequest(val name: String)

data class RenameDeviceResponse(
    val success: Boolean = false,
    val device: DeviceItem? = null
)

data class RevokeOthersResponse(
    val success: Boolean = false,
    val removed: Int = 0
)

data class LogoutRequest(val username: String)

/**
 * 通用返回。
 * `current = true` 表示本次被下线的设备就是当前设备，App 应立即清登录态。
 */
data class SimpleResponse(
    val success: Boolean = false,
    val error: String? = null,
    val code: String? = null,
    val current: Boolean? = null
)

/** 把服务端返回的 tiancoin 字段（数字或 "unlimited"）转成展示用文本。 */
fun tiancoinText(value: Any?, unlimited: Boolean = false): String {
    if (unlimited) return "∞"
    return when (value) {
        null -> "0"
        is String -> if (value.equals("unlimited", ignoreCase = true)) "∞" else value
        is Double -> if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
        is Number -> value.toString()
        else -> value.toString()
    }
}
