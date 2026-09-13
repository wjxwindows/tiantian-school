package com.tiantian.school.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.nav.Routes
import com.tiantian.school.vm.AppState
import kotlinx.coroutines.delay

private data class MainTab(
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(navController: NavHostController) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // 被封禁账号：只允许申诉或注销
    if (AppState.isBanned) {
        BannedScreen(navController)
        return
    }

    // ---------- 防沉迷：策略 + 心跳 ----------
    var guardState by remember { mutableStateOf("normal") }
    var guardMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val repo = TiantianRepository()
        // 进入时先取一次策略
        when (val r = repo.guardPolicy()) {
            is ApiResult.Ok -> r.data.policy?.let {
                guardState = it.state
                guardMessage = it.message
            }
            is ApiResult.Err -> Unit
        }
        // 前台每 60 秒上报一次心跳，由服务端判定是否该锁定
        while (true) {
            delay(60_000)
            when (val r = repo.guardHeartbeat()) {
                is ApiResult.Ok -> {
                    val p = r.data.policy
                    guardState = p?.state ?: r.data.state ?: "normal"
                    guardMessage = p?.message ?: r.data.message
                }
                is ApiResult.Err -> Unit
            }
        }
    }

    // 被锁定（休息 / 今日用尽 / 夜间锁定）时整屏接管
    if (guardState != "normal") {
        GuardLockScreen(
            navController = navController,
            state = guardState,
            message = guardMessage
        )
        return
    }

    val tabs = listOf(
        MainTab("首页", Icons.Rounded.Home),
        MainTab("试卷", Icons.Rounded.Description),
        MainTab("AI 助手", Icons.Rounded.AutoAwesome),
        MainTab("商城", Icons.Rounded.Storefront),
        MainTab("我的", Icons.Rounded.Person)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(tab.icon, contentDescription = tab.label)
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeTab(navController)
                1 -> PapersTab(navController)
                2 -> ChatTab()
                3 -> StoreTab()
                else -> ProfileTab(navController)
            }
        }
    }
}

@Composable
private fun BannedScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "账号已被封禁",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "该账号暂时无法使用校园、试卷、AI 等主要功能。\n你可以提交申诉，或注销账号。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                // 管理员不会被封禁；即使服务端误标，也不给申诉入口
                if (!AppState.isAdmin) {
                    Button(
                        onClick = { navController.navigate(Routes.APPEAL) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("提交申诉")
                    }
                    Spacer(Modifier.height(10.dp))
                }
                Button(
                    onClick = {
                        AppState.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("退出登录")
                }
            }
        }
    }
}
