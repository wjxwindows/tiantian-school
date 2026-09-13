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
import androidx.compose.material.icons.rounded.MonetizationOn
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tiantian.school.data.model.TiancoinInfo
import com.tiantian.school.data.model.TiancoinLog
import com.tiantian.school.data.model.tiancoinText
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.EmptyBox
import com.tiantian.school.ui.components.ErrorBox
import com.tiantian.school.ui.components.LoadingBox
import com.tiantian.school.ui.theme.DangerRed
import com.tiantian.school.ui.theme.GoldYellow
import com.tiantian.school.ui.theme.SuccessGreen
import com.tiantian.school.vm.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiancoinScreen(navController: NavHostController) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<TiancoinInfo?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshKey) {
        loading = true
        error = null
        val name = AppState.username
        if (name.isBlank()) {
            error = "请先登录"
        } else {
            when (val r = TiantianRepository().tiancoin(name)) {
                is ApiResult.Ok -> info = r.data
                is ApiResult.Err -> error = r.message
            }
        }
        loading = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("我的天币") },
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
                loading -> LoadingBox(text = "正在加载天币…")
                error != null -> ErrorBox(message = error.orEmpty()) { refreshKey++ }
                else -> {
                    val data = info
                    val logs = data?.log.orEmpty()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            // 余额卡
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier.background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFB8860B), Color(0xFFFFC53D))
                                        )
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.MonetizationOn,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(26.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "天币余额",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = tiancoinText(
                                                data?.balance,
                                                data?.unlimited == true
                                            ),
                                            fontSize = 38.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "收支明细",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (logs.isEmpty()) {
                            item {
                                EmptyBox(
                                    message = "还没有天币记录",
                                    hint = "完成学习任务、参与活动可以获得天币",
                                    modifier = Modifier.height(220.dp)
                                )
                            }
                        } else {
                            items(logs) { log -> TiancoinLogRow(log) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TiancoinLogRow(log: TiancoinLog) {
    val isIncome = log.type == "income" || log.amount > 0
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.note.ifBlank {
                        when (log.type) {
                            "income" -> "天币收入"
                            "spend" -> "天币支出"
                            else -> "系统调整"
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (log.time.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = log.time,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = when {
                    log.amount > 0 -> "+${log.amount}"
                    log.amount < 0 -> log.amount.toString()
                    else -> "0"
                },
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    log.amount > 0 -> SuccessGreen
                    log.amount < 0 -> DangerRed
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
