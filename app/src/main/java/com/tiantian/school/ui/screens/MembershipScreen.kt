package com.tiantian.school.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tiantian.school.data.model.MembershipPlan
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.ErrorBox
import com.tiantian.school.ui.components.LoadingBox
import com.tiantian.school.ui.components.MessageDialog
import com.tiantian.school.ui.theme.BrandBlue
import com.tiantian.school.ui.theme.BrandCyan
import com.tiantian.school.ui.theme.GoldYellow
import com.tiantian.school.vm.AppState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var plans by remember { mutableStateOf<List<MembershipPlan>>(emptyList()) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var purchasing by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(refreshKey) {
        loading = true
        error = null
        when (val r = TiantianRepository().membershipPricing()) {
            is ApiResult.Ok -> plans = r.data.plans
            is ApiResult.Err -> error = r.message
        }
        loading = false
    }

    // 拉一次最新会员状态
    LaunchedEffect(refreshKey) {
        val name = AppState.username
        if (name.isNotBlank()) {
            when (val r = TiantianRepository().membershipStatus(name)) {
                is ApiResult.Ok -> {
                    val st = r.data.status
                    if (st != null) {
                        AppState.patchUser { it.copy(membershipStatus = st) }
                    }
                }
                is ApiResult.Err -> Unit
            }
        }
    }

    fun purchase(plan: MembershipPlan) {
        val name = AppState.username
        if (name.isBlank()) {
            dialog = "请先登录" to "登录后才能开通会员"
            return
        }
        purchasing = plan.type
        scope.launch {
            when (val r = TiantianRepository().purchaseMembership(name, plan.type)) {
                is ApiResult.Ok -> {
                    val res = r.data
                    if (res.success) {
                        dialog = "开通成功" to "已成功开通「${plan.name}」，祝学习愉快！"
                        refreshKey++
                    } else {
                        dialog = "开通失败" to (res.error ?: "请稍后再试")
                    }
                }
                is ApiResult.Err -> {
                    dialog = if (r.unauthorized) {
                        "登录已过期" to "请重新登录后再试"
                    } else {
                        "开通失败" to r.message
                    }
                }
            }
            purchasing = null
        }
    }

    val membership = AppState.user?.membershipStatus
    val statusText = when {
        membership?.isValid == true && membership.days == null -> "永久会员"
        membership?.isValid == true -> "剩余 ${membership.days} 天"
        else -> "尚未开通"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("会员中心") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                loading -> LoadingBox(text = "正在加载会员套餐…")
                error != null -> ErrorBox(message = error.orEmpty()) { refreshKey++ }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // 会员状态卡
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFF3A2B12), Color(0xFF8A6414)))
                                    )
                                    .padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.WorkspacePremium,
                                        contentDescription = null,
                                        tint = GoldYellow,
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Spacer(Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = if (membership?.isValid == true) {
                                                membership.name.ifBlank { "会员" }
                                            } else {
                                                "开通会员"
                                            },
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = GoldYellow
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "选择套餐",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    if (plans.isEmpty()) {
                        item {
                            Text(
                                text = "服务端暂未配置会员套餐",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(plans, key = { it.type }) { plan ->
                            MembershipPlanCard(
                                plan = plan,
                                buying = purchasing == plan.type,
                                onBuy = { purchase(plan) }
                            )
                        }
                    }

                    item {
                        Text(
                            text = "会员按天计算，未过期续费会叠加天数；封禁期间暂停扣减。",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    dialog?.let { (title, message) ->
        MessageDialog(title = title, message = message, onDismiss = { dialog = null })
    }
}

@Composable
private fun MembershipPlanCard(
    plan: MembershipPlan,
    buying: Boolean,
    onBuy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.name.ifBlank { plan.type },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${plan.days} 天",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${plan.price} 天币",
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandBlue
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = onBuy,
                    enabled = !buying,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (buying) "处理中" else "开通")
                }
            }
        }
    }
}
