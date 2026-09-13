package com.tiantian.school.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.ConfirmDialog
import com.tiantian.school.ui.nav.Routes
import com.tiantian.school.ui.theme.BrandBlue
import com.tiantian.school.ui.theme.BrandCyan
import com.tiantian.school.vm.AppState
import kotlinx.coroutines.launch

@Composable
fun ProfileTab(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var showLogout by remember { mutableStateOf(false) }

    val user = AppState.user
    val membership = user?.membershipStatus
    val membershipText = when {
        membership?.isValid == true && membership.days == null -> "永久会员"
        membership?.isValid == true -> "会员 · 剩 ${membership.days} 天"
        else -> "普通用户"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 个人信息头部
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(BrandBlue, BrandCyan)))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppState.nickname.take(1).ifBlank { "学" },
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppState.nickname,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "@${AppState.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                    if (AppState.equippedBadge.isNotBlank()) {
                        Spacer(Modifier.height(5.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "🏅 ${AppState.equippedBadge}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 概览
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewItem("会员", membershipText)
                OverviewItem(
                    "学制",
                    if (user?.eduSystem == "54") "五四制" else "六三制"
                )
                OverviewItem("年级", "${user?.grade ?: 1} 年级")
            }
        }

        Spacer(Modifier.height(18.dp))

        // 常用功能
        ProfileGroup {
            ProfileRow(Icons.Rounded.MonetizationOn, "我的天币", "查看余额与明细") {
                navController.navigate(Routes.TIANCOIN)
            }
            ProfileRow(Icons.Rounded.WorkspacePremium, "会员中心", "开通或续费会员") {
                navController.navigate(Routes.MEMBERSHIP)
            }
            ProfileRow(Icons.Rounded.Inventory2, "我的仓库", "查看道具与佩戴徽章") {
                navController.navigate(Routes.INVENTORY)
            }
        }

        Spacer(Modifier.height(12.dp))

        ProfileGroup {
            ProfileRow(Icons.Rounded.Edit, "编辑资料", "昵称、邮箱、学校等") {
                navController.navigate(Routes.PROFILE_EDIT)
            }
            ProfileRow(Icons.Rounded.Devices, "登录设备管理", "查看并下线其他设备") {
                navController.navigate(Routes.DEVICES)
            }
            // 孩子端专属：绑定家长入口（未绑定时也能在这里找到）
            ProfileRow(Icons.Rounded.FamilyRestroom, "绑定家长", "输入家长端的配对码完成绑定") {
                navController.navigate(Routes.BIND_PARENT)
            }
            ProfileRow(Icons.Rounded.Settings, "设置", "服务器地址与 3D 校园地址") {
                navController.navigate(Routes.SETTINGS)
            }
            // 管理员是系统级账号，不会被封禁，因此不提供申诉入口
            if (!AppState.isAdmin) {
                ProfileRow(Icons.Rounded.ReportProblem, "账号申诉", "对封禁或异常进行处理") {
                    navController.navigate(Routes.APPEAL)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        ProfileGroup {
            ProfileRow(
                Icons.AutoMirrored.Rounded.Logout,
                "退出登录",
                null,
                danger = true
            ) {
                showLogout = true
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = "天天校园 v1.0.0",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }

    if (showLogout) {
        ConfirmDialog(
            title = "退出登录",
            message = "确定要退出当前账号吗？",
            onDismiss = { showLogout = false },
            onConfirm = {
                showLogout = false
                val username = AppState.username
                scope.launch {
                    // 通知服务端注销本设备会话；失败也照常清本地登录态
                    if (username.isNotBlank()) {
                        TiantianRepository().logout(username)
                    }
                    AppState.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            confirmText = "退出"
        )
    }
}

@Composable
private fun OverviewItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileGroup(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column { content() }
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (danger) MaterialTheme.colorScheme.error else BrandBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (danger) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}
