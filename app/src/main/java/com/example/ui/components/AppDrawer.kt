package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.theme.BluePrimary

@Composable
fun AppDrawerContent(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
            .testTag("app_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(BluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AK",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Amit Kumar",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Mithila Academy • Class XII",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Student ID: SPA-2026-101",
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Active",
                                color = Color(0xFF4ADE80),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Items
            val navItems = listOf(
                DrawerItemData("Home", Icons.Default.Home, AppTab.HOME),
                DrawerItemData("All Courses", Icons.Default.School, AppTab.ALL_COURSES),
                DrawerItemData("My Courses", Icons.Default.Assignment, AppTab.MY_COURSES),
                DrawerItemData("Notes & PDFs", Icons.AutoMirrored.Filled.MenuBook, AppTab.NOTES),
                DrawerItemData("Test Series", Icons.Default.Quiz, AppTab.TEST),
                DrawerItemData("Free Videos", Icons.Default.PlayCircle, AppTab.FREE_VIDEOS),
                DrawerItemData("Free Notes", Icons.Default.LibraryBooks, AppTab.FREE_NOTES),
                DrawerItemData("AI Doubts (Live Teacher)", Icons.Default.SmartToy, AppTab.AI_CHAT),
                DrawerItemData("Live Class Room", Icons.Default.LiveTv, AppTab.LIVE_CLASS),
                DrawerItemData("All Video Lectures", Icons.Default.VideoLibrary, AppTab.VIDEOS),
                DrawerItemData("Downloads", Icons.Default.CloudDownload, AppTab.DOWNLOADS),
                DrawerItemData("Notice Board", Icons.Default.Campaign, AppTab.NOTICE_BOARD),
                DrawerItemData("Teacher Admin Panel", Icons.Default.AdminPanelSettings, AppTab.CREATE)
            )

            navItems.forEach { item ->
                val isSelected = currentTab == item.tab
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) BluePrimary else Color(0xFF64748B),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) BluePrimary else Color(0xFF1E293B)
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        onTabSelected(item.tab)
                        onCloseDrawer()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFFEBF3FE),
                        unselectedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // App branding footer
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text(
                    text = "SPA AI Teacher • v2.6.0",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "Powered by Gemini Live & Mithila Academy",
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

data class DrawerItemData(
    val title: String,
    val icon: ImageVector,
    val tab: AppTab
)
