package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProgramsData
import com.example.model.Program
import com.example.model.ProgramCourse
import com.example.model.ProgramTerm
import com.example.model.FAQItem
import com.example.ui.theme.*

@Composable
fun AppDrawerContent(
    isArabic: Boolean,
    onSelectProgram: (Program) -> Unit,
    previousChats: List<com.example.model.ChatSession>,
    onSelectPreviousChat: (com.example.model.ChatSession) -> Unit,
    onDeleteSession: (com.example.model.ChatSession) -> Unit,
    onNewChatClick: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var drawerTabSelected by remember { mutableIntStateOf(0) } // 0 = Academic Tree, 1 = Chat History

    // Expandable states for Tree
    var bachelorsExpanded by remember { mutableStateOf(false) }
    var diplomasExpanded by remember { mutableStateOf(true) }
    var coursesExpanded by remember { mutableStateOf(false) }
    var gradStudiesExpanded by remember { mutableStateOf(false) }

    val allPrograms = remember { ProgramsData.getAllPrograms() }
    val nursingProg = allPrograms.firstOrNull { it.id == "nursing_diploma" }
    val anesthesiaProg = allPrograms.firstOrNull { it.id == "anesthesia_diploma" }
    val mfaProg = allPrograms.firstOrNull { it.id == "military_first_aid" }

    val bachelorPrograms = listOf(
        "بكالوريوس الطب البشري والجراحة العسكرية",
        "إدارة طبية ميدانية",
        "تخدير عسكري أساسي",
        "مختبرات عسكرية طبية",
        "تمريض عسكري عام",
        "طب وقائي واستقصائي"
    )

    val trainingCourses = listOf(
        "دورة إسعاف المقاتل التكتيكي (TCCC)",
        "دورة إسعاف متقدم تكتيكي",
        "دورة إنعاش قلبي ممتد",
        "دورة السيطرة اللوجستية"
    )

    val postGradStudies = listOf(
        "الطب الجراحي العسكري المتقدم",
        "جراحة الحروب والكسور الجسيمة",
        "الطب الوقائي العسكري وإدارة الأزمات"
    )

    // Segment helper
    val synthesizedCatalogProgram = { title: String ->
        Program(
            id = "synthesized_" + title.hashCode(),
            title = title,
            degree = "بكالوريوس / ثقافة تخصصية عسكرية",
            department = "أقسام الأكاديمية العسكرية بكلية الطب",
            description = "مساق أكاديمي أمني تخصصي لإعداد ضباط قادرين على قيادة العمل الطبي واللوجستي بكفاءة واقتدار عملاً بروح ودور رائد الأطباء الشهيد د. زيد طاووس.",
            duration = "سير دراسي معتمد",
            language = "العربية والإنجليزية",
            objectives = listOf(
                "تطوير منظومة الرعاية التكتيكية والطبية للجرحى عسكرياً.",
                "ترسيخ منظومة الدعم السريري عالي الدقة في الخطوط والمشافي الميدانية."
            ),
            plos = listOf("K1 - فهم شامل للرعاية التكتيكية"),
            curriculum = listOf(
                ProgramTerm("الفصل الأول تمهيدي", listOf(ProgramCourse("MED101", "مصطلحات طبية تطبيقية", 3)))
            ),
            faq = listOf(FAQItem("ما شروط الانضمام؟", "اللياقة الطبية والبدنية المعتمدة عسكرياً.")),
            admissionRequirements = "أن يكون من منتسبي الكلية أو الحاصلين على ثانوية علمي بمعدل 80% فأكثر واجتياز كشوف اللياقة الشاملة.",
            graduationRequirements = "إكمال المساقات المقررة بنجاح واجتياز محاكاة الفحص السريري الموحد."
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(DarkNavy)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)))
    ) {
        // Drawer Header with Slate-to-Navy gradient and Golden emblem
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF162636), DarkNavy)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(GoldenBrass.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = GoldenBrass,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isArabic) "مرشد الشهيد د. زيد" else "Dr. Zaid Medical Advisor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GoldenBrass,
                    fontFamily = CairoFont
                )
                Text(
                    text = if (isArabic) "كلية الطب والعلوم الصحية العسكرية" else "Military Medical & Health Sciences College",
                    fontSize = 10.sp,
                    color = Slate400,
                    fontFamily = CairoFont
                )
            }
        }

        // Toggle Tabs (Academic Programs vs Chat History)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(Color.White.copy(alpha = 0.02f), CircleShape)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), CircleShape)
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (drawerTabSelected == 0) GoldenBrass else Color.Transparent)
                    .clickable { drawerTabSelected = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) "الشجرة الأكاديمية" else "Academic Tree",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (drawerTabSelected == 0) Color.Black else Slate300,
                    fontFamily = CairoFont
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (drawerTabSelected == 1) GoldenBrass else Color.Transparent)
                    .clickable { drawerTabSelected = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) "المحفوظات التكتيكية" else "Archive Log",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (drawerTabSelected == 1) Color.Black else Slate300,
                    fontFamily = CairoFont
                )
            }
        }

        Divider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)

        // Contents Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            if (drawerTabSelected == 0) {
                // TAB 0: Expandable Academic Directory tree
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Category 1: Bachelors list
                    item {
                        Column {
                            ExpandableSectionHeader(
                                title = if (isArabic) "برامج الكلية (البكالوريوس)" else "College Programs (Bachelors)",
                                expanded = bachelorsExpanded,
                                onToggle = { bachelorsExpanded = !bachelorsExpanded }
                            )
                            AnimatedVisibility(visible = bachelorsExpanded) {
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    bachelorPrograms.forEach { name ->
                                        TreeItemRow(title = name) {
                                            val matched = allPrograms.firstOrNull { it.title.contains(name) || name.contains(it.title) || it.id == "general_medicine" && name == "بكالوريوس الطب البشري والجراحة العسكرية" }
                                            onSelectProgram(matched ?: synthesizedCatalogProgram(name))
                                            onCloseDrawer()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Category 2: specialist Diplomas list (Active)
                    item {
                        Column {
                            ExpandableSectionHeader(
                                title = if (isArabic) "برامج المعهد (الدبلومات العسكرية)" else "Institute Programs (Diplomas)",
                                expanded = diplomasExpanded,
                                onToggle = { diplomasExpanded = !diplomasExpanded }
                            )
                            AnimatedVisibility(visible = diplomasExpanded) {
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    if (mfaProg != null) {
                                        TreeItemRow(title = mfaProg.title, activeHighlight = true) {
                                            onSelectProgram(mfaProg)
                                            onCloseDrawer()
                                        }
                                    }
                                    if (nursingProg != null) {
                                        TreeItemRow(title = nursingProg.title, activeHighlight = true) {
                                            onSelectProgram(nursingProg)
                                            onCloseDrawer()
                                        }
                                    }
                                    if (anesthesiaProg != null) {
                                        TreeItemRow(title = anesthesiaProg.title, activeHighlight = true) {
                                            onSelectProgram(anesthesiaProg)
                                            onCloseDrawer()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Category 3: Courses list
                    item {
                        Column {
                            ExpandableSectionHeader(
                                title = if (isArabic) "برامج الدورات التدريبية والترشيح" else "Training Courses",
                                expanded = coursesExpanded,
                                onToggle = { coursesExpanded = !coursesExpanded }
                            )
                            AnimatedVisibility(visible = coursesExpanded) {
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    trainingCourses.forEach { name ->
                                        TreeItemRow(title = name) {
                                            onSelectProgram(synthesizedCatalogProgram(name))
                                            onCloseDrawer()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Category 4: Postgraduate studies
                    item {
                        Column {
                            ExpandableSectionHeader(
                                title = if (isArabic) "الدراسات الأكاديمية العليا" else "Postgraduate Studies",
                                expanded = gradStudiesExpanded,
                                onToggle = { gradStudiesExpanded = !gradStudiesExpanded }
                            )
                            AnimatedVisibility(visible = gradStudiesExpanded) {
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    postGradStudies.forEach { name ->
                                        TreeItemRow(title = name) {
                                            onSelectProgram(synthesizedCatalogProgram(name))
                                            onCloseDrawer()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: Chat history and previous conversation archives
                Column(modifier = Modifier.fillMaxSize()) {
                    // New Chat Action Row
                    Surface(
                        onClick = {
                            onNewChatClick()
                            onCloseDrawer()
                        },
                        color = GoldenBrass.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp, top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = GoldenBrass,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "محادثة جديدة تكتيكية" else "New Tactical Chat",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldenBrass,
                                fontFamily = CairoFont
                            )
                        }
                    }

                    if (previousChats.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = GoldenBrass.copy(alpha = 0.2f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isArabic) "لا توجد محفوظات تكتيكية حالية" else "No previous archives logged",
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        ) {
                            items(previousChats) { session ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    onSelectPreviousChat(session)
                                                    onCloseDrawer()
                                                },
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(GoldenBrass.copy(alpha = 0.1f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ChatBubble,
                                                    contentDescription = null,
                                                    tint = GoldenBrass,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = session.title,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Slate300,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                                                    .format(java.util.Date(session.createdAt))
                                                Text(
                                                    text = dateStr,
                                                    fontSize = 9.sp,
                                                    color = Slate400
                                                )
                                            }
                                        }

                                        // Delete session button
                                        IconButton(
                                            onClick = { onDeleteSession(session) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Session",
                                                tint = CoralRed.copy(alpha = 0.8f),
                                                modifier = Modifier.size(14.dp)
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

        // Drawer lower footer with legal notes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isArabic) "تصديق عسكري - نظام مشفر ومحمي للكلية" else "Military Endorsement - Encrypted Node",
                fontSize = 9.sp,
                color = Slate400.copy(alpha = 0.5f),
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ExpandableSectionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (expanded) GoldenBrass else GoldenBrass.copy(alpha = 0.5f), CircleShape)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldenBrass,
                fontFamily = CairoFont
            )
        }
        Icon(
            imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = GoldenBrass.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun TreeItemRow(
    title: String,
    activeHighlight: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (activeHighlight) GoldenBrass.copy(alpha = 0.05f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = null,
            tint = if (activeHighlight) GoldenBrass else Slate400,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = title,
            fontSize = 11.sp,
            color = if (activeHighlight) GoldenBrass else Slate300,
            fontWeight = if (activeHighlight) FontWeight.Bold else FontWeight.Normal,
            fontFamily = CairoFont,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
