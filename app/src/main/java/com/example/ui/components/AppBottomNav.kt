package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.theme.BluePrimary

@Composable
fun AppBottomNav(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("app_bottom_nav")
    ) {
        val items = listOf(
            Triple(AppTab.HOME, Icons.Filled.Home, Icons.Outlined.Home),
            Triple(AppTab.MY_COURSES, Icons.Filled.Assignment, Icons.Outlined.Assignment),
            Triple(AppTab.DOWNLOADS, Icons.Filled.CloudDownload, Icons.Outlined.CloudDownload),
            Triple(AppTab.NOTICE_BOARD, Icons.Filled.Campaign, Icons.Outlined.Campaign)
        )

        items.forEach { (tab, selectedIcon, unselectedIcon) ->
            val isSelected = currentTab == tab

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) selectedIcon else unselectedIcon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) BluePrimary else Color(0xFF6B7280)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFE8F1FC),
                    selectedTextColor = BluePrimary,
                    unselectedTextColor = Color(0xFF6B7280)
                ),
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
            )
        }
    }
}


