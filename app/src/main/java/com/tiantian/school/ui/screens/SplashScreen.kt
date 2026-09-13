package com.tiantian.school.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tiantian.school.data.local.AppPrefs
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.nav.Routes
import com.tiantian.school.ui.theme.BrandBlue
import com.tiantian.school.ui.theme.BrandCyan
import com.tiantian.school.vm.AppState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        val started = System.currentTimeMillis()

        // 自动定位可用服务器：覆盖 WSA(adb reverse)、标准 AVD、真机局域网三种情况
        TiantianRepository.resolveServer()

        val repo = TiantianRepository()

        // 顺手探一次服务器版本，失败不影响进入 App
        when (val v = repo.version()) {
            is ApiResult.Ok -> AppState.serverVersion = v.data.version
            is ApiResult.Err -> Unit
        }

        val goMain = {
            navController.navigate(Routes.MAIN) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
        val goLogin = {
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }

        val cachedUser = AppPrefs.username
        if (AppPrefs.isLoggedIn() && !cachedUser.isNullOrBlank()) {
            when (val r = repo.getUser(cachedUser)) {
                is ApiResult.Ok -> {
                    r.data.resolve()?.let { AppState.updateUser(it) }
                    // 保证启动动画有最小展示时间
                    val elapsed = System.currentTimeMillis() - started
                    if (elapsed < 700) delay(700 - elapsed)
                    goMain()
                }
                is ApiResult.Err -> {
                    if (r.unauthorized) {
                        AppState.logout()
                        val elapsed = System.currentTimeMillis() - started
                        if (elapsed < 700) delay(700 - elapsed)
                        goLogin()
                    } else {
                        // 网络异常时允许沿用本地登录态进入
                        val elapsed = System.currentTimeMillis() - started
                        if (elapsed < 700) delay(700 - elapsed)
                        goMain()
                    }
                }
            }
        } else {
            val elapsed = System.currentTimeMillis() - started
            if (elapsed < 700) delay(700 - elapsed)
            goLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(BrandBlue, BrandCyan))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color.White.copy(alpha = 0.18f), MaterialTheme.shapes.extraLarge),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.School,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "天天校园",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "在线学习 · AI 辅导 · 3D 校园",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(36.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
