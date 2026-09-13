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
import androidx.navigation.NavHostController
import com.tiantian.school.data.local.AppPrefs
import com.tiantian.school.data.model.InventoryItem
import com.tiantian.school.data.remote.ApiClient
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.EmptyBox
import com.tiantian.school.ui.components.ErrorBox
import com.tiantian.school.ui.components.LoadingBox
import com.tiantian.school.ui.components.MessageDialog
import com.tiantian.school.ui.components.NetworkImage
import com.tiantian.school.vm.AppState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<InventoryItem>>(emptyList()) }
    var equipped by remember { mutableStateOf(AppState.equippedBadge) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var busyId by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(refreshKey) {
        loading = true
        error = null
        val name = AppState.username
        if (name.isBlank()) {
            error = "请先登录"
        } else {
            when (val r = TiantianRepository().inventory(name)) {
                is ApiResult.Ok -> items = r.data.inventory
                is ApiResult.Err -> error = r.message
            }
        }
        loading = false
    }

    fun toggleEquip(item: InventoryItem) {
        val name = AppState.username
        if (name.isBlank()) return
        // 已佩戴则取下（传空字符串）
        val targetId = if (equipped == item.itemId) "" else item.itemId
        busyId = item.itemId
        scope.launch {
            when (val r = TiantianRepository().equipBadge(name, targetId)) {
                is ApiResult.Ok -> {
                    if (r.data.success) {
                        equipped = targetId
                        AppState.patchUser { it.copy(equippedBadge = targetId) }
                    } else {
                        dialog = "操作失败" to (r.data.error ?: "请稍后再试")
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
                title = { Text("我的仓库") },
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
                loading -> LoadingBox(text = "正在加载仓库…")
                error != null -> ErrorBox(message = error.orEmpty()) { refreshKey++ }
                items.isEmpty() -> EmptyBox(
                    message = "仓库还是空的",
                    hint = "去商城用天币兑换心仪的徽章和道具吧"
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.itemId + it.boughtAt }) { item ->
                        InventoryRow(
                            item = item,
                            equipped = equipped == item.itemId,
                            busy = busyId == item.itemId,
                            onToggle = { toggleEquip(item) }
                        )
                    }
                }
            }
        }
    }

    dialog?.let { (title, message) ->
        MessageDialog(title = title, message = message, onDismiss = { dialog = null })
    }
}

@Composable
private fun InventoryRow(
    item: InventoryItem,
    equipped: Boolean,
    busy: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkImage(
                url = resolveIcon(item.icon),
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name.ifBlank { "未命名物品" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (item.desc.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = item.desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.boughtAt.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "购买时间：${item.boughtAt}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            if (item.kind == "badge") {
                if (equipped) {
                    Button(
                        onClick = onToggle,
                        enabled = !busy,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (busy) "处理中" else "已佩戴")
                    }
                } else {
                    OutlinedButton(
                        onClick = onToggle,
                        enabled = !busy,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (busy) "处理中" else "佩戴")
                    }
                }
            } else {
                Text(
                    text = "已拥有",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun resolveIcon(path: String): String {
    if (path.isBlank()) return ""
    if (path.startsWith("http", true)) return path
    return ApiClient.normalizeBaseUrl(AppPrefs.baseUrl).trimEnd('/') +
        "/" + path.trimStart('/')
}
