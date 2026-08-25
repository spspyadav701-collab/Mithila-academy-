package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CourseEntity
import com.example.ui.AiTeacherViewModel
import com.example.ui.AppTab
import com.example.ui.components.StudentProgressTrackerCard
import com.example.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    viewModel: AiTeacherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enrolledCourses by viewModel.enrolledCourses.collectAsState()
    val studentProgress by viewModel.studentSubjectProgress.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Courses & Progress",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF7FAFD),
        modifier = modifier.testTag("my_courses_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Visual Learning Progress Tracker Card
            item {
                StudentProgressTrackerCard(
                    progressList = studentProgress,
                    onSubjectClick = { subject ->
                        viewModel.setSelectedSubject(subject.subjectName)
                    },
                    onMarkCompleted = { subjectId ->
                        viewModel.markModuleCompleted(subjectId)
                    },
                    onAskAiDoubt = { query ->
                        viewModel.setTab(AppTab.AI_CHAT)
                        viewModel.sendUserMessage(query)
                    }
                )
            }

            item {
                Text(
                    text = "Enrolled Courses",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            if (enrolledCourses.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "You have not enrolled in any full batch yet.",
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.setTab(AppTab.ALL_COURSES) },
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Browse All Courses", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(enrolledCourses, key = { it.courseId }) { course ->
                    EnrolledCourseCard(
                        course = course,
                        onContinue = { viewModel.openCourseDetail(course) }
                    )
                }
            }
        }
    }
}

@Composable
fun EnrolledCourseCard(
    course: CourseEntity,
    onContinue: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onContinue)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEBF3FE)
                ) {
                    Text(
                        text = course.category,
                        color = BluePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${course.progressPercent}% Completed",
                    color = BluePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = course.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Teacher: ${course.teacherName}",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { course.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = BluePrimary,
                trackColor = Color(0xFFE2E8F0),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${course.completedLessons}/${course.lessonsCount} lessons finished",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Continue", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
