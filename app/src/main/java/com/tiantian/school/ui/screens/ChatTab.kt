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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiantian.school.data.model.ChatMsg
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.theme.BrandBlue
import com.tiantian.school.vm.AppState
import kotlinx.coroutines.launch

private data class ChatBubble(
    val role: String,
    val content: String
)

@Composable
fun ChatTab() {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            ChatBubble(
                role = "assistant",
                content = "你好呀！我是天天 AI 助手 👋\n有不会的题目、想听的知识点，都可以问我。"
            )
        )
    }
    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }

    fun send() {
        val text = input.trim()
        if (text.isEmpty() || sending) return

        val history = messages
            .filter { it.role == "user" || it.role == "assistant" }
            .map { ChatMsg(role = it.role, content = it.content) }

        messages.add(ChatBubble(role = "user", content = text))
        input = ""
        sending = true
        scope.launch {
            val result = TiantianRepository().chat(
                username = AppState.username,
                message = text,
                history = history
            )
            val reply = when (result) {
                is ApiResult.Ok -> {
                    val body = result.data
                    if (body.success && !body.reply.isNullOrBlank()) {
                        body.reply
                    } else {
                        body.error ?: "AI 暂时没有给出回答，请稍后再试"
                    }
                }
                is ApiResult.Err -> "请求失败：${result.message}"
            }
            messages.add(ChatBubble(role = "assistant", content = reply))
            sending = false
        }
    }

    LaunchedEffect(messages.size, sending) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // 标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = BrandBlue
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = "AI 学习助手",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "本地大模型推理，回答可能需要几十秒",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { bubble -> MessageBubble(bubble) }
            if (sending) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "天天正在思考…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 输入栏
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("输入你的问题…") },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (sending || input.isBlank()) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        BrandBlue
                    }
                ) {
                    IconButton(
                        onClick = { send() },
                        enabled = !sending && input.isNotBlank(),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "发送",
                            tint = if (sending || input.isBlank()) {
                                MaterialTheme.colorScheme.outline
                            } else {
                                Color.White
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(bubble: ChatBubble) {
    val isUser = bubble.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) BrandBlue else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Text(
                text = bubble.content,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
