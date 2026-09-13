package com.tiantian.school.data.remote

import com.tiantian.school.data.model.AdsResponse
import com.tiantian.school.data.model.AppealRequest
import com.tiantian.school.data.model.BuyRequest
import com.tiantian.school.data.model.BuyResult
import com.tiantian.school.data.model.ChatRequest
import com.tiantian.school.data.model.ChatResponse
import com.tiantian.school.data.model.DeviceListResponse
import com.tiantian.school.data.model.EquipBadgeRequest
import com.tiantian.school.data.model.InventoryResponse
import com.tiantian.school.data.model.LoginRequest
import com.tiantian.school.data.model.LoginResponse
import com.tiantian.school.data.model.LogoutRequest
import com.tiantian.school.data.model.MembershipPricing
import com.tiantian.school.data.model.MembershipStatusResponse
import com.tiantian.school.data.model.PaperResponse
import com.tiantian.school.data.model.PapersResponse
import com.tiantian.school.data.model.PurchaseMembershipRequest
import com.tiantian.school.data.model.RegisterRequest
import com.tiantian.school.data.model.RenameDeviceRequest
import com.tiantian.school.data.model.RenameDeviceResponse
import com.tiantian.school.data.model.RevokeOthersResponse
import com.tiantian.school.data.model.SchoolMapResponse
import com.tiantian.school.data.model.SimpleResponse
import com.tiantian.school.data.model.SimpleResult
import com.tiantian.school.data.model.StoreItemsResponse
import com.tiantian.school.data.model.TiancoinInfo
import com.tiantian.school.data.model.UpdateUserRequest
import com.tiantian.school.data.model.UserResponse
import com.tiantian.school.data.model.VersionInfo
import com.tiantian.school.data.model.AccountMeResponse
import com.tiantian.school.data.model.AccountRegisterRequest
import com.tiantian.school.data.model.AddMistakeRequest
import com.tiantian.school.data.model.CalendarResponse
import com.tiantian.school.data.model.CheckinResponse
import com.tiantian.school.data.model.CheckinStatusResponse
import com.tiantian.school.data.model.ClaimTaskRequest
import com.tiantian.school.data.model.DailyTasksResponse
import com.tiantian.school.data.model.FamilyBindRequest
import com.tiantian.school.data.model.FamilyBindResponse
import com.tiantian.school.data.model.FamilyInfoResponse
import com.tiantian.school.data.model.FamilyLimitsResponse
import com.tiantian.school.data.model.FamilyPinRequest
import com.tiantian.school.data.model.FamilyPinResponse
import com.tiantian.school.data.model.FamilyReportResponse
import com.tiantian.school.data.model.GuardHeartbeatResponse
import com.tiantian.school.data.model.GuardPolicyResponse
import com.tiantian.school.data.model.GuardProfileResponse
import com.tiantian.school.data.model.GuardUsageResponse
import com.tiantian.school.data.model.MistakesResponse
import com.tiantian.school.data.model.ReportRequest
import com.tiantian.school.data.model.StudyProgressResponse
import com.tiantian.school.data.model.UpdateFamilyLimitsRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 天天校园 REST 接口。
 * 认证统一走 Authorization: Bearer <token>，由 [AuthInterceptor] 自动注入。
 */
interface TiantianApi {

    // ---------- 基础 ----------

    @GET("api/version")
    suspend fun version(): VersionInfo

    @GET("api/ads")
    suspend fun ads(@Query("position") position: String = "home"): AdsResponse

    // ---------- 认证 ----------

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/users")
    suspend fun register(@Body body: RegisterRequest): LoginResponse

    // ---------- 用户 ----------

    @GET("api/users/{username}")
    suspend fun getUser(@Path("username") username: String): UserResponse

    @PUT("api/users/{username}")
    suspend fun updateUser(
        @Path("username") username: String,
        @Body body: UpdateUserRequest
    ): SimpleResult

    @POST("api/appeal")
    suspend fun appeal(@Body body: AppealRequest): SimpleResult

    // ---------- 天币 / 仓库 / 商城 ----------

    @GET("api/tiancoin/{username}")
    suspend fun tiancoin(@Path("username") username: String): TiancoinInfo

    @GET("api/inventory/{username}")
    suspend fun inventory(@Path("username") username: String): InventoryResponse

    @GET("api/store/items")
    suspend fun storeItems(): StoreItemsResponse

    @POST("api/store/buy")
    suspend fun buy(@Body body: BuyRequest): BuyResult

    @POST("api/badge/equip")
    suspend fun equipBadge(@Body body: EquipBadgeRequest): SimpleResult

    // ---------- 会员 ----------

    @GET("api/membership/pricing")
    suspend fun membershipPricing(): MembershipPricing

