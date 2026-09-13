package com.tiantian.school.data.repo

import com.google.gson.JsonParser
import com.tiantian.school.data.local.AppPrefs
import com.tiantian.school.data.model.AppealRequest
import com.tiantian.school.data.model.BuyRequest
import com.tiantian.school.data.model.ChatMsg
import com.tiantian.school.data.model.ChatRequest
import com.tiantian.school.data.model.EquipBadgeRequest
import com.tiantian.school.data.model.LoginRequest
import com.tiantian.school.data.model.LogoutRequest
import com.tiantian.school.data.model.PurchaseMembershipRequest
import com.tiantian.school.data.model.RegisterRequest
import com.tiantian.school.data.model.RenameDeviceRequest
import com.tiantian.school.data.model.UpdateUserRequest
import com.tiantian.school.data.model.AccountRegisterRequest
import com.tiantian.school.data.model.AddMistakeRequest
import com.tiantian.school.data.model.ClaimTaskRequest
import com.tiantian.school.data.model.FamilyBindRequest
import com.tiantian.school.data.model.FamilyPinRequest
import com.tiantian.school.data.model.ReportRequest
import com.tiantian.school.data.model.UpdateFamilyLimitsRequest
import com.tiantian.school.data.remote.ApiClient
import com.tiantian.school.data.remote.SessionEvents
import com.tiantian.school.data.remote.TiantianApi
import retrofit2.HttpException
import java.net.ConnectException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 所有网络请求的统一入口。
 *
 * 把「HTTP 异常 / 网络异常 / 业务错误」统一转换成 [ApiResult]，
 * 避免每个页面重复写 try-catch。
 */
class TiantianRepository(private val api: TiantianApi = ApiClient.api()) {

    // ---------- 基础 ----------
    suspend fun version() = call { api.version() }
    suspend fun ads(position: String = "home") = call { api.ads(position) }

    // ---------- 认证 ----------
    // 登录/注册本身返回 401 属于「账号密码错」，不应触发全局登录失效提示
    suspend fun login(account: String, password: String) =
        call(notifyExpired = false) {
            api.login(
                LoginRequest(
                    account = account,
                    password = password,
                    deviceId = AppPrefs.deviceId,
                    deviceName = AppPrefs.deviceName,
                    platform = "android"
                )
            )
        }

    suspend fun register(body: RegisterRequest) =
        call(notifyExpired = false) { api.register(body) }

    // ---------- 用户 ----------
    suspend fun getUser(username: String) = call { api.getUser(username) }
    suspend fun updateUser(username: String, body: UpdateUserRequest) =
        call { api.updateUser(username, body) }
    suspend fun appeal(username: String, reason: String) =
        call { api.appeal(AppealRequest(username, reason)) }

    // ---------- 天币 / 仓库 / 商城 ----------
    suspend fun tiancoin(username: String) = call { api.tiancoin(username) }
    suspend fun inventory(username: String) = call { api.inventory(username) }
    suspend fun storeItems() = call { api.storeItems() }
    suspend fun buy(username: String, itemId: String) =
        call { api.buy(BuyRequest(username, itemId)) }
    suspend fun equipBadge(username: String, itemId: String) =
        call { api.equipBadge(EquipBadgeRequest(username, itemId)) }

    // ---------- 会员 ----------
    suspend fun membershipPricing() = call { api.membershipPricing() }
    suspend fun membershipStatus(username: String) = call { api.membershipStatus(username) }
    suspend fun purchaseMembership(username: String, planType: String) =
        call { api.purchaseMembership(PurchaseMembershipRequest(username, planType)) }

    // ---------- 学习内容 ----------
    suspend fun papers() = call { api.papers() }
    suspend fun paper(code: String) = call { api.paper(code) }
    suspend fun chat(username: String, message: String, history: List<ChatMsg>) =
        call { api.chat(ChatRequest(username = username, message = message, history = history)) }

    // ---------- 3D 校园地图 ----------
    suspend fun getSchoolMap(username: String) = call { api.getSchoolMap(username) }
    suspend fun putSchoolMap(username: String, map: Map<String, Any?>) =
        call { api.putSchoolMap(username, map) }

    // ---------- 多设备登录管理 ----------

    suspend fun devices(username: String) = call { api.devices(username) }

    suspend fun renameDevice(username: String, deviceId: String, name: String) =
        call { api.renameDevice(username, deviceId, RenameDeviceRequest(name)) }

    /**
     * 退出指定设备。
     * 不自动触发全局登录失效事件，因为需要根据返回的 current 字段决定是否登出本机。
     */
    suspend fun revokeDevice(username: String, deviceId: String) =
        call(notifyExpired = false) { api.revokeDevice(username, deviceId) }

    suspend fun revokeOtherDevices(username: String) =
        call { api.revokeOtherDevices(username) }

    /** 主动退出当前设备；无论成功失败，调用方都应清本地登录态。 */
    suspend fun logout(username: String) =
        call(notifyExpired = false) { api.logout(LogoutRequest(username)) }

    // ---------- 账号（双端）----------

    suspend fun accountsRegister(body: AccountRegisterRequest) =
        call(notifyExpired = false) { api.accountsRegister(body) }

    suspend fun accountMe() = call { api.accountMe() }

    // ---------- 家庭绑定 ----------

    suspend fun familyPin(note: String? = null) =
        call { api.familyPin(FamilyPinRequest(note = note)) }

    suspend fun familyBind(pin: String? = null, qrContent: String? = null) =
        call(notifyExpired = false) {
            api.familyBind(FamilyBindRequest(pin = pin, qrContent = qrContent))
        }

