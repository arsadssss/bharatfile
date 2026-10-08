package com.bharatfile.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.R
import com.bharatfile.app.navigation.Screen
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.ElectricBlue

@Composable
fun NavigationDrawerContent(
    currentRoute: String,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BharatFileThemeCustom.colors
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(color = colors.cardSurface)
            .padding(vertical = 24.dp)
            .verticalScroll(scrollState)
    ) {
        // Brand Header inside Drawer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_bharatfile_logo),
                contentDescription = null,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "BharatFile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = colors.textPrimary
                )
                Text(
                    text = "100% On-Device & Private",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp
                    ),
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Core Navigation Items
        DrawerItem(
            title = "Home",
            icon = Icons.Outlined.Home,
            isSelected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        DrawerItem(
            title = "All Tools",
            icon = Icons.Outlined.Category,
            isSelected = currentRoute == "tools",
            onClick = { onNavigate("tools") }
        )
        DrawerItem(
            title = "History",
            icon = Icons.Outlined.History,
            isSelected = currentRoute == "history",
            onClick = { onNavigate("history") }
        )
        DrawerItem(
            title = "My Profile",
            icon = Icons.Outlined.Person,
            isSelected = currentRoute == "profile",
            onClick = { onNavigate("profile") }
        )
        DrawerItem(
            title = "Settings",
            icon = Icons.Outlined.Settings,
            isSelected = currentRoute == "settings",
            onClick = { onNavigate("settings") }
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(
            color = colors.cardBorder,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))

        DrawerItem(
            title = "Compress PDF",
            icon = Icons.Outlined.Description,
            isSelected = currentRoute == "compress_pdf",
            onClick = { onNavigate("compress_pdf") }
        )
        DrawerItem(
            title = "Compress Image",
            icon = Icons.Outlined.Image,
            isSelected = currentRoute == "compress_image",
            onClick = { onNavigate("compress_image") }
        )
        DrawerItem(
            title = "Resize Image",
            icon = Icons.Outlined.AspectRatio,
            isSelected = currentRoute == "resize_image",
            onClick = { onNavigate("resize_image") }
        )
        DrawerItem(
            title = "Convert Image",
            icon = Icons.Outlined.SwapHoriz,
            isSelected = currentRoute == "convert_image",
            onClick = { onNavigate("convert_image") }
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(
            color = colors.cardBorder,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Dark Mode Toggle Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Dark Mode",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                    color = colors.textPrimary
                )
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = { onToggleDarkMode() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ElectricBlue
                )
            )
        }

        // About / Policy / Terms / Contact
        DrawerItem(
            title = "About BharatFile",
            icon = Icons.Outlined.Info,
            isSelected = currentRoute == "about",
            onClick = { onNavigate("about") }
        )
        DrawerItem(
            title = "Privacy Policy",
            icon = Icons.Outlined.Lock,
            isSelected = currentRoute == "privacy",
            onClick = { onNavigate("privacy") }
        )
        DrawerItem(
            title = "Terms of Service",
            icon = Icons.Outlined.Policy,
            isSelected = currentRoute == "terms",
            onClick = { onNavigate("terms") }
        )
        DrawerItem(
            title = "Contact Us",
            icon = Icons.Outlined.Email,
            isSelected = currentRoute == "contact",
            onClick = { onNavigate("contact") }
        )

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "BharatFile v1.0 • Made with pride in India 🇮🇳",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = colors.textSecondary.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun DrawerItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = BharatFileThemeCustom.colors
    val bg = if (isSelected) colors.segmentedContainer else Color.Transparent
    val tint = if (isSelected) ElectricBlue else colors.textSecondary
    val textWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp,
                fontWeight = textWeight
            ),
            color = if (isSelected) ElectricBlue else colors.textPrimary
        )
    }
}