    @GET("api/membership/status/{username}")
    suspend fun membershipStatus(
        @Path("username") username: String
    ): MembershipStatusResponse

    @POST("api/membership/purchase")
    suspend fun purchaseMembership(@Body body: PurchaseMembershipRequest): SimpleResult

    // ---------- 学习内容 ----------

    @GET("api/papers")
    suspend fun papers(): PapersResponse

    @GET("api/papers/{code}")
    suspend fun paper(@Path("code") code: String): PaperResponse

    @POST("api/chat")
    suspend fun chat(@Body body: ChatRequest): ChatResponse

    // ---------- 3D 校园地图 ----------

    @GET("api/schoolmap/{username}")
    suspend fun getSchoolMap(@Path("username") username: String): SchoolMapResponse

    @PUT("api/schoolmap/{username}")
    suspend fun putSchoolMap(
        @Path("username") username: String,
        @Body body: Map<String, Any?>
    ): SimpleResult

    // ---------- 多设备登录管理 ----------

    @GET("api/users/{username}/devices")
    suspend fun devices(@Path("username") username: String): DeviceListResponse

    @PUT("api/users/{username}/devices/{deviceId}")
    suspend fun renameDevice(
        @Path("username") username: String,
        @Path("deviceId") deviceId: String,
        @Body body: RenameDeviceRequest
    ): RenameDeviceResponse

    @DELETE("api/users/{username}/devices/{deviceId}")
    suspend fun revokeDevice(
        @Path("username") username: String,
        @Path("deviceId") deviceId: String
    ): SimpleResponse

    @POST("api/users/{username}/devices/revoke-others")
    suspend fun revokeOtherDevices(@Path("username") username: String): RevokeOthersResponse

    @POST("api/logout")
    suspend fun logout(@Body body: LogoutRequest): SimpleResponse

    // ---------- 账号（双端共用）----------

    @POST("api/accounts/register")
    suspend fun accountsRegister(@Body body: AccountRegisterRequest): LoginResponse

    @GET("api/accounts/me")
    suspend fun accountMe(): AccountMeResponse

    // ---------- 家庭绑定 ----------

    /** 家长端生成一次性 PIN（同时返回二维码内容）。 */
    @POST("api/family/pin")
    suspend fun familyPin(@Body body: FamilyPinRequest): FamilyPinResponse

    /** 孩子端用 PIN 或二维码内容完成绑定。 */
    @POST("api/family/bind")
    suspend fun familyBind(@Body body: FamilyBindRequest): FamilyBindResponse

    @GET("api/family")
    suspend fun family(): FamilyInfoResponse

    @DELETE("api/family/bindings/{bindingId}")
    suspend fun familyUnbind(@Path("bindingId") bindingId: String): SimpleResponse

    @GET("api/family/report/{childId}")
    suspend fun familyReport(
        @Path("childId") childId: String,
        @Query("range") range: String = "week"
    ): FamilyReportResponse

    @GET("api/family/limits/{childId}")
    suspend fun familyLimits(@Path("childId") childId: String): FamilyLimitsResponse

    @PUT("api/family/limits/{childId}")
    suspend fun updateFamilyLimits(
        @Path("childId") childId: String,
        @Body body: UpdateFamilyLimitsRequest
    ): FamilyLimitsResponse

    // ---------- 防沉迷 ----------

    @GET("api/guard/profile")
    suspend fun guardProfile(): GuardProfileResponse

    @GET("api/guard/policy")
    suspend fun guardPolicy(): GuardPolicyResponse

    @POST("api/guard/heartbeat")
    suspend fun guardHeartbeat(): GuardHeartbeatResponse

    @GET("api/guard/usage")
    suspend fun guardUsage(): GuardUsageResponse

    // ---------- 学习数据 ----------

    @GET("api/progress/{username}")
    suspend fun progress(@Path("username") username: String): StudyProgressResponse

    @GET("api/mistakes/{username}")
    suspend fun mistakes(@Path("username") username: String): MistakesResponse

    @POST("api/mistakes")
    suspend fun addMistake(@Body body: AddMistakeRequest): SimpleResult

    @POST("api/checkin")
    suspend fun checkin(): CheckinResponse

    @GET("api/checkin/status")
    suspend fun checkinStatus(): CheckinStatusResponse

    @GET("api/tasks/daily")
    suspend fun dailyTasks(): DailyTasksResponse

    @POST("api/tasks/claim")
    suspend fun claimTask(@Body body: ClaimTaskRequest): SimpleResult

    @GET("api/calendar/{username}")
    suspend fun calendar(
        @Path("username") username: String,
        @Query("month") month: String? = null
    ): CalendarResponse

    // ---------- AI 举报 ----------

    @POST("api/report")
    suspend fun report(@Body body: ReportRequest): SimpleResult
}
