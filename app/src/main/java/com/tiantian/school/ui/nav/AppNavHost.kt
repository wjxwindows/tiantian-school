package com.tiantian.school.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tiantian.school.data.remote.SessionEvents
import com.tiantian.school.ui.components.MessageDialog
import com.tiantian.school.vm.AppState
import com.tiantian.school.ui.screens.AppealScreen
import com.tiantian.school.ui.screens.AddChildScreen
import com.tiantian.school.ui.screens.BindParentScreen
import com.tiantian.school.ui.screens.ChildDetailScreen
import com.tiantian.school.ui.screens.DeviceManageScreen
import com.tiantian.school.ui.screens.InventoryScreen
import com.tiantian.school.ui.screens.LoginScreen
import com.tiantian.school.ui.screens.MainScreen
import com.tiantian.school.ui.screens.MembershipScreen
import com.tiantian.school.ui.screens.ParentMainScreen
import com.tiantian.school.ui.screens.PaperDetailScreen
import com.tiantian.school.ui.screens.ProfileEditScreen
import com.tiantian.school.ui.screens.RegisterScreen
import com.tiantian.school.ui.screens.SchoolWebScreen
import com.tiantian.school.ui.screens.SettingsScreen
import com.tiantian.school.ui.screens.SplashScreen
import com.tiantian.school.ui.screens.TiancoinScreen
import com.tiantian.school.AppRole

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH
) {
    // 监听「登录态失效」事件：被顶号 / Token 过期时，提示并强制回到登录页
    val expiredMessage = SessionEvents.pendingMessage
    var expiredDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(expiredMessage) {
        val message = expiredMessage ?: return@LaunchedEffect
        SessionEvents.consume()
        // 清掉本地用户缓存，避免残留旧账号信息
        AppState.logout()
        expiredDialog = message
        navController.navigate(Routes.LOGIN) {
            // 清空整个回退栈，防止用返回键回到已失效的页面
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.REGISTER) { RegisterScreen(navController) }
        // 同一个入口，按 flavor 决定进入孩子端还是家长端
        composable(Routes.MAIN) {
            if (AppRole.isChild) {
                MainScreen(navController)
            } else {
                ParentMainScreen(navController)
            }
        }

        composable(
            route = Routes.PAPER_DETAIL,
            arguments = listOf(
                navArgument(Routes.ARG_CODE) { type = NavType.StringType }
            )
        ) { entry ->
            PaperDetailScreen(
                navController = navController,
                code = entry.arguments?.getString(Routes.ARG_CODE).orEmpty()
            )
        }

        composable(Routes.MEMBERSHIP) { MembershipScreen(navController) }
        composable(Routes.TIANCOIN) { TiancoinScreen(navController) }
        composable(Routes.INVENTORY) { InventoryScreen(navController) }
        composable(Routes.APPEAL) { AppealScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
        composable(Routes.PROFILE_EDIT) { ProfileEditScreen(navController) }
        composable(Routes.DEVICES) { DeviceManageScreen(navController) }
        composable(Routes.SCHOOL) { SchoolWebScreen(navController) }

        // ---------- 家长端 ----------
        composable(Routes.ADD_CHILD) { AddChildScreen(navController) }
        composable(
            route = Routes.CHILD_DETAIL,
            arguments = listOf(
                navArgument(Routes.ARG_CHILD_ID) { type = NavType.StringType }
            )
        ) { entry ->
            ChildDetailScreen(
                navController = navController,
                childId = entry.arguments?.getString(Routes.ARG_CHILD_ID).orEmpty()
            )
        }

        // ---------- 孩子端 ----------
        composable(Routes.BIND_PARENT) { BindParentScreen(navController) }
    }

    expiredDialog?.let { message ->
        MessageDialog(
            title = "登录已失效",
            message = message,
            onDismiss = { expiredDialog = null },
            confirmText = "重新登录"
        )
    }
}
