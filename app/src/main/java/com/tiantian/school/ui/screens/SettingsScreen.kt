package com.tiantian.school.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tiantian.school.data.local.AppPrefs
import com.tiantian.school.data.remote.ApiClient
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.MessageDialog
import com.tiantian.school.ui.theme.DangerRed
import com.tiantian.school.ui.theme.SuccessGreen
import com.tiantian.school.ui.theme.WarningAmber
import com.tiantian.school.vm.AppState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()

    var baseUrl by remember { mutableStateOf(AppPrefs.baseUrl) }
    var schoolUrl by remember { mutableStateOf(AppPrefs.schoolUrl) }
    var testing by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var dialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    // 判断当前是否运行在模拟器上，用于给出更准确的地址提示
    val isEmulator = remember {
        val fp = android.os.Build.FINGERPRINT
        val model = android.os.Build.MODEL
        val product = android.os.Build.PRODUCT
        fp.contains("generic", true) || fp.contains("emulator", true) ||
            model.contains("sdk", true) || product.contains("sdk", true) ||
            product.contains("emulator", true)
    }

    fun persist() {
        AppPrefs.baseUrl = ApiClient.normalizeBaseUrl(baseUrl)
        AppPrefs.schoolUrl = schoolUrl.trim().ifBlank { AppPrefs.DEFAULT_SCHOOL_URL }
        ApiClient.reset()
        baseUrl = AppPrefs.baseUrl
        schoolUrl = AppPrefs.schoolUrl
    }

    fun saveAndTest() {
        persist()
        testing = true
        testResult = null
        scope.launch {
            when (val r = TiantianRepository().version()) {
                is ApiResult.Ok -> testResult = true to
                    "连接成功，服务器版本 ${r.data.version ?: "未知"}"
                is ApiResult.Err -> testResult = false to r.message
            }
            testing = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "服务器",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("API 地址") },
                    supportingText = {
                        Text(
                            if (isEmulator) {
                                "当前为模拟器，推荐使用电脑局域网 IP：${AppPrefs.DEFAULT_BASE_URL}"
                            } else {
                                "当前为真机，请填电脑局域网 IP（在电脑上执行 ipconfig 查看），如 http://192.168.3.11:3000/"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 真机却填了模拟器专用地址时，直接把问题点出来
                if (!isEmulator && baseUrl.contains("10.0.2.2")) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WarningAmber.copy(alpha = 0.16f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠ 10.0.2.2 是模拟器访问宿主机的专用地址，真机连不上。" +
                                "请把这里改成电脑的局域网 IP，例如 http://192.168.3.11:3000/",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8A5B00),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = schoolUrl,
                    onValueChange = { schoolUrl = it },
                    label = { Text("3D 校园地址") },
                    supportingText = { Text("用于在 App 内打开 3D 校园页面") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { persist() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("仅保存")
                    }
                    Button(
                        onClick = { saveAndTest() },
                        enabled = !testing,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (testing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (testing) "测试中" else "保存并测试")
                    }
                }

                testResult?.let { (ok, message) ->
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (ok) {
                            SuccessGreen.copy(alpha = 0.12f)
                        } else {
                            DangerRed.copy(alpha = 0.10f)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (ok) SuccessGreen else DangerRed,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))
                Text(
                    text = "账号",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "当前登录：${AppState.username.ifBlank { "未登录" }}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "昵称：${AppState.nickname}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { navController.navigate(com.tiantian.school.ui.nav.Routes.APPEAL) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("账号申诉")
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("应用版本", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("1.0.0")
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("服务器版本", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(AppState.serverVersion ?: "未知")
                        }
                    }
                }

                Spacer(Modifier.height(30.dp))
                Text(
                    text = "天天校园 · 在线学习平台",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    dialog?.let { (title, message) ->
        MessageDialog(title = title, message = message, onDismiss = { dialog = null })
    }
}
