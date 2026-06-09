package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.model.Program
import com.example.ui.theme.*
import com.example.ui.components.MilitaryProgressBar
import com.example.ui.components.RadarWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    program: Program,
    onBack: () -> Unit,
    onStartProgramChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryTab by remember { mutableIntStateOf(0) }
    val categories = listOf("نظرة عامة", "الخطة الدراسية", "مخرجات التعلم", "القبول والأسئلة", "تعقّب التقدم", "المستشار الذكي")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تفاصيل البرنامج الأكاديمي",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldenBrass
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = GoldenBrass
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = GoldenBrass
                ),
                modifier = Modifier.border(0.dp, Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartProgramChat,
                containerColor = GoldenBrass,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "محادثة",
                        tint = Color.Black
                    )
                },
                text = {
                    Text(
                        text = "محادثة مستشار البرنامج",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = CairoFont
                    )
                }
            )
        },
        containerColor = DarkNavy,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Program Hero Section with a premium top gradient (from-[#1A2F47] to-[#0D1B2A])
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SolidNavy, DarkNavy)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Category Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tactical Department tag
                    Box(
                        modifier = Modifier
                            .background(GoldenBrass.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = program.department.substringBefore(" بكلية").trim(),
                            color = GoldenBrass,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Level/Degree tag
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = program.degree,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = program.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 30.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Short Description
                Text(
                    text = program.description,
                    fontSize = 13.sp,
                    color = Slate400,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Key Stats Grid (3 Col Grid inline with theme)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val defaultHours = if (program.id.contains("diploma")) "98" else "142"
                    val studyTermsCount = if (program.id.contains("diploma")) "4" else "8"
                    listOf(
                        defaultHours to "ساعة معتمدة",
                        program.duration to "المدة الدراسية",
                        "منهجية ${studyTermsCount} فصول" to "الفصول الدراسية"
                    ).forEach { (value, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(StatCardBackground, RoundedCornerShape(16.dp))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(16.dp))
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = value,
                                    color = GoldenBrass,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    color = Slate400,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Material 3 Custom Bottom Line Tab Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy)
                    .horizontalScroll(rememberScrollState())
                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)))
                    .padding(vertical = 2.dp)
            ) {
                categories.forEachIndexed { index, title ->
                    val selected = selectedCategoryTab == index
                    Box(
                        modifier = Modifier
                            .clickable { selectedCategoryTab = index }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) GoldenBrass else Color.White.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(2.dp)
                                    .background(if (selected) GoldenBrass else Color.Transparent)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Tab Views with Professional 24.dp Rounded Cards and Borders
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                when (selectedCategoryTab) {
                    0 -> OverviewSection(program)
                    1 -> CurriculumSection(program)
                    2 -> PlosSection(program)
                    3 -> AdmissionFaqSection(program)
                    4 -> ProgressTrackerSection(program)
                    5 -> GeminiChatbotSection(program)
                }
            }
        }
    }
}

