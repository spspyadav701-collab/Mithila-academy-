package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.CourseEntity
import com.example.ui.AiTeacherViewModel
import com.example.ui.AppTab
import com.example.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCoursesScreen(
    viewModel: AiTeacherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allCourses by viewModel.allCourses.collectAsState()
    val activeCourseDetail by viewModel.activeCourseDetail.collectAsState()
    val courseLessons by viewModel.courseLessons.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Spoken English", "Physics", "Mathematics", "BPSC", "Biology")

    val filteredCourses = remember(allCourses, selectedCategory) {
        if (selectedCategory == "All") allCourses
        else allCourses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "All Courses",
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
        modifier = modifier.testTag("all_courses_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = cat,
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

            if (filteredCourses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No courses found for $selectedCategory",
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
                    items(filteredCourses, key = { it.courseId }) { course ->
                        CourseCardItem(
                            course = course,
                            onOpenDetail = { viewModel.openCourseDetail(course) },
                            onEnroll = { viewModel.enrollInCourse(course.courseId) }
                        )
                    }
                }
            }
        }
    }

    // Course Detail Modal
    activeCourseDetail?.let { course ->
        Dialog(onDismissRequest = { viewModel.closeCourseDetail() }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Course Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(onClick = { viewModel.closeCourseDetail() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = course.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Educator: ${course.teacherName}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "⭐ ${course.rating}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = course.description,
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lessons Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Syllabus & Lessons (${courseLessons.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        if (course.isEnrolled) {
                            Text(
                                text = "Progress: ${course.progressPercent}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lessons List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(courseLessons) { lesson ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (lesson.isCompleted) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (lesson.isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = if (lesson.isCompleted) Color(0xFF16A34A) else BluePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = lesson.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${lesson.durationMinutes} mins • ${lesson.notesSummary}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (!lesson.isCompleted) {
                                        TextButton(
                                            onClick = { viewModel.markLessonComplete(lesson.lessonId, course.courseId) }
                                        ) {
                                            Text("Mark Done", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!course.isEnrolled) {
                        Button(
                            onClick = { viewModel.enrollInCourse(course.courseId) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Enroll Now (${course.price})", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.closeCourseDetail()
                                viewModel.setTab(AppTab.MY_COURSES)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Continue in My Courses", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCardItem(
    course: CourseEntity,
    onOpenDetail: () -> Unit,
    onEnroll: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetail)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEBF3FE)
                ) {
                    Text(
                        text = course.category,
                        color = BluePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Price Tag
                Text(
                    text = course.price,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (course.isFree) Color(0xFF16A34A) else Color(0xFF0F172A)
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
                text = course.description,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👨‍🏫 ${course.teacherName}",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${course.duration}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                if (course.isEnrolled) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "Enrolled (${course.progressPercent}%)",
                            color = Color(0xFF16A34A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onEnroll,
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Enroll", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
