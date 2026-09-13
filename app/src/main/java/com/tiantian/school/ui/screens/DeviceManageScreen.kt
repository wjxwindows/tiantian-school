package com.tiantian.school.ui.screens

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
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import com.tiantian.school.data.local.AppPrefs
import com.tiantian.school.data.model.DeviceItem
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.ConfirmDialog
import com.tiantian.school.ui.components.EmptyBox
import com.tiantian.school.ui.components.ErrorBox
import com.tiantian.school.ui.components.LoadingBox
import com.tiantian.school.ui.components.MessageDialog
import com.tiantian.school.ui.nav.Routes
import com.tiantian.school.ui.theme.BrandBlue
import com.tiantian.school.ui.theme.GoldYellow
import com.tiantian.school.ui.theme.SuccessGreen
import com.tiantian.school.vm.AppState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceManageScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var devices by remember { mutableStateOf<List<DeviceItem>>(emptyList()) }
    var currentDeviceId by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var busyId by remember { mutableStateOf<String?>(null) }

    var renameTarget by remember { mutableStateOf<DeviceItem?>(null) }
    var renameText by remember { mutableStateOf("") }
    var revokeTarget by remember { mutableStateOf<DeviceItem?>(null) }
    var confirmRevokeOthers by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    val myDeviceId = AppPrefs.deviceId

    LaunchedEffect(refreshKey) {
        loading = true
        error = null
        val username = AppState.username
        if (username.isBlank()) {
            error = "请先登录"
        } else {
            when (val r = TiantianRepository().devices(username)) {
                is ApiResult.Ok -> {
                    devices = r.data.devices
                    currentDeviceId = r.data.currentDeviceId
                }
                is ApiResult.Err -> error = r.message
            }
        }
        loading = false
    }

    fun logoutToLogin(message: String?) {
        AppState.logout()
        message?.let { dialog = "已退出登录" to it }
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun doRename(device: DeviceItem, newName: String) {
        val username = AppState.username
        val targetId = device.stableId()
        if (username.isBlank() || targetId.isBlank()) return
        busyId = targetId
        scope.launch {
            when (val r = TiantianRepository().renameDevice(username, targetId, newName)) {
                is ApiResult.Ok -> {
                    if (r.data.success) {
                        // 如果改的是本机，同步本地名称，后续登录会带上
                        if (targetId == myDeviceId) AppPrefs.deviceName = newName
                        refreshKey++
                    } else {
                        dialog = "重命名失败" to "请稍后再试"
                    }
                }
                is ApiResult.Err -> dialog = "重命名失败" to r.message
            }
            busyId = null
        }
    }

    fun doRevoke(device: DeviceItem) {
        val username = AppState.username
        val targetId = device.stableId()
        if (username.isBlank() || targetId.isBlank()) return
        busyId = targetId
        scope.launch {
            when (val r = TiantianRepository().revokeDevice(username, targetId)) {
                is ApiResult.Ok -> {
                    val res = r.data
                    if (res.success) {
                        val isSelf = res.current == true || targetId == myDeviceId
                        if (isSelf) {
                            logoutToLogin("当前设备已退出登录")
                        } else {
                            dialog = "已退出" to "「${device.displayName()}」已被下线"
                            refreshKey++
                        }
                    } else {
                        dialog = "操作失败" to (res.error ?: "请稍后再试")
                    }
                }
                is ApiResult.Err -> dialog = "操作失败" to r.message
            }
            busyId = null
        }
    }

    fun doRevokeOthers() {
        val username = AppState.username
        if (username.isBlank()) return
        busyId = "__others__"
        scope.launch {
            when (val r = TiantianRepository().revokeOtherDevices(username)) {
                is ApiResult.Ok -> {
                    if (r.data.success) {
                        dialog = "已完成" to "已退出其他 ${r.data.removed} 台设备"
                        refreshKey++
                    } else {
                        dialog = "操作失败" to "请稍后再试"
                    }
                }
                is ApiResult.Err -> dialog = "操作失败" to r.message
            }
            busyId = null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("登录设备管理") },
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
                loading -> LoadingBox(text = "正在获取登录设备…")
                error != null -> ErrorBox(message = error.orEmpty()) { refreshKey++ }
                devices.isEmpty() -> EmptyBox(
                    message = "没有查询到登录设备",
                    hint = "该账号当前没有活跃会话"
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Devices,
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "同一账号可在多台设备同时登录，各自独立。下线其他设备不会影响本机。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(devices, key = { it.stableId() + it.createdAt }) { device ->
                        DeviceRow(
                            device = device,
                            busy = busyId == device.stableId(),
                            onRename = {
                                renameTarget = device
                                renameText = device.displayName()
                            },
                            onRevoke = { revokeTarget = device }
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = { confirmRevokeOthers = true },
                            enabled = busyId == null && devices.size > 1,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("退出其他全部设备")
                        }
                    }

                    item {
                        Text(
                            text = "本机设备 ID：${AppPrefs.deviceId}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }

    // 重命名对话框
    renameTarget?.let { device ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("重命名设备") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("设备名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = renameText.trim()
                        if (name.isNotEmpty()) {
                            doRename(device, name)
                            renameTarget = null
                        }
                    }
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("取消") }
            }
        )
    }

    // 退出单设备确认
    revokeTarget?.let { device ->
        val isSelf = device.stableId() == myDeviceId || device.current
        ConfirmDialog(
            title = if (isSelf) "退出当前设备" else "退出该设备",
            message = if (isSelf) {
                "退出后本机将返回登录页，需要重新登录。"
            } else {
                "「${device.displayName()}」将立即失去登录状态。"
            },
            confirmText = "退出",
            onDismiss = { revokeTarget = null },
            onConfirm = {
                revokeTarget = null
                doRevoke(device)
            }
        )
    }

    if (confirmRevokeOthers) {
        ConfirmDialog(
            title = "退出其他全部设备",
            message = "除本机外的所有登录设备都会被下线，确定继续吗？",
            confirmText = "全部退出",
            onDismiss = { confirmRevokeOthers = false },
            onConfirm = {
                confirmRevokeOthers = false
                doRevokeOthers()
            }
        )
    }

    dialog?.let { (title, message) ->
        MessageDialog(title = title, message = message, onDismiss = { dialog = null })
    }
}