@Composable
fun OverviewSection(program: Program) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(GoldenBrass.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 16.sp)
                        }
                        Text(
                            text = "وصف البرنامج الأكاديمي المطور",
                            fontWeight = FontWeight.Bold,
                            color = GoldenBrass,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = program.description,
                        color = Slate300,
                        fontSize = 13.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(GoldenBrass.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📋", fontSize = 16.sp)
                        }
                        Text(
                            text = "تفاصيل ومؤشرات البرنامج",
                            fontWeight = FontWeight.Bold,
                            color = GoldenBrass,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(label = "القسم الأكاديمي التخصصي", value = program.department)
                    DetailRow(label = "مدة الدراسة والتدريب الميداني", value = program.duration)
                    DetailRow(label = "لغة التعليم الأكاديمي والتدريب", value = program.language)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(GoldenBrass.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎯", fontSize = 16.sp)
                        }
                        Text(
                            text = "أهداف البرنامج والخطط الاستراتيجية",
                            fontWeight = FontWeight.Bold,
                            color = GoldenBrass,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    program.objectives.forEachIndexed { _, obj ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "●", color = GoldenBrass, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            Text(
                                text = obj,
                                fontSize = 13.sp,
                                color = Slate300,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurriculumSection(program: Program) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(program.curriculum) { term ->
            var expanded by remember { mutableStateOf(true) }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = term.termName,
                            fontWeight = FontWeight.Bold,
                            color = GoldenBrass,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "توسيع",
                            tint = GoldenBrass
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.05f))
                            )
                            Column(modifier = Modifier.padding(18.dp)) {
                                term.courses.forEachIndexed { i, course ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = course.name,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "كود المقرر الأمني: ${course.code}",
                                                fontSize = 11.sp,
                                                color = Slate400
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(GoldenBrass.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .border(BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${course.hours} ساعات",
                                                fontSize = 10.sp,
                                                color = GoldenBrass,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (i < term.courses.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(0.5.dp)
                                                .background(Color.White.copy(alpha = 0.05f))
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
}

@Composable
fun PlosSection(program: Program) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "مخرجات التعلم المستهدفة المعتمدة للبرنامج",
                fontWeight = FontWeight.Bold,
                color = GoldenBrass,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        items(program.plos) { plo ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GoldenBrass.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎖️", fontSize = 16.sp)
                    }
                    Text(
                        text = plo,
                        fontSize = 13.sp,
                        color = Slate300,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun AdmissionFaqSection(program: Program) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(GoldenBrass.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔑", fontSize = 16.sp)
                        }
                        Text(
                            text = "شروط وكفاءات القبول والتسجيل",
                            fontWeight = FontWeight.Bold,
                            color = GoldenBrass,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = program.admissionRequirements,
                        fontSize = 13.sp,
                        color = Slate300,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(GoldenBrass.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎓", fontSize = 16.sp)
                        }
                        Text(
                            text = "شروط التخرج والجاهزية الطبية الميدانية",
                            fontWeight = FontWeight.Bold,
                            color = GoldenBrass,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = program.graduationRequirements,
                        fontSize = 13.sp,
                        color = Slate300,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "الاستقصاءات والأسئلة الشائعة الأكاديمية",
                fontWeight = FontWeight.Bold,
                color = GoldenBrass,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(program.faq) { faqItem ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "س: ${faqItem.q}",
                            fontWeight = FontWeight.Bold,
                            color = GoldenBrass,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "عرض السائل",
                            tint = GoldenBrass.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (expanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(Color.White.copy(alpha = 0.05f))
                        )
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.15f))
                                .padding(14.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "ج: ${faqItem.a}",
                                fontSize = 13.sp,
                                color = Slate300,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = Slate400, fontSize = 13.sp)
            Text(text = value, color = GoldenBrass, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.05f))
        )
    }
}

// ==========================================
// 1. PROGRESS TRACKER COMPONENT (STUDENT PROGRESS CHECKPOINTS)
// ==========================================

data class TrackerItem(
    val key: String,
    val text: String,
    val category: String,
    val subtitle: String = ""
)

@Composable
fun ProgressTrackerSection(program: Program) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("program_progress_pref", Context.MODE_PRIVATE) }
    
    val trackerItems = remember(program) {
        val items = mutableListOf<TrackerItem>()
        // Add academic courses from curriculum
        program.curriculum.forEach { term ->
            term.courses.forEach { course ->
                items.add(
                    TrackerItem(
                        key = "course_${course.code}",
                        text = course.name,
                        category = "مقررات: ${term.termName}",
                        subtitle = "رمز المقرر: ${course.code} | ساعات معتمدة: ${course.hours}"
                    )
                )
            }
        }
        // Add graduation and readiness checkpoints
        items.add(TrackerItem("req_doc", "تأمين الملف الأكاديمي والوثائق الطبية المعتمدة بنص الشروط", "متطلبات عامة وجاهزية", "مراجعة واعتماد الإدارة وشؤون الطلاب"))
        items.add(TrackerItem("req_physical", "اجتياز اختبارات اللياقة البدنية والتحمل الميداني العسكري", "متطلبات عامة وجاهزية", "التحمل وسرعة التدخل التكتيكي"))
        items.add(TrackerItem("req_clinical", "استكمال الساعات السريرية والتدريب التفاعلي بالمشافي والخنادق", "متطلبات عامة وجاهزية", "سجل الحالات والأنشطة الصحية المكتملة"))
        items.add(TrackerItem("req_final_exam", "النجاح في اختبار الهيئة العليا للجنة الطبية للشهيد طاووس", "متطلبات عامة وجاهزية", "التقييم الشامل الموحد للدراسة"))
        items.add(TrackerItem("req_pledge", "توقيع وثيقة الشرف الطبي للالتزام بالخدمة في جبهات الصمود", "متطلبات عامة وجاهزية", "العهد والمسؤولية الميدانية النهائية"))
        items
    }
    
    val checkedStates = remember(program) {
        mutableStateMapOf<String, Boolean>().apply {
            trackerItems.forEach { item ->
                put(item.key, sharedPref.getBoolean("checked_${program.id}_${item.key}", false))
            }
        }
    }
    
    val checkedCount = checkedStates.values.count { it }
    val totalCount = trackerItems.size
    val progressValue = if (totalCount > 0) checkedCount.toFloat() / totalCount.toFloat() else 0f
    val progressPercent = (progressValue * 100).toInt()
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Progress Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "مستوى الإنجاز الأكاديمي والجاهزية المعنوية",
                        fontWeight = FontWeight.Bold,
                        color = GoldenBrass,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${checkedCount} من أصل ${totalCount} متطلبات مكتملة",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$progressPercent%",
                            color = GoldenBrass,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = GoldenBrass,
                        trackColor = LightNavy.copy(alpha = 0.3f)
                    )
                }
            }
        }
        
        // Group items by category
        val grouped = trackerItems.groupBy { it.category }
        grouped.forEach { (category, items) ->
            item {
                Text(
                    text = category,
                    fontWeight = FontWeight.Bold,
                    color = GoldenBrass,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(items) { item ->
                val checked = checkedStates[item.key] ?: false
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (checked) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.02f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp, 
                        if (checked) GoldenBrass.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newChecked = !checked
                            checkedStates[item.key] = newChecked
                            sharedPref.edit().putBoolean("checked_${program.id}_${item.key}", newChecked).apply()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { newChecked ->
                                checkedStates[item.key] = newChecked
                                sharedPref.edit().putBoolean("checked_${program.id}_${item.key}", newChecked).apply()
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = GoldenBrass,
                                uncheckedColor = Color.White.copy(alpha = 0.25f),
                                checkmarkColor = Color.Black
                            )
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.text,
                                fontSize = 13.sp,
                                fontWeight = if (checked) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (checked) Color.White else Color(0xFFE2E8F0),
                            )
                            if (item.subtitle.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.subtitle,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. EMBEDDED CONVERSATIONAL GEMINI CHATBOT
// ==========================================

data class DetailChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" or "assistant"
    val content: String,
    var liked: Boolean = false
)

@Composable
fun GeminiChatbotSection(program: Program) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var inputQuery by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf<DetailChatMessage>() }
    var isTyping by remember { mutableStateOf(false) }

    val queryChips = remember(program) {
        listOf(
            "ما هي شروط القبول والتسجيل؟",
            "ما هي المقررات المعتمدة بالخطة الدراسية؟",
            "ما هي أهداف ومخرجات التعلم؟",
            "ما هي متطلبات التخرج النهائية؟"
        )
    }

    // Load initial welcome message
    LaunchedEffect(program) {
        if (chatMessages.isEmpty()) {
            chatMessages.add(
                DetailChatMessage(
                    role = "assistant",
                    content = """
                        مرحباً بك في النافذة الاستشارية التفاعلية لبرنامج **"${program.title}"** المستندة إلى خطة الدكتور الشهيد زيد طاووس للعلوم الطبية العسكرية. 🛡️
                        
                        اسألني أي سؤال بخصوص شروط القبول والمعدلات وسجل الساعات والمقررات، وسأقوم بالرد فوراً معتمداً على الذكاء الاصطناعي Gemini ومطابقة أسئلتك ببيانات البرنامج الرسمية!
                    """.trimIndent()
                )
            )
        }
    }

    val sendMessage = { queryText: String ->
        if (queryText.trim().isNotEmpty()) {
            chatMessages.add(DetailChatMessage(role = "user", content = queryText))
            inputQuery = ""
            isTyping = true

            scope.launch {
                val reply = askProgramDetailBot(program, queryText)
                chatMessages.add(DetailChatMessage(role = "assistant", content = reply))
                isTyping = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            queryChips.forEach { chip ->
                Surface(
                    onClick = { sendMessage(chip) },
                    color = LightNavy.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = chip,
                        fontSize = 11.sp,
                        color = GoldenBrass,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat History List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        color = if (isUser) GoldenBrass else Color.White.copy(alpha = 0.03f),
                        border = if (!isUser) BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "◀ الطالب:" else "🤖 المستشار الأكاديمي المباشر:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isUser) Color.Black.copy(alpha = 0.6f) else GoldenBrass,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = msg.content,
                                color = if (isUser) Color.Black else Color(0xFFF1F5F9),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal
                            )

                            if (!isUser) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Copy
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Reply", msg.content)
                                            clipboard.setPrimaryClip(clip)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "نسخ",
                                            tint = GoldenBrass.copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    // Share
                                    IconButton(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "${program.title}:\n\n${msg.content}")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة"))
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "مشاركة",
                                            tint = GoldenBrass.copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    // Like
                                    var msgLiked by remember { mutableStateOf(msg.liked) }
                                    IconButton(
                                        onClick = {
                                            msgLiked = !msgLiked
                                            msg.liked = msgLiked
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (msgLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "إعجاب",
                                            tint = if (msgLiked) Color.Red else GoldenBrass.copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isTyping) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.02f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadarWidget(modifier = Modifier.size(24.dp))
                                Text(
                                    text = "جاري الاتصال وصياغة الإجابة التكتيكية...",
                                    color = GoldenBrass,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("اطرح تساؤلاً حول هذا البرنامج...", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldenBrass,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = LightNavy.copy(alpha = 0.15f),
                    unfocusedContainerColor = LightNavy.copy(alpha = 0.08f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            )

            FloatingActionButton(
                onClick = { sendMessage(inputQuery) },
                containerColor = GoldenBrass,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "إرسال",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private suspend fun askProgramDetailBot(program: Program, query: String): String = withContext(Dispatchers.IO) {
    val apiKey = try {
        BuildConfig.GEMINI_API_KEY
    } catch (e: Exception) {
        ""
    }

    val hasGemini = apiKey.isNotEmpty() && !apiKey.contains("placeholder") && !apiKey.contains("MY_GEMINI")

    val objBuilder = StringBuilder()
    program.objectives.forEachIndexed { idx, obj -> objBuilder.append("${idx + 1}. $obj\n") }

    val termBuilder = StringBuilder()
    program.curriculum.forEach { term ->
        termBuilder.append("- __${term.termName}__:\n")
        term.courses.forEach { course ->
            termBuilder.append("  • ${course.name} [رمز: ${course.code}, ساعات: ${course.hours}]\n")
        }
    }

    val systemPrompt = """
        أنت رائد ومستشار أكاديمي عسكري مخصص بقسم المنهج والتخطيط لبرنامج "${program.title}".
        الجهة الأكاديمية: ${program.department}
        الدرجة: ${program.degree}
        المدة وفصول الدراسة: ${program.duration}
        لغة التعليم: ${program.language}
        
        شروط القبول:
        ${program.admissionRequirements}
        
        شروط التخرج:
        ${program.graduationRequirements}
        
        الأهداف الاستراتيجية للبرنامج:
        ${objBuilder}
        
        الخطة الدراسية والمقررات:
        ${termBuilder}
        
        الرجاء مراجعة أسئلة المنهج الشائعة لإعطاء تفاصيل دقيقة:
        ${program.faq.joinToString("\n") { "س: ${it.q} - ج: ${it.a}" }}
        
        التعليمات:
        - أجب باللغة العربية بدقة متناهية وبأسلوب عسكري وقور ملخص ومنظم.
        - ركز إجابتك حصراً وبطريقة تطبيقية على محتوى ومقررات وتفاصيل ومخرجات برنامج "${program.title}" دون تفرع خارجي.
        - استخدم الرموز الطبية والجهادية واللوجستية مثل ⚕️, 🛡️, 🎖️ بانسجام تام.
    """.trimIndent()

    if (hasGemini) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        try {
            val jsonObject = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", query)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonObject.toString().toRequestBody(mediaType)
            val request = Request.Builder().url(url).post(requestBody).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val resObj = JSONObject(body)
                    val candidates = resObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // fall back to heuristic
        }
    }

    // Heuristic Fallback
    val lowerQuery = query.lowercase()
    if (lowerQuery.contains("شرط") || lowerQuery.contains("قبول") || lowerQuery.contains("التحاق") || lowerQuery.contains("تسجيل")) {
        return@withContext "🎖️ **شروط وكفاءات القبول المحددة للبرنامج:**\n${program.admissionRequirements}"
    }
    if (lowerQuery.contains("تخرج") || lowerQuery.contains("انه") || lowerQuery.contains("مؤهل") || lowerQuery.contains("نجاح")) {
        return@withContext "🎖️ **شروط التخرج والتقييم العسكري النهائي:**\n${program.graduationRequirements}"
    }
    if (lowerQuery.contains("مقرر") || lowerQuery.contains("خطة") || lowerQuery.contains("درس") || lowerQuery.contains("فصل") || lowerQuery.contains("ساع")) {
        return@withContext "🎖️ **الخطة والمناهج الدراسية لـ ${program.title}:**\n$termBuilder"
    }
    if (lowerQuery.contains("هدف") || lowerQuery.contains("رؤية") || lowerQuery.contains("استراتيج")) {
        return@withContext "🎖️ **الأهداف التطبيقية المحددة للبرنامج:**\n$objBuilder"
    }

    val matchedFaq = program.faq.firstOrNull { it.q.contains(query) || query.contains(it.q) }
    if (matchedFaq != null) {
        return@withContext "🎖️ **جـ:** ${matchedFaq.a}"
    }

    return@withContext "🎖️ تم استلام استفسارك الأكاديمي عسكرياً ومطابقتها للمنهج.\nهذا البرنامج يتكون من ${program.duration} ويُدرس باللغة ${program.language}. للاستفسارات التفاعلية الدقيقة يتطلب وجود اتصال بخدمة ومفاتيح Gemini."
}
