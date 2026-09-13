package com.tiantian.school.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.MessageDialog
import com.tiantian.school.ui.theme.BrandBlue
import com.tiantian.school.ui.theme.DangerRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BindParentScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successDialog by remember { mutableStateOf(false) }

    fun submit() {
        val code = pin.trim()
        if (code.length < 4) {
            error = "请输入家长端显示的配对码"
            return
        }
        error = null
        submitting = true
        scope.launch {
            when (val r = TiantianRepository().familyBind(pin = code)) {
                is ApiResult.Ok -> {
                    val res = r.data
                    if (res.success) {
                        successDialog = true
                    } else {
                        error = res.error ?: when (res.code) {
                            "pin_expired" -> "配对码已过期，请让家长重新生成"
                            "pin_used" -> "配对码已被使用"
                            "pin_invalid" -> "配对码不正确"
                            "pin_locked" -> "错误次数过多，请稍后再试"
                            else -> "绑定失败，请检查配对码"
                        }
                    }
                }
                is ApiResult.Err -> error = r.message
            }
            submitting = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("绑定家长") },
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                Icon(
                    imageVector = Icons.Rounded.FamilyRestroom,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = BrandBlue
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "输入家长端的配对码",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "让家长打开「家长端 → 添加孩子」生成配对码，在这里输入即可完成绑定。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { input ->
                        pin = input.filter { it.isLetterOrDigit() }.take(12)
                    },
                    label = { Text("配对码") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        letterSpacing = 6.sp,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DangerRed.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error.orEmpty(),
                            color = DangerRed,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                Button(
                    onClick = { submit() },
                    enabled = !submitting,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(if (submitting) "绑定中…" else "确认绑定")
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "绑定后家长可以查看你的学习报告并设置使用时长。",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (successDialog) {
        MessageDialog(
            title = "绑定成功",
            message = "已成功绑定家长账号，现在可以开始学习啦！",
            confirmText = "开始学习",
            onDismiss = {
                successDialog = false
                navController.popBackStack()
            }
        )
    }
}
