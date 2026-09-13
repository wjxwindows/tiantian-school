package com.tiantian.school.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tiantian.school.AppRole
import com.tiantian.school.data.local.AppPrefs
import com.tiantian.school.data.model.Ad
import com.tiantian.school.data.model.tiancoinText
import com.tiantian.school.data.remote.ApiClient
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.NetworkImage
import com.tiantian.school.ui.components.SectionHeader
import com.tiantian.school.ui.nav.Routes
import com.tiantian.school.ui.theme.BrandBlue
import com.tiantian.school.ui.theme.BrandCyan
import com.tiantian.school.ui.theme.GoldYellow
import com.tiantian.school.ui.theme.SuccessGreen
import com.tiantian.school.vm.AppState

private data class GridEntry(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeTab(
    navController: NavHostController,
    onSwitchTab: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var coinText by remember { mutableStateOf("—") }
    var ads by remember { mutableStateOf<List<Ad>>(emptyList()) }
    var refreshing by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshKey) {
        refreshing = true
        val repo = TiantianRepository()
        val name = AppState.username
        if (name.isNotBlank()) {
            when (val r = repo.tiancoin(name)) {
                is ApiResult.Ok -> {
                    val unlimited = r.data.unlimited ||
                        (r.data.balance as? String)?.equals("unlimited", true) == true
                    coinText = tiancoinText(r.data.balance, unlimited)
                }
                is ApiResult.Err -> coinText = "—"
            }
        }
        when (val r = repo.ads("home")) {
            is ApiResult.Ok -> ads = r.data.ads
            is ApiResult.Err -> ads = emptyList()
        }
        refreshing = false
    }

    fun openUrl(rawUrl: String) {
        if (rawUrl.isBlank()) return
        val url = if (rawUrl.startsWith("http", true)) {
            rawUrl
        } else {
            ApiClient.normalizeBaseUrl(AppPrefs.baseUrl).trimEnd('/') +
                "/" + rawUrl.trimStart('/')
        }
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    val membership = AppState.user?.membershipStatus
    val membershipText = when {
        membership?.isValid == true && membership.days == null -> "永久会员"
        membership?.isValid == true -> "会员 · 剩 ${membership.days} 天"
        else -> "普通用户"
    }

    val entries = listOf(
        GridEntry("3D 校园", Icons.Rounded.ViewInAr, BrandBlue) {
            navController.navigate(Routes.SCHOOL)
        },
        GridEntry("试卷中心", Icons.Rounded.Description, SuccessGreen) {
            onSwitchTab(1)
        },
        GridEntry("AI 助手", Icons.Rounded.AutoAwesome, Color(0xFF9B6BFF)) {
            onSwitchTab(2)
        },
        GridEntry("商城", Icons.Rounded.Storefront, Color(0xFFFF8A3D)) {
            onSwitchTab(3)
        },
        GridEntry("会员中心", Icons.Rounded.WorkspacePremium, GoldYellow) {
            navController.navigate(Routes.MEMBERSHIP)
        },
        GridEntry("我的天币", Icons.Rounded.MonetizationOn, Color(0xFFFFB300)) {
            navController.navigate(Routes.TIANCOIN)
        },
        GridEntry("我的仓库", Icons.Rounded.Inventory2, Color(0xFF2FB4C8)) {
            navController.navigate(Routes.INVENTORY)
        },
        GridEntry("设置", Icons.Rounded.Settings, Color(0xFF7A869A)) {
            navController.navigate(Routes.SETTINGS)
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ---------- 欢迎头部 ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(BrandBlue, BrandCyan)))
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "你好，${AppState.nickname}",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "${AppState.user?.eduSystem?.let { edu ->
                                if (edu == "54") "五四制" else "六三制"
                            } ?: "六三制"} · ${AppState.user?.grade ?: 1} 年级",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }
                    IconButton(onClick = { refreshKey++ }) {
                        if (refreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = "刷新",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "天币余额",
                        value = coinText,
                        icon = Icons.Rounded.MonetizationOn,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "会员状态",
                        value = membershipText,
                        icon = Icons.Rounded.WorkspacePremium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ---------- 未绑定家长提示（孩子端显眼位置）----------
        val guardianStatus = AppState.user?.guardianStatus
        val needBindGuardian = AppRole.isChild &&
            (guardianStatus == null || guardianStatus == "unbound" || guardianStatus == "pending")
        if (needBindGuardian) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .clickable { navController.navigate(Routes.BIND_PARENT) },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF3E0)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFE0B2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FamilyRestroom,
                            contentDescription = null,
                            tint = Color(0xFF9A6B00),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (guardianStatus == "pending") "等待家长确认" else "还没有绑定家长",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF8A5B00)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "点这里输入家长端的配对码，绑定后可同步学习报告",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF9A6B00)
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF9A6B00)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        // ---------- 功能宫格 ----------
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            SectionHeader(title = "学习入口", subtitle = "校园、试卷、AI 与更多")
            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)) {
                    entries.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { entry ->
                                GridCell(entry = entry, modifier = Modifier.weight(1f))
                            }
                            // 补齐空位，保证最后一行对齐
                            repeat(4 - row.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }

        // ---------- 广告位 ----------
        if (ads.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                SectionHeader(title = "推荐", subtitle = "来自天天校园")
                Spacer(Modifier.height(12.dp))
                ads.forEach { ad ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clickable { openUrl(ad.url) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NetworkImage(
                                url = if (ad.image.startsWith("http", true)) {
                                    ad.image
                                } else {
                                    ApiClient.normalizeBaseUrl(AppPrefs.baseUrl).trimEnd('/') +
                                        "/" + ad.image.trimStart('/')
                                },
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ad.title.ifBlank { "天天校园推荐" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = "点击查看详情",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "天天校园 · 让学习更有趣",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(9.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun GridCell(entry: GridEntry, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { entry.onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(entry.color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = entry.icon,
                contentDescription = entry.title,
                tint = entry.color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = entry.title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
