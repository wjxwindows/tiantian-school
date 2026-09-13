package com.tiantian.school.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tiantian.school.data.model.Paper
import com.tiantian.school.data.model.Question
import com.tiantian.school.data.repo.ApiResult
import com.tiantian.school.data.repo.TiantianRepository
import com.tiantian.school.ui.components.ErrorBox
import com.tiantian.school.ui.components.LoadingBox
import com.tiantian.school.ui.theme.DangerRed
import com.tiantian.school.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperDetailScreen(
    navController: NavHostController,
    code: String
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var paper by remember { mutableStateOf<Paper?>(null) }
    val answers = remember { mutableStateMapOf<String, String>() }
    var submitted by remember { mutableStateOf(false) }

    LaunchedEffect(code) {
        loading = true
        error = null
        when (val r = TiantianRepository().paper(code)) {
            is ApiResult.Ok -> paper = r.data.paper
            is ApiResult.Err -> error = r.message
        }
        loading = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = paper?.title?.ifBlank { "试卷详情" } ?: "试卷详情",
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
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
                loading -> LoadingBox(text = "正在加载试卷…")
                error != null -> ErrorBox(message = error.orEmpty())
                paper == null -> ErrorBox(message = "没有找到这份试卷")
                else -> {
                    val current = paper!!
                    val questions = current.questions
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Column {
                                Text(
                                    text = current.title.ifBlank { "未命名试卷" },
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = buildString {
                                        if (current.subject.isNotBlank()) append(current.subject)
                                        current.grade?.let { append(" · ${it} 年级") }
                                        append(" · 共 ${questions.size} 题")
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (questions.isEmpty()) {
                            item {
                                ErrorBox(message = "这份试卷还没有题目")
                            }
                        }

                        itemsIndexed(questions, key = { index, q ->
                            q.id.ifBlank { "q_$index" }
                        }) { index, question ->
                            QuestionCard(
                                index = index + 1,
                                question = question,
                                answer = answers[question.id.ifBlank { "q_$index" }].orEmpty(),
                                submitted = submitted,
                                onAnswer = { value ->
                                    answers[question.id.ifBlank { "q_$index" }] = value
                                }
                            )
                        }

                        if (questions.isNotEmpty()) {
                            item {
                                val score = if (submitted) calculateScore(current, answers) else 0
                                Column {
                                    if (submitted) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = SuccessGreen.copy(alpha = 0.12f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "本次得分：$score 分",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = SuccessGreen,
                                                modifier = Modifier.padding(16.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(12.dp))
                                    }
                                    Button(
                                        onClick = { submitted = !submitted },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    ) {
                                        Text(
                                            if (submitted) "重新作答" else "提交并查看答案",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Spacer(Modifier.height(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    index: Int,
    question: Question,
    answer: String,
    submitted: Boolean,
    onAnswer: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = question.stem.ifBlank { "（题目内容为空）" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            when (question.type) {
                "single" -> {
                    question.options.forEachIndexed { optIndex, option ->
                        val label = optionLabel(optIndex)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !submitted) { onAnswer(label) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = answer == label,
                                onClick = { if (!submitted) onAnswer(label) }
                            )
                            Text(
                                text = "$label. $option",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                "multiple" -> {
                    question.options.forEachIndexed { optIndex, option ->
                        val label = optionLabel(optIndex)
                        val picked = answer.contains(label)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !submitted) {
                                    val set = answer.toCharArray().filter { it.isLetter() }.toMutableSet()
                                    if (picked) set.remove(label.first()) else set.add(label.first())
                                    onAnswer(set.sorted().joinToString(""))
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = picked,
                                onCheckedChange = { checked ->
                                    if (submitted) return@Checkbox
                                    val set = answer.toCharArray().filter { it.isLetter() }.toMutableSet()
                                    if (checked) set.add(label.first()) else set.remove(label.first())
                                    onAnswer(set.sorted().joinToString(""))
                                }
                            )
                            Text(
                                text = "$label. $option",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                "judge" -> {
                    listOf("T" to "正确", "F" to "错误").forEach { (value, text) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !submitted) { onAnswer(value) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = answer == value,
                                onClick = { if (!submitted) onAnswer(value) }
                            )
                            Text(text = text, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                else -> {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { if (!submitted) onAnswer(it) },
                        enabled = !submitted,
                        placeholder = { Text("在这里作答…") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (submitted) {
                Spacer(Modifier.height(12.dp))
                val correct = question.answer.orEmpty()
                val isRight = isAnswerCorrect(question, answer)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isRight) {
                        SuccessGreen.copy(alpha = 0.12f)
                    } else {
                        DangerRed.copy(alpha = 0.10f)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isRight) "✓ 回答正确" else "✗ 回答错误",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isRight) SuccessGreen else DangerRed
                        )
                        if (correct.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "参考答案：$correct",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        val analysis = question.analysis
                        if (!analysis.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "解析：$analysis",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun optionLabel(index: Int): String =
    ('A' + index).toString()

private fun normalize(text: String): String =
    text.trim().lowercase().replace(" ", "").replace("，", ",").replace("。", ".")

private fun isAnswerCorrect(question: Question, answer: String): Boolean {
    val expected = question.answer.orEmpty()
    if (expected.isBlank()) return false
    return when (question.type) {
        "single", "judge" -> normalize(answer) == normalize(expected)
        "multiple" -> {
            val a = normalize(answer).toCharArray().filter { it.isLetter() }.sorted()
            val b = normalize(expected).toCharArray().filter { it.isLetter() }.sorted()
            a == b
        }
        else -> normalize(answer).contains(normalize(expected)) ||
            normalize(expected).contains(normalize(answer)).let { it && answer.isNotBlank() }
    }
}

private fun calculateScore(
    paper: Paper,
    answers: Map<String, String>
): Int {
    if (paper.questions.isEmpty()) return 0
    val per = if (paper.questions.all { it.score != null }) {
        null
    } else {
        100 / paper.questions.size
    }
    var score = 0
    paper.questions.forEachIndexed { index, q ->
        val key = q.id.ifBlank { "q_$index" }
        if (isAnswerCorrect(q, answers[key].orEmpty())) {
            score += q.score ?: per ?: 0
        }
    }
    return score
}
