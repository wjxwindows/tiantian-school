package com.tiantian.school.data.model

/**
 * 家庭绑定、防沉迷、学习数据相关模型。
 * 所有字段都给了默认值，服务端字段缺失时不会崩。
 */

// ---------- 账号 ----------

data class AccountRegisterRequest(
    val username: String,
    val password: String,
    /** child / parent，由客户端声明，服务端仍会校验 */
    val accountType: String,
    val nickname: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val age: Int? = null,
    val eduSystem: String? = null,
    val grade: Int? = null,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val platform: String = "android"
)

data class AccountMeResponse(
    val success: Boolean = true,
    val username: String? = null,
    val accountType: String? = null,
    val nickname: String? = null,
    val guardianStatus: String? = null,
    val ageMode: String? = null,
    val isBanned: Boolean = false,
    val user: User? = null
) {
    fun resolveUser(): User? = user ?: username?.let {
        User(username = it, nickname = nickname, isBanned = isBanned)
    }
}

// ---------- 家庭绑定 ----------

data class FamilyPinRequest(
    /** 可选的备注，例如「给小明用」 */
    val note: String? = null
)

data class FamilyPinResponse(
    val success: Boolean = false,
    val pin: String? = null,
    /** 二维码里要编码的内容，服务端给就用，没给就用 pin */
    val qrContent: String? = null,
    val expiresIn: Int = 300,
    val familyId: String? = null,
    val error: String? = null
)

data class FamilyBindRequest(
    val pin: String? = null,
    /** 扫二维码时直接带内容 */
    val qrContent: String? = null
)

data class FamilyBindResponse(
    val success: Boolean = false,
    val familyId: String? = null,
    val childId: String? = null,
    val error: String? = null,
    val code: String? = null
)

data class FamilyBinding(
    val bindingId: String = "",
    val childId: String = "",
    val childName: String? = null,
    val childNickname: String? = null,
    val parentId: String? = null,
    val parentName: String? = null,
    val relation: String? = null,
    val status: String = "active",
    val createdAt: String? = null,
    val approvedAt: String? = null
) {
    fun childDisplayName(): String =
        childName?.takeIf { it.isNotBlank() }
            ?: childNickname?.takeIf { it.isNotBlank() }
            ?: childId.ifBlank { "未命名孩子" }
}

data class FamilyInfoResponse(
    val success: Boolean = true,
    val familyId: String? = null,
    val role: String? = null,
    val bindings: List<FamilyBinding> = emptyList(),
    val children: List<FamilyBinding> = emptyList(),
    val parents: List<FamilyBinding> = emptyList(),
    val guardianStatus: String? = null,
    val pendingCount: Int = 0
) {
    /** 统一取孩子列表：服务端可能返回 children 或 bindings */
    fun childList(): List<FamilyBinding> =
        if (children.isNotEmpty()) children else bindings
}

// ---------- 家长设置的限制 ----------

data class FamilyLimits(
    val dailyMinutes: Int = 120,
    val nightLockStart: String = "21:30",
    val nightLockEnd: String = "06:30",
    val allowStore: Boolean = false,
    val allowAi: Boolean = true,
    val restEveryMinutes: Int = 40
)

data class UpdateFamilyLimitsRequest(
    val dailyMinutes: Int? = null,
    val nightLockStart: String? = null,
    val nightLockEnd: String? = null,
    val allowStore: Boolean? = null,
    val allowAi: Boolean? = null,
    val restEveryMinutes: Int? = null
)

data class FamilyLimitsResponse(
    val success: Boolean = true,
    val limits: FamilyLimits? = null,
    val error: String? = null
)

// ---------- 学习报告 ----------

data class FamilyReport(
    val childId: String = "",
    val range: String = "week",
    val studyMinutes: Int = 0,
    val questionCount: Int = 0,
    val correctRate: Int = 0,
    val mistakeCount: Int = 0,
    val checkinDays: Int = 0,
    val taskDone: Int = 0,
    val subjects: List<SubjectProgress> = emptyList(),
    val generatedAt: String? = null
)

data class FamilyReportResponse(
    val success: Boolean = true,
    val report: FamilyReport? = null,
    val error: String? = null
)

