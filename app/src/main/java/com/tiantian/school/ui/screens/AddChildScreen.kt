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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.LoadingBox
import com.tiantian.school.ui.components.QrCodeImage
import com.tiantian.school.ui.theme.BrandBlue
import com.tiantian.school.ui.theme.DangerRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pin by remember { mutableStateOf<String?>(null) }
    var qrContent by remember { mutableStateOf<String?>(null) }
    var remainSeconds by remember { mutableIntStateOf(300) }

    fun generate() {
        loading = true
        error = null
        scope.launch {
            when (val r = TiantianRepository().familyPin()) {
                is ApiResult.Ok -> {
                    pin = r.data.pin
                    qrContent = r.data.qrContent ?: r.data.pin
                    remainSeconds = if (r.data.expiresIn > 0) r.data.expiresIn else 300
                }
                is ApiResult.Err -> error = r.message
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { generate() }

    // 倒计时
    LaunchedEffect(pin) {
        if (pin.isNullOrBlank()) return@LaunchedEffect
        while (remainSeconds > 0) {
            delay(1000)
            remainSeconds -= 1
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("添加孩子") },
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
                loading -> LoadingBox(text = "正在生成配对码…")
                error != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = error.orEmpty(),
                        color = DangerRed,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { generate() }) { Text("重试") }
                }
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "让孩子端打开 App，进入「绑定家长」，输入下面的配对码即可完成绑定。" +
                                "配对码 5 分钟内有效、只能用一次。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    // PIN 大字展示
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BrandBlue.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "配对码",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrandBlue
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = pin.orEmpty(),
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue,
                                letterSpacing = 8.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = if (remainSeconds > 0) {
                                    val m = remainSeconds / 60
                                    val s = remainSeconds % 60
                                    "剩余 %d:%02d".format(m, s)
                                } else {
                                    "已过期，请重新生成"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (remainSeconds > 0) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    DangerRed
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // 二维码
                    if (!qrContent.isNullOrBlank() && remainSeconds > 0) {
                        QrCodeImage(
                            content = qrContent.orEmpty(),
                            modifier = Modifier.size(230.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "也可以让孩子端扫这个码",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { generate() },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("重新生成")
                        }
                        Button(
                            onClick = { navController.popBackStack() },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("完成")
                        }
                    }

                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}
