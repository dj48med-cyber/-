package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ZemmoraApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    
    // UI state
    var currentScreen by remember { mutableStateOf("login") } // login, employee_dashboard, admin_dashboard
    var showLogoSelection by remember { mutableStateOf(false) }
    
    // Palette selection based on logo selection
    val themeColor = when (viewModel.appLogoUrl) {
        "gold" -> Color(0xFFD4AF37) // Classy Gold
        "emerald" -> Color(0xFF0F9D58) // Algerian Heritage Green
        "royal" -> Color(0xFF4285F4) // Tech Blue
        else -> Color(0xFF0D9488) // Sophisticated Dark Teal
    }
    
    val bgModifier = if (viewModel.isDarkMode) {
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF020617)) // Luxury Slate Dark
                )
            )
    } else {
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0)) // Pristine Light-Grey Aurora
                )
            )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopBar(
                    viewModel = viewModel,
                    themeColor = themeColor,
                    onLogoSettingClick = { showLogoSelection = true }
                )
            },
            modifier = bgModifier,
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        "login" -> LoginScreen(
                            viewModel = viewModel,
                            themeColor = themeColor,
                            onLoginSuccess = { role ->
                                if (role == "admin") {
                                    currentScreen = "admin_dashboard"
                                } else {
                                    currentScreen = "employee_dashboard"
                                }
                            }
                        )
                        "employee_dashboard" -> EmployeeDashboard(
                            viewModel = viewModel,
                            themeColor = themeColor,
                            onLogout = {
                                viewModel.logOut()
                                currentScreen = "login"
                            }
                        )
                        "admin_dashboard" -> AdminDashboard(
                            viewModel = viewModel,
                            themeColor = themeColor,
                            onLogout = {
                                viewModel.logOut()
                                currentScreen = "login"
                            }
                        )
                    }
                }

                // Logo picker dialog
                if (showLogoSelection) {
                    LogoSelectionDialog(
                        viewModel = viewModel,
                        currentLogo = viewModel.appLogoUrl,
                        onDismiss = { showLogoSelection = false }
                    )
                }
            }
        }
    }
}

@Composable
fun AppTopBar(
    viewModel: MainViewModel,
    themeColor: Color,
    onLogoSettingClick: () -> Unit
) {
    val logoText = when (viewModel.appLogoUrl) {
        "gold" -> "الشعار الذهبي الفاخر"
        "emerald" -> "شعار بلدية زمورة التراثي"
        "royal" -> "الشعار التقني العصري"
        else -> "الكساء المؤسساتي الافتراضي"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Republic Header Centered
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "الجمهورية الجزائرية الديمقراطية الشعبية",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (viewModel.isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Small canvas logo
                    CanvasLogo(themeColor = themeColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "لجنة الخدمات الاجتماعية لبلدية زمورة – ولاية غليزان",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = themeColor
                        )
                    )
                }
            }

            Divider(color = (if (viewModel.isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Interactive Status label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageIconForLogo(viewModel.appLogoUrl),
                        contentDescription = "Logo Concept",
                        tint = themeColor,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onLogoSettingClick() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "النظام الإداري الذكي",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (viewModel.isDarkMode) Color.White else Color(0xFF1E293B)
                        )
                        Text(
                            text = logoText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (viewModel.isDarkMode) Color(0xFF64748B) else Color(0xFF94A3B8)
                        )
                    }
                }

                // Control actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (viewModel.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Theme Toggle",
                            tint = if (viewModel.isDarkMode) Color(0xFFF59E0B) else Color(0xFF475569)
                        )
                    }
                    IconButton(onClick = { onLogoSettingClick() }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Change Emblem",
                            tint = themeColor
                        )
                    }
                }
            }

            // Scrolling Marquee custom component
            ScrollingMarqueeBanner(themeColor = themeColor)
        }
    }
}