// ---------- 防沉迷 ----------

data class GuardProfile(
    val username: String = "",
    val accountType: String = "child",
    val ageMode: String = "unknown",
    val guardianStatus: String = "unbound",
    val isMinor: Boolean = false,
    val realNameStatus: String? = null
)

data class GuardProfileResponse(
    val success: Boolean = true,
    val profile: GuardProfile? = null
)

data class GuardPolicy(
    val dailyMinutes: Int = 120,
    val usedTodayMinutes: Int = 0,
    val remainingMinutes: Int = 120,
    val nightLockStart: String = "21:30",
    val nightLockEnd: String = "06:30",
    val nightLocked: Boolean = false,
    val restEveryMinutes: Int = 40,
    val restRequired: Boolean = false,
    /** normal / rest / daily_limit / night_lock */
    val state: String = "normal",
    val message: String? = null
)

data class GuardPolicyResponse(
    val success: Boolean = true,
    val policy: GuardPolicy? = null
)

data class GuardHeartbeatResponse(
    val success: Boolean = true,
    val policy: GuardPolicy? = null,
    val state: String? = null,
    val message: String? = null
)

data class GuardUsage(
    val todayMinutes: Int = 0,
    val remainingMinutes: Int = 120,
    val continuousMinutes: Int = 0,
    val sessionCount: Int = 0
)

data class GuardUsageResponse(
    val success: Boolean = true,
    val usage: GuardUsage? = null
)

// ---------- 学习数据 ----------

data class SubjectProgress(
    val subject: String = "",
    val questionCount: Int = 0,
    val correctCount: Int = 0,
    val correctRate: Int = 0,
    val mistakeCount: Int = 0,
    val masteredPoints: Int = 0,
    val totalPoints: Int = 0
)

data class StudyProgressResponse(
    val success: Boolean = true,
    val studyMinutes: Int = 0,
    val questionCount: Int = 0,
    val correctRate: Int = 0,
    val mistakeCount: Int = 0,
    val streakDays: Int = 0,
    val subjects: List<SubjectProgress> = emptyList()
)

data class MistakeItem(
    val id: String = "",
    val subject: String = "",
    val stem: String = "",
    val myAnswer: String? = null,
    val correctAnswer: String? = null,
    val analysis: String? = null,
    val knowledgePoint: String? = null,
    val mastered: Boolean = false,
    val createdAt: String? = null
)

data class MistakesResponse(
    val success: Boolean = true,
    val mistakes: List<MistakeItem> = emptyList()
)

data class AddMistakeRequest(
    val subject: String,
    val stem: String,
    val myAnswer: String? = null,
    val correctAnswer: String? = null,
    val analysis: String? = null,
    val knowledgePoint: String? = null
)

data class CheckinStatus(
    val checkedToday: Boolean = false,
    val streakDays: Int = 0,
    val totalDays: Int = 0,
    val todayReward: Int = 0,
    val lastCheckinAt: String? = null
)

data class CheckinStatusResponse(
    val success: Boolean = true,
    val status: CheckinStatus? = null
)

data class CheckinResponse(
    val success: Boolean = false,
    val streakDays: Int = 0,
    val reward: Int = 0,
    val error: String? = null
)

data class DailyTask(
    val id: String = "",
    val title: String = "",
    val desc: String? = null,
    val target: Int = 1,
    val progress: Int = 0,
    val reward: Int = 0,
    val claimed: Boolean = false,
    val completed: Boolean = false
)

data class DailyTasksResponse(
    val success: Boolean = true,
    val tasks: List<DailyTask> = emptyList()
)

data class ClaimTaskRequest(val taskId: String)

data class CalendarEntry(
    val date: String = "",
    val minutes: Int = 0,
    val questionCount: Int = 0,
    val checkedIn: Boolean = false
)

data class CalendarResponse(
    val success: Boolean = true,
    val days: List<CalendarEntry> = emptyList()
)

// ---------- AI 举报 ----------

data class ReportRequest(
    val username: String,
    val targetType: String = "chat",
    val content: String? = null,
    val reason: String? = null
)
