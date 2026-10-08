package com.bharatfile.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bharatfile.app.data.LocalAuthRepository
import com.bharatfile.app.model.UserProfile
import com.bharatfile.app.navigation.Screen
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.ElectricBlue
import com.bharatfile.app.theme.ElectricBlueDark
import com.bharatfile.app.theme.PillShape
import com.bharatfile.app.theme.SuccessGreen
import com.bharatfile.app.ui.components.GlassCard
import com.bharatfile.app.ui.components.GradientButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authRepo = remember { LocalAuthRepository.getInstance(context) }
    val userProfile by authRepo.currentUser.collectAsState()
    val colors = BharatFileThemeCustom.colors
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            userProfile?.let { user ->
                authRepo.updateProfile(user.fullName, user.username, user.email, user.phoneNumber, it.toString())
                scope.launch { snackbarHostState.showSnackbar("Profile picture updated!") }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile & Account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            val user = userProfile
            if (user != null) {
                // Profile Avatar & Info Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(contentAlignment = Alignment.BottomEnd) {
                            if (user.avatarUri != null) {
                                AsyncImage(
                                    model = user.avatarUri,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, ElectricBlue, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(listOf(ElectricBlue, ElectricBlueDark))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.initials,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                            }

                            // Camera button overlay
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(colors.cardSurface)
                                    .border(1.dp, colors.cardBorder, CircleShape)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = "Change Avatar",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Full Name & Username
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "@${user.username}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = ElectricBlue
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = user.formattedMemberSince,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Contact Details rows
                        ProfileDetailRow(
                            icon = Icons.Outlined.Email,
                            label = "Email",
                            value = user.email
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProfileDetailRow(
                            icon = Icons.Outlined.Phone,
                            label = "Phone",
                            value = user.phoneNumber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Card: Files Processed & Total Space Saved
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${user.filesProcessedCount}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = ElectricBlue
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Files Processed",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = colors.textSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(38.dp)
                                .background(colors.cardBorder)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = user.formattedTotalSaved,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = SuccessGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Storage Saved",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions Section: Edit Profile, Change Password, Switch Account
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileActionButton(
                        icon = Icons.Outlined.Edit,
                        title = "Edit Profile",
                        subtitle = "Update name, username, email & phone",
                        onClick = { showEditProfileDialog = true }
                    )

                    ProfileActionButton(
                        icon = Icons.Outlined.LockReset,
                        title = "Change Password",
                        subtitle = "Update your local account password",
                        onClick = { showChangePasswordDialog = true }
                    )

                    ProfileActionButton(
                        icon = Icons.Outlined.Logout,
                        title = "Sign Out / Switch Account",
                        subtitle = "Switch user account or sign in with another",
                        onClick = {
                            authRepo.signOut()
                            showAuthDialog = true
                        }
                    )
                }
            } else {
                // Not Logged In View
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(68.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sign In to BharatFile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Keep your preferences, sync defaults, and track storage savings across sessions.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    GradientButton(
                        text = "Sign In / Register",
                        onClick = { showAuthDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }

        // Edit Profile Dialog
        if (showEditProfileDialog && userProfile != null) {
            EditProfileDialog(
                current = userProfile!!,
                onDismiss = { showEditProfileDialog = false },
                onSave = { name, username, email, phone ->
                    val res = authRepo.updateProfile(name, username, email, phone, userProfile?.avatarUri)
                    if (res.isSuccess) {
                        scope.launch { snackbarHostState.showSnackbar("Profile updated successfully!") }
                        showEditProfileDialog = false
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Update failed") }
                    }
                }
            )
        }

        // Change Password Dialog
        if (showChangePasswordDialog) {
            ChangePasswordDialog(
                onDismiss = { showChangePasswordDialog = false },
                onSave = { oldPass, newPass ->
                    val res = authRepo.changePassword(oldPass, newPass)
                    if (res.isSuccess) {
                        scope.launch { snackbarHostState.showSnackbar("Password changed successfully!") }
                        showChangePasswordDialog = false
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Password update failed") }
                    }
                }
            )
        }

        // Auth Dialog (Login / Register)
        if (showAuthDialog) {
            AuthDialog(
                onDismiss = { showAuthDialog = false },
                onSignIn = { userOrEmail, pass ->
                    val res = authRepo.signIn(userOrEmail, pass)
                    if (res.isSuccess) {
                        scope.launch { snackbarHostState.showSnackbar("Welcome back, ${res.getOrNull()?.fullName}!") }
                        showAuthDialog = false
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Sign in failed") }
                    }
                },
                onSignUp = { name, uname, email, phone, pass ->
                    val res = authRepo.signUp(name, uname, email, phone, pass)
                    if (res.isSuccess) {
                        scope.launch { snackbarHostState.showSnackbar("Account created! Welcome, $name.") }
                        showAuthDialog = false
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Sign up failed") }
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    val colors = BharatFileThemeCustom.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.segmentedContainer)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricBlue,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun ProfileActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = BharatFileThemeCustom.colors
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(ElectricBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    current: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    val colors = BharatFileThemeCustom.colors
    var name by remember { mutableStateOf(current.fullName) }
    var username by remember { mutableStateOf(current.username) }
    var email by remember { mutableStateOf(current.email) }
    var phone by remember { mutableStateOf(current.phoneNumber) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (error != null) {
                    Text(text = error!!, color = colors.error, fontSize = 12.sp)
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        error = "Full Name is required"
                    } else if (username.length < 3) {
                        error = "Username must be at least 3 characters"
                    } else {
                        onSave(name, username, email, phone)
                    }
                }
            ) {
                Text("Save", color = ElectricBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val colors = BharatFileThemeCustom.colors
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password", fontWeight = FontWeight.Bold, color = colors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (error != null) {
                    Text(text = error!!, color = colors.error, fontSize = 12.sp)
                }
                OutlinedTextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("New Password (min 6 chars)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPass.length < 6) {
                        error = "New password must be at least 6 characters"
                    } else if (newPass != confirmPass) {
                        error = "Passwords do not match"
                    } else {
                        onSave(oldPass, newPass)
                    }
                }
            ) {
                Text("Update", color = ElectricBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}

@Composable
private fun AuthDialog(
    onDismiss: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String, String, String) -> Unit
) {
    val colors = BharatFileThemeCustom.colors
    var isSignUpTab by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PillShape)
                    .background(colors.segmentedContainer)
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(if (!isSignUpTab) colors.cardSurface else Color.Transparent)
                        .clickable { isSignUpTab = false; validationError = null }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign In",
                        fontWeight = if (!isSignUpTab) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isSignUpTab) ElectricBlue else colors.textSecondary,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(if (isSignUpTab) colors.cardSurface else Color.Transparent)
                        .clickable { isSignUpTab = true; validationError = null }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Register",
                        fontWeight = if (isSignUpTab) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSignUpTab) ElectricBlue else colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (validationError != null) {
                    Text(text = validationError!!, color = colors.error, fontSize = 12.sp)
                }

                if (!isSignUpTab) {
                    // Sign In Form
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email or Username") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                } else {
                    // Sign Up Form
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username * (min 3 chars)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password * (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password *") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isSignUpTab) {
                        if (email.isBlank() || password.isBlank()) {
                            validationError = "Please enter both identifier and password"
                        } else {
                            onSignIn(email, password)
                        }
                    } else {
                        if (fullName.isBlank()) {
                            validationError = "Full Name is required"
                        } else if (username.trim().length < 3) {
                            validationError = "Username must be at least 3 characters"
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                            validationError = "Enter a valid email address"
                        } else if (password.length < 6) {
                            validationError = "Password must be at least 6 characters"
                        } else if (password != confirmPassword) {
                            validationError = "Passwords do not match"
                        } else {
                            onSignUp(fullName, username, email, phone, password)
                        }
                    }
                }
            ) {
                Text(if (!isSignUpTab) "Sign In" else "Create Account", color = ElectricBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}