    suspend fun family() = call { api.family() }

    suspend fun familyUnbind(bindingId: String) = call { api.familyUnbind(bindingId) }

    suspend fun familyReport(childId: String, range: String = "week") =
        call { api.familyReport(childId, range) }

    suspend fun familyLimits(childId: String) = call { api.familyLimits(childId) }

    suspend fun updateFamilyLimits(childId: String, body: UpdateFamilyLimitsRequest) =
        call { api.updateFamilyLimits(childId, body) }

    // ---------- 防沉迷 ----------

    suspend fun guardProfile() = call { api.guardProfile() }

    suspend fun guardPolicy() = call { api.guardPolicy() }

    /** 心跳不触发全局登录失效提示，避免与锁定页逻辑冲突。 */
    suspend fun guardHeartbeat() = call(notifyExpired = false) { api.guardHeartbeat() }

    suspend fun guardUsage() = call { api.guardUsage() }

    // ---------- 学习数据 ----------

    suspend fun progress(username: String) = call { api.progress(username) }

    suspend fun mistakes(username: String) = call { api.mistakes(username) }

    suspend fun addMistake(body: AddMistakeRequest) = call { api.addMistake(body) }

    suspend fun checkin() = call(notifyExpired = false) { api.checkin() }

    suspend fun checkinStatus() = call { api.checkinStatus() }

    suspend fun dailyTasks() = call { api.dailyTasks() }

    suspend fun claimTask(taskId: String) =
        call(notifyExpired = false) { api.claimTask(ClaimTaskRequest(taskId)) }

    suspend fun calendar(username: String, month: String? = null) =
        call { api.calendar(username, month) }

    // ---------- AI 举报 ----------

    suspend fun report(username: String, content: String?, reason: String?) =
        call(notifyExpired = false) {
            api.report(ReportRequest(username = username, content = content, reason = reason))
        }

    // ---------- 内部 ----------

    private suspend fun <T> call(
        notifyExpired: Boolean = true,
        block: suspend () -> T
    ): ApiResult<T> {
        return try {
            ApiResult.Ok(block())
        } catch (e: HttpException) {
            val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            val parsed = parseError(raw)
            val httpCode = e.code()
            if (notifyExpired && (httpCode == 401 || httpCode == 403)) {
                AppPrefs.clearSession()
                // 必须通知界面：否则会静默留在主页，出现「用户名空、数据全空」的假死
                val message = parsed.first
                    ?: if (httpCode == 403) {
                        "账号已在其他设备登录，当前登录已失效，请重新登录"
                    } else {
                        "登录已过期，请重新登录"
                    }
                SessionEvents.notifyExpired(message)
            }
            ApiResult.Err(
                message = parsed.first ?: defaultMessage(httpCode),
                code = parsed.second,
                httpCode = httpCode,
                unauthorized = httpCode == 401 || httpCode == 403
            )
        } catch (e: UnknownHostException) {
            ApiResult.Err("找不到服务器地址，请在「设置」里检查 API 地址是否填写正确")
        } catch (e: ConnectException) {
            ApiResult.Err(
                "无法连接到服务器。请确认服务已启动，并且手机与电脑在同一网络；" +
                    "真机请填电脑局域网 IP（如 http://192.168.3.11:3000/），而不是 10.0.2.2"
            )
        } catch (e: SocketTimeoutException) {
            ApiResult.Err("连接超时：服务器地址可能不正确或网络不通，可在「设置」里更换地址后重试")
        } catch (e: IOException) {
            ApiResult.Err("网络连接失败：请检查网络，并在「设置」里确认 API 地址")
        } catch (e: Throwable) {
            ApiResult.Err(e.message ?: "请求失败，请稍后重试")
        }
    }

    /** 解析 `{success:false,error,code}` 结构。 */
    private fun parseError(body: String?): Pair<String?, String?> {
        if (body.isNullOrBlank()) return null to null
        return try {
            val obj = JsonParser.parseString(body).asJsonObject
            val error = obj.get("error")?.takeIf { !it.isJsonNull }?.asString
            val code = obj.get("code")?.takeIf { !it.isJsonNull }?.asString
            error to code
        } catch (_: Throwable) {
            null to null
        }
    }

    private fun defaultMessage(httpCode: Int): String = when (httpCode) {
        400 -> "请求参数有误"
        401 -> "请先登录"
        403 -> "登录已过期，请重新登录"
        404 -> "请求的资源不存在"
        429 -> "操作过于频繁，请稍后再试"
        500, 502, 503, 504 -> "服务器开小差了，请稍后再试"
        else -> "请求失败（$httpCode）"
    }

    companion object {
        /** 快速探测某个地址是否为可用的天天校园服务器。 */
        suspend fun probe(rawBase: String): Boolean {
            return try {
                ApiClient.probeApi(rawBase).version().success
            } catch (t: Throwable) {
                false
            }
        }

        /**
         * 依次尝试「当前地址 → 候选地址」，返回第一个可用的。
         * 覆盖 WSA(adb reverse)、标准 AVD、真机局域网三种情况。
         */
        suspend fun resolveServer(): String {
            val current = AppPrefs.baseUrl
            if (probe(current)) return current
            for (candidate in ApiClient.candidateBaseUrls) {
                if (candidate == current) continue
                if (probe(candidate)) {
                    AppPrefs.baseUrl = candidate
                    ApiClient.reset()
                    return candidate
                }
            }
            return current
        }
    }
}
