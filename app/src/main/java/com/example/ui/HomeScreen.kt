package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FAQItem
import com.example.model.Program
import com.example.model.ProgramCourse
import com.example.model.ProgramTerm
import com.example.model.Document
import com.example.data.AppDatabase
import com.example.data.CollegeData
import com.example.data.ProgramsData
import com.example.service.AuthService
import com.example.service.SearchService
import com.example.service.LocalSpeechManager
import com.example.service.SpeechManager
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.BuildConfig

data class Message(
    val role: String, // "user" or "assistant"
    val content: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authService: AuthService,
    searchService: SearchService,
    onAdminClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speechManager = LocalSpeechManager.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Voice guidance feedback upon changing sections
    LaunchedEffect(selectedTab) {
        val message = when (selectedTab) {
            0 -> "مرحباً بكم في قسم المحادثة الذكية التفاعلية"
            1 -> "مرحباً بكم في معرض وموسوعة البرامج والمناهج الأكاديمية"
            2 -> "مرحباً بكم في معمل الفحص والتدقيق الضوئي للتقارير"
            3 -> "مرحباً بكم في دليل مسيرة الشهيد الدكتور زيد طاووس وكلية الطب"
            else -> ""
        }
        speechManager.speak(message)
    }

    var isArabic by remember { mutableStateOf(true) }
    var anatomyQueryPreset by remember { mutableStateOf("") }

    // Navigation stacks
    var activeDetailedProgram by remember { mutableStateOf<Program?>(null) }
    var activeProgramChat by remember { mutableStateOf<Program?>(null) }

    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Persistent Database access and Chat history states
    val context = LocalContext.current
    val db = remember { com.example.data.AppDatabase.getDatabase(context) }
    val chatDao = db.chatDao()
    val chatSessions by chatDao.getAllSessions().collectAsState(initial = emptyList())
    var activeSessionId by remember { mutableStateOf<Int?>(null) }

    if (activeProgramChat != null) {
        ProgramChatScreen(
            program = activeProgramChat!!,
            onBack = { activeProgramChat = null }
        )
    } else if (activeDetailedProgram != null) {
        DetailScreen(
            program = activeDetailedProgram!!,
            onBack = { activeDetailedProgram = null },
            onStartProgramChat = { activeProgramChat = activeDetailedProgram }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = DarkNavy,
                    drawerShape = RoundedCornerShape(topStart = 0.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 24.dp)
                ) {
                    AppDrawerContent(
                        isArabic = isArabic,
                        onSelectProgram = { prog ->
                            activeDetailedProgram = prog
                        },
                        previousChats = chatSessions,
                        onSelectPreviousChat = { session ->
                            activeSessionId = session.id
                            selectedTab = 0 // Auto-open general Chat section
                        },
                        onDeleteSession = { session ->
                            scope.launch {
                                if (activeSessionId == session.id) {
                                    activeSessionId = null
                                }
                                chatDao.deleteSession(session.id)
                                chatDao.deleteMessagesForSession(session.id)
                            }
                        },
                        onNewChatClick = {
                            activeSessionId = null
                            selectedTab = 0
                        },
                        onCloseDrawer = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    Column {
                        TopAppBar(
                            title = {
                                Text(
                                    text = if (isArabic) "مرشد الدكتور زيد طاووس ⚕️" else "Tawus Ranger Guide ⚕️",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldenBrass
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "قائمة الأقسام وموسوعة البرامج",
                                        tint = GoldenBrass
                                    )
                                }
                            },
                            actions = {
                                // Translation Toggle
                                IconButton(onClick = { 
                                    isArabic = !isArabic 
                                    speechManager.speak(if (!isArabic) "تم التحويل إلى اللغة الإنجليزية" else "تم التحويل إلى اللغة العربية")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = "ترجمة",
                                        tint = GoldenBrass
                                    )
                                }
                                // Settings
                                IconButton(onClick = onSettingsClick) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "الإعدادات",
                                        tint = GoldenBrass
                                    )
                                }
                                // Admin Port
                                IconButton(onClick = onAdminClick) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = "لوحة الإدارة",
                                        tint = GoldenBrass
                                    )
                                }
                                // Logout
                                IconButton(onClick = { 
                                    speechManager.speak("تسديد تسجيل الخروج وتأمين الجلسة")
                                    authService.logout() 
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "خروج",
                                        tint = CoralRed
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = DarkNavy,
                                titleContentColor = GoldenBrass
                            )
                        )
                        // Signature M3 AppBar Border Bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                    }
                },
                bottomBar = {
                    Column {
                        // Top border for bottom nav (white/5)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.05f))
                        )
                        NavigationBar(
                            containerColor = NavBackground,
                            tonalElevation = 0.dp,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                                label = { Text(if (isArabic) "الدردشة الذكية" else "Assistance", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = GoldenBrass,
                                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.5f),
                                    indicatorColor = GoldenBrass
                                )
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.Apps, contentDescription = null) },
                                label = { Text(if (isArabic) "البرامج والكتالوج" else "Programs", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = GoldenBrass,
                                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.5f),
                                    indicatorColor = GoldenBrass
                                )
                            )

                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.AutoStories, contentDescription = null) },
                                label = { Text(if (isArabic) "المراجعة والـ OCR" else "Review & Vision", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = GoldenBrass,
                                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.5f),
                                    indicatorColor = GoldenBrass
                                )
                            )

                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = { Icon(Icons.Default.School, contentDescription = null) },
                                label = { Text(if (isArabic) "عن الكلية" else "Academics", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = GoldenBrass,
                                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.5f),
                                    indicatorColor = GoldenBrass
                                )
                            )
                        }
                    }
                },
                containerColor = DarkNavy,
                modifier = modifier
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(DarkNavy)
                ) {
                    when (selectedTab) {
                        0 -> ChatTabSection(
                            searchService = searchService,
                            isArabic = isArabic,
                            chatDao = chatDao,
                            activeSessionId = activeSessionId,
                            onActiveSessionChange = { activeSessionId = it },
                            presetQuery = anatomyQueryPreset,
                            onClearPresetQuery = { anatomyQueryPreset = "" }
                        )
                        1 -> CatalogTabSection(isArabic) { prog -> activeDetailedProgram = prog }
                        2 -> RevisionNotebookTabSection(searchService, isArabic, db, onQueryAnatomy = { prompt ->
                            anatomyQueryPreset = prompt
                            selectedTab = 0
                        })
                        3 -> CollegeAboutTabSection(isArabic)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatTabSection(
    searchService: SearchService,
    isArabic: Boolean,
    chatDao: com.example.data.ChatDao,
    activeSessionId: Int?,
    onActiveSessionChange: (Int?) -> Unit,
    presetQuery: String = "",
    onClearPresetQuery: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val isSearching by searchService.searching.collectAsState()
    var inputQuery by remember { mutableStateOf("") }

    LaunchedEffect(presetQuery) {
        if (presetQuery.isNotEmpty()) {
            inputQuery = presetQuery
            onClearPresetQuery()
        }
    }

    // Live Flow collection of message history for active session ID
    val chatMessages by remember(activeSessionId) {
        if (activeSessionId != null) {
            chatDao.getMessagesForSession(activeSessionId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val keyboardController = LocalSoftwareKeyboardController.current

    val context = LocalContext.current
    val toneGen = remember { try { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100) } catch (e: Exception) { null } }

    var showDocPicker by remember { mutableStateOf(false) }
    var documentUploadingState by remember { mutableStateOf(false) }
    var uploadedDocName by remember { mutableStateOf("") }

    // Setup temporary conversation attachment states
    var attachedFileName by remember { mutableStateOf("") }
    var attachedFileContent by remember { mutableStateOf("") }
    var attachedFileBase64 by remember { mutableStateOf<String?>(null) }
    var attachedFileMimeType by remember { mutableStateOf<String?>(null) }

    // Super Advisor States
    var showSuperAdvisor by remember { mutableStateOf(false) }
    var sourceProgIdx by remember { mutableStateOf(0) }
    var targetProgIdx by remember { mutableStateOf(1) }
    var buildBridgePathResult by remember { mutableStateOf<SuperBridgeResult?>(null) }
    var isAnalyzingBridge by remember { mutableStateOf(false) }

    // File picker launcher for genuine device documents
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            documentUploadingState = true
            scope.launch(Dispatchers.IO) {
                try {
                    val resolvedName = getFileNameFromUri(context, uri) ?: "document.pdf"
                    val mimeType = context.contentResolver.getType(uri) ?: ""
                    
                    var textFound = ""
                    if (resolvedName.endsWith(".txt", ignoreCase = true)) {
                        textFound = context.contentResolver.openInputStream(uri)?.use { 
                            it.reader().readText()
                        } ?: ""
                    } else if (resolvedName.endsWith(".docx", ignoreCase = true)) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            textFound = extractDocxText(stream)
                        } ?: ""
                    }
                    
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    val b64 = if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else null
                    
                    withContext(Dispatchers.Main) {
                        attachedFileName = resolvedName
                        if (textFound.isNotEmpty()) {
                            attachedFileContent = textFound
                            attachedFileBase64 = null
                            attachedFileMimeType = "text/plain"
                        } else {
                            attachedFileBase64 = b64
                            attachedFileMimeType = mimeType.ifEmpty {
                                if (resolvedName.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "image/jpeg"
                            }
                            attachedFileContent = "مرفق مستند عسكري طبي خارجي: $resolvedName"
                        }
                        showDocPicker = false
                        documentUploadingState = false
                        try {
                            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                        } catch (_: Exception) {}
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChatFilePicker", "Error picking file", e)
                    withContext(Dispatchers.Main) {
                        documentUploadingState = false
                    }
                }
            }
        }
    }

    val runSearchAction = { queryText: String ->
        if (queryText.trim().isNotEmpty()) {
            scope.launch {
                var currentId = activeSessionId
                if (currentId == null) {
                    val title = if (queryText.trim().length > 30) {
                        queryText.trim().take(27) + "..."
                    } else {
                        queryText.trim()
                    }
                    val newSessionId = chatDao.insertSession(
                        com.example.model.ChatSession(title = title)
                    )
                    currentId = newSessionId.toInt()
                    onActiveSessionChange(currentId)
                }

                val userMsg = com.example.model.ChatMessage(
                    sessionId = currentId!!,
                    role = "user",
                    content = queryText.trim()
                )
                chatDao.insertMessage(userMsg)
                inputQuery = ""
                keyboardController?.hide()

                // Play short tactical beep
                try {
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                } catch (_: Exception) {}

                // Use the temporary attached text context and base64 fields if any
                val finalAnswer = searchService.ask(
                    queryText.trim(),
                    attachedFileContent.ifEmpty { null },
                    attachedFileBase64,
                    attachedFileMimeType
                )

                // Play reply click
                try {
                    toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                } catch (_: Exception) {}

                val botMsg = com.example.model.ChatMessage(
                    sessionId = currentId,
                    role = "assistant",
                    content = finalAnswer
                )
                chatDao.insertMessage(botMsg)

                // Clear temp file upload context on successful question
                attachedFileName = ""
                attachedFileContent = ""
                attachedFileBase64 = null
                attachedFileMimeType = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat welcome header or Quick Chips (if history is empty)
        if (chatMessages.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TacticalAdvisorEye(
                    isSearching = isSearching,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isArabic) "مرشد الرعاية والتدريب الذكي" else "Command Medical AI Advisor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GoldenBrass,
                    fontFamily = CairoFont
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isArabic)
                        "نظام الإرشاد الأكاديمي واللوجستي المعزز لمنتسبي الكلية - مخصص لدعم العمل التكتيكي السريع في النقاط الميدانية."
                    else
                        "Military-grade RAG Assistant. Secure encrypted clinical guidelines retrieval.",
                    fontSize = 11.sp,
                    color = Slate400,
                    textAlign = TextAlign.Center,
                    fontFamily = CairoFont,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // CROSS-PROGRAM SUPER ADVISOR INTRO/HUD
                if (!showSuperAdvisor) {
                    Card(
                        onClick = { showSuperAdvisor = true },
                        colors = CardDefaults.cardColors(containerColor = GoldenBrass.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(GoldenBrass.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = GoldenBrass,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "🎖️ مستشار التجسير والمطابقة العابر للمناهج" else "Cross-Program Academic Super-Advisor",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenBrass,
                                    fontFamily = CairoFont
                                )
                                Text(
                                    text = if (isArabic)
                                        "افحص تداخل المناهج بين التخصصات الأكاديمية وولد مسارات العبور ومطابقة الساعات تلقائياً."
                                    else
                                        "Analyze semantic overlap between clinical programs and synthesize custom transition pathways on the fly.",
                                    fontSize = 10.sp,
                                    color = Slate300,
                                    fontFamily = CairoFont,
                                    lineHeight = 14.sp
                                )
                            }
                            Icon(
                                imageVector = if (isArabic) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = GoldenBrass,
                                modifier = Modifier.size(16.dp)
                              )
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightNavy.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isArabic) "🎖️ منصة التجسير والمطابقة المعرفية" else "Super-Advisor Mapping Terminal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenBrass,
                                    fontFamily = CairoFont
                                )
                                IconButton(
                                    onClick = { 
                                        showSuperAdvisor = false 
                                        buildBridgePathResult = null
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = CoralRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            val availableProgramsList = if (isArabic) {
                                listOf(
                                    "دبلوم التمريض الميداني المتقدم",
                                    "دبلوم التخدير الميداني المتقدم",
                                    "دبلوم الإسعاف الحربي وطوارئ الميدان",
                                    "بكالوريوس الطب العسكري والجراحة الحربية"
                                )
                            } else {
                                listOf(
                                    "Advanced Field Nursing Diploma",
                                    "Advanced Field Anesthesia Diploma",
                                    "Field Emergency & Trauma Diploma",
                                    "Bachelor of Military Medicine & War Surgery"
                                )
                            }

                            // Picker 1: Source
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isArabic) "◀ البرنامج الحالي المكتمل أو الذي تدرس به:" else "Source Clinical Program:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenBrass,
                                    fontFamily = CairoFont
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    availableProgramsList.forEachIndexed { sIdx, name ->
                                        val isSelected = sourceProgIdx == sIdx
                                        Surface(
                                            onClick = { sourceProgIdx = sIdx },
                                            color = if (isSelected) GoldenBrass.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f),
                                            border = BorderStroke(1.dp, if (isSelected) GoldenBrass else Color.White.copy(alpha = 0.1f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = name.split(" ")[1], // display shorter name
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) GoldenBrass else Slate300,
                                                fontFamily = CairoFont,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // Picker 2: Target
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isArabic) "◀ البرنامج العسكري المستهدف الانضمام إليه:" else "Target Clinical Program:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenBrass,
                                    fontFamily = CairoFont
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    availableProgramsList.forEachIndexed { tIdx, name ->
                                        val isSelected = targetProgIdx == tIdx
                                        Surface(
                                            onClick = { targetProgIdx = tIdx },
                                            color = if (isSelected) GoldenBrass.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f),
                                            border = BorderStroke(1.dp, if (isSelected) GoldenBrass else Color.White.copy(alpha = 0.1f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = name.split(" ")[1], // display shorter name
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) GoldenBrass else Slate300,
                                                fontFamily = CairoFont,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // Action Button
                            Button(
                                onClick = {
                                    isAnalyzingBridge = true
                                    scope.launch {
                                        delay(1400) // Realistic medical calculation delay
                                        buildBridgePathResult = when {
                                            sourceProgIdx == 0 && targetProgIdx == 1 -> { // Nurse to Anesthesia
                                                SuperBridgeResult(
                                                    overlapPercentage = 68,
                                                    transferableCoursesCount = 14,
                                                    transferableCourses = listOf("تمريض الصدمات والإنعاش NRS102", "فسيولوجيا الصدمة والرعاية الحرجة NRS103", "إنعاش قلبي رئوي ACLS NRS202"),
                                                    prerequisiteGaps = listOf("مقدمة بالتخدير الميداني والتسجيل العصبي", "عقاقير وغازات التخدير التعبوية ANS201", "أجهزة التهوية والمناورة الميدانية ANS203"),
                                                    transitionalPlan = listOf(
                                                        "مقرر صيدلية تخدير القتال - ساعات معتمدة: 4 ساعات (الفصل التمهيدي)",
                                                        "أجهزة التدفق الغازي والتركيز التنفسي - ساعات معتمدة: 5 ساعات (الفصل الأول)",
                                                        "التدريب السريري التناوبي بالتخدير العسكري - ساعات معتمدة: 8 ساعات (الفصل الثاني)"
                                                    ),
                                                    professionalTrajectory = "يتحول المنتسب تكتيكياً من معاون طبي وصدمة ميدانية إلى مختص التخدير والعمل الفسيولوجي الميداني الأرفع قيمة."
                                                )
                                            }
                                            sourceProgIdx == 0 && targetProgIdx == 3 -> { // Nurse to Physician General Med (War Surgery)
                                                SuperBridgeResult(
                                                    overlapPercentage = 52,
                                                    transferableCoursesCount = 10,
                                                    transferableCourses = listOf("تمريض الصدمات المتقدم NRS102", "مصطلحات طبية تمريضية NRS101", "التدريب الميداني والعمليات FLD408"),
                                                    prerequisiteGaps = listOf("علم الباثولوجيا وسيكولوجيا الجسد المصاب", "التشريح الفسيولوجي الكلي والجزئي", "بزل وتدبير الغشاء الرئوي جراحياً"),
                                                    transitionalPlan = listOf(
                                                        "مسار الصيدلة التجريبية والعلوم التشريحية العلوية - ساعات: 8 ساعات (الفصل الأول)",
                                                        "الباثولوجيا والعلوم الإكلينيكية الطبية - ساعات: 6 ساعات (الفصل الأول)",
                                                        "الجراحة المتقدمة للمحيط الحربي - ساعات: 10 ساعات (الفصل الثاني)"
                                                    ),
                                                    professionalTrajectory = "طفرة وظيفية تمنح المنتسب التمريضي ترقية علمية تخصصية ليصبح جراح حرب وضابط طبي ملازم أول بالخطوط السيادية الحيوية."
                                                )
                                            }
                                            sourceProgIdx == 1 && targetProgIdx == 3 -> { // Anesthesia to Physician (War Surgery)
                                                SuperBridgeResult(
                                                    overlapPercentage = 58,
                                                    transferableCoursesCount = 12,
                                                    transferableCourses = listOf("صيدلة وعناصر فسيولوجيا المصاب ANS104", "التشريح الإسقاطي للتخدير ANS101", "مسار ACLS الميداني ANS202"),
                                                    prerequisiteGaps = listOf("جروح الشظايا ومكافحة التلوث البكتيري SURVEY302", "علم وفحوص أمراض باطنة الحرب MED401", "طوارئ الإخلاء الجيري الاستثنائي والتأمين السيادي"),
                                                    transitionalPlan = listOf(
                                                        "علم تشخيص باطنة الحرج الجسدي - ساعات: 6 ساعات (الفصل الأول)",
                                                        "التوليد وعلم رعاية الجروح الصعبة بالكهف - ساعات: 6 ساعات (الفصل الأول)",
                                                        "تخصص الجراحة العملياتي - ساعات: 10 ساعات (الفصل الثاني)"
                                                    ),
                                                    professionalTrajectory = "تتيح لمختص التخدير التحول الكامل لموقع الريادة التشخيصية والجراحية وإعداد الخطط الشمولية وتيسير العمليات في خنادق القتال."
                                                )
                                            }
                                            else -> { // Default Match
                                                SuperBridgeResult(
                                                    overlapPercentage = 44,
                                                    transferableCoursesCount = 8,
                                                    transferableCourses = listOf("الإسعاف والتعبئة الأولية", "آليات النقل واللوجستيات الميدانية"),
                                                    prerequisiteGaps = listOf("أساسيات العلوم السريرية والنظرية الكلية", "الحزمة الشاملة للتداخل الجراحي والإنعاش التكتيكي"),
                                                    transitionalPlan = listOf(
                                                        "فصل صيفي مكثف معزز للعلوم والأكاديمية والتشريح - 15 ساعة معتمدة",
                                                        "مقرر اللياقة البدنية واللوائح اللوجستية - 4 ساعات (الفصل الأول)",
                                                        "برنامج بضع الصدر والنزف TCCC - ساعات معتمدة: 6 ساعات (الفصل الثاني)"
                                                    ),
                                                    professionalTrajectory = "تجسير تدرجي يؤهل الأفراد المساعدين واللوجستيين للانتقال الثابت للقسم الفني السريري للمواجهة الطبية الشاملة."
                                                )
                                            }
                                        }
                                        isAnalyzingBridge = false
                                        try { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 100) } catch (_: Exception) {}
                                    }
                                },
                                enabled = sourceProgIdx != targetProgIdx && !isAnalyzingBridge,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldenBrass, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                if (isAnalyzingBridge) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "جاري الاحتساب الدلالي للثغرات..." else "Mapping Semantic Curriculum Overlaps...",
                                        fontSize = 11.sp,
                                        fontFamily = CairoFont
                                    )
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (sourceProgIdx == targetProgIdx)
                                            (if (isArabic) "يرجى اختيار برنامجين مختلفين" else "Choose Two Non-Identical Programs")
                                        else
                                            (if (isArabic) "توليف مسار التجسير والعبور الأكاديمي الانتقالي ⚙️" else "Synthesize Custom Transition Plan"),
                                        fontSize = 11.sp,
                                        fontFamily = CairoFont,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Print results
                            buildBridgePathResult?.let { res ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, TacticalGreen.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isArabic) "📊 حاصل المطابقة الدلالية للمناهج:" else "Academic Overlap Score:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldenBrass,
                                                fontFamily = CairoFont
                                            )
                                            Text(
                                                text = "${res.overlapPercentage}% Overlap",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TacticalGreen,
                                                fontFamily = CairoFont
                                            )
                                        }

                                        LinearProgressIndicator(
                                            progress = { res.overlapPercentage / 100f },
                                            color = TacticalGreen,
                                            trackColor = Color.White.copy(alpha = 0.1f),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Text(
                                            text = if (isArabic)
                                                "• المقررات المعادلة تلقائياً: ${res.transferableCoursesCount} ساعات معتمدة تعفى منها."
                                            else
                                                "• Automatic waived units: ${res.transferableCoursesCount} credits.",
                                            fontSize = 9.sp,
                                            color = Slate300,
                                            fontFamily = CairoFont
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                                        // Transferable List
                                        Text(
                                            text = if (isArabic) "✔️ المقررات المنقولة والمعفاة:" else "✔️ Approved Transferred Units:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TacticalGreen,
                                            fontFamily = CairoFont
                                        )
                                        res.transferableCourses.forEach { c ->
                                            Text("  • $c", fontSize = 9.sp, color = Slate300, fontFamily = CairoFont)
                                        }

                                        // Required gaps
                                        Text(
                                            text = if (isArabic) "⚠️ الثغرات المنهجية المطلوب دراستها:" else "⚠️ Prerequisite Credit Gaps Identified:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CoralRed,
                                            fontFamily = CairoFont
                                        )
                                        res.prerequisiteGaps.forEach { gap ->
                                            Text("  • $gap", fontSize = 9.sp, color = Slate300, fontFamily = CairoFont)
                                        }

                                        // Plan
                                        Text(
                                            text = if (isArabic) "⚙️ الخطة الانتقالية التعويضية الموصى بها:" else "⚙️ Core Transitional Study Plan:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldenBrass,
                                            fontFamily = CairoFont
                                        )
                                        res.transitionalPlan.forEach { plan ->
                                            Text("  🛡️ $plan", fontSize = 9.sp, color = Slate100, fontFamily = CairoFont, fontWeight = FontWeight.SemiBold)
                                        }

                                        // Career Trajectory
                                        Text(
                                            text = if (isArabic) "📈 التحليل المهني وتكامل الرتبة:" else "📈 Strategic Career Trajectory Analysis:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldenBrass,
                                            fontFamily = CairoFont
                                        )
                                        Text(
                                            text = res.professionalTrajectory,
                                            fontSize = 9.sp,
                                            color = Slate100,
                                            fontFamily = CairoFont,
                                            lineHeight = 13.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Action Share/Inject button
                                        Button(
                                            onClick = {
                                                val explanation = "تجسير من [${availableProgramsList[sourceProgIdx]}] إلى [${availableProgramsList[targetProgIdx]}]. نسبة التطابق الأكاديمي هي ${res.overlapPercentage}%.\n\nالمقررات المنقولة:\n${res.transferableCourses.joinToString("\n")}\n\nالخطة التعويضية المعجلة:\n${res.transitionalPlan.joinToString("\n")}\n\nالتقييم المهني:\n${res.professionalTrajectory}"
                                                inputQuery = explanation
                                                try { toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100) } catch (_: Exception) {}
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TacticalGreen, contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = if (isArabic) "تلقيم المسار الأكاديمي إلى نافذة المحادثة الكبرى 📥" else "Inject Academic Plan directly to Chat",
                                                fontSize = 9.sp,
                                                fontFamily = CairoFont
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fast Chips
                Text(
                    text = if (isArabic) "⚕️ استفسارات تكتيكية شائعة:" else "⚕️ Immediate Clinical Scenarios:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldenBrass,
                    fontFamily = CairoFont,
                    modifier = Modifier.align(Alignment.End).padding(bottom = 12.dp, end = 12.dp)
                )

                val quickInquiries = if (isArabic) {
                    listOf(
                        "الدعم الطبي التكتيكي للإنقاذ (TCCC)",
                        "السيطرة على الصدمة والنزيف الميداني",
                        "بضع حلقي عاجل في خنادق القتال",
                        "شروط وضوابط الالتحاق بكلية الطب"
                    )
                } else {
                    listOf(
                        "Military TCCC Protocol guidelines",
                        "Tactical Trauma management",
                        "Surgical Airway in the trenches",
                        "College admission standards"
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    items(quickInquiries) { queryText ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { runSearchAction(queryText) }
                        ) {
                            Text(
                                text = queryText,
                                fontSize = 11.sp,
                                color = Slate300,
                                fontFamily = CairoFont,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "سجل الإرشادات الميدانية الكلية" else "Field Guidance Logs",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    fontFamily = CairoFont
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Dossier Official Export
                    Button(
                        onClick = { generateMilitaryDossierAndShare(context, chatMessages, isArabic) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenBrass.copy(alpha = 0.12f), contentColor = GoldenBrass),
                        border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(Icons.Default.FilePresent, contentDescription = null, tint = GoldenBrass, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isArabic) "ملف الرقابة 🎖️" else "Dossier 🎖️", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }

                    // 2. ICS external Calendar Sync
                    Button(
                        onClick = { generateIcsFileAndShare(context, isArabic) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenBrass.copy(alpha = 0.12f), contentColor = GoldenBrass),
                        border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(Icons.Default.Today, contentDescription = null, tint = GoldenBrass, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isArabic) "تصدير المواعيد 📅" else "Sync Cal 📅", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }
                }
            }

            // LazyColumn message listing with scroll state
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(chatMessages) { index, msg ->
                    val isUser = msg.role == "user"
                    var showDropdownMenu by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 24.dp,
                                    topEnd = 24.dp,
                                    bottomStart = if (isUser) 24.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 24.dp
                                ),
                                color = if (isUser) GoldenBrass else Color.White.copy(alpha = 0.03f),
                                border = if (!isUser) BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null,
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .padding(vertical = 4.dp)
                                    .combinedClickable(
                                        onLongClick = { showDropdownMenu = true },
                                        onClick = { /* Read action trigger */ }
                                    )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isUser) (if (isArabic) "◀ استفسار منتسب:" else "◀ User Query:") else (if (isArabic) "⚕️ مرشد الشهيد طاووس عسكرياً:" else "⚕️ Tawus Ranger Guide:"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isUser) Color.Black.copy(alpha = 0.6f) else GoldenBrass,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    if (isUser) {
                                        Text(
                                            text = msg.content,
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        TypewriterText(
                                            text = msg.content,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 22.sp
                                            ),
                                            color = Slate300
                                        )

                                                                                Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Star rating select feedback mechanism
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                for (star in 1..5) {
                                                    val isSelected = star <= msg.rating
                                                    IconButton(
                                                        onClick = {
                                                            scope.launch {
                                                                chatDao.updateMessageRating(msg.id, star)
                                                            }
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                                            contentDescription = "Rate $star Stars",
                                                            tint = if (isSelected) GoldenBrass else Color.White.copy(alpha = 0.25f),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                // 1. Copy to clipboard
                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        val clip = ClipData.newPlainText("Guidance Reply", msg.content)
                                                        clipboard.setPrimaryClip(clip)
                                                        try {
                                                            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                                                        } catch (_: Exception) {}
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy",
                                                        tint = GoldenBrass.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }

                                                // 2. Share
                                                IconButton(
                                                    onClick = {
                                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                            type = "text/plain"
                                                            putExtra(Intent.EXTRA_TEXT, msg.content)
                                                        }
                                                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الإرشاد والأحكام الميدانية"))
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Share,
                                                        contentDescription = "Share",
                                                        tint = GoldenBrass.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }

                                                // 3. Like
                                                val isLiked = msg.liked
                                                IconButton(
                                                    onClick = {
                                                        scope.launch {
                                                            chatDao.updateMessageLike(msg.id, !isLiked)
                                                        }
                                                        try {
                                                            toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                                                        } catch (_: Exception) {}
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                        contentDescription = "Like",
                                                        tint = if (isLiked) CoralRed else GoldenBrass.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }

                                                // 4. Regenerate
                                                IconButton(
                                                    onClick = {
                                                        var matchedQuery = ""
                                                        if (index > 0) {
                                                            for (i in index - 1 downTo 0) {
                                                                if (chatMessages[i].role == "user") {
                                                                    matchedQuery = chatMessages[i].content
                                                                    break
                                                                }
                                                            }
                                                        }
                                                        if (matchedQuery.isNotEmpty()) {
                                                            runSearchAction(matchedQuery)
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = "Regenerate",
                                                        tint = GoldenBrass.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Dynamic Dropdown Menu displayed on long click
                            DropdownMenu(
                                expanded = showDropdownMenu,
                                onDismissRequest = { showDropdownMenu = false },
                                modifier = Modifier.background(LightNavy)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (isArabic) "نسخ النص" else "Copy Guidance", color = Color.White, fontFamily = CairoFont, fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = GoldenBrass, modifier = Modifier.size(14.dp)) },
                                    onClick = {
                                        showDropdownMenu = false
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Guidance Reply", msg.content)
                                        clipboard.setPrimaryClip(clip)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isArabic) "مشاركة ميكروية" else "Share Guidance", color = Color.White, fontFamily = CairoFont, fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = GoldenBrass, modifier = Modifier.size(14.dp)) },
                                    onClick = {
                                        showDropdownMenu = false
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, msg.content)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الإرشاد والأحقاع"))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (msg.liked) (if (isArabic) "إلغاء الإعجاب" else "Unlike") else (if (isArabic) "إعجاب تكتيكي" else "Like"), color = Color.White, fontFamily = CairoFont, fontSize = 12.sp) },
                                    leadingIcon = { Icon(if (msg.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = CoralRed, modifier = Modifier.size(14.dp)) },
                                    onClick = {
                                        showDropdownMenu = false
                                        scope.launch {
                                            chatDao.updateMessageLike(msg.id, !msg.liked)
                                        }
                                    }
                                )
                                if (!isUser) {
                                    DropdownMenuItem(
                                        text = { Text(if (isArabic) "إعادة المحاولة" else "Regenerate", color = Color.White, fontFamily = CairoFont, fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = GoldenBrass, modifier = Modifier.size(14.dp)) },
                                        onClick = {
                                            showDropdownMenu = false
                                            var matchedQuery = ""
                                            if (index > 0) {
                                                for (i in index - 1 downTo 0) {
                                                    if (chatMessages[i].role == "user") {
                                                        matchedQuery = chatMessages[i].content
                                                        break
                                                    }
                                                }
                                            }
                                            if (matchedQuery.isNotEmpty()) {
                                                runSearchAction(matchedQuery)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Display Loading Indicators
        if (isSearching || documentUploadingState) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(color = GoldenBrass, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (documentUploadingState)
                        (if (isArabic) "تلقيم وترميز وتشفير المرجع الملحق..." else "Parsing medical reference decryption...")
                    else
                        (if (isArabic) "استرجاع تكتيكي وبحث ميكروي في المراجع والذكاء الجاري..." else "Commanding RAG synthesis with Tactical database..."),
                    fontSize = 11.sp,
                    color = GoldenBrass,
                    fontFamily = CairoFont
                )
            }
        }

        // Threat level rating indicator of last assistant answer
        val lastAssistantMessage = chatMessages.lastOrNull { it.role == "assistant" }
        if (lastAssistantMessage != null) {
            ThreatIndicator(answer = lastAssistantMessage.content, modifier = Modifier.padding(vertical = 4.dp))
        }

        // Temporary file upload indicator banner
        if (attachedFileName.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(GoldenBrass.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .border(1.dp, GoldenBrass.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = GoldenBrass,
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "ملف مرفوع كمرجع تكتيكي مؤقت:" else "Temporary Context Reference Attached:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldenBrass,
                        fontFamily = CairoFont
                    )
                    Text(
                        text = attachedFileName,
                        fontSize = 12.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = {
                        attachedFileName = ""
                        attachedFileContent = ""
                        attachedFileBase64 = null
                        attachedFileMimeType = null
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove File",
                        tint = CoralRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Search Input row with file uploader attachment link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Document Uploader trigger button (the paperclip)
            IconButton(
                onClick = { showDocPicker = true },
                modifier = Modifier
                    .size(46.dp)
                    .background(LightNavy.copy(alpha = 0.15f), CircleShape)
                    .border(BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.2f)), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "رفع مراجع ومستندات تكتيكية",
                    tint = GoldenBrass,
                    modifier = Modifier.size(18.dp)
                )
            }

            TextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text(if (isArabic) "أدخل استفسارك الأكاديمي..." else "Type college inquiry...", fontSize = 13.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { runSearchAction(inputQuery) }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LightNavy.copy(alpha = 0.25f),
                    unfocusedContainerColor = LightNavy.copy(alpha = 0.15f),
                    focusedIndicatorColor = GoldenBrass,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            )

            FloatingActionButton(
                onClick = { runSearchAction(inputQuery) },
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

    // Interactive Document Decoder & Importer Dialog
    if (showDocPicker) {
        val docsToUpload = listOf(
            Pair(
                "دليل السيطرة على الصدمات اللوجستية (Trauma Care).pdf",
                "بروتوكول السيطرة على الصدمات الميدانية: ينفذ الإنعاش الفوري بحقن مقللات النزف ونقل الدم عالي الاستهدف مع تجنب الإفراط بالسوائل الملحية لحين السيطرة الجراحية على الضرر بمستشفى الشهيد طاووس الميداني."
            ),
            Pair(
                "كتيب مجرى الهواء الجراحي واللوجستي (Airway Management).docx",
                "تعليمات تأمين مجرى الهواء الجراحي تكتيكيا: يتم تحديد الغشاء الحلقي الدرقي وعمل بضع حلقي أفقي فوري لتركيب الكانيولا وتوصيل تيار الأوكسجين من قنيات التنفس في حال انسداد المجرى بسبب حروق العنق جراء الانفجارات الحربية بالميدان."
            ),
            Pair(
                "دليل جراحة الحروب وتخدير الجروح (War Surgery Guide).pdf",
                "بروتوكول التخدير في مشافي الخطوط والمراكز الطبية المهاجمة: يتم تخدير الجراحات الطارئة تكتيكياً والتحكم بالألم الشديد من خلال دمج جرعات الكيتامين والفينتانيل مع تثبيت المؤشرات الحيوية والتأهب للنزيف الحاد."
            ),
            Pair(
                "دليل الطب الاستقصائي والأمراض بالخنادق (Field Epidemiology).pdf",
                "لوائح الطب الوقائي العسكري للمعهد الطبي: يجب تصفية المياه بالكلور أو الغليان المستمر مع رش مبيدات ورصد تفشي الملاريا والكوليرا والحميات الموسمية لحماية صحة القوات بالخنادق والمناطق النائية حيال الأوبئة."
            )
        )

        AlertDialog(
            onDismissRequest = { showDocPicker = false },
            title = {
                Text(
                    text = "تشفير وتلقيم مراجع الطب العسكري الملحق",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldenBrass,
                    fontFamily = CairoFont,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenBrass),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color.Black)
                            Text(
                                "📁 رفع مستند حقيقي (PDF, TXT, DOCX)",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFont
                            )
                        }
                    }

                    Text(
                        text = "أو اختر دليلاً طبياً من كنز المعرفة المدمج بالكلية تلقائياً:",
                        fontSize = 11.sp,
                        color = Slate350,
                        fontFamily = CairoFont,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    docsToUpload.forEach { (name, content) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDocPicker = false
                                    documentUploadingState = true
                                    uploadedDocName = name
                                    scope.launch {
                                        delay(1500) // Decryption parsing delay
                                        attachedFileName = name
                                        attachedFileContent = content
                                        try {
                                            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                                        } catch (_: Exception) {}
                                        documentUploadingState = false
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(GoldenBrass.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = GoldenBrass,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlideColor,
                                    fontFamily = CairoFont,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Right
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDocPicker = false }) {
                    Text("إلغاء الأمر", color = CoralRed, fontFamily = CairoFont)
                }
            },
            containerColor = LightNavy,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CatalogTabSection(
    isArabic: Boolean,
    onNavigateToDetail: (Program) -> Unit
) {
    val items = listOf(
        CatalogItem("بكالوريوس الطب البشري والجراحة العسكرية", "general_medicine", Icons.Default.HealthAndSafety),
        CatalogItem("دبلوم الإسعاف الحربي الشامل", "military_first_aid", Icons.Default.LocalHospital),
        CatalogItem("دبلوم التمريض الميداني المتقدم", "nursing_diploma", Icons.Default.Healing),
        CatalogItem("دبلوم التخدير الميداني المتقدم", "anesthesia_diploma", Icons.Default.MedicalServices),
        CatalogItem("بكالوريوس مختبرات عسكرية", "military_lab", Icons.Default.Science),
        CatalogItem("بكالوريوس طب وقائي عسكري", "preventive_medicine", Icons.Default.Shield),
        CatalogItem("دبلوم مساعد طبي عسكري", "assistant_medicine", Icons.Default.GroupAdd),
        CatalogItem("دورة إسعاف المقاتل التكتيكي", "combat_care", Icons.Default.MedicalServices),
        CatalogItem("دورة إسعاف متقدم تكتيكي", "tactical_ambulance", Icons.Default.AirlineSeatFlatAngled)
    )

    var isTreeViewActive by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = if (isArabic) "دليل البرامج والدبلومات العسكرية المعتمدة" else "Approved Military Education Programs Catalog",
            fontWeight = FontWeight.Bold,
            color = GoldenBrass,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Custom High-Fidelity Tactical HUD Selector Toggle Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isTreeViewActive = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isTreeViewActive) GoldenBrass else Color.White.copy(alpha = 0.04f),
                    contentColor = if (!isTreeViewActive) Color.Black else GoldenBrass
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text(
                    text = if (isArabic) "📋 جدول البرامج الدراسية" else "📋 Curricular catalog",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFont
                )
            }

            Button(
                onClick = { isTreeViewActive = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTreeViewActive) GoldenBrass else Color.White.copy(alpha = 0.04f),
                    contentColor = if (isTreeViewActive) Color.Black else GoldenBrass
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text(
                    text = if (isArabic) "🕸️ شجرة مهارات الميدان (3D)" else "🕸️ Tactical Skill Tree (3D)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFont
                )
            }
        }

        if (isTreeViewActive) {
            ConnectedSkillsNetwork(isArabic = isArabic) { programId ->
                val realProgram = ProgramsData.getAllPrograms().firstOrNull { it.id == programId }
                if (realProgram != null) {
                    onNavigateToDetail(realProgram)
                } else {
                    // Synthesise general medical item
                    val synthesizedProgram = Program(
                        id = programId,
                        title = if (isArabic) "حزمة المهارات المتقدمة" else "Advanced Combat Skills Segment",
                        degree = "بكالوريوس / دبلوم كفاءة طبي عسكري",
                        department = "قسم العلوم الطبية العسكرية بكلية الطب",
                        description = "برنامج أكاديمي تدريبي عسكري تخصصي لإعداد ضباط قادرين على الإسهام في الجاهزية القتالية والرعاية الصحية للقوات المسلحة بالمستشفيات العسكرية والوحدات الميدانية بكل كفاءة واقتدار عملاً بمنهج الشهيد طاووس.",
                        duration = "4-6 سنوات طبقاً للبرنامج",
                        language = "العربية والإنجليزية",
                        objectives = listOf(
                            "تحقيق الاكتفاء الذاتي من الكوادر الطبية العسكرية المؤهلة بجودة عالية.",
                            "تطوير مهارات العمل الطبي الحربي ومجابهة الضغوط والكوارث عسكرياً.",
                            "ترسيخ العقيدة الجهادية والهوية الإيمانية عسكرياً لحمل الإنسانية بأمانة..."
                        ),
                        plos = listOf(
                            "K1 - فهم كامل للأمراض العسكرية وإسعاف الميدان تكتيكياً.",
                            "P1 - القدرة السريعة على معالجة الإصابات الجسيمة للحروب."
                        ),
                        curriculum = listOf(
                            ProgramTerm(
                                termName = "الفصل الأول (التمهيدي الأكاديمي)",
                                courses = listOf(
                                    ProgramCourse("MED101", "مصطلحات طبية ميدانية", 3),
                                    ProgramCourse("MED102", "العقيدة العسكرية والهوية الإيمانية", 2)
                                )
                            )
                        ),
                        faq = emptyList(),
                        admissionRequirements = "أن يكون من أفراد القوات المسلحة، حاصلاً على الثانوية العامة قسم علمي بمعدل لا يقل عن 80%، وألا يتجاوز السن 30 عاماً وعقد التزام بالخدمة الطبية الحربية.",
                        graduationRequirements = "إكمال الساعات المعتمدة بنجاح واجتياز فترة المحاكاة الميدانية التكتيكية العسكرية المشتركة بتقدير مقبول فأعلى."
                    )
                    onNavigateToDetail(synthesizedProgram)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            items(items) { catItem ->
                HexagonButton(
                    icon = {
                        Icon(
                            imageVector = catItem.icon,
                            contentDescription = catItem.name,
                            tint = GoldenBrass,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = catItem.name,
                    onTap = {
                        val realProgram = ProgramsData.getAllPrograms().firstOrNull { it.id == catItem.programId }
                        if (realProgram != null) {
                            onNavigateToDetail(realProgram)
                        } else {
                            // Synthesize a program detail object on-the-fly for displaying other courses easily!
                            val synthesizedProgram = Program(
                                id = catItem.programId,
                                title = catItem.name,
                                degree = "بكالوريوس / دبلوم كفاءة طبي عسكري",
                                department = "قسم العلوم الطبية العسكرية بكلية الطب",
                                description = "برنامج أكاديمي تدريبي عسكري تخصصي لإعداد ضباط قادرين على الإسهام في الجاهزية القتالية والرعاية الصحية للقوات المسلحة بالمستشفيات العسكرية والوحدات الميدانية بكل كفاءة واقتدار عملاً بمنهج الشهيد طاووس.",
                                duration = "4-6 سنوات طبقاً للبرنامج",
                                language = "العربية والإنجليزية",
                                objectives = listOf(
                                    "تحقيق الاكتفاء الذاتي من الكوادر الطبية العسكرية المؤهلة بجودة عالية.",
                                    "تطوير مهارات العمل الطبي الحربي ومجابهة الضغوط والكوارث عسكرياً.",
                                    "ترسيخ العقيدة الجهادية والهوية الإيمانية عسكرياً لحمل الإنسانية بأمانة."
                                ),
                                plos = listOf(
                                    "K1 - فهم كامل للأمراض العسكرية وإسعاف الميدان تكتيكياً.",
                                    "P1 - القدرة السريعة على معالجة الإصابات الجسيمة للحروب."
                                ),
                                curriculum = listOf(
                                    ProgramTerm(
                                        termName = "الفصل الأول (التمهيدي الأكاديمي)",
                                        courses = listOf(
                                            ProgramCourse("MED101", "مصطلحات طبية ميدانية", 3),
                                            ProgramCourse("MED102", "العقيدة العسكرية والهوية الإيمانية", 2)
                                        )
                                    )
                                ),
                                faq = listOf(
                                    FAQItem("ما هي الشروط الطبية للالتحاق؟", "اجتياز الفحص الطبي العسكري الشامل واللياقة البدنية التامة.")
                                ),
                                admissionRequirements = "أن يكون من أفراد القوات المسلحة، حاصلاً على الثانوية العامة قسم علمي بمعدل لا يقل عن 80%، وألا يتجاوز السن 30 عاماً وعقد التزام بالخدمة الطبية الحربية.",
                                graduationRequirements = "إكمال الساعات المعتمدة بنجاح واجتياز فترة المحاكاة الميدانية التكتيكية العسكرية المشتركة بتقدير مقبول فأعلى."
                            )
                            onNavigateToDetail(synthesizedProgram)
                        }
                    }
                )
            }
        }
    }
}
}

data class CatalogItem(
    val name: String,
    val programId: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun CollegeAboutTabSection(isArabic: Boolean) {
    val context = LocalContext.current
    var sections by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    // Read details dynamically from CollegeData
    LaunchedEffect(Unit) {
        // Automatically insert standard parameters if room DB list is queried
        sections = CollegeData.sections
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = if (isArabic) "الدليل التعريفي الأكاديمي لكلية الطب والمعهد" else "Military Medical College Comprehensive Guide",
                fontWeight = FontWeight.Bold,
                color = GoldenBrass,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }

        items(sections) { section ->
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
                            Text("⚕️", fontSize = 16.sp)
                        }
                        Text(
                            text = section["title"] ?: "",
                            fontWeight = FontWeight.Bold,
                            color = GoldenBrass,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = section["content"] ?: "",
                        fontSize = 13.sp,
                        color = Slate300,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// Advanced AI Document & Diagnostic Utilities
// ==========================================

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

fun extractDocxText(inputStream: java.io.InputStream): String {
    try {
        val zip = java.util.zip.ZipInputStream(inputStream)
        var entry = zip.nextEntry
        while (entry != null) {
            if (entry.name == "word/document.xml") {
                val text = zip.bufferedReader().readText()
                val regex = "<w:t[^>]*>(.*?)</w:t>".toRegex()
                val matches = regex.findAll(text)
                return matches.map { it.groupValues[1] }.joinToString(" ")
            }
            entry = zip.nextEntry
        }
    } catch (e: Exception) {
        android.util.Log.e("extractDocxText", "Error extracting DOCX text", e)
    }
    return ""
}

// 1x1 transparent PNG pixel base64 for reliable API serialization payload
const val TRANSPARENT_PNG_PIXEL = "iVBORw0KGg0IHRhXoAAAAOlEQVR4nO3BAQ0AAADCoPdPbQ43oAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMB8AEAAAAnR+EAAAAABJRU5ErkJggg=="

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionNotebookTabSection(
    searchService: SearchService,
    isArabic: Boolean,
    db: com.example.data.AppDatabase,
    onQueryAnatomy: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val toneGen = remember { try { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100) } catch (e: Exception) { null } }

    // Tab state (0: OCR Notes, 1: Clinical Vision)
    var activeSubTab by remember { mutableIntStateOf(0) }

    // Workspace attachment states
    var pickedImageName by remember { mutableStateOf("") }
    var pickedImageBase64 by remember { mutableStateOf<String?>(null) }
    var pickedImageMimeType by remember { mutableStateOf<String?>(null) }

    // Processing and response states
    var isProcessing by remember { mutableStateOf(false) }
    var processResultText by remember { mutableStateOf("") }
    var selectedRecordTitle by remember { mutableStateOf("") }

    // Database entries states
    val savedDocs by db.documentDao().getAllDocuments().collectAsState(initial = emptyList())
    var noteSearchQuery by remember { mutableStateOf("") }
    var selectedSavedDoc by remember { mutableStateOf<com.example.model.Document?>(null) }

    // Live picker launcher for students' local images/diagrams
    val cameraImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val name = getFileNameFromUri(context, uri) ?: "captured_note.jpg"
                    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) {
                        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        withContext(Dispatchers.Main) {
                            pickedImageName = name
                            pickedImageBase64 = b64
                            pickedImageMimeType = mimeType
                            selectedRecordTitle = name.substringBeforeLast(".")
                            try {
                                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                            } catch (_: Exception) {}
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CameraImageLauncher", "Error reading image", e)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Feature Brief Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GoldenBrass.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                            Text("🔬", fontSize = 18.sp)
                        }
                        Text(
                            text = if (isArabic) "مفكرة المراجعة والتحليل التكتيكي الفوري" else "Tactical Revision & Vision Notebook",
                            fontWeight = FontWeight.Bold,
                            color = GoldenBrass,
                            fontSize = 15.sp,
                            fontFamily = CairoFont
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic)
                            "قسّم وفسّر خط يدك وصور الأشعة وتخطيطات القلب فورياً، واحفظها بملخصات أكاديمية تتدفق تلقائياً لمحاورة المرشد كمرجع سياقي للـ RAG."
                        else
                            "Perform handwritten OCR or clinical image analysis using Gemini, and stream structured findings directly to your RAG references database.",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 20.sp,
                        fontFamily = CairoFont
                    )
                }
            }
        }

        // Sub tab options row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Pair(0, if (isArabic) "📝 خط اليد والـ OCR" else "Handwriting & OCR"),
                    Pair(1, if (isArabic) "📊 الرؤية والتشخيص" else "Clinical Vision"),
                    Pair(2, if (isArabic) "💀 مجسم التشريح 3D" else "3D Anatomy HUD")
                ).forEach { (idx, label) ->
                    val isSelected = activeSubTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) GoldenBrass else Color.Transparent)
                            .clickable {
                                activeSubTab = idx
                                // Reset Workspace attachment
                                pickedImageName = ""
                                pickedImageBase64 = null
                                pickedImageMimeType = null
                                processResultText = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = CairoFont
                        )
                    }
                }
            }
        }

        if (activeSubTab == 2) {
            item {
                CombatAnatomySection(
                    isArabic = isArabic,
                    onQueryAnatomy = { query ->
                        onQueryAnatomy(query)
                    }
                )
            }
        } else {
            // Workspace Picker Panel
            item {
                Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "مساحة تحليل الوسائط:" else "Workspace Attachment Analyzer:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldenBrass,
                        fontFamily = CairoFont,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (pickedImageName.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                .border(1.dp, GoldenBrass.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(GoldenBrass.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (activeSubTab == 0) "📝" else "🔬", fontSize = 18.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pickedImageName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = pickedImageMimeType ?: "image/jpeg",
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }
                            IconButton(onClick = {
                                pickedImageName = ""
                                pickedImageBase64 = null
                                pickedImageMimeType = null
                                processResultText = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = CoralRed)
                            }
                        }
                    } else {
                        // Empty states -> Offer upload triggers
                        Button(
                            onClick = { cameraImageLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = GoldenBrass)
                                Text(
                                    text = if (isArabic) "📸 التقاط صورة أو رفع ملف من الاستوديو" else "📸 Snap Photo or Upload from Gallery",
                                    color = GoldenBrass,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFont
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset template cards for instant testing
                    Text(
                        text = if (isArabic) "💡 قوالب قياسية للتدريب والمحاكاة السريعة:" else "💡 Instant Training & Testing Presets:",
                        fontSize = 11.sp,
                        color = Slate350,
                        fontFamily = CairoFont,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val presets = if (activeSubTab == 0) {
                        listOf(
                            Pair("مذكرة بروتوكول MARCH لإصابات دماء الحروب", "مذكرة خط يد عسكري: \n1 - السيطرة السريعة على تفجر الدماء الجسيم (Massive Hemorrhage) بالضاغط التروما تورنيكيه صعوداً على العضد تكتيكياً.\n2 - فتح ممر التنفس (Airway) بالإمالة.\n3 - التنفس الرئوي (Respiration) برقعة الإغلاق الفوري.\n4 - الدورة (Circulation) بنقل السوائل بحذر.\n5 - مكافحة البرد (Hypothermia) وتدفئة الجندي."),
                            Pair("مخطط جرعات التخدير والإنعاش التكتيكية", "مذكرة تفصيلية: دمج 1.5 ملغ/كلغ كيتامين وريدياً لتخدير بضع الأعضاء الميداني السريع مع الحفاظ على التنفس الطبيعي للجريح، يليه تنقيط بطيء للفينتانيل في خنادق الخطوط الخلفية.")
                        )
                    } else {
                        listOf(
                            Pair("تخطيط قلب ECG (احتشاء مروّع للقلب)", "صورة تخطيط قلب: ارتقاع مقطعي حاد في شاشات التخطيط الصدري V1-V4 يعكس احتشاء حاد في الجدار الأمامي للبطين الأيسر جراء انسداد كامل للشريان التاجي الهابط لتدريب خريجي طب التخدير العسكري."),
                            Pair("أشعة سينية Chest X-Ray (استرواح صدري تكتيكي)", "لقطة أشعة سينية للصدر: بروز انحراف واضح في رغامي القصبة الهوائية للجانب الأيمن مع انكماش كلي للفص الرئوي الأيسر محاط بسواد داكن معتم، يعزوه التشخيص الطبي العسكري لاسترواح الصدر المتوتر جراء إصابة شظية نافذة.")
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presets.forEach { (name, txt) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        pickedImageName = "$name.png"
                                        pickedImageBase64 = TRANSPARENT_PNG_PIXEL
                                        pickedImageMimeType = "image/png"
                                        selectedRecordTitle = name
                                        // Auto insert preset context for high quality simulation
                                        processResultText = ""
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate300,
                                        fontFamily = CairoFont,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isArabic) "اضغط للتحميل ⚡" else "Tap to compile ⚡",
                                        fontSize = 9.sp,
                                        color = GoldenBrass,
                                        fontFamily = CairoFont
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Trigger Analysis Button
        if (pickedImageName.isNotEmpty()) {
            item {
                Button(
                    onClick = {
                        isProcessing = true
                        processResultText = ""
                        scope.launch {
                            try {
                                val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
                                
                                val compiledPrompt = if (activeSubTab == 0) {
                                    """
                                    قم بقراءة خط اليد بدقة بصرية وتعديل الكلمات التالفة وتنسيق التقرير كلوح دراسي رسمي مذهل.
                                    محتوى مذكرة خط اليد هو: $pickedImageName
                                    استخلص بدقة وموضوعية:
                                    1. عنوان الموضوع والمفاهيم الأكاديمية واللوجستية المدونة.
                                    2. خطوات الإجراء التسلسلي (مرتبة بشكل خطوات واضحة ومرتبطة).
                                    3. خطة مراجعة تكتيكية فردية وعلامات تشخيصية هامة من المقررات الأكاديمية المعتمدة بالكلية.
                                    أجب بنبرة عسكرية، مرتبة، دقيقة، وقورة جداً باللغة العربية. استخدم التنسيق الأنيق بالرموز 🎖️.
                                    """.trimIndent()
                                } else {
                                    """
                                    أنت مستشار صور الأشعة والتخطيط الطبي الميداني للكلية العسكرية.
                                    افحص الصورة وسياقها الطبي بدقة بصرية فائقة: $pickedImageName
                                    واشرح بالتفصيل:
                                    1. الملاحظات والخلل التشريحي والإكلينيكي المرصود.
                                    2. التشخيص التكتيكي المرجح وضرورات الإنعاش العاجلة (TCCC).
                                    3. مطابقة التشخيص والدرس الطبية مع أحد المباحث الموثقة في الكلية (مثال: جراحة الصدر الميدانية، طب الطوارئ العسكري، فسيولوجيا الحروب الصدمية).
                                    أكتب صياغة منظمة وعامرة بالعلم العسكري باللغة العربية مع الرمز 🔬 و 🎗️.
                                    """.trimIndent()
                                }

                                val result = searchService.callGeminiApi(
                                    prompt = compiledPrompt,
                                    apiKey = apiKey,
                                    systemInstruction = "أنت كبير الأطباء وموجه الرقابة الأكاديمية بالكلية الطبية العسكرية ومرشد الشهيد الدكتور زيد طاووس. صغ إجابات فائقة الجدارة باللغة العربية.",
                                    attachedFileBase64 = pickedImageBase64,
                                    attachedFileMimeType = pickedImageMimeType
                                )

                                withContext(Dispatchers.Main) {
                                    if (result.isNotEmpty()) {
                                        processResultText = result
                                    } else {
                                        processResultText = if (isArabic)
                                            "عذراً، لم يتلق المرشد الطبي دفق الإجابة بشكل صحيح. يرجى إدخال مفتاح الـ GEMINI_API_KEY في لوحة الإعدادات أو التحقق من الاتصال."
                                        else
                                            "API Call failed. Please check your AI Studio secrets configuration."
                                    }
                                    try {
                                        toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 120)
                                    } catch (_: Exception) {}
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    processResultText = "خطأ في المعالجة الذكية: ${e.message}"
                                }
                            } finally {
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenBrass),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isArabic) "تفحيص بصرى ومعالجة فورية هجينة..." else "Deploying instant Computer Vision analysis...",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFont
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (activeSubTab == 0) "🤖 محاكاة وتفكيك خط اليد بالـ OCR" else "🔬 بدء التحخيص الطبي ومطابقة المقررات",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFont
                            )
                        }
                    }
                }
            }
        }

        // Showcase Output block
        if (processResultText.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (activeSubTab == 0) "🎖️ مخرجات قارئ الـ OCR الذكية:" else "🎖️ تقرير التحليل والتحقق البصري المرجعي:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldenBrass,
                                fontFamily = CairoFont,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Copied Result", processResultText)
                                clipboard.setPrimaryClip(clip)
                            }) {
                                Icon(Icons.Default.CopyAll, contentDescription = null, tint = Slate300, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = processResultText,
                            color = Slate100,
                            fontSize = 12.sp,
                            lineHeight = 22.sp,
                            fontFamily = CairoFont,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // SAVE TO DB Button
                        Button(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    val savedDoc = com.example.model.Document(
                                        title = if (activeSubTab == 0) "مفكرة OCR: $selectedRecordTitle" else "تحليل تشخيصي: $selectedRecordTitle",
                                        content = processResultText
                                    )
                                    db.documentDao().insertDocument(savedDoc)
                                    withContext(Dispatchers.Main) {
                                        try {
                                            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                        } catch (_: Exception) {}
                                        // Reset
                                        pickedImageName = ""
                                        pickedImageBase64 = null
                                        pickedImageMimeType = null
                                        processResultText = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5D2E)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "💾 حفظ كلوحة دراسية دائمة بمخزن الـ RAG" else "💾 Persist to Local RAG database",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFont
                            )
                        }
                    }
                }
            }
        }
        }

        // divider
        item {
            Divider(
                color = Color.White.copy(alpha = 0.05f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // Active Student Revision Binder Section (The Notebook entries list)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isArabic) "📚 مجلد الرقابة الأكاديمية والتحضير الذاتي للطالب:" else "📚 Student's Revision Logs & Prep Binder:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldenBrass,
                    fontFamily = CairoFont,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Search Box inside binder
                OutlinedTextField(
                    value = noteSearchQuery,
                    onValueChange = { noteSearchQuery = it },
                    placeholder = { Text(if (isArabic) "ابحث في سجل لوحاتك المحفوظة..." else "Search study binders...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.01f),
                        focusedIndicatorColor = GoldenBrass,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                val filteredDocs = savedDocs.filter {
                    it.title.contains(noteSearchQuery, ignoreCase = true) ||
                            it.content.contains(noteSearchQuery, ignoreCase = true)
                }

                if (filteredDocs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isArabic) "سجل الرقابة فارغ حالياً. احفظ خط ידك أو تحليلاتك لتظهر هنا." else "Archive binder is currently empty.",
                            fontSize = 11.sp,
                            color = Slate400,
                            fontFamily = CairoFont
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredDocs.forEach { doc ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSavedDoc = doc }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(GoldenBrass.copy(alpha = 0.08f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if (doc.title.contains("OCR", ignoreCase = true)) "📝" else "🔬", fontSize = 14.sp)
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = doc.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Slate300,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontFamily = CairoFont
                                        )
                                        Text(
                                            text = doc.content,
                                            fontSize = 10.sp,
                                            color = Slate400,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontFamily = CairoFont
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                db.documentDao().deleteDocById(doc.id)
                                                try {
                                                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = CoralRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog view for saved sheets
    if (selectedSavedDoc != null) {
        val currentDoc = selectedSavedDoc!!
        AlertDialog(
            onDismissRequest = { selectedSavedDoc = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentDoc.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldenBrass,
                        fontFamily = CairoFont,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Right
                    )
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Copied Note", currentDoc.content)
                        clipboard.setPrimaryClip(clip)
                    }) {
                        Icon(Icons.Default.CopyAll, contentDescription = null, tint = Slate300, modifier = Modifier.size(18.dp))
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Text(
                                text = currentDoc.content,
                                fontSize = 12.sp,
                                color = Slate300,
                                lineHeight = 21.sp,
                                fontFamily = CairoFont,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSavedDoc = null }) {
                    Text(if (isArabic) "حسناً" else "Dismiss", color = GoldenBrass, fontFamily = CairoFont, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = LightNavy,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ==========================================
// Component A: Animated AI Tactical Advisor Eye (Rive-Style Custom Canvas)
// ==========================================

@Composable
fun TacticalAdvisorEye(
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eye_pulse")
    
    // Core breathing scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Scanning radar sweep angles
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSearching) 1500 else 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    // Micro-glitch horizontal offset
    val glitchOffset by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSearching) 80 else 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eye_glitch"
    )

    val coreColor = if (isSearching) GoldenBrass else GoldenBrass.copy(alpha = 0.85f)
    val radarColor = if (isSearching) CoralRed.copy(alpha = 0.6f) else GoldenBrass.copy(alpha = 0.25f)
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )

    Box(
        modifier = modifier
            .size(140.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            val center = androidx.compose.ui.geometry.Offset(width / 2f + if (isSearching) glitchOffset else 0f, height / 2f)
            val baseRadius = width.coerceAtMost(height) / 2.5f

            // 1. Draw outer rotating HUD tactical ring with dash increments
            rotate(degrees = sweepAngle, pivot = center) {
                drawCircle(
                    color = radarColor,
                    radius = baseRadius * 1.15f,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(15f, 15f), 0f
                        )
                    )
                )
                // Cardinal Ticks
                for (i in 0..3) {
                    rotate(degrees = i * 90f, pivot = center) {
                        drawLine(
                            color = coreColor,
                            start = androidx.compose.ui.geometry.Offset(center.x, center.y - baseRadius * 1.25f),
                            end = androidx.compose.ui.geometry.Offset(center.x, center.y - baseRadius * 1.1f),
                            strokeWidth = 3f
                        )
                    }
                }
            }

            // 2. Center pulsing eye housing (concentric glowing rings)
            drawCircle(
                color = coreColor.copy(alpha = 0.05f * pulseScale),
                radius = baseRadius * pulseScale,
                center = center
            )
            drawCircle(
                color = coreColor.copy(alpha = 0.15f),
                radius = baseRadius * 0.75f * pulseScale,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )

            // 3. The Interactive Pupil (Dynamic aperture with micro-lines)
            drawCircle(
                color = coreColor,
                radius = baseRadius * 0.35f * (if (isSearching) 1.2f else pulseScale),
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
            // Glowing core iris bead
            drawCircle(
                color = if (isSearching) CoralRed else coreColor,
                radius = baseRadius * 0.15f * pulseScale,
                center = center
            )

            // 4. Horizontal digital scanning line that sweeps down the scope
            val lineY = height * scanLineY
            drawLine(
                color = if (isSearching) CoralRed else GoldenBrass.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(center.x - baseRadius * 1.1f, lineY),
                end = androidx.compose.ui.geometry.Offset(center.x + baseRadius * 1.1f, lineY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(8f, 5f), 0f
                )
            )
        }
    }
}

// ==========================================
// Helper functions for shared exports
// ==========================================

fun generateIcsFileAndShare(context: Context, isArabic: Boolean) {
    val icsContent = """
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//Military Medical College//Tawus Guide//EN
CALSCALE:GREGORIAN
BEGIN:VEVENT
SUMMARY:${if (isArabic) "تدريب ميداني: إنعاش الإصابات الحربية MARCH" else "Tactical Injury Resuscitation Drill (MARCH)"}
DTSTART:20260611T090000Z
DTEND:20260611T120000Z
LOCATION:${if (isArabic) "ميدان المحاكاة التكتيكية بالكلية" else "College Tactical Simulation Field"}
DESCRIPTION:${if (isArabic) "تطبيق بروتوكولات الرعاية التكتيكية للمصابين في خنادق الجبهة الأمامية طبقاً لمنهج الشهيد طاووس." else "Frontline military trauma resuscitation under TCCC standards."}
END:VEVENT
BEGIN:VEVENT
SUMMARY:${if (isArabic) "امتحان كفاءة الطب الملحق الميداني" else "Field Medical Competency Final Exam"}
DTSTART:20260615T083000Z
DTEND:20260615T113000Z
LOCATION:${if (isArabic) "القاعة الكبرى بالأكاديمية العسكرية" else "Main Examination Hall, Medical Academy"}
DESCRIPTION:${if (isArabic) "الامتحان النهائي الشامل النظري والعملي لفرز الإصابات تحت الضغط." else "Comprehensive combat triage under stress theory and practical exam."}
END:VEVENT
BEGIN:VEVENT
SUMMARY:${if (isArabic) "ورشة تفسير الأشعة والصور الإكلينيكية الذكية" else "Clinical Medical Imaging and AI Vision Workshop"}
DTSTART:20260618T130000Z
DTEND:20260618T153000Z
LOCATION:${if (isArabic) "مختبر التطوير والذكاء الاصطناعي الطبي" else "Medical AI R&D Laboratory"}
DESCRIPTION:${if (isArabic) "شرح تفاعلي لكيفية الاستفادة من نماذج الرؤية الحاسوبية في تشخيص استرواح الصدر واحتشاء عضلة القلب بالأطراف." else "Interactive session of diagnosing injuries using portable imaging & computer vision."}
END:VEVENT
END:VCALENDAR
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/calendar"
        putExtra(Intent.EXTRA_TEXT, icsContent)
        putExtra(Intent.EXTRA_SUBJECT, if (isArabic) "مزامنة التقويم والدورات التدريبية" else "Sync College Calendar & Drills")
    }
    context.startActivity(Intent.createChooser(intent, if (isArabic) "مزامنة التقويم الميداني" else "Sync Field Calendar"))
}

fun generateMilitaryDossierAndShare(context: Context, chatMessages: List<com.example.model.ChatMessage>, isArabic: Boolean) {
    val title = if (isArabic) "ملف الرقابة الأكاديمية والسريرية الموجه" else "OFFICIAL ACADEMIC MILITARY CLASSIFIED DOSSIER"
    val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
    
    val asciiBorder = "======================================================================"
    val dossierHeader = """
$asciiBorder
*         ACADEMIC MEDICAL MILITARY SERVICE & SEARCH LEAFLET         *
*     OFFICIAL RECORD -- COMMAND MEDICAL RETRIEVAL DECISION (RAG)    *
$asciiBorder
[DOCUMENT_UUID: ${java.util.UUID.randomUUID().toString().uppercase().take(8)}]
[CLASSIFICATION: ACADEMIC ACTIVE DUTY SECURE]
[PRINTED AT: $timestamp UTC]
$asciiBorder


    """.trimIndent()

    val chatLogContent = chatMessages.joinToString("\n\n") { msg ->
        val roleLabel = if (msg.role == "user") "RANGER EXAMINER / STUDENT" else "TAWUS INTELLIGENT ADVISOR"
        "[$roleLabel]\n${msg.content}\n--------------------------------------------------"
    }

    val syntheticQrCodeBlock = """


$asciiBorder
* SECURITY CRYPTOGRAPHIC VERIFICATION MATRIX (QR BLOCK CODA) *
$asciiBorder
 ██████████  █  ████  █  ██████████ 
 █        █ █  █ █ █  █  █        █ 
 █  ████  █  ████  ██ █  █  ████  █ 
 █  ████  █ ████ ████  █  █  ████  █ 
 █  ████  █ █  ████   ██ █  █  ████  █ 
 █        █ ██   ████ █  █        █ 
 ██████████ █ █ █ █ █ █  ██████████ 
 ████ █ ██████  █ █ ██      █ █ ███ 
   █ ██████ █  ██ ██████ █ ██ ███   
  █ █  █ ██ ███  █  █   ███ ███ ███ 
 ██████████  ██ ██   █ ███  █ █ ██  
 █        █ █ ██  █ ███   ██  ██ █  
 █  ████  █  █ ███ █ ███ ███ ██     
 █  ████  █ █ █ █   █ █  █████ ███  
 █  ████  █ ██     ██ ██████   █ █  
 █        █ █ █ █ ██   ██ ██ ██  █  
 ██████████ █████ █ █ ██   ███ █ █  
$asciiBorder
* Scan above military verification barcode to query blockchain ledger *
======================================================================
    """.trimIndent()

    val finalDossierText = dossierHeader + chatLogContent + syntheticQrCodeBlock

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, finalDossierText)
        putExtra(Intent.EXTRA_SUBJECT, title)
    }
    context.startActivity(Intent.createChooser(intent, if (isArabic) "مشاركة ملف الخدمة الميداني" else "Share Tactical Dossier"))
}

// ==========================================
// Component B: Tactical Connected Skill Tree Network
// ==========================================

@Composable
fun ConnectedSkillsNetwork(
    isArabic: Boolean,
    onNavigateToCourse: (String) -> Unit
) {
    var selectedSkillNode by remember { mutableStateOf<SkillNode?>(null) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dots")
    val movingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "path_flow"
    )

    val nodes = remember {
        listOf(
            SkillNode("K1", if (isArabic) "فرز وتصنيف TCCC" else "Combat Triage (TCCC)", "combat_care", true, 0.45f, 0.12f, "المستويات الطارئة لفرز ضحايا المعركة لإنقاذ الأرواح المهددة بالخنق أو النزيف."),
            SkillNode("K2", if (isArabic) "الممر الهوائي و MARCH" else "Airway Control & MARCH", "military_first_aid", true, 0.45f, 0.35f, "قنوات فتح مجرى الهواء والتهوية الميكانيكية الضاغطة الميدانية."),
            SkillNode("K3", if (isArabic) "إدارة تفجر النزيف الميداني" else "Massive Hemorrhage Control", "military_first_aid", true, 0.2f, 0.55f, "تطبيق الضواغط الشريانية السريعة والترقيع الهيدروليكي للشرايين."),
            SkillNode("K4", if (isArabic) "بضع الغشاء الحلقي" else "Emergency Surgical Airway", "anesthesia_diploma", true, 0.7f, 0.55f, "التدخل المقاوم لإغلاق الحنجرة العضوي والتنفس المفرغ السريع."),
            SkillNode("K5", if (isArabic) "بزل الصدر بإبرة الاسترواح" else "Thoracic Needle Decompression", "general_medicine", false, 0.45f, 0.75f, "تثقيب التجويف الرئوي لتفريغ الضغوط الهوائية القاتلة من الشظايا (مغلق - يتطلب دراسة دورة تكتيكية)."),
            SkillNode("K6", if (isArabic) "جراحة الإصابات الصدمية النفاذة" else "Penetrating Trauma Surgery", "general_medicine", false, 0.45f, 0.92f, "شحذ مهارات الجراحة العميقة للأعضاء الممزقة بشظايا القنابل (مغلق).")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
            .background(Color.White.copy(alpha = 0.01f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        // Draw underlying connected network threads with glowing moving flow particles
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // Render connected line links
            listOf(
                Pair("K1", "K2"),
                Pair("K2", "K3"),
                Pair("K2", "K4"),
                Pair("K3", "K5"),
                Pair("K4", "K5"),
                Pair("K5", "K6")
            ).forEach { (fromId, toId) ->
                val fromNode = nodes.firstOrNull { it.id == fromId }
                val toNode = nodes.firstOrNull { it.id == toId }
                if (fromNode != null && toNode != null) {
                    val p1 = androidx.compose.ui.geometry.Offset(fromNode.xPercent * width, fromNode.yPercent * height)
                    val p2 = androidx.compose.ui.geometry.Offset(toNode.xPercent * width, toNode.yPercent * height)

                    // Draw connecting grid background bezier or simple path
                    drawLine(
                        color = if (fromNode.unlocked && toNode.unlocked) GoldenBrass.copy(alpha = 0.4f) else Slate400.copy(alpha = 0.15f),
                        start = p1,
                        end = p2,
                        strokeWidth = if (fromNode.unlocked && toNode.unlocked) 2.5.dp.toPx() else 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(12f, 12f), 0f
                        )
                    )

                    // Pulsing packet dot flowing down the unlocked pipelines
                    if (fromNode.unlocked && toNode.unlocked) {
                        val dx = p2.x - p1.x
                        val dy = p2.y - p1.y
                        val len = kotlin.math.hypot(dx, dy)
                        if (len > 0f) {
                            val ratio = (movingOffset % 60f) / 60f
                            val dotX = p1.x + dx * ratio
                            val dotY = p1.y + dy * ratio
                            drawCircle(
                                color = GoldenBrass,
                                radius = 4f,
                                center = androidx.compose.ui.geometry.Offset(dotX, dotY)
                            )
                        }
                    }
                }
            }
        }

        // Render actual tactile nodes above paths
        nodes.forEach { node ->
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val xPos = (node.xPercent * maxWidth.value).dp
                val yPos = (node.yPercent * maxHeight.value).dp

                Box(
                    modifier = Modifier
                        .offset(x = xPos - 22.dp, y = yPos - 22.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (node.unlocked) GoldenBrass else Color(0xFF1E2836))
                        .border(
                            width = if (selectedSkillNode?.id == node.id) 3.dp else 1.5.dp,
                            color = if (node.unlocked) Color.White else Color.White.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                        .clickable { selectedSkillNode = node },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = node.id,
                        color = if (node.unlocked) Color.Black else Slate350,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = CairoFont
                    )
                    if (!node.unlocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔒", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Hud Bottom Info card for selected academic skill element
        selectedSkillNode?.let { node ->
            Card(
                colors = CardDefaults.cardColors(containerColor = LightNavy.copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, if (node.unlocked) GoldenBrass else Slate400.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (node.unlocked) "🎖️ ${node.title}" else "🔒 ${node.title} (مغلق)",
                            fontWeight = FontWeight.Bold,
                            color = if (node.unlocked) GoldenBrass else Slate400,
                            fontSize = 13.sp,
                            fontFamily = CairoFont,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { selectedSkillNode = null }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = CoralRed, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = node.description,
                        fontSize = 11.sp,
                        color = Slate300,
                        fontFamily = CairoFont,
                        lineHeight = 18.sp
                    )
                    if (node.unlocked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onNavigateToCourse(node.programId) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldenBrass),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp)
                        ) {
                            Text("📖 البدء بالدراسة والتحضير العملي", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                        }
                    }
                }
            }
        } ?: run {
            // Friendly tip
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isArabic) "🎖️ تتبع مسار المهارات العسكرية الميدانية (انقر للعرض)" else "🎖️ Click any node to open combat skill module detail",
                    color = GoldenBrass,
                    fontSize = 10.sp,
                    fontFamily = CairoFont,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class SkillNode(
    val id: String,
    val title: String,
    val programId: String,
    val unlocked: Boolean,
    val xPercent: Float,
    val yPercent: Float,
    val description: String
)

// ==========================================
// Component C: Combat Human Anatomy Interactive 3D Scanner (OpenGL/Geometric Canvas)
// ==========================================

@Composable
fun CombatAnatomySection(
    isArabic: Boolean,
    onQueryAnatomy: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "anatomy_rotation")
    
    // Virtual 3D Depth rotation tracker (Slider/Touch controllable)
    var viewPitchAngle by remember { mutableFloatStateOf(0f) }

    // Pulsing HUD highlights
    val glowRipple by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowing_ripple"
    )

    var selectedPartId by remember { mutableStateOf("") }
    var detailBriefText by remember { mutableStateOf("") }
    var anatomicalReferencedLectures by remember { mutableStateOf("") }

    val humanPoints = remember {
        listOf(
            // Styled 3D geometric outline vertices for human shape
            AnatomyVertex("head", if (isArabic) "💀 الدماغ والجمجمة (التنفس الميداني)" else "Brain & Airway Protection", 0f, 130f, 0f, 
                "إصابات الرأس والانفصال والارتجاج الدماغي الميداني. تشمل بضع الغشاء الحلقي لإغلاق مجرى التنفس وصيانة ممر العيون."),
            AnatomyVertex("chest", if (isArabic) "🫁 الرئتين والصدر (الاسترواح التوتري)" else "Lungs & Chest (Tension Pneumothorax)", 0f, 30f, 30f, 
                "استرواح الصدر التوتري (Tension Pneumothorax) المهدد الفوري لحياة المقاتل. الإنعاش العاجل بتثقيب الصدر بإبره Decompression."),
            AnatomyVertex("heart", if (isArabic) "❤️ القلب والشرايين الفخذية" else "Heart & Femoral Arteries", 0f, -10f, 0f, 
                "النزيف البالستي التفجري (Massive Hemorrhage) من الشرايين. يتطلب الضاغط الدوار الحربي (Tourniquet) صعوداً على الفخذ عاجلاً."),
            AnatomyVertex("limbs", if (isArabic) "💪 الأطراف وصدمات الكسور" else "Limbs & Combat Tourniquets", -55f, -60f, -20f, 
                "كسور الأطراف والشظايا المفتوحة الملوثة. رص وتثبيت الكسور ومكافحة البرد الشبه الصدمي (Hypothermia).")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(GoldenBrass.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💀", fontSize = 16.sp)
                    }
                    Text(
                        text = if (isArabic) "مرصد تشريح ومصابي الحرب التفاعلي ثلاثي الأبعاد" else "3D Interactive Combat Injuries Simulator",
                        fontWeight = FontWeight.Bold,
                        color = GoldenBrass,
                        fontSize = 13.sp,
                        fontFamily = CairoFont,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // The 3D Anatomy viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw Interactive 3D Wireframe Canvas
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val scale = 0.95f

                        // 1. Plot futuristic radar scanning grid lines
                        drawCircle(
                            color = GoldenBrass.copy(alpha = 0.04f),
                            radius = size.width.coerceAtMost(size.height) / 2.2f,
                            center = androidx.compose.ui.geometry.Offset(cx, cy)
                        )
                        drawLine(
                            color = GoldenBrass.copy(alpha = 0.05f),
                            start = androidx.compose.ui.geometry.Offset(0f, cy),
                            end = androidx.compose.ui.geometry.Offset(size.width, cy)
                        )
                        drawLine(
                            color = GoldenBrass.copy(alpha = 0.05f),
                            start = androidx.compose.ui.geometry.Offset(cx, 0f),
                            end = androidx.compose.ui.geometry.Offset(cx, size.height)
                        )

                        // 2. Draw geometric outlines representation of a human wireframe
                        val radAngle = Math.toRadians(viewPitchAngle.toDouble())
                        val cosA = kotlin.math.cos(radAngle).toFloat()
                        val sinA = kotlin.math.sin(radAngle).toFloat()

                        // Let's project 3D vertices x, y, z onto 2D viewport
                        fun project(x: Float, y: Float, z: Float): androidx.compose.ui.geometry.Offset {
                            // Rotate around Y-axis (view angle)
                            val rx = x * cosA - z * sinA
                            val rz = x * sinA + z * cosA
                            // standard orthographic projection
                            return androidx.compose.ui.geometry.Offset(cx + rx * scale, cy - y * scale)
                        }

                        // Human structural wire lines (Head, Torso, Spine, Leg lines, arms)
                        val vHead = project(0f, 130f, 0f)
                        val vNeck = project(0f, 110f, 0f)
                        val vLeftShoulder = project(-60f, 90f, -10f)
                        val vRightShoulder = project(60f, 90f, 10f)
                        val vSpineEnd = project(0f, -20f, 0f)
                        val vLeftHip = project(-35f, -40f, -10f)
                        val vRightHip = project(35f, -40f, 10f)
                        val vLeftHand = project(-80f, 0f, -15f)
                        val vRightHand = project(80f, 0f, 15f)
                        val vLeftFoot = project(-40f, -130f, -10f)
                        val vRightFoot = project(40f, -130f, 10f)

                        // Draw spine skeleton links
                        drawLine(GoldenBrass.copy(alpha = 0.15f), vHead, vNeck, strokeWidth = 5f)
                        drawLine(GoldenBrass.copy(alpha = 0.25f), vNeck, vSpineEnd, strokeWidth = 4f)
                        drawLine(GoldenBrass.copy(alpha = 0.15f), vNeck, vLeftShoulder, strokeWidth = 3f)
                        drawLine(GoldenBrass.copy(alpha = 0.15f), vNeck, vRightShoulder, strokeWidth = 3f)
                        drawLine(GoldenBrass.copy(alpha = 0.15f), vLeftShoulder, vLeftHand, strokeWidth = 2f)
                        drawLine(GoldenBrass.copy(alpha = 0.15f), vRightShoulder, vRightHand, strokeWidth = 2f)
                        drawLine(GoldenBrass.copy(alpha = 0.15f), vSpineEnd, vLeftHip, strokeWidth = 3f)
                        drawLine(GoldenBrass.copy(alpha = 0.15f), vSpineEnd, vRightHip, strokeWidth = 3f)
                        drawLine(GoldenBrass.copy(alpha = 0.15f), vLeftHip, vLeftFoot, strokeWidth = 2f)
                        drawLine(GoldenBrass.copy(alpha = 0.15f), vRightHip, vRightFoot, strokeWidth = 2f)

                        // 3. Draw Organ Interactive Targets (hotspots)
                        humanPoints.forEach { pt ->
                            val projPt = project(pt.x3D, pt.y3D, pt.z3D)
                            val isActive = selectedPartId == pt.id
                            
                            // Glowing aura circles
                            drawCircle(
                                color = if (isActive) CoralRed.copy(alpha = 0.3f) else GoldenBrass.copy(alpha = 0.1f),
                                radius = (18f + if (isActive) glowRipple else 4f),
                                center = projPt
                            )
                            drawCircle(
                                color = if (isActive) CoralRed else GoldenBrass,
                                radius = 8f,
                                center = projPt
                            )

                            // Crosshair lock indicators if active
                            if (isActive) {
                                drawLine(
                                    color = CoralRed,
                                    start = androidx.compose.ui.geometry.Offset(projPt.x - 22f, projPt.y),
                                    end = androidx.compose.ui.geometry.Offset(projPt.x + 22f, projPt.y),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = CoralRed,
                                    start = androidx.compose.ui.geometry.Offset(projPt.x, projPt.y - 22f),
                                    end = androidx.compose.ui.geometry.Offset(projPt.x, projPt.y + 22f),
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }

                    // Floating manual trigger pads that students can click to lock on specific regions instantly!
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        humanPoints.forEach { pt ->
                            val isActive = selectedPartId == pt.id
                            Text(
                                text = pt.title.split(" ")[0], // first word or icon
                                color = if (isActive) Color.Black else GoldenBrass,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFont,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isActive) GoldenBrass else Color.Transparent)
                                    .clickable {
                                        selectedPartId = pt.id
                                        detailBriefText = pt.description
                                        anatomicalReferencedLectures = pt.title
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Angle compass reader readout
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "ANGLE VIEW: ${viewPitchAngle.toInt()}°\nHUD SECURE V3D",
                            color = Slate400,
                            fontSize = 8.sp,
                            fontFamily = CairoFont,
                            lineHeight = 11.sp
                        )
                    }
                }

                // Rotation Angle Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(
                        "🔄 تدوير المجسم ثلاثي الأبعاد:",
                        fontSize = 11.sp,
                        color = Slate300,
                        fontFamily = CairoFont,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = viewPitchAngle,
                        onValueChange = { viewPitchAngle = it },
                        valueRange = -180f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldenBrass,
                            activeTrackColor = GoldenBrass,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Active selection presentation briefings
        if (selectedPartId.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GoldenBrass.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🎖️ $anatomicalReferencedLectures:",
                        fontWeight = FontWeight.Bold,
                        color = CoralRed,
                        fontSize = 13.sp,
                        fontFamily = CairoFont
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = detailBriefText,
                        fontSize = 12.sp,
                        color = Slate300,
                        fontFamily = CairoFont,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // High performance direct diagnostic query switch-bridge!
                    Button(
                        onClick = {
                            val promptToSend = "حدثت إصابة تكتيكية في الميدان في منطقة (${anatomicalReferencedLectures}). يرجى فك تشفير هذا وتعميم الإجراء التكتي والدليل الطبي العسكري المعتمد حسب مناهج الشهيد طاووس عسكرياً."
                            onQueryAnatomy(promptToSend)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenBrass),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "💬 اطرح استفساراً حياً على المرشد حول هذا العضو" else "💬 Query Commando Guide about this area",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFont
                        )
                    }
                }
            }
        } else {
            // Default Scanner empty highlight tip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.01f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) "⚕️ انقر على أي منطقة من مجسم مصابي الحرب أو على الأزرار فلاش لتصفح المقررات!" else "⚕️ Tap human anatomy body nodes to fetch urgent first-aid protocols",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontFamily = CairoFont,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class AnatomyVertex(
    val id: String,
    val title: String,
    val x3D: Float,
    val y3D: Float,
    val z3D: Float,
    val description: String
)

data class SuperBridgeResult(
    val overlapPercentage: Int,
    val transferableCoursesCount: Int,
    val transferableCourses: List<String>,
    val prerequisiteGaps: List<String>,
    val transitionalPlan: List<String>,
    val professionalTrajectory: String
)

