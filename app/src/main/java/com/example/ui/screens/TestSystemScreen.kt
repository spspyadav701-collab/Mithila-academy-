package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TestEntity
import com.example.data.local.TestQuestionEntity
import com.example.ui.ActiveTestState
import com.example.ui.AiTeacherViewModel
import com.example.ui.AppTab
import com.example.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSystemScreen(
    viewModel: AiTeacherViewModel,
    isFreeOnly: Boolean = false,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTests by if (isFreeOnly) viewModel.freeTests.collectAsState() else viewModel.allTests.collectAsState()
    val activeTestState by viewModel.activeTestState.collectAsState()

    var selectedSubject by remember { mutableStateOf("All") }
    val subjects = listOf("All", "Spoken English", "Physics", "Mathematics", "BPSC")

    val filteredTests = remember(allTests, selectedSubject) {
        if (selectedSubject == "All") allTests
        else allTests.filter { it.subject.equals(selectedSubject, ignoreCase = true) }
    }

    if (activeTestState != null) {
        ActiveTestRunnerScreen(
            state = activeTestState!!,
            viewModel = viewModel,
            onClose = { viewModel.closeTest() }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isFreeOnly) "Free Mock Tests" else "Online Test Series",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color(0xFFF7FAFD),
            modifier = modifier.testTag("test_system_screen")
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Subject Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subjects) { subj ->
                        val isSelected = selectedSubject == subj
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubject = subj },
                            label = {
                                Text(
                                    text = subj,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                if (filteredTests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tests available for $selectedSubject",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredTests, key = { it.testId }) { test ->
                            TestCardItem(
                                test = test,
                                onStartTest = { viewModel.startTest(test) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestCardItem(
    test: TestEntity,
    onStartTest: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStartTest)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEBF3FE)
                ) {
                    Text(
                        text = test.subject,
                        color = BluePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (test.isFree) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (test.isFree) "FREE" else "PRO",
                        color = if (test.isFree) Color(0xFF16A34A) else Color(0xFFD97706),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = test.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱️ ${test.durationMinutes} Mins",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "📝 ${test.totalQuestions} Questions",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "🏆 ${test.totalMarks} Marks",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👥 ${test.attemptsCount}+ Attempts",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Button(
                    onClick = onStartTest,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Start Test", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTestRunnerScreen(
    state: ActiveTestState,
    viewModel: AiTeacherViewModel,
    onClose: () -> Unit
) {
    val questions = state.questions
    val currentIndex = state.currentQuestionIndex
    val currentQuestion = questions.getOrNull(currentIndex)
    val userAnswers = state.userAnswers
    val isSubmitted = state.isSubmitted
    val result = state.result

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isSubmitted) "Test Report & Solutions" else state.test.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close Test")
                    }
                },
                actions = {
                    if (!isSubmitted) {
                        // Timer Indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.test.durationMinutes}:00",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7FAFD)
    ) { innerPadding ->
        if (isSubmitted && result != null) {
            // --- RESULT SCORECARD & SOLUTIONS ---
            val resultScroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(resultScroll)
                    .padding(16.dp)
            ) {
                // Scorecard Banner
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (result.percentage >= 60) "🎉 Great Job!" else "Keep Practicing!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (result.percentage >= 60) Color(0xFF16A34A) else Color(0xFFEA580C)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Score: ${result.score} / ${state.test.totalMarks}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )

                        Text(
                            text = "Accuracy: ${result.percentage}%",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatPill("✅ Correct", "${result.correctCount}", Color(0xFF16A34A), Color(0xFFDCFCE7))
                            StatPill("❌ Incorrect", "${result.incorrectCount}", Color(0xFFDC2626), Color(0xFFFEE2E2))
                            StatPill("📝 Total", "${result.totalQuestions}", Color(0xFF1E293B), Color(0xFFF1F5F9))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Question-by-Question Review",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // List each question with solution explanation
                questions.forEachIndexed { idx, q ->
                    val userAns = userAnswers[idx]
                    val isCorrect = userAns == q.correctOption

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Q${idx + 1}.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = q.questionText,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCorrect) "✅ +10" else "❌ 0",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isCorrect) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                            options.forEachIndexed { optIdx, optText ->
                                val isChosen = userAns == optIdx
                                val isAnswerKey = q.correctOption == optIdx

                                val bgColor = when {
                                    isAnswerKey -> Color(0xFFDCFCE7)
                                    isChosen && !isCorrect -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFF8FAFC)
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = bgColor,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${('A' + optIdx)}. $optText",
                                            fontSize = 12.sp,
                                            fontWeight = if (isAnswerKey || isChosen) FontWeight.Bold else FontWeight.Normal,
                                            color = Color(0xFF0F172A)
                                        )
                                        if (isAnswerKey) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text("(Correct Answer)", fontSize = 10.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                        } else if (isChosen) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text("(Your Answer)", fontSize = 10.sp, color = Color(0xFFDC2626))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Explanation Box
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "💡 Explanation: ${q.explanation}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Finish & Return", fontWeight = FontWeight.Bold)
                }
            }
        } else if (currentQuestion != null) {
            // --- LIVE QUESTION RUNNER ---
            val testScroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(testScroll)
                    .padding(16.dp)
            ) {
                // Question progress tracker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${currentIndex + 1} of ${questions.size}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )

                    Text(
                        text = "${userAnswers.size} Answered",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = BluePrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Question Palette Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(questions.indices.toList()) { qIdx ->
                        val isAnswered = userAnswers.containsKey(qIdx)
                        val isCurrent = qIdx == currentIndex
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isCurrent -> BluePrimary
                                isAnswered -> Color(0xFFDCFCE7)
                                else -> Color(0xFFE2E8F0)
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { viewModel.setTestQuestionIndex(qIdx) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${qIdx + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Color.White else if (isAnswered) Color(0xFF16A34A) else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = currentQuestion.questionText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        val options = listOf(
                            currentQuestion.optionA,
                            currentQuestion.optionB,
                            currentQuestion.optionC,
                            currentQuestion.optionD
                        )

                        val selectedOption = userAnswers[currentIndex]

                        options.forEachIndexed { optIdx, optText ->
                            val isSelected = selectedOption == optIdx

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color(0xFFE8F1FC) else Color(0xFFF8FAFC),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) BluePrimary else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        viewModel.selectTestAnswer(currentIndex, optIdx)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) BluePrimary else Color(0xFFE2E8F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${('A' + optIdx)}",
                                            color = if (isSelected) Color.White else Color(0xFF475569),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = optText,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        if (selectedOption != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { viewModel.clearCurrentQuestionAnswer(currentIndex) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Clear Selection", fontSize = 11.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentIndex > 0) {
                        OutlinedButton(
                            onClick = { viewModel.setTestQuestionIndex(currentIndex - 1) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Previous")
                        }
                    }

                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = { viewModel.setTestQuestionIndex(currentIndex + 1) },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Next Question", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.submitTest() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Submit Test 🚀", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String, textColor: Color, bgColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}
