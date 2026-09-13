package com.tiantian.school.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tiantian.school.data.local.AppPrefs
import com.tiantian.school.data.model.AccountRegisterRequest
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.nav.Routes
import com.tiantian.school.vm.AppState
import com.tiantian.school.AppRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("undisclosed") }
    var eduSystem by rememberSaveable { mutableStateOf("63") }
    var grade by rememberSaveable { mutableStateOf(1) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successTip by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        if (username.trim().length < 2) return "用户名至少 2 个字符"
        if (password.length < 8) return "密码至少 8 位"
        if (phone.trim().length < 6) return "请填写有效的手机号"
        return null
    }

    fun doRegister() {
        validate()?.let { error = it; return }
        loading = true
        error = null
        scope.launch {
            val repo = TiantianRepository()
            val body = AccountRegisterRequest(
                username = username.trim(),
                password = password,
                accountType = if (AppRole.isParent) "parent" else "child",
                phone = phone.trim(),
                email = email.trim().ifBlank { null },
                nickname = nickname.trim().ifBlank { null },
                gender = gender,
                eduSystem = eduSystem,
                grade = grade,
                deviceId = AppPrefs.deviceId,
                deviceName = AppPrefs.deviceName,
                platform = "android"
            )
            when (val r = repo.accountsRegister(body)) {
                is ApiResult.Ok -> {
                    val res = r.data
                    val token = res.token
                    if (res.success && !token.isNullOrBlank()) {
                        // 服务端直接返回登录态：自动进入主页
                        AppPrefs.saveSession(
                            token = token,
                            expiresInSeconds = res.expiresIn,
                            user = res.user?.username ?: username.trim(),
                            nick = res.user?.nickname ?: nickname.trim()
                        )
                        AppState.updateUser(res.user)
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else if (res.success) {
                        successTip = "注册成功，请使用新账号登录"
                    } else {
                        error = res.error ?: "注册失败"
                    }
                }
                is ApiResult.Err -> {
                    error = when (r.httpCode) {
                        429 -> "注册过于频繁，请稍后再试"
                        else -> r.message
                    }
                }
            }
            loading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                }
                Text(
                    text = if (AppRole.isParent) "注册家长账号" else "注册孩子账号",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名 *") },
                    supportingText = { Text("2-20 位，支持中英文、数字、下划线") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码 *") },
                    supportingText = { Text("至少 8 位") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("手机号 *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱（选填）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("昵称（选填，最多 3 人重复）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                SelectField(
                    label = "性别",
                    options = listOf(
                        "undisclosed" to "保密",
                        "male" to "男",
                        "female" to "女"
                    ),
                    selected = gender,
                    onSelect = { gender = it }
                )
                Spacer(Modifier.height(10.dp))
                SelectField(
                    label = "学制",
                    options = listOf(
                        "63" to "六三制（小学 6 年 + 初中 3 年）",
                        "54" to "五四制（小学 5 年 + 初中 4 年）"
                    ),
                    selected = eduSystem,
                    onSelect = { eduSystem = it }
                )
                Spacer(Modifier.height(10.dp))
                SelectField(
                    label = "年级",
                    options = (1..9).map { it to "$it 年级" },
                    selected = grade,
                    onSelect = { grade = it }
                )

                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (successTip != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = successTip.orEmpty(),
                        color = com.tiantian.school.ui.theme.SuccessGreen,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { doRegister() },
                    enabled = !loading,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(if (loading) "提交中…" else "注 册")
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "已有账号？",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("去登录")
                    }
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun <T> SelectField(
    label: String,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: ""

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )
        // 透明点击层，避免只读输入框吞掉点击
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
