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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.navigation.NavHostController
import com.tiantian.school.data.model.FamilyReport
import com.tiantian.school.data.model.UpdateFamilyLimitsRequest
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.ConfirmDialog
import com.tiantian.school.ui.components.LoadingBox
import com.tiantian.school.ui.components.MessageDialog
import com.tiantian.school.ui.nav.Routes
import com.tiantian.school.ui.theme.BrandBlue
import com.tiantian.school.ui.theme.SuccessGreen
import com.tiantian.school.vm.AppState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDetailScreen(
    navController: NavHostController,
    childId: String
) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var report by remember { mutableStateOf<FamilyReport?>(null) }
    var dialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var confirmUnbind by remember { mutableStateOf(false) }

    var dailyMinutes by remember { mutableIntStateOf(120) }
    var restEvery by remember { mutableIntStateOf(40) }
    var nightStart by remember { mutableStateOf("21:30") }
    var nightEnd by remember { mutableStateOf("06:30") }
    var allowStore by remember { mutableStateOf(false) }
    var allowAi by remember { mutableStateOf(true) }

    LaunchedEffect(childId) {
        loading = true
        error = null
        val repo = TiantianRepository()

        when (val r = repo.familyLimits(childId)) {
            is ApiResult.Ok -> r.data.limits?.let { l ->
                dailyMinutes = l.dailyMinutes
                restEvery = l.restEveryMinutes
                nightStart = l.nightLockStart
                nightEnd = l.nightLockEnd
                allowStore = l.allowStore
                allowAi = l.allowAi
            }
            is ApiResult.Err -> error = r.message
        }

        when (val r = repo.familyReport(childId)) {
            is ApiResult.Ok -> report = r.data.report
            is ApiResult.Err -> Unit
        }

        loading = false
    }

    fun save() {
        saving = true
        scope.launch {
            val body = UpdateFamilyLimitsRequest(
                dailyMinutes = dailyMinutes,
                nightLockStart = nightStart,
                nightLockEnd = nightEnd,
                allowStore = allowStore,
                allowAi = allowAi,
                restEveryMinutes = restEvery
            )
            when (val r = TiantianRepository().updateFamilyLimits(childId, body)) {
                is ApiResult.Ok -> {
                    if (r.data.success) {
                        dialog = "已保存" to "孩子的使用限制已更新，孩子端会在下次同步时生效。"
                    } else {
                        dialog = "保存失败" to (r.data.error ?: "请稍后再试")
                    }
                }
                is ApiResult.Err -> dialog = "保存失败" to r.message
            }
            saving = false
        }
    }

    fun unbind() {
        scope.launch {
            val family = TiantianRepository().family()
            val bindingId = (family as? ApiResult.Ok)?.data?.childList()
                ?.firstOrNull { it.childId == childId }?.bindingId
            if (bindingId.isNullOrBlank()) {
                dialog = "操作失败" to "找不到绑定关系，请刷新后重试"
                return@launch
            }
            when (val r = TiantianRepository().familyUnbind(bindingId)) {
                is ApiResult.Ok -> {
                    if (r.data.success) {
                        dialog = "已解除绑定" to "已与「$childId」解除家庭绑定"
                    } else {
                        dialog = "操作失败" to (r.data.error ?: "请稍后再试")
                    }
                }
                is ApiResult.Err -> dialog = "操作失败" to r.message
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("孩子详情") },
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
            if (loading) {
                LoadingBox(text = "正在加载孩子信息…")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp)
                ) {
                    // 学习报告
                    val rp = report
                    if (rp != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "本周学习摘要",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ReportStat("学习时长", "${rp.studyMinutes}分")
                                    ReportStat("做题", "${rp.questionCount}题")
                                    ReportStat("正确率", "${rp.correctRate}%")
                                    ReportStat("错题", "${rp.mistakeCount}")
                                }
                                if (rp.subjects.isNotEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    rp.subjects.take(4).forEach { s ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = s.subject.ifBlank { "未分类" },
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "${s.correctRate}%",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = SuccessGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // 使用限制
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "使用限制",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "每日使用时长：$dailyMinutes 分钟",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Slider(
                                value = dailyMinutes.toFloat(),
                                onValueChange = { dailyMinutes = it.roundToInt() },
                                valueRange = 30f..300f,
                                steps = 8
                            )

                            Text(
                                text = "连续使用 $restEvery 分钟后强制休息",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Slider(
                                value = restEvery.toFloat(),
                                onValueChange = { restEvery = it.roundToInt() },
                                valueRange = 20f..120f,
                                steps = 4
                            )

                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = nightStart,
                                    onValueChange = { nightStart = it },
                                    label = { Text("夜间锁定开始") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = nightEnd,
                                    onValueChange = { nightEnd = it },
                                    label = { Text("夜间锁定结束") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(Modifier.height(8.dp))
                            SwitchRow(
                                title = "允许使用商城",
                                subtitle = "关闭后孩子无法用天币消费",
                                checked = allowStore,
                                onCheckedChange = { allowStore = it }
                            )
                            SwitchRow(
                                title = "允许使用 AI",
                                subtitle = "关闭后孩子无法进入 AI 助手",
                                checked = allowAi,
                                onCheckedChange = { allowAi = it }
                            )
                        }
                    }

                    if (error != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = { save() },
                        enabled = !saving,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(if (saving) "保存中…" else "保存限制")
                    }

                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { confirmUnbind = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("解除家庭绑定", color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }

    if (confirmUnbind) {
        ConfirmDialog(
            title = "解除绑定",
            message = "解除后你将不再收到该孩子的学习报告，也无法管理其使用限制。确定继续吗？",
            confirmText = "解除",
            onDismiss = { confirmUnbind = false },
            onConfirm = {
                confirmUnbind = false
                unbind()
            }
        )
    }

    dialog?.let { (title, message) ->
        MessageDialog(
            title = title,
            message = message,
            onDismiss = {
                dialog = null
                if (title.startsWith("已解除")) navController.popBackStack()
            }
        )
    }
}

@Composable
private fun ReportStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = BrandBlue
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
