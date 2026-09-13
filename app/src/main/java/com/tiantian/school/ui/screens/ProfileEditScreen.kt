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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tiantian.school.data.model.UpdateUserRequest
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.MessageDialog
import com.tiantian.school.vm.AppState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val user = AppState.user

    var nickname by remember { mutableStateOf(user?.nickname.orEmpty()) }
    var email by remember { mutableStateOf(user?.email.orEmpty()) }
    var school by remember { mutableStateOf(user?.school.orEmpty()) }
    var phone by remember { mutableStateOf(user?.phone.orEmpty()) }
    var gender by remember { mutableStateOf(user?.gender ?: "undisclosed") }
    var eduSystem by remember { mutableStateOf(user?.eduSystem ?: "63") }
    var grade by remember { mutableStateOf(user?.grade ?: 1) }

    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    fun save() {
        val name = AppState.username
        if (name.isBlank()) {
            error = "请先登录"
            return
        }
        saving = true
        error = null
        scope.launch {
            val body = UpdateUserRequest(
                nickname = nickname.trim().ifBlank { null },
                phone = phone.trim().ifBlank { null },
                email = email.trim().ifBlank { null },
                school = school.trim().ifBlank { null },
                gender = gender,
                eduSystem = eduSystem,
                grade = grade
            )
            when (val r = TiantianRepository().updateUser(name, body)) {
                is ApiResult.Ok -> {
                    if (r.data.success) {
                        // 本地同步一份，避免再拉一次
                        AppState.patchUser {
                            it.copy(
                                nickname = nickname.trim().ifBlank { it.nickname },
                                email = email.trim().ifBlank { null },
                                phone = phone.trim().ifBlank { null },
                                school = school.trim().ifBlank { null },
                                gender = gender,
                                eduSystem = eduSystem,
                                grade = grade
                            )
                        }
                        dialog = "保存成功" to "资料已更新"
                    } else {
                        error = r.data.error ?: "保存失败"
                    }
                }
                is ApiResult.Err -> error = r.message
            }
            saving = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("编辑资料") },
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
                OutlinedTextField(
                    value = AppState.username,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("用户名（不可修改）") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("昵称") },
                    supportingText = { Text("昵称最多被 3 位用户使用") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("手机号") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("学校") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Text("性别", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "undisclosed" to "保密",
                        "male" to "男",
                        "female" to "女"
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = gender == value,
                            onClick = { gender = value },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("学制", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("63" to "六三制", "54" to "五四制").forEach { (value, label) ->
                        FilterChip(
                            selected = eduSystem == value,
                            onClick = { eduSystem = value },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("年级", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..9).chunked(5).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { g ->
                                FilterChip(
                                    selected = grade == g,
                                    onClick = { grade = g },
                                    label = { Text("$g") }
                                )
                            }
                        }
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(22.dp))
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
                    Text(if (saving) "保存中…" else "保存资料")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    dialog?.let { (title, message) ->
        MessageDialog(
            title = title,
            message = message,
            onDismiss = {
                dialog = null
                navController.popBackStack()
            }
        )
    }
}
