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
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.tiantian.school.data.local.AppPrefs
import com.tiantian.school.data.model.StoreItem
import com.tiantian.school.data.remote.ApiClient
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.ErrorCode
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.EmptyBox
import com.tiantian.school.ui.components.ErrorBox
import com.tiantian.school.ui.components.LoadingBox
import com.tiantian.school.ui.components.MessageDialog
import com.tiantian.school.ui.components.NetworkImage
import com.tiantian.school.ui.theme.GoldYellow
import com.tiantian.school.vm.AppState
import kotlinx.coroutines.launch

@Composable
fun StoreTab() {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<StoreItem>>(emptyList()) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var buyingId by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(refreshKey) {
        loading = true
        error = null
        when (val r = TiantianRepository().storeItems()) {
            is ApiResult.Ok -> items = r.data.items
            is ApiResult.Err -> error = r.message
        }
        loading = false
    }

    fun buy(item: StoreItem) {
        val name = AppState.username
        if (name.isBlank()) {
            dialog = "请先登录" to "登录后才能购买商品"
            return
        }
        buyingId = item.id
        scope.launch {
            when (val r = TiantianRepository().buy(name, item.id)) {
                is ApiResult.Ok -> {
                    val res = r.data
                    dialog = when {
                        res.success -> "购买成功" to "「${item.name}」已放入你的仓库"
                        res.code == ErrorCode.NO_COIN ->
                            "天币不足" to "哎呀哈，天币为零！快去努力获得或者赶快登录你的账号吧！"
                        res.code == ErrorCode.OWNED -> "已经买过啦" to "该商品只能购买一次"
                        res.code == ErrorCode.NO_LOGIN -> "请先登录" to "登录后才能购买商品"
                        else -> "购买失败" to (res.error ?: "请稍后再试")
                    }
                }
                is ApiResult.Err -> {
                    dialog = if (r.unauthorized) {
                        "登录已过期" to "请重新登录后再试"
                    } else {
                        "购买失败" to r.message
                    }
                }
            }
            buyingId = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> LoadingBox(text = "正在加载商品…")
            error != null -> ErrorBox(message = error.orEmpty()) { refreshKey++ }
            items.isEmpty() -> EmptyBox(
                message = "商城暂时没有上架商品",
                hint = "过段时间再来看看吧"
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "天天商城",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "用天币兑换徽章、会员与学习道具",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(items, key = { it.id }) { item ->
                    StoreItemCard(
                        item = item,
                        buying = buyingId == item.id,
                        onBuy = { buy(item) }
                    )
                }
            }
        }
    }

    dialog?.let { (title, message) ->
        MessageDialog(
            title = title,
            message = message,
            onDismiss = { dialog = null }
        )
    }
}

@Composable
private fun StoreItemCard(
    item: StoreItem,
    buying: Boolean,
    onBuy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkImage(
                url = resolveAssetUrl(item.icon),
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (item.kind == "badge") {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GoldYellow.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "徽章",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color2Gold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (item.desc.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.MonetizationOn,
                        contentDescription = null,
                        tint = GoldYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${item.price} 天币",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!item.repeatable) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "限购一次",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onBuy,
                enabled = !buying,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ShoppingBag,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (buying) "购买中" else "购买")
            }
        }
    }
}

private val Color2Gold = androidx.compose.ui.graphics.Color(0xFF9A6B00)

/** 把商品图标相对路径补全为完整地址。 */
private fun resolveAssetUrl(path: String): String {
    if (path.isBlank()) return ""
    if (path.startsWith("http", true)) return path
    return ApiClient.normalizeBaseUrl(AppPrefs.baseUrl).trimEnd('/') +
        "/" + path.trimStart('/')
}