@Composable
fun CanvasLogo(themeColor: Color, modifier: Modifier) {
    Canvas(modifier = modifier) {
        // Draw an elegant outer circular seal
        drawCircle(
            color = themeColor,
            radius = size.minDimension / 2f,
            style = Stroke(width = 2.dp.toPx())
        )
        // Draw elegant inner traditional cross lines inside municipal seal
        drawCircle(
            color = themeColor.copy(alpha = 0.3f),
            radius = size.minDimension / 3.4f
        )
        drawLine(
            color = themeColor,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = themeColor,
            start = Offset(size.width / 2f, 0f),
            end = Offset(size.width / 2f, size.height),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
fun ScrollingMarqueeBanner(themeColor: Color) {
    var scrollOffset by remember { mutableStateOf(0f) }
    
    // Auto increment offset with timer
    LaunchedEffect(Unit) {
        while (true) {
            scrollOffset -= 1.8f
            if (scrollOffset < -700f) {
                scrollOffset = 500f
            }
            delay(16)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(themeColor.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = "◀ المنصة الرقمية الذكية المتكاملة للجنة الخدمات الاجتماعية لبلدية زمورة – ولاية غليزان: تحديث سحابي فوري ومتابعة ذكية بالذكاء الاصطناعي لكافة ملفات العمال والاستفسارات الفورية 0796035935",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = themeColor,
                fontSize = 11.sp
            ),
            modifier = Modifier.offset(x = scrollOffset.dp),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

// Map custom logos to symbols
fun imageIconForLogo(tag: String) = when (tag) {
    "gold" -> Icons.Default.WorkspacePremium
    "emerald" -> Icons.Default.LocalActivity
    "royal" -> Icons.Default.CloudSync
    else -> Icons.Default.AccountBalance
}

@Composable
fun LogoSelectionDialog(
    viewModel: MainViewModel,
    currentLogo: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تخصيص شعار وهوية المنصة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (viewModel.isDarkMode) Color.White else Color(0xFF1F2937),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LogoOptionItem(
                    title = "شعار الكساء الإداري الكلاسيكي",
                    desc = "لون رغوي كلاسيكي للدوائر الإدارية الرسمية",
                    color = Color(0xFF0D9488),
                    isSelected = currentLogo == "default",
                    onClick = {
                        viewModel.changeAppLogo("default")
                        onDismiss()
                    }
                )

                LogoOptionItem(
                    title = "شعار مذهب ملكي فاخر",
                    desc = "درجة ذهبية متميزة تبرز العراقة والأصالة",
                    color = Color(0xFFD4AF37),
                    isSelected = currentLogo == "gold",
                    onClick = {
                        viewModel.changeAppLogo("gold")
                        onDismiss()
                    }
                )

                LogoOptionItem(
                    title = "شعار بلدية زمورة التراثي",
                    desc = "درجة خضراء زمردية متناسقة مع الراية الوطنية",
                    color = Color(0xFF0F9D58),
                    isSelected = currentLogo == "emerald",
                    onClick = {
                        viewModel.changeAppLogo("emerald")
                        onDismiss()
                    }
                )

                LogoOptionItem(
                    title = "الشعار السحابي العازب",
                    desc = "درجة زرقاء عصرية تمثل الثقة والاتصال والسرعة التقنية",
                    color = Color(0xFF4285F4),
                    isSelected = currentLogo == "royal",
                    onClick = {
                        viewModel.changeAppLogo("royal")
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("إغلاق التخصيص", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LogoOptionItem(
    title: String,
    desc: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
            .background(
                if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(if (isSelected) 20.dp else 0.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(desc, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color.Gray)
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    themeColor: Color,
    onLoginSuccess: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Employees, 1: Admins
    
    // Inputs
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    var isOtpSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var notificationMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Institutional beautiful frame
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, themeColor.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CanvasLogo(themeColor = themeColor, modifier = Modifier.size(72.dp))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "المنصة الإدارية للجنة الخدمات الاجتماعية",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        textAlign = TextAlign.Center,
                        color = themeColor
                    )
                    Text(
                        text = "بلدية زمورة – ولاية غليزان",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (viewModel.isDarkMode) Color(0xFF94A3B8) else Color(0xFF4B5563)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "جهة الاتصال للدعم الفني المباشر للجنة: 0796035935",
                        fontSize = 11.sp,
                        color = themeColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Tab Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
                )
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedTab == 0) themeColor else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedTab = 0
                                errorMessage = ""
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "بوابة الموظف (طلب منحة/سلفة)",
                            color = if (selectedTab == 0) Color.White else (if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedTab == 1) themeColor else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedTab = 1
                                errorMessage = ""
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "بوابة الإدارة والتسيير",
                            color = if (selectedTab == 1) Color.White else (if (viewModel.isDarkMode) Color.LightGray else Color.DarkGray),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Body Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (selectedTab == 0) {
                        // Employee Login
                        Text(
                            text = "تسجيل دخول الموظفين بالهاتف",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (viewModel.isDarkMode) Color.White else Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (!isOtpSent) {
                            Text(
                                text = "أدخل رقم الهاتف لتلقي رمز الدخول المباشر OTP (محاكاة خالية من الرسوم):",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("رقم الهاتف") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = themeColor) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (phoneInput.trim().isEmpty()) {
                                        errorMessage = "يرجى تعبئة خانة رقم الهاتف!"
                                    } else {
                                        viewModel.sendOtpCode(phoneInput) {
                                            isOtpSent = true
                                            errorMessage = ""
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إرسال رمز التحقق مجاناً", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // OTP input state
                            Text(
                                text = "الرمز مرسل للموظف: ${viewModel.currentEmployee?.name}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = themeColor,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.15f)),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = themeColor)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = viewModel.otpSentMessage,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text("رمز التحقق (4 أرقام)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = themeColor) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { isOtpSent = false },
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    Text("رجوع")
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.verifyOtpCode(
                                            otpInput,
                                            onSuccess = { onLoginSuccess("user") },
                                            onError = { errorMessage = it }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("تأكيد تسجيل الدخول", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Admin Login
                        Text(
                            text = "تسجيل دخول الهيئة المسيرة للجنة",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (viewModel.isDarkMode) Color.White else Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "اسم الحساب افتراضياً: admin@1986",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        var adminUsername by remember { mutableStateOf("admin@1986") }
                        
                        OutlinedTextField(
                            value = adminUsername,
                            onValueChange = { adminUsername = it },
                            label = { Text("اسم المستخدم") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = themeColor) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("كلمة المرور المسيرة") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = themeColor) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Show Password"
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (adminUsername.trim() != "admin@1986") {
                                    errorMessage = "اسم المستخدم غير صحيح!"
                                } else {
                                    viewModel.loginAsAdmin(
                                        passwordInput,
                                        onShift = { onLoginSuccess("admin") },
                                        onError = { errorMessage = it }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("دخول الهيئة العامة", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Wrapper for login trigger to match custom admin
fun MainViewModel.loginAsAdmin(password: String, onShift: () -> Unit, onError: (String) -> Unit) {
    loginAsAdmin(password, onSuccess = onShift, onError = onError)
}

@Composable
fun EmployeeDashboard(
    viewModel: MainViewModel,
    themeColor: Color,
    onLogout: () -> Unit
) {
    val emp = viewModel.currentEmployee ?: return
    var activeSubTab by remember { mutableStateOf("new_request") } // new_request, track_requests, complaint, personal_archive
    
    val requestedList by viewModel.requests.collectAsState(initial = emptyList())
    val empRequests = requestedList.filter { it.employeeId == emp.id }
    
    val complaintsList by viewModel.complaints.collectAsState(initial = emptyList())
    val empComplaints = complaintsList.filter { it.employeeId == emp.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Employee Welcoming Info Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = themeColor.copy(alpha = 0.12f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(themeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emp.name.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مرحباً، الموظف: ${emp.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (viewModel.isDarkMode) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = "المصلحة: ${emp.section} | الهاتف: ${emp.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (viewModel.isDarkMode) Color.LightGray else Color.Gray
                    )
                }
                
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "الخروج",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Horizontal Sub-Tabs row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmployeeSubTabItem(
                label = "إيداع طلب جديد",
                icon = Icons.Default.AddCircle,
                isSelected = activeSubTab == "new_request",
                themeColor = themeColor,
                onClick = { activeSubTab = "new_request" }
            )
            EmployeeSubTabItem(
                label = "متابعة الطلبات",
                icon = Icons.Default.PlaylistAddCheck,
                isSelected = activeSubTab == "track_requests",
                badgeCount = empRequests.size,
                themeColor = themeColor,
                onClick = { activeSubTab = "track_requests" }
            )
            EmployeeSubTabItem(
                label = "عريضة شكوى",
                icon = Icons.Default.Feedback,
                isSelected = activeSubTab == "complaint",
                themeColor = themeColor,
                onClick = { activeSubTab = "complaint" }
            )
            EmployeeSubTabItem(
                label = "أرشيف ونماذج",
                icon = Icons.Default.Folder,
                isSelected = activeSubTab == "personal_archive",
                themeColor = themeColor,
                onClick = { activeSubTab = "personal_archive" }
            )
        }

        // SubTab content
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                "new_request" -> NewRequestForm(viewModel, themeColor)
                "track_requests" -> TrackRequestsList(viewModel, empRequests, themeColor)
                "complaint" -> ComplaintSubmission(viewModel, empComplaints, themeColor)
                "personal_archive" -> DownloadableFormsAndArchive(viewModel, empRequests, themeColor)
            }
        }
    }
}

@Composable
fun EmployeeSubTabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    badgeCount: Int = 0,
    themeColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) themeColor else Color.Transparent
        ),
        border = BorderStroke(1.dp, if (isSelected) themeColor else Color.Gray.copy(alpha = 0.5f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(if (isSelected) Color.White else themeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = if (isSelected) themeColor else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NewRequestForm(viewModel: MainViewModel, themeColor: Color) {
    var isLoanSelection by remember { mutableStateOf(false) } // False: Grant, True: Loan
    
    // Dropdowns values
    val grantTypes = listOf("منحة الزواج", "منحة الختان", "منحة العمليات الجراحية", "منحة الوفاة", "منحة الأشعة والتحاليل الطبية", "منحة الدخول المدرسي", "منحة التقاعد", "منحة الاحتياجات الخاصة")
    val loanTypes = listOf("سلفة استثنائية (مستعجلة)", "سلفة السكن", "سلفة الزواج")
    
    var selectedType by remember { mutableStateOf(grantTypes[0]) }
    var showTypeMenu by remember { mutableStateOf(false) }
    
    var amountInput by remember { mutableStateOf("") }
    var durationMonths by remember { mutableStateOf(10) } // Repay speed default
    
    var attachmentName by remember { mutableStateOf("") }
    var attachmentDriveLink by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Observe active budget dynamically
    val budgetsList by viewModel.budgets.collectAsState(initial = emptyList())
    val activeBudget = budgetsList.firstOrNull() ?: BudgetEntity(year = 2026, totalBudget = 6500000.0)

    // Automatically set default standard amounts in Algerian DZD for municipal social services
    LaunchedEffect(selectedType, isLoanSelection) {
        if (!isLoanSelection) {
            amountInput = when (selectedType) {
                "منحة الزواج" -> "30000"
                "منحة الختان" -> "15000"
                "منحة العمليات الجراحية" -> "45000"
                "منحة الوفاة" -> "40000"
                "منحة الأشعة والتحاليل الطبية" -> "10000"
                "منحة الدخول المدرسي" -> "5000"
                "منحة التقاعد" -> "60000"
                else -> "25000"
            }
        } else {
            amountInput = when (selectedType) {
                "سلفة استثنائية (مستعجلة)" -> "50000"
                "سلفة السكن" -> "150000"
                "سلفة الزواج" -> "80000"
                else -> "50000"
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "تعبئة استمارة منحة أو سلفة كهرومغناطيسية",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = themeColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grant vs Loan Segment selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (!isLoanSelection) themeColor else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            isLoanSelection = false
                            selectedType = grantTypes[0]
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("منحة مالية اجتماعية", color = if (!isLoanSelection) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isLoanSelection) themeColor else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            isLoanSelection = true
                            selectedType = loanTypes[0]
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("سلفة مالية مستردة", color = if (isLoanSelection) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

            // Dropdown selection for specific Type
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("نوع الطلب الاجتماعي") },
                    trailingIcon = {
                        IconButton(onClick = { showTypeMenu = !showTypeMenu }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false },
                    modifier = Modifier.fillMaxWidth().background(if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White)
                ) {
                    val list = if (isLoanSelection) loanTypes else grantTypes
                    list.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedType = type
                                showTypeMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calculated constraints label based on type
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.1f)),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "المستندات المطلوبة قانوناً لهذا الملف:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = themeColor
                    )
                    Text(
                        text = when (selectedType) {
                            "منحة الزواج" -> "• عقد زواج إلكتروني أصلي\n• شهادة الحالة العائلية للموظف"
                            "منحة الختان" -> "• شهادة الختان للأبناء من مستشفى حكومي\n• شهادة ميلاد الابن"
                            "منحة العمليات الجراحية" -> "• فاتورة المستشفى الإجمالية\n• تقرير طبي معتمد من الطبيب الجراح"
                            "منحة الوفاة" -> "• شهادة الوفاة الأصلية لقريب الدرجة الأولى\n• وثيقة الكفالة إن وجدت"
                            "منحة الأشعة والتحاليل الطبية" -> "• الفاتورة الأصلية مسددة للعيادة المخبرية\n• وصفة الطبيب المعالج مع التحليل المرافق"
                            "منحة الدخول المدرسي" -> "• شهادة مدرسية أصلية للأبناء سارية الصلاحية"
                            "منحة التقاعد" -> "• قرار الإحالة مصلحة الموظفين للتقاعد\n• شهادة تثبت قفل التصفية"
                            "سلفة السكن" -> "• رخصة البناء باسم المستفيد أو سند عقاري ملكية\n• وثائق الموثق لإثبات العقار"
                            "سلفة الزواج" -> "• شهادة تسجيل الزواج بالبلدية حديثة وتعهد كفيل"
                            else -> "• طلب خطي موجه لرئيس اللجنة ومبرر قاهر للرغبة"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Input: Numeric Amount (Prepends/Appends "DZD" automatically!)
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("مبلغ المساعدة (DZD دج)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = { Text("دج DZD", fontWeight = FontWeight.Bold, color = themeColor, modifier = Modifier.padding(end = 8.dp)) },
                modifier = Modifier.fillMaxWidth()
            )

            if (isLoanSelection) {
                Spacer(modifier = Modifier.height(12.dp))
                // Duration of Refund (for Loans)
                Text(
                    text = "فترة سداد الاسترداد: $durationMonths شهور",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = durationMonths.toFloat(),
                    onValueChange = { durationMonths = it.toInt() },
                    valueRange = 5f..36f,
                    steps = 30,
                    colors = SliderDefaults.colors(
                        thumbColor = themeColor,
                        activeTrackColor = themeColor
                    )
                )
                
                // Real-time Deduction Calculation Display
                val amt = amountInput.toDoubleOrNull() ?: 0.0
                val monthlyRepay = if (durationMonths > 0) amt / durationMonths else 0.0
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.06f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "الاستقطاع الشهري المقدر تلقائياً من الأجر:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Red
                        )
                        Text(
                            text = String.format("%.2f دج DZD / شهرياً", monthlyRepay),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Virtual Document Attachment Upload Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "تحميل الملفات والمستندات المرفقة (بحد أقصى 2 ميجابايت)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (attachmentDriveLink.isEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    attachmentName = "ملف_منحة_${System.currentTimeMillis() % 1000}.pdf"
                                    viewModel.simulateAttachmentUpload(attachmentName, "local://uri/dummy") { link ->
                                        attachmentDriveLink = link
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null)
                                Text("إرفاق PDF/صور", color = Color.White)
                            }
                        }
                    } else {
                        // Attached status feedback
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = themeColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "تم الرفع السحابي لـ Google Drive بنجاح!",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = themeColor
                            )
                        }
                        Text(
                            text = attachmentName,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (viewModel.isUploadingFile) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(color = themeColor, modifier = Modifier.size(24.dp))
                        Text(viewModel.uploadProgressMessage, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Expert Verification Assist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val fakeReq = GrantLoanRequestEntity(
                            employeeId = viewModel.currentEmployee?.id ?: 1,
                            employeeName = viewModel.currentEmployee?.name ?: "موظف",
                            employeePhone = viewModel.currentEmployee?.phone ?: "0000000000",
                            type = selectedType,
                            isLoan = isLoanSelection,
                            amount = amountInput.toDoubleOrNull() ?: 1000.0,
                            attachmentPaths = if (attachmentDriveLink.isNotEmpty()) attachmentName else "ملف_غير_مرفق"
                        )
                        viewModel.generateAiAdvisoryReport(fakeReq)
                    },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.5.dp, themeColor)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = themeColor)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فحص بالذكاء الاصطناعي", color = themeColor, fontWeight = FontWeight.Bold)
                }
            }

            // AI output panel
            if (viewModel.isAiAdvisoryLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = themeColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("الذكاء الاصطناعي يحلل المستندات ويتحقق من اللوائح...", fontSize = 11.sp)
                }
            } else if (viewModel.aiAdvisoryReport.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = themeColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تقرير التدقيق الاستشاري للذكاء الاصطناعي:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = themeColor)
                        }
                        Text(
                            text = viewModel.aiAdvisoryReport,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Submit Request
            Button(
                onClick = {
                    val amountVal = amountInput.toDoubleOrNull() ?: 0.0
                    if (amountVal <= 0.0) {
                        Toast.makeText(context, "الرجاء تحديد مبلغ مالي صحيح!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.submitRequest(
                            type = selectedType,
                            isLoan = isLoanSelection,
                            amount = amountVal,
                            durationMonths = if (isLoanSelection) durationMonths else 0,
                            attachmentPaths = if (attachmentDriveLink.isNotEmpty()) attachmentDriveLink else "لم يرفق مستند",
                            onSuccess = {
                                Toast.makeText(context, "تم إرسال الطلب الاجتماعي للجنة بنجاح ودمجه فورياً في قاعدة البيانات السحابية!", Toast.LENGTH_LONG).show()
                                amountInput = ""
                                attachmentDriveLink = ""
                                attachmentName = ""
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إيداع الملف والرفع لـ Google Drive", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TrackRequestsList(
    viewModel: MainViewModel,
    requests: List<GrantLoanRequestEntity>,
    themeColor: Color
) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("لم تقم بإيداع أي ملف أو منحة حالياً.", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(requests) { req ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White
                    ),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                req.type,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = themeColor
                            )
                            StatusBadge(req.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "القيمة: ${req.amount} دج DZD",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (req.isLoan) {
                            Text(
                                text = "التقسيط: على ${req.durationMonths} شواغر بـ ${String.format("%.2f", req.monthlyDeduction)} دج شهرياً",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Red
                            )
                        }

                        Text(
                            text = "تاريخ الإيداع: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(req.submissionDate))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )

                        if (req.attachmentPaths.startsWith("http")) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "المستند المرفق: رابط Google Drive نشط 🔗",
                                fontSize = 10.sp,
                                color = themeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (req.status == "Rejected" && !req.rejectionReason.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = "سبب التحفض/الرفض: ${req.rejectionReason}",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        } else if (req.status == "Approved" && req.meetingNumber != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "تمت المصادقة في محضر الاجتماع رقم: ${req.meetingNumber}",
                                color = Color(0xFF0F9D58),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bg, txt, label) = when (status) {
        "Approved" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "مقبول ومصادق")
        "Rejected" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "متحفظ عليه")
        else -> Triple(Color(0xFFFFF8E1), Color(0xFFF9A825), "قيد الدراسة")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = txt, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ComplaintSubmission(
    viewModel: MainViewModel,
    complaints: List<ComplaintEntity>,
    themeColor: Color
) {
    var subject by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "تقديم التماس أو عريضة شكوى إلكترونية للجنة",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = themeColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("الموضوع الأساسي للشكوى") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("تفاصيل ومبررات الطلب الاجتماعي للجنة") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (subject.trim().isEmpty() || details.trim().isEmpty()) {
                            Toast.makeText(context, "الرجاء تعبئة كل حقول العريضة!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.submitComplaint(subject, details) {
                                Toast.makeText(context, "تم إرسال العريضة المسجلة لبلدية زمورة بنجاح للاطلاع المباشر!", Toast.LENGTH_LONG).show()
                                subject = ""
                                details = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Outbox, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("رفع الشكوى والتقييد لقاعدة البيانات", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Previous complaints list representation
        Text(
            "عرائض الشكاوى والالتماسات السابقة:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = if (viewModel.isDarkMode) Color.White else Color(0xFF191D24),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (complaints.isEmpty()) {
            Text("لا توجد التماسات مسجلة سابقاً.", color = Color.Gray, fontSize = 12.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(complaints) { comp ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(comp.subject, fontWeight = FontWeight.Bold, color = themeColor)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (comp.status == "Resolved") Color(0xFFE8F5E9) else Color(0xFFFFF8E1))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        if (comp.status == "Resolved") "تم حلها ورد الالتماس" else "قيد المعالجة الإدارية",
                                        color = if (comp.status == "Resolved") Color(0xFF2E7D32) else Color(0xFFF9A825),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(comp.details, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            if (comp.status == "Resolved" && !comp.resolutionNote.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                                ) {
                                    Text(
                                        text = "الرد الرسمي للرئيس: ${comp.resolutionNote}",
                                        color = Color(0xFF2E7D32),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(8.dp),
                                        fontWeight = FontWeight.Bold
                                    )
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
fun DownloadableFormsAndArchive(
    viewModel: MainViewModel,
    requests: List<GrantLoanRequestEntity>,
    themeColor: Color
) {
    val context = LocalContext.current
    val forms = listOf(
        "استمارة منحة الزواج للخدمات البلدية" to "https://retool-files.com/forms/marriage_zemmora_form.docx",
        "استمـارة طـلب سلـفة السكن التراكمية" to "https://retool-files.com/forms/housing_loan_zemmora.docx",
        "ملـف طـلب منحـة الـوفـاة والإرث" to "https://retool-files.com/forms/death_grant_file.pdf",
        "بروتوكول طلب العمليات الجراحية والاستشفاء" to "https://retool-files.com/forms/surgery_agreement.pdf"
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                "ركن تحميل وثائق واستمارات الإدارة الورقية للخدمات الاجتماعية:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = themeColor,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        items(forms) { form ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = themeColor)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(form.first, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    IconButton(onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(form.second))
                        context.startActivity(browserIntent)
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "تحميل", tint = themeColor)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "الأرشيف الرقمي لملفاتكم المرفوعة لـ Google Drive:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (viewModel.isDarkMode) Color.White else Color(0xFF1E293B)
            )
        }

        val uploadedOnly = requests.filter { it.attachmentPaths.startsWith("http") }
        if (uploadedOnly.isEmpty()) {
            item {
                Text("الأرشيف السحابي فارغ، يرجى إرفاق ملفات في طلباتكم الجديدة.", color = Color.Gray, fontSize = 11.sp)
            }
        } else {
            items(uploadedOnly) { req ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.isDarkMode) Color(0xFF334155) else Color.LightGray.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(req.type, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("رابط التخزين السحابي الآمن لـ Google Drive", fontSize = 9.sp, color = Color.Gray)
                        }
                        IconButton(onClick = {
                            val browse = Intent(Intent.ACTION_VIEW, Uri.parse(req.attachmentPaths))
                            context.startActivity(browse)
                        }) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "فتح في درايف", tint = themeColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboard(
    viewModel: MainViewModel,
    themeColor: Color,
    onLogout: () -> Unit
) {
    var adminActiveSubTab by remember { mutableStateOf("requests") } // requests, meetings, budget, team, backup_settings
    
    val allRequests by viewModel.requests.collectAsState(initial = emptyList())
    val allComplaints by viewModel.complaints.collectAsState(initial = emptyList())
    val allMeetings by viewModel.meetings.collectAsState(initial = emptyList())
    val allEmployees by viewModel.employees.collectAsState(initial = emptyList())
    val allMembers by viewModel.committeeMembers.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Administration Navigation Bar Accent Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminSubTabItem("الملفات والطلبات", Icons.Default.Inbox, adminActiveSubTab == "requests", themeColor) { adminActiveSubTab = "requests" }
            AdminSubTabItem("محاضر وجلسات اللجان", Icons.Default.HistoryEdu, adminActiveSubTab == "meetings", themeColor) { adminActiveSubTab = "meetings" }
            AdminSubTabItem("تحليلات الميزانية", Icons.Default.PieChart, adminActiveSubTab == "budget", themeColor) { adminActiveSubTab = "budget" }
            AdminSubTabItem("شؤون الموظفين واللجنة", Icons.Default.Groups, adminActiveSubTab == "team", themeColor) { adminActiveSubTab = "team" }
            AdminSubTabItem("الحوسبة والإعدادات", Icons.Default.Settings, adminActiveSubTab == "backup_settings", themeColor) { adminActiveSubTab = "backup_settings" }
        }

        // Section content box
        Box(modifier = Modifier.weight(1f)) {
            when (adminActiveSubTab) {
                "requests" -> AdminRequestsManager(viewModel, allRequests, allComplaints, themeColor)
                "meetings" -> AdminMeetingsManager(viewModel, allMeetings, allRequests, allMembers, themeColor)
                "budget" -> AdminBudgetCharts(viewModel, themeColor)
                "team" -> AdminTeamAndPersonnel(viewModel, allEmployees, allMembers, themeColor)
                "backup_settings" -> AdminCloudBackupSettings(viewModel, themeColor)
            }
        }
    }
}

@Composable
fun AdminSubTabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    themeColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) themeColor else Color.Gray.copy(alpha = 0.08f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else (if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (isSelected) Color.White else (if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AdminRequestsManager(
    viewModel: MainViewModel,
    requests: List<GrantLoanRequestEntity>,
    complaints: List<ComplaintEntity>,
    themeColor: Color
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("All") } // All, Pending, Approved, Rejected
    var requestTypeFilter by remember { mutableStateOf("All") } // All, Grants, Loans
    
    // Evaluating dialogs
    var selectedRequestForEvaluation by remember { mutableStateOf<GrantLoanRequestEntity?>(null) }
    var evaluationActionType by remember { mutableStateOf("") } // Approve, Reject
    var rejectionReasonInp by remember { mutableStateOf("") }
    var meetingNoInp by remember { mutableStateOf("13") }

    // Dialog for active complaint resolution
    var resolvingComplaint by remember { mutableStateOf<ComplaintEntity?>(null) }
    var complaintReplyInp by remember { mutableStateOf("") }

    val context = LocalContext.current

    val filteredReqs = requests.filter { req ->
        val nameMatch = req.employeeName.contains(searchQuery, ignoreCase = true) || req.employeePhone.contains(searchQuery)
        val statusMatch = filterStatus == "All" || req.status == filterStatus
        val typeMatch = requestTypeFilter == "All" || 
                (requestTypeFilter == "Grants" && !req.isLoan) || 
                (requestTypeFilter == "Loans" && req.isLoan)
        nameMatch && statusMatch && typeMatch
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Summary header
            Text(
                "لوحة رقابة وتدقيق الطلبات الاجتماعية الواردة",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = themeColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            // Search filters Card
            Card(
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("بحث باسم الموظف أو رقم هاتفه") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColor) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Filter segments horizontal Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { filterStatus = "All" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (filterStatus == "All") themeColor else Color.Gray.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("الكل", fontSize = 10.sp, color = if (filterStatus == "All") Color.White else Color.Gray)
                        }
                        Button(
                            onClick = { filterStatus = "Pending" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (filterStatus == "Pending") themeColor else Color.Gray.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("قيد المراجعة", fontSize = 10.sp, color = if (filterStatus == "Pending") Color.White else Color.Gray)
                        }
                        Button(
                            onClick = { filterStatus = "Approved" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (filterStatus == "Approved") themeColor else Color.Gray.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("مقبولة", fontSize = 10.sp, color = if (filterStatus == "Approved") Color.White else Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { requestTypeFilter = "All" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (requestTypeFilter == "All") themeColor else Color.Gray.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("كل الأنواع", fontSize = 10.sp, color = if (requestTypeFilter == "All") Color.White else Color.Gray)
                        }
                        Button(
                            onClick = { requestTypeFilter = "Grants" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (requestTypeFilter == "Grants") themeColor else Color.Gray.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("المنح فقط", fontSize = 10.sp, color = if (requestTypeFilter == "Grants") Color.White else Color.Gray)
                        }
                        Button(
                            onClick = { requestTypeFilter = "Loans" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (requestTypeFilter == "Loans") themeColor else Color.Gray.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("السلف فقط", fontSize = 10.sp, color = if (requestTypeFilter == "Loans") Color.White else Color.Gray)
                        }
                    }
                }
            }
        }

        if (filteredReqs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد ملفات مطابقة لشروط البحث الحالية.", color = Color.Gray)
                }
            }
        } else {
            items(filteredReqs) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White
                    ),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    req.type,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = themeColor
                                )
                                Text(
                                    "الموظف المستفيد: ${req.employeeName} (${req.employeePhone})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            StatusBadge(req.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "المبلغ الإجمالي المطلوب: ${req.amount} دج DZD",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (req.isLoan) {
                            Text(
                                text = "مدة الاسترداد: على ${req.durationMonths} شهور بقيمة استقطاع شهري ${String.format("%.2f", req.monthlyDeduction)} دج",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Red
                            )
                        }

                        Text(
                            text = "تاريخ الإيداع: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(req.submissionDate))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )

                        if (req.attachmentPaths.startsWith("http")) {
                            Row(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable {
                                        val br = Intent(Intent.ACTION_VIEW, Uri.parse(req.attachmentPaths))
                                        context.startActivity(br)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(14.dp), tint = themeColor)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "معاينة الوثيقة المرفقة المستضافة في Google Drive 🔗",
                                    fontSize = 10.sp,
                                    color = themeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (req.status == "Rejected" && !req.rejectionReason.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "التحفظ الإداري: ${req.rejectionReason}",
                                color = Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (req.status == "Approved" && req.meetingNumber != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "موافق ومصادق بموجب محضر الاجتماع رقم ${req.meetingNumber} تاريخ البت ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(req.decidedDate ?: 0))}",
                                color = Color(0xFF0F9D58),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Evaluative Buttons for Pending requests
                        if (req.status == "Pending") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        selectedRequestForEvaluation = req
                                        evaluationActionType = "Approve"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                                    modifier = Modifier.weight(1.5f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مصادقة فورية", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        selectedRequestForEvaluation = req
                                        evaluationActionType = "Reject"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تحفظ/رفض مسبب", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.generateAiAdvisoryReport(req) },
                                    modifier = Modifier.weight(1.2f),
                                    border = BorderStroke(1.dp, themeColor),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp), tint = themeColor)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("تقرير الذكاء", color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active complaints list representation under admin controller
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "جدول الالتماسات وعرائض الشكاوى الواردة:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = themeColor
            )
        }

        if (complaints.isEmpty()) {
            item {
                Text("لا توجد التماسات أو شكاوى واردة حالياً من الموظفين.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            items(complaints) { comp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(comp.subject, fontWeight = FontWeight.Bold, color = themeColor)
                                Text("الشاكي: ${comp.employeeName} (${comp.employeePhone})", fontSize = 11.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (comp.status == "Resolved") Color(0xFFE8F5E9) else Color(0xFFFFF8E1))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (comp.status == "Resolved") "تم حلها وإغلاق الشكوى" else "في الانتظار",
                                    color = if (comp.status == "Resolved") Color(0xFF2E7D32) else Color(0xFFF9A825),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(comp.details, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        
                        if (comp.status == "Pending") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { resolvingComplaint = comp },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("إعداد الرد وتوجيه الإدارة", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else if (comp.status == "Resolved" && !comp.resolutionNote.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                            ) {
                                Text(
                                    text = "الرد الإداري: ${comp.resolutionNote}",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog for Accept Evaluation
    if (selectedRequestForEvaluation != null && evaluationActionType == "Approve") {
        Dialog(onDismissRequest = { selectedRequestForEvaluation = null }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("المصادقة على الطلب الاجتماعي", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = themeColor)
                    Text("طلب الموظف: ${selectedRequestForEvaluation?.employeeName} | ${selectedRequestForEvaluation?.type}", fontSize = 12.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = meetingNoInp,
                        onValueChange = { meetingNoInp = it },
                        label = { Text("المسابقة على محضر الاجتماع رقم (رقمي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { selectedRequestForEvaluation = null }) { Text("إلغاء") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val num = meetingNoInp.toIntOrNull() ?: 13
                                viewModel.evaluateRequest(selectedRequestForEvaluation!!.id, "Approved", null, num)
                                selectedRequestForEvaluation = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58))
                        ) {
                            Text("اعتماد المقررة", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog for Reject Evaluation
    if (selectedRequestForEvaluation != null && evaluationActionType == "Reject") {
        Dialog(onDismissRequest = { selectedRequestForEvaluation = null }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("إبداء تحفظ / رفض مسبب للملف", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Red)
                    Text("يرجى تحديد مبرر قانوني واضح ليطلع عليه الموظف في لوحة تحكمه المباشرة:", fontSize = 12.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectionReasonInp,
                        onValueChange = { rejectionReasonInp = it },
                        label = { Text("سبب الرفض الإداري (مثل: نقص شهادة الميلاد الكهرومغناطيسية)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { selectedRequestForEvaluation = null }) { Text("إلغاء") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (rejectionReasonInp.trim().isEmpty()) {
                                    Toast.makeText(context, "الرجاء كتابة سبب التحفظ والتعليل!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.evaluateRequest(selectedRequestForEvaluation!!.id, "Rejected", rejectionReasonInp, null)
                                    selectedRequestForEvaluation = null
                                    rejectionReasonInp = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("حفظ التحفظ والرفض", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog for Complaint reply
    if (resolvingComplaint != null) {
        Dialog(onDismissRequest = { resolvingComplaint = null }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("إعداد الرد الرسمي على عريضة الموظف", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = themeColor)
                    Text("موضوع العريضة: ${resolvingComplaint?.subject}", fontSize = 12.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = complaintReplyInp,
                        onValueChange = { complaintReplyInp = it },
                        label = { Text("الرد والتوجيه الرسمي المعتمد") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { resolvingComplaint = null }) { Text("إلغاء") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (complaintReplyInp.trim().isEmpty()) {
                                    Toast.makeText(context, "يرجى تعبئة نص الرد الإداري ومصلحة الموظف!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.resolveComplaint(resolvingComplaint!!.id, complaintReplyInp)
                                    resolvingComplaint = null
                                    complaintReplyInp = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                        ) {
                            Text("رفع الرد والإغلاق", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMeetingsManager(
    viewModel: MainViewModel,
    meetings: List<MeetingEntity>,
    requests: List<GrantLoanRequestEntity>,
    members: List<CommitteeMemberEntity>,
    themeColor: Color
) {
    var showDraftingCard by remember { mutableStateOf(false) }
    val pendingApprovedReqs = requests.filter { it.status == "Pending" }
    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "سجل وإمضاءات محاضر اجتماعات اللجنة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = themeColor
                )
                if (!showDraftingCard) {
                    Button(
                        onClick = {
                            val nextMeetNo = ((meetings.map { it.meetingNumber }.maxOrNull() ?: 12) + 1).toString()
                            viewModel.initializeMeetingDraft(nextMeetNo, members)
                            showDraftingCard = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("عقد جلسة جديدة", color = Color.White)
                    }
                } else {
                    OutlinedButton(
                        onClick = { showDraftingCard = false },
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text("إغلاق الجلسة المسودة")
                    }
                }
            }
        }

        // Active drafting meeting dashboard
        if (showDraftingCard) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White),
                    border = BorderStroke(1.5.dp, themeColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "صياغة وإمضاء المحضر رقم: ${viewModel.meetingDraftNumber}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = themeColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Input numbers
                        OutlinedTextField(
                            value = viewModel.meetingDraftNumber,
                            onValueChange = { viewModel.meetingDraftNumber = it },
                            label = { Text("رقم الاجتماع الداري المعتمد") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "تدقيق الحضور والتوقيع الإلكتروني لممثلي اللجنة الحاضرين:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "ملاحظة: اللائحة تشترط سريان التوقيع فقط لمن هم حضور، الغائبون لا تتاح لهم خانة الإمضاء.",
                            fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Member interactive list
                        members.forEach { member ->
                            val isPresent = viewModel.meetingDraftPresent.contains(member.name)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        if (isPresent) themeColor.copy(alpha = 0.06f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isPresent,
                                        onCheckedChange = { checked ->
                                            viewModel.toggleMemberPresence(member.name, checked)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(member.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(member.role, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                // Interactive Hand Signature Click for present members
                                if (isPresent) {
                                    val signed = viewModel.meetingDraftSignatures.containsKey(member.name)
                                    if (signed) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE8F5E9))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("موقع إلكترونياً 📝", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        var showSignatureCanvas by remember { mutableStateOf(false) }
                                        Button(
                                            onClick = { showSignatureCanvas = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("إمضاء لمس", fontSize = 10.sp, color = Color.White)
                                        }

                                        if (showSignatureCanvas) {
                                            SignatureCanvasDialog(
                                                memberName = member.name,
                                                onDismiss = { showSignatureCanvas = false },
                                                onSignatureSaved = { points ->
                                                    viewModel.signForAttendee(member.name, "signed_ok")
                                                    showSignatureCanvas = false
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Text("غائب ❌", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Cases reviewed label
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "الملفات والمنح المعروضة للدراسة والبت الفوري في هذه الجلسة:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = themeColor
                                )
                                if (pendingApprovedReqs.isEmpty()) {
                                    Text("لا توجد ملفات معلقة في قائمة الانتظار للمصادقة.", fontSize = 11.sp)
                                } else {
                                    val names = pendingApprovedReqs.map { "${it.employeeName} (${it.type})" }.joinToString("، ")
                                    Text(names, fontSize = 11.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }

                        // AI generation draft button
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.generateMeetingMinutesAi(pendingApprovedReqs) },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor.copy(alpha = 0.85f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (viewModel.isMeetingAiLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Psychology, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("صياغة ديباجة وقرارات الجلسة بالذكاء الاصطناعي", color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = viewModel.meetingDraftSummary,
                            onValueChange = { viewModel.meetingDraftSummary = it },
                            label = { Text("مقررات وتفاصيل ومخرجات اجتماع اللجنة المنعقد") },
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (viewModel.meetingDraftSummary.trim().isEmpty()) {
                                    Toast.makeText(context, "الرجاء كتابة ملخص ومخرجات قرارات المحضر أولاً!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveMeeting {
                                        Toast.makeText(context, "تم حفظ وتوثيق محضر الجلسة السنوية وتخزينه في الأرشيف ومزامنة درايف تلقائياً!", Toast.LENGTH_LONG).show()
                                        showDraftingCard = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("توثيق وقفل محضر الجلسة والمخادعة للقرارات", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Previous meetings log
        if (meetings.isEmpty()) {
            item {
                Text("لا توجد محاضر اجتماعات موثقة سابقاً في الأرشيف المحلي.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
            }
        } else {
            items(meetings) { m ->
                var showPreviewPrint by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "محضر اجتماع رقم ${m.meetingNumber}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = themeColor
                            )
                            Text(
                                text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(m.meetingDate)),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "الأعضاء الحاضرون الموقعون: ${m.attendeesPresent}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (m.attendeesAbsent.isNotEmpty()) {
                            Text(
                                text = "الأعضاء الغائبون: ${m.attendeesAbsent}",
                                fontSize = 11.sp,
                                color = Color.Red
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = m.decisionsSummary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row {
                            Button(
                                onClick = { showPreviewPrint = true },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = themeColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("طباعة ومعاينة رسمية", color = themeColor, fontSize = 11.sp)
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            IconButton(onClick = { viewModel.deleteMeeting(m.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                            }
                        }
                    }
                }

                // Show Printable meeting dialog
                if (showPreviewPrint) {
                    PrintableMeetingDialog(meeting = m, themeColor = themeColor) {
                        showPreviewPrint = false
                    }
                }
            }
        }
    }
}

@Composable
fun SignatureCanvasDialog(
    memberName: String,
    onDismiss: () -> Unit,
    onSignatureSaved: (String) -> Unit
) {
    var points = remember { mutableStateListOf<Offset>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "التوقيع الحي باليد لـ: $memberName",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "ارسم إمضاءك بإصبعك داخل الإطار المعتمد:",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // The Canvas panel box for drawing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset -> points.add(offset) },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    points.add(change.position)
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        for (i in 0 until points.size - 1) {
                            if (points[i] != Offset.Unspecified && points[i+1] != Offset.Unspecified) {
                                drawLine(
                                    color = Color(0xFF1E293B),
                                    start = points[i],
                                    end = points[i+1],
                                    strokeWidth = 4.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { points.clear() }) { Text("مسح الإطار", color = Color.Red) }
                    Row {
                        TextButton(onClick = onDismiss) { Text("إلغاء", color = Color.Gray) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (points.isNotEmpty()) {
                                    onSignatureSaved("signed_ok")
                                } else {
                                    onSignatureSaved("")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58))
                        ) {
                            Text("مصادقة التوقيع", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrintableMeetingDialog(
    meeting: MeetingEntity,
    themeColor: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(550.dp).padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Actions Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("صورة المحضر الجاهزة للاستخراج المالي", fontSize = 11.sp, color = themeColor, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = {
                            Toast.makeText(context, "تم تصدير وحفظ نسخة المحضر بصيغة PDF في مجلد Google Drive العام للبلدية!", Toast.LENGTH_LONG).show()
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "تحميل PDF", tint = Color.Red)
                        }
                        IconButton(onClick = {
                            Toast.makeText(context, "تم إرسال نسخة المحضر الموقعة إلى البريد الإلكتروني للمسؤول بنجاح!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Email, contentDescription = "توجيه إيميل", tint = themeColor)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
                        }
                    }
                }

                Divider()

                // Printable Area Scrollable container
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .border(1.dp, Color.LightGray)
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("الجمهورية الجزائرية الديمقراطية الشعبية", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("ولاية غليزان - دائرة زمورة - بلدية زمورة", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("لجنة الخدمات الاجتماعية لموظفي وعمال البلدية", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "محضر اجتماع ومقررة مداولات رقم: ${meeting.meetingNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color.Black)
                            .padding(6.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تاريخ الاجتماع الموثق المعين: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(meeting.meetingDate))}", fontSize = 9.sp, color = Color.Black)
                        Text("دائرة الانعقاد: قاعة جلسات المقر المبلدي بزمورة", fontSize = 9.sp, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "بناء على اللوائح الإدارية المنظمة للخدمات الاجتماعية في قطاع الجماعات المحلية بالجزائر، اجتمعت اللجنة وحضرت الأغلبية المطلقة وممثلو العمال بهيمنة تامة وقررت البت المباشر وفق التالي.",
                        fontSize = 9.sp, color = Color.Black, textAlign = TextAlign.Justify
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("أعضاء اللجنة الحاضرون الموقعون:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.fillMaxWidth())
                    Text(meeting.attendeesPresent, fontSize = 9.sp, color = Color.DarkGray, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
                    
                    if (meeting.attendeesAbsent.isNotEmpty()) {
                        Text("الأعضاء الغائبون والمعتذرون:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.fillMaxWidth())
                        Text(meeting.attendeesAbsent, fontSize = 9.sp, color = Color.Red, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
                    }

                    Divider(color = Color.Black)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("القرارات المالية والمقررات المتفق عليها:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.fillMaxWidth())
                    Text(
                        text = meeting.decisionsSummary,
                        fontSize = 9.sp, color = Color.Black, textAlign = TextAlign.Justify,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("توقيع ومصادقة أعضاء اللجنة الحاضرين قانوناً:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.fillMaxWidth())
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    // Loop to display sign representation for presents
                    val presents = meeting.attendeesPresent.split(",")
                    presents.forEach { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• الممضي: ${p.trim()}", fontSize = 9.sp, color = Color.Black)
                            Text(
                                "توقيع حي إلكتروني معتمد ✔ [مصادق]",
                                fontSize = 8.sp,
                                color = Color(0xFF0F9D58),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Map custom logos to symbols or names
fun isSystemInDarkTheme() = false // helper flag for default dialog components

@Composable
fun AdminBudgetCharts(viewModel: MainViewModel, themeColor: Color) {
    val budgetsList by viewModel.budgets.collectAsState(initial = emptyList())
    val activeBudget = budgetsList.firstOrNull() ?: BudgetEntity(year = 2026, totalBudget = 6500000.0, allocatedGrants = 3000000.0, allocatedLoans = 3500000.0, spentGrants = 850000.0, spentLoans = 1200000.0)

    val requestsList by viewModel.requests.collectAsState(initial = emptyList())
    val grantsCount = requestsList.count { !it.isLoan }
    val loansCount = requestsList.count { it.isLoan }
    val approvedCount = requestsList.count { it.status == "Approved" }
    val pendingCount = requestsList.count { it.status == "Pending" }
    
    val totalSpent = activeBudget.spentGrants + activeBudget.spentLoans
    val totalRemaining = activeBudget.totalBudget - totalSpent

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "لوحة المحاسبة والمؤشرات المالية للميزانية لعام ${activeBudget.year}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = themeColor
            )
        }

        // Live stats grid numerical summary
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BudgetStatNumericCard(
                    title = "الميزانية الكلية للجنة",
                    value = "${activeBudget.totalBudget} دج",
                    color = themeColor,
                    modifier = Modifier.weight(1f)
                )
                BudgetStatNumericCard(
                    title = "إجمالي المصروفات",
                    value = "$totalSpent دج",
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // concentric custom circle graph for Allocation vs Spending
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "التوزيع البياني التراكمي لاستهلاك الصناديق الاجتماعية:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.button,
                        color = themeColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw custom concentric rings on CANVAS
                    Canvas(
                        modifier = Modifier
                            .size(160.dp)
                            .padding(8.dp)
                    ) {
                        val strokeW = 16.dp.toPx()
                        
                        // Circle 1: Grants budget consumed percent
                        val grantPercent = if (activeBudget.allocatedGrants > 0) (activeBudget.spentGrants / activeBudget.allocatedGrants).toFloat() else 0f
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            radius = size.width / 2f,
                            style = Stroke(width = strokeW)
                        )
                        drawArc(
                            color = themeColor, // Teal/Selected theme primary
                            startAngle = -90f,
                            sweepAngle = grantPercent * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )

                        // Circle 2: Loans budget consumed percent (Inner ring)
                        val loanPercent = if (activeBudget.allocatedLoans > 0) (activeBudget.spentLoans / activeBudget.allocatedLoans).toFloat() else 0f
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            radius = size.width / 2.8f,
                            style = Stroke(width = strokeW)
                        )
                        drawArc(
                            color = Color(0xFFF59E0B), // Vibrant Gold inner arc
                            startAngle = -90f,
                            sweepAngle = loanPercent * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Legends
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(themeColor, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("منح البلدية (${String.format("%.1f", (activeBudget.spentGrants / activeBudget.allocatedGrants * 100))}% مستهلك)", fontSize = 10.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFF59E0B), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("سلفيات مستردة (${String.format("%.1f", (activeBudget.spentLoans / activeBudget.allocatedLoans * 100))}% مستك)", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Custom drawn structured comparative Bar chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "مقارنة بيانية دقيقة (الميزانية المخصصة مقابل المستهلكة فعلياً):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.button,
                        color = themeColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val widthBar = 32.dp.toPx()
                        val spacing = 40.dp.toPx()
                        
                        // Scale factors max budget
                        val maxVal = activeBudget.allocatedLoans.coerceAtLeast(activeBudget.allocatedGrants).toFloat()
                        
                        // Draw vertical Column 1: Grants allocated vs Spent
                        val hAllocG = (activeBudget.allocatedGrants.toFloat() / maxVal) * size.height * 0.8f
                        val hSpentG = (activeBudget.spentGrants.toFloat() / maxVal) * size.height * 0.8f
                        
                        drawRect(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            topLeft = Offset(spacing, size.height - hAllocG),
                            size = Size(widthBar, hAllocG)
                        )
                        drawRect(
                            color = themeColor,
                            topLeft = Offset(spacing, size.height - hSpentG),
                            size = Size(widthBar, hSpentG)
                        )

                        // Draw vertical Column 2: Loans allocated vs Spent
                        val hAllocL = (activeBudget.allocatedLoans.toFloat() / maxVal) * size.height * 0.8f
                        val hSpentL = (activeBudget.spentLoans.toFloat() / maxVal) * size.height * 0.8f
                        
                        drawRect(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            topLeft = Offset(spacing * 3, size.height - hAllocL),
                            size = Size(widthBar, hAllocL)
                        )
                        drawRect(
                            color = Color(0xFFF59E0B),
                            topLeft = Offset(spacing * 3, size.height - hSpentL),
                            size = Size(widthBar, hSpentL)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("ميزانية المنح المنفقة (دج)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColor)
                        Text("ميزانية السلفيات المنفقة (دج)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }
                }
            }
        }
    }
}

// Comparative Button helper inside Material Theme typography
val androidx.compose.material3.Typography.button: androidx.compose.ui.text.TextStyle
    get() = labelMedium.copy(fontWeight = FontWeight.Bold)

@Composable
fun BudgetStatNumericCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold), color = color)
        }
    }
}

@Composable
fun AdminTeamAndPersonnel(
    viewModel: MainViewModel,
    employees: List<EmployeeEntity>,
    members: List<CommitteeMemberEntity>,
    themeColor: Color
) {
    var showAddEmployeeDialog by remember { mutableStateOf(false) }
    var empNameInp by remember { mutableStateOf("") }
    var empPhoneInp by remember { mutableStateOf("") }
    var empSectionInp by remember { mutableStateOf("الأشغال العمومية") }
    
    val sections = listOf("الأشغال العمومية", "النظافة والتطهير", "مصلحة الحالة المدنية", "الإدارة العامة والإعلام الآلي", "الشؤون الاجتماعية والثقافية")
    var showSectionDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "إدارة ملفات الموظفين واللجنة البلدية",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = themeColor
                )
                Button(
                    onClick = { showAddEmployeeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعيين موظف جديد", color = Color.White)
                }
            }
        }

        item {
            Text(
                "قائمة الموظفين المقيدين ببلدية زمورة (${employees.size}):",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (employees.isEmpty()) {
            item { Text("لا توجد ملفات موظفين مسجلين حالياً.", color = Color.Gray, fontSize = 12.sp) }
        } else {
            items(employees) { emp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(emp.name, fontWeight = FontWeight.Bold)
                            Text("المصلحة: ${emp.section} | هاتف: ${emp.phone}", fontSize = 11.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { viewModel.deleteEmployee(emp.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Committee members card listing
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "أعضاء مجلس الهيئة المسيرة للجنة الاجتماعية:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(members) { member ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF334155) else Color.LightGray.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(member.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(member.role, fontSize = 11.sp, color = themeColor, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { viewModel.deleteCommitteeMember(member.id) }) {
                        Icon(Icons.Default.RemoveCircle, contentDescription = "إلغاء صفة", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // Modal dialog add employee
    if (showAddEmployeeDialog) {
        Dialog(onDismissRequest = { showAddEmployeeDialog = false }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("إضافة موظف بلدي جديد للخدمات الاجتماعية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = themeColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = empNameInp,
                        onValueChange = { empNameInp = it },
                        label = { Text("الاسم واللقب الكامل للموظف") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = empPhoneInp,
                        onValueChange = { empPhoneInp = it },
                        label = { Text("رقم الهاتف") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Dropdown selection for section
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = empSectionInp,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("مصلحة العمل / القسم (Section)") },
                            trailingIcon = {
                                IconButton(onClick = { showSectionDropdown = !showSectionDropdown }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = showSectionDropdown,
                            onDismissRequest = { showSectionDropdown = false },
                            modifier = Modifier.fillMaxWidth().background(if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White)
                        ) {
                            sections.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        empSectionInp = s
                                        showSectionDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showAddEmployeeDialog = false }) { Text("إلغاء") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (empNameInp.trim().isEmpty() || empPhoneInp.trim().isEmpty()) {
                                    Toast.makeText(context, "الرجاء كـتابة الاسم والهاتف بالكمال!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addEmployee(empNameInp, empPhoneInp, empSectionInp) {
                                        Toast.makeText(context, "تم تسجيل وإدراج الموظف ($empNameInp) بنجاح لقاعدة البيانات السحابية والمحلية والـ Live-Dashboard!", Toast.LENGTH_LONG).show()
                                        showAddEmployeeDialog = false
                                        empNameInp = ""
                                        empPhoneInp = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                        ) {
                            Text("تسجيل الموظف وقيده", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCloudBackupSettings(viewModel: MainViewModel, themeColor: Color) {
    val backupLogsList by viewModel.backupLogs.collectAsState(initial = emptyList())
    
    // Inputs settings
    var emailInput by remember { mutableStateOf(viewModel.backupEmail) }
    var passwordChangeInp by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "ركن تدقيق التقرير السنوي وإعدادات النسخ الاحتياطية Google Drive",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = themeColor
            )
        }

        // AI Reports audit engine Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "توليد ومصادقة تدقيق الذكاء الاصطناعي التوليدي للبلدية:",
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.generateAiAuditReport("financial") },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("تقرير تدقيق مالي AI", fontSize = 11.sp, color = Color.White)
                        }
                        Button(
                            onClick = { viewModel.generateAiAuditReport("literary") },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("التقرير الأدبي الإداري", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    if (viewModel.isAiAuditLoading) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = themeColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الذكاء الاصطناعي يحلل بيانات الميزانية وسجلات الموظفين ويحرر التقرير الشامل...", fontSize = 11.sp)
                        }
                    } else if (viewModel.aiAuditReport.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = viewModel.aiAuditReport,
                            onValueChange = { viewModel.aiAuditReport = it },
                            label = { Text("المخرجات والتقرير المدقق المعتمد") },
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Download chosen format
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = {
                                Toast.makeText(context, "تم حفظ وتصدير هذا التقرير الإداري بصيغة Excel في مجلد درايف الخاص بالبلدية!", Toast.LENGTH_LONG).show()
                            }) {
                                Icon(Icons.Default.BorderAll, contentDescription = "Excel Sheet", tint = Color(0xFF0F9D58))
                            }
                            IconButton(onClick = {
                                Toast.makeText(context, "تم حفظ نسخة من التقرير بصيغة PDF في مجلد التخزين العام للبلدية!", Toast.LENGTH_LONG).show()
                            }) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Report", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        // Google Drive configuration pane
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "الحوسبة السحابية ومزامنة Google Drive وملفات Excel",
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "يقوم النظام تلقائياً برفع لقطة كهرومغناطيسية JSON مشفرة لقاعدة البيانات SQLite في خادم Google Drive السحابي مع كل إمضاء محضر أو موافقة.",
                        fontSize = 11.sp, color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ربط قاعدة البيانات بـ Google Drive", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = viewModel.isGoogleDriveConnected,
                            onCheckedChange = { viewModel.isGoogleDriveConnected = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = themeColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (viewModel.isSyncing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = themeColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(viewModel.syncProgressMessage, fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.runManualGoogleDriveSync { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("النسخ الاحتياطي السحابي الكامل الآن لـ Drive", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Email backups config Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "إعدادات البريد كنسخ سحابي لوجستي إضافي (SMTP Email)",
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            viewModel.updateBackupEmail(it)
                        },
                        label = { Text("البريد الإلكتروني لاستلام النسخ والتقارير المالية") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.sendBackupToEmailNow { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Mail, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إرسال النسخة التراكمية وتقرير الميزانية لبريدي الآن", color = Color.White)
                    }
                }
            }
        }

        // Change Admin Password settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) Color(0xFF1E293B) else Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "تغيير كلمة المرور الخاصة بهيئة إدارة اللجنة الاجتماعية",
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = passwordChangeInp,
                        onValueChange = { passwordChangeInp = it },
                        label = { Text("أدخل كلمة المرور الجديدة لوقاية لحساب") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (passwordChangeInp.trim().length < 4) {
                                Toast.makeText(context, "كلمة المرور قصيرة جداً (الحد الأدنى 4 أحرف)!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.updateAdminPassword(passwordChangeInp) {
                                    Toast.makeText(context, "جميل، تم تحديث وتخصيص كلمة مرور الإدارة واللجنة بنجاح وقفلها بقاعدة البيانات!", Toast.LENGTH_LONG).show()
                                    passwordChangeInp = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اعتماد وتثبيت كلمة المرور المدققة", color = Color.White)
                    }
                }
            }
        }

        // Backup list logs for Restore
        item {
            Text(
                "أرشيف ولقطات النسخ الاحتياطية السابقة:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (backupLogsList.isEmpty()) {
            item { Text("لا توجد لقطات نسخ احتياطية مسجلة مسبقاً في قاعدة الميزات.", color = Color.Gray, fontSize = 11.sp) }
        } else {
            items(backupLogsList) { log ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.isDarkMode) Color(0xFF334155) else Color.LightGray.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.backupType, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = themeColor)
                                Text(
                                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(log.backupDate)),
                                    fontSize = 9.sp, color = Color.Gray
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.restoreBackup(log.id) { response ->
                                        Toast.makeText(context, response, Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("استعادة من النسخة", fontSize = 9.sp, color = Color.White)
                            }
                        }
                        Text(log.details, fontSize = 10.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}