@Composable
private fun DeviceRow(
    device: DeviceItem,
    busy: Boolean,
    onRename: () -> Unit,
    onRevoke: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (device.current) 3.dp else 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (device.platform == "android") {
                        Icons.Rounded.PhoneAndroid
                    } else {
                        Icons.Rounded.Public
                    },
                    contentDescription = null,
                    tint = if (device.current) BrandBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = device.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (device.current) {
                    TagChip(text = "本机", color = SuccessGreen)
                }
                if (device.admin) {
                    Spacer(Modifier.width(6.dp))
                    TagChip(text = "管理员", color = GoldYellow)
                }
            }

            Spacer(Modifier.height(8.dp))

            val meta = buildString {
                append(if (device.platform == "android") "Android" else device.platform ?: "未知平台")
                device.ip?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
            }
            Text(
                text = meta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "最近活跃：${prettyTime(device.lastSeenAt)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onRename,
                    enabled = !busy,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("重命名")
                }
                OutlinedButton(
                    onClick = onRevoke,
                    enabled = !busy,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(4.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(if (device.current) "退出本机" else "退出")
                }
            }
        }
    }
}

@Composable
private fun TagChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.16f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

/** 把 ISO 时间转成本地时区的可读文本。 */
private fun prettyTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "未知"
    return runCatching {
        val instant = java.time.Instant.parse(raw)
        instant.atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }.getOrElse { raw }
}
