package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.model.Program
import com.example.service.LocalSpeechManager
import com.example.ui.components.MilitaryProgressBar
import com.example.ui.components.RadarWidget
import com.example.ui.theme.*
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

data class ProgramChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" or "assistant"
    val content: String,
    var liked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramChatScreen(
    program: Program,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speechManager = LocalSpeechManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val toneGen = remember { try { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100) } catch (e: Exception) { null } }

    var inputQuery by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf<ProgramChatMessage>() }
    var isSearching by remember { mutableStateOf(false) }
    var activeMode by remember { mutableStateOf(0) } // 0 = Chat, 1 = Tactical Scenario, 2 = Dossier Auditor

    // Roleplay state
    var roleplayStep by remember { mutableStateOf(0) } // 0 = Not started, 1, 2, 3 = Active Steps, 4 = Success, 5 = Failure
    var roleplayHp by remember { mutableStateOf(95) } // Patient survival probability %
    var roleplayScore by remember { mutableStateOf(100) }
    var selectedOptionIdx by remember { mutableStateOf<Int?>(null) }
    var stepFeedbackText by remember { mutableStateOf("") }
    var isOptionEvaluated by remember { mutableStateOf(false) }

    // Dossier Auditor state
    var selectedPresetDossierIdx by remember { mutableStateOf<Int?>(null) }
    var customDossierText by remember { mutableStateOf("") }
    var auditOutput by remember { mutableStateOf<AuditResult?>(null) }
    var isAuditing by remember { mutableStateOf(false) }

    // Custom program specific chips
    val queryChips = remember(program) {
        when {
            program.id.contains("nursing") -> {
                listOf(
                    "ما هي المهارات العملية المستهدفة؟",
                    "ما هى متطلبات ومجموعة ساعات التخرج؟",
                    "ما هي أهم مقررات الجزء الجراحي والإنعاشي؟",
                    "ما هي شروط القبول الميدانية والبدنية؟"
                )
            }
            program.id.contains("military_first_aid") -> {
                listOf(
                    "ما هي أولويات الإسعاف وفق MARCH؟",
                    "ما هي شروط وبدء دبلوم الإسعاف الحربي؟",
                    "ما تفاصيل الفصلين الدراسيين للمقررات؟",
                    "ما هي متطلبات التخرج وسجل المهارات؟"
                )
            }
            program.id.contains("general_medicine") -> {
                listOf(
                    "ما هي التخصصات الطبية للطب والجراحة؟",
                    "ما هي شروط القبول للطب البشري؟",
                    "ما تفاصيل سنوات الدراسة والامتياز؟",
                    "ما هي مخرجات التعلم بكلية طاووس؟"
                )
            }
            else -> {
                listOf(
                    "ما هي كفاءات التخدير العام والموضعي؟",
                    "ما هي فصول ومقررات التخدير التكتيكي؟",
                    "ما هي المتطلبات والشروط الطبية المحددة؟",
                    "ما هو المنهج المعتمد للتخرج بنجاح؟"
                )
            }
        }
    }

    // Default welcome message
    LaunchedEffect(program) {
        if (chatMessages.isEmpty()) {
            chatMessages.add(
                ProgramChatMessage(
                    role = "assistant",
                    content = """
                        مرحباً بك أيها المنتسب الأكاديمي. ⚕️
                        أنا مستشارك الخاص لبرنامج **"${program.title}"** بكناية الشهيد د. زيد طاووس.
                        
                        كيف يمكنني مساعدتك اليوم في توضيح:
                        - المقررات الأكاديمية وصيدلة وعلوم العمليات الميدانية.
                        - شروط القبول واللياقة الطبية والبدنية المفروضة.
                        - أهداف البرنامج وخطة السير الدراسي المعتمدة.
                        - تفاصيل التدريب السريري والمحاكاة التكتيكية النهائية.
                    """.trimIndent()
                )
            )
            speechManager.speak("مرحباً بك في لوحة الاستشارة لبرنامج " + program.title)
        }
    }

    val executeChatQuery = { queryText: String ->
        if (queryText.trim().isNotEmpty()) {
            chatMessages.add(ProgramChatMessage(role = "user", content = queryText))
            inputQuery = ""
            keyboardController?.hide()
            isSearching = true

            scope.launch {
                try {
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                } catch (_: Exception) {}

                val reply = askProgramBot(program, queryText)

                try {
                    toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                } catch (_: Exception) {}

                chatMessages.add(ProgramChatMessage(role = "assistant", content = reply))
                speechManager.speak(reply)
                isSearching = false
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "مستشار البرنامج الأكاديمي",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldenBrass
                            )
                            Text(
                                text = program.title,
                                fontSize = 11.sp,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
                    actions = {
                        IconButton(
                            onClick = {
                                if (chatMessages.isNotEmpty()) {
                                    val exportContent = chatMessages.joinToString("\n\n") { msg ->
                                        val roleLabel = if (msg.role == "user") "أنا (المنتسب الأكاديمي)" else "🤖 مستشار البرنامج الأكاديمي"
                                        "[$roleLabel]:\n${msg.content}"
                                    }
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, exportContent)
                                        putExtra(Intent.EXTRA_SUBJECT, "سجل استشارة لبرنامج: ${program.title}")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "تصدير المحادثة"))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "تصدير المحادثة",
                                tint = GoldenBrass
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkNavy,
                        titleContentColor = GoldenBrass
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
            }
        },
        containerColor = DarkNavy,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkNavy)
                .padding(8.dp)
        ) {
            // Program context badge
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(GoldenBrass.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GoldenBrass,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "محاورة مباشرة مبنية على الخطة المعتمدة والمستهدفة رسمياً للبرنامج.",
                        fontSize = 11.sp,
                        color = Slate400,
                        fontFamily = CairoFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // TRIPLE CONTROL BAR (Chat, Tactical Roleplay, Dossier Audit)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: General Chat
                Button(
                    onClick = { activeMode = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeMode == 0) GoldenBrass else Color.White.copy(alpha = 0.03f),
                        contentColor = if (activeMode == 0) Color.Black else GoldenBrass
                    ),
                    border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("المستشار الأكاديمي", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                }

                // Button 2: Tactical Roleplay
                Button(
                    onClick = { activeMode = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeMode == 1) CoralRed else Color.White.copy(alpha = 0.03f),
                        contentColor = if (activeMode == 1) Color.White else GoldenBrass
                    ),
                    border = BorderStroke(1.dp, if (activeMode == 1) CoralRed else GoldenBrass.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.3f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("المحاكاة الميدانية (MARCH)", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                }

                // Button 3: Dossier Auditor
                Button(
                    onClick = { activeMode = 2 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeMode == 2) GoldenBrass else Color.White.copy(alpha = 0.03f),
                        contentColor = if (activeMode == 2) Color.Black else GoldenBrass
                    ),
                    border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تدقيق المهارات", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // MODE RENDERING SWITCH
            if (activeMode == 0) {
                // MODE 0: ORIGINAL CHAT LAYOUT
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 50.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            queryChips.forEach { chip ->
                                Surface(
                                    onClick = { executeChatQuery(chip) },
                                    color = LightNavy.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = chip,
                                        fontSize = 10.sp,
                                        color = GoldenBrass,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFont,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Loader
                AnimatedVisibility(
                    visible = isSearching,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadarWidget()
                        Text(
                            text = "استخراج البيانات وتحليل المناهج المقارنة...",
                            fontSize = 11.sp,
                            color = GoldenBrass,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = CairoFont
                        )
                        MilitaryProgressBar(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Chat column
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = if (isUser) 20.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 20.dp
                                ),
                                color = if (isUser) GoldenBrass else Color.White.copy(alpha = 0.03f),
                                border = if (!isUser) BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null,
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isUser) "◀ تساؤل المنتسب:" else "🤖 مستشار المنهج الأكاديمي:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isUser) Color.Black.copy(alpha = 0.6f) else GoldenBrass,
                                        modifier = Modifier.padding(bottom = 6.dp),
                                        fontFamily = CairoFont
                                    )

                                    Text(
                                        text = msg.content,
                                        color = if (isUser) Color.Black else Slate100,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        fontFamily = CairoFont,
                                        fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal
                                    )

                                    // Action Options for each Assistant Message
                                    if (!isUser) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(0.5.dp)
                                                .background(Color.White.copy(alpha = 0.05f))
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Option 1: Copy
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = ClipData.newPlainText("Consultation Reply", msg.content)
                                                    clipboard.setPrimaryClip(clip)
                                                    try {
                                                        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                                                    } catch (_: Exception) {}
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "نسخ",
                                                    tint = GoldenBrass.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }

                                            // Option 2: Share
                                            IconButton(
                                                onClick = {
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_TEXT, "${program.title}:\n\n${msg.content}")
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة المشورة"))
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = "مشاركة",
                                                    tint = GoldenBrass.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }

                                            // Option 3: Like
                                            IconButton(
                                                onClick = {
                                                    msg.liked = !msg.liked
                                                    try {
                                                        toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                                                    } catch (_: Exception) {}
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (msg.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "أعجبني",
                                                    tint = if (msg.liked) CoralRed else GoldenBrass.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }

                                            // Option 4: Re-evaluate / Repeat
                                            IconButton(
                                                onClick = {
                                                    val lastUserQuery = chatMessages.lastOrNull { it.role == "user" }?.content
                                                    if (lastUserQuery != null) {
                                                        executeChatQuery(lastUserQuery)
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "إعادة",
                                                    tint = GoldenBrass.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Search input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = { Text("اطرح سؤالاً عن المقررات أو شروط هذا البرنامج...", fontSize = 12.sp, fontFamily = CairoFont) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { executeChatQuery(inputQuery) }),
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
                        onClick = { executeChatQuery(inputQuery) },
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
            } else if (activeMode == 1) {
                // MODE 1: TACTICAL ROLEPLAY GAME PANEL (MARCH ENGINE)
                val scenarios = remember(program) {
                    when {
                        program.id.contains("nursing") || program.id.contains("care") -> {
                            RoleplayScenario(
                                title = "مجرى الهواء والصدمة وعاصبة الجروح الشريانية الميدانية ⚕️",
                                initialSituation = "أنت ممرض ميداني بقوات الشهيد طاووس. جُلب جندي جريح ينزف بشدة من يده اليمنى جراء انفجار لغم، ويعاني من بحة حشرجة بمجرى التنفس مع غبار القصف المكثف بالخارج. كيف تبدأ خطة العلاج المباشرة؟",
                                steps = listOf(
                                    RoleplayStep(
                                        prompt = "الخطوة الأولى: تدبر الخطر المباشر والنزيف الحاد التعبوي 🩸",
                                        options = listOf(
                                            RoleplayOption("بدء قراءة العلامات الحيوية وإحصاء دقات النبض ببطء.", false, "خطأ تكتيكي! المريض يخسر دمه الشرياني المتدفق بالثانية، تفيد أولويات MARCH بإخماد النزف الغزير فوراً! (-15% بقاء)"),
                                            RoleplayOption("تركيب محاليل ملحية وريدية عريضة لرفع الضغط الهابط.", false, "خطأ طبي تكتيكي! تدفق المحاليل سيزيد ضخ النزيف ويمدد عناصر التخثر ما يقود لثمار الصدمة الوفاتية! (-25% بقاء)"),
                                            RoleplayOption("تطبيق عاصبة شريانية (Tourniquet) فوراً بارتفاع 2 بوصة فوق منبع النزف المشدود.", true, "ممتاز! السيطرة المباشرة على النزف الغزير (Massive Hemorrhage) هي الخلية الأولى للمحافظة على ضغط الأوعية والحياة!")
                                        )
                                    ),
                                    RoleplayStep(
                                        prompt = "الخطوة الثانية: تأمين مجرى النفس الفموي وتجنب الاختناق الميداني 🩺",
                                        options = listOf(
                                            RoleplayOption("محاولة إدخال أنبوب رغامي فموي صلب وسط ساحة الغبار.", false, "خطأ! إدخال أنبوب صلب بالمنشأة الميدانية المغبرة بدون تهيئة يزيد تهيج القصبات وخطر الإنزلاق الخاطئ! (-20% بقاء)"),
                                            RoleplayOption("إدخال نفق أنفي بلعومي مرن (NPA) وتثبيت المصاب مستلقياً على جنبه (Recovery Position).", true, "رائع! الأنبوب الأنفي البلعومي (NPA) يضمن مجرى تنفس (Airway) مفتوح ومأمون مع منع اللسان من سد الغشاء البلعومي أثناء الاستلقاء الجانبي ومقاومة الاستنشاق الطبيعي."),
                                            RoleplayOption("إعطاء المريض جرعات عالية من مسكنات المورفين بالفم فوراً.", false, "خطأ فاجع! المورفين يهبط نشاط الجهاز العصبي والتنفسي وقد يوقف حركة التبادل الهوائي بالكامل! (-30% بقاء)")
                                        )
                                    ),
                                    RoleplayStep(
                                        prompt = "الخطوة الثالثة: وقاية الجسد الصاعق من انخفاض الحرارة التراكمي (Prevent Hypothermia) 🛡️",
                                        options = listOf(
                                            RoleplayOption("تجريد الجندي من ملابسه العسكرية الرطبة لتوفير تسلل هواء منعش.", false, "خطأ وخيم! تبريد المريض يزيد حدة ثالوث الموت (انخفاض الحرارة، الحموضة، خلل التخثر) ويقضي عليه عاجلاً! (-35% بقاء)"),
                                            RoleplayOption("تغليف المريض بملحف فضي عاكس للحرارة عازل (Survival Blanket) مع شحذ بطاريات النقالة الطبية الدافئة.", true, "طوبي لك! الحفاظ الشامل على حرارة الجسد يمنع الاعتلال التخثري ويختم دورك الإسعافي بنجاح باهر!")
                                        )
                                    )
                                )
                            )
                        }
                        program.id.contains("general_medicine") -> {
                            RoleplayScenario(
                                title = "تدبير لغم الفخذ الأيمن وعلامات صدمة الدوران الميدانية 🩸",
                                initialSituation = "أنت الطبيب العسكري المناوب بنقطة المعركة. جندي أصيب بنثر شظايا صاروخية، ولديه بتر بالفخذ الأيمن مع نزيف فوار مستعر. يعاني من ذوبان الوعي وضغط دم متدهور 80/40 زئبقي.",
                                steps = listOf(
                                    RoleplayStep(
                                        prompt = "الخطوة الأولى: موافقة أولويات MARCH لإخماد الموت 🩸",
                                        options = listOf(
                                            RoleplayOption("حقنه فوراً بمادة الأدرينالين لتحفيز ضربات القلب المتقهقرة.", false, "خطأ! تسريع عضلة القلب ضد حجم وعائي فارغ ينتهي بفشل ضخ القلب الفوري والوفاة الحتمية! (-30% بقاء)"),
                                            RoleplayOption("التوجيه الفوري لشحذ لولب العاصبة (Tourniquet) في الثلث العلوي للفخذ وإحكامه بروافع الضغط الحديدية.", true, "أصبت! لابد من عاصبة سريعة ومحكمة لإقفال شريان الفخذ وإيقاف النزف الهائل المترادف مع هبوط الدورة!"),
                                            RoleplayOption("تضميد الجرح الكتانى الهائل بشاش ناعم ومراقبة توسع البؤبؤ.", false, "خطأ مميت! الشاش لا يحبس شريان الفخذ المتفجر؛ سيتصفى دمه ويموت خلال ثوان! (-40% بقاء)")
                                        )
                                    ),
                                    RoleplayStep(
                                        prompt = "الخطوة الثانية: معالجة مجرى النفس المهدد وتجنب الاستنشاق الرئوي 🩺",
                                        options = listOf(
                                            RoleplayOption("وضع المريض بالاستلقاء الظهري المسطح التام والبدء الإجباري بالتهوية اليدوية بالحقيبة الرغامية.", false, "خطأ تكتيكي كلاسيكي! المريض الفاقد وعيه قد يستنشق القيء والدم العالق؛ مما يشل الرئة صمتاً! (-20% بقاء)"),
                                            RoleplayOption("التحقق من سلامة الفم، وضع أنبوب أنفي بلعومي مرن (NPA) ووضعية الإفاقة الجانبية لمنع ارتخاء اللسان.", true, "ممتاز! NPA مع الإنعاش الجانبي يضمنان تهوية مريحة ومنع انسداد المخرج التنفسي بسوائل الجسم المعطلة لتبادل الغازات."),
                                            RoleplayOption("إجراء بزل الغشاء الحلقي فوراً دون التحقق من سالكية المسالك الهوائية.", false, "تسرع خاطئ! الاختراق الجراحي للرقبة لا يلجأ إليه إلا عند الاستعصاء التام للمسالك الطبيعية كالحروق الفكية المعقدة! (-15% بقاء)")
                                        )
                                    ),
                                    RoleplayStep(
                                        prompt = "الخطوة الثالثة: السيطرة الحيوية على هبوط الدورة الدموية ومقاومة الصقيع 🛡️",
                                        options = listOf(
                                            RoleplayOption("تغذية المصاب ببطء بسوائل باردة عن طريق الفم لتحسين وعيه.", false, "خطورة قاتلة! المريض المتدهور وعيه سيموت خنقاً جراء تسرب السوائل للرئتين لتعطل منعكسات البلع! (-30% بقاء)"),
                                            RoleplayOption("تركيب خطين وريدين وبدء حقن حمض الترانيكساميك (TXA) للحد من الجلطات والتدفئة بغطاء الصوف الحراري ونقله.", true, "عبقري! حقن TXA المبكر يمنع انحلال الخثرات والحفاظ الحراري يعطل ثالوث الموت الحتمي. تمت المحاكاة بنجاح!")
                                        )
                                    )
                                )
                            )
                        }
                        else -> { // Anesthesia & Standard Default
                            RoleplayScenario(
                                title = "بضع الغشاء الحلقي في خنادق القتال لصد الانسداد الحنجري ⚔️",
                                initialSituation = "أنت مسعف ميداني متقدم في وحدة جبهية متطورة. مقاتل تعرض لشظايا في الفك السفلي والحنجرة مسبباً ضيقاً شديداً بالتهوية وزراقاً حرجاً بوجهه، مع استعصاء التنفس الفموي التام. ماذا تفعل؟",
                                steps = listOf(
                                    RoleplayStep(
                                        prompt = "الخطوة الأولى: تدبير انسداد المخرج التنفسي العلوي الحرج 🩺",
                                        options = listOf(
                                            RoleplayOption("البدء بالضغط الرئوي المتكرر على القفص الصدري لعلاج توقف التنفس.", false, "خطأ فادح! عضلات الصدر تتحرك ولكن الهواء لا ينفذ للداخل لعوائق الحنجرة والفك المهشمة! (-25% بقاء)"),
                                            RoleplayOption("إجراء جراحي فوري لبضع الغشاء الحلقي والدرقي (Cricothyroidotomy) باستعمال مبزل تعبوي طبي معقم وتمرير أنبوب مجوف.", true, "مدهش! بضع الغشاء الحلقي والدرقي هو المنقذ المطلق لحالات البتر الفكي المحتقن لمنع الاختناق بالدماء والعقد التشريحية المكسورة!"),
                                            RoleplayOption("تعليق المريض من قدميه لإفراغ الدماء المتجمعة بالحلق تلقائياً.", false, "إجراء بدائي يعرض الفقرات والعنق للتهشيم الإضافي والوفاة المفاجئة! (-35% بقاء)")
                                        )
                                    ),
                                    RoleplayStep(
                                        prompt = "الخطوة الثانية: آلية الاستئصال الموضعي وبطاقة الأداء الجراحي الميداني 🩺",
                                        options = listOf(
                                            RoleplayOption("حقن الغشاء بليدوكائين مخدر سريع إن أمكن، ثم القيام بشق صليبى متسرع للغاية.", false, "خطأ! الشق الصليبي يشوه الأوعية الجانبية الحيوية بالرقبة مما يسبب نزيفاً غزيراً إضافياً بالحجرة الهوائية! (-20% بقاء)"),
                                            RoleplayOption("تطهير الجلد وثبات الغضروف الحلقي، ثم القيام بشق طولي رأسي بطول 1.5 سم ثم تدشين الشق الأفقي بالغشاء لخلخلة الأنبوب داخلياً وتثبيته.", true, "تفوق باهر! الشق العمودي (الرأسي) يحد من إصابة الأوعية الدموية بالخط المتوسط، والشق الأفقي اللاحق يضمن مسار الأنبوب بلا نزيف محتقن."),
                                            RoleplayOption("صب مادة كحولية حارقة بداخل الفتحة لتأكيد خلو البكتيريا.", false, "كارثة طبية! يسبب كحول الصب تدميراً كيميائياً فظيعاً لأنسجة الرغامى الهشة وينتهي بالوفاة الفورية! (-40% بقاء)")
                                        )
                                    ),
                                    RoleplayStep(
                                        prompt = "الخطوة الثالثة: الفحص التماثلي للتنفس والوقاية من البرد 🔬",
                                        options = listOf(
                                            RoleplayOption("وصل الأنبوب بكيس التهوية اليدوي وسماع دوي الشهيق بالجهتين مع دثر المقاتل بالبطانية العازلة والتركيز للإخلاء.", true, "علامة كاملة! الاستماع للأصوات يتأكد من سلامة تمدد الرئتين في الجانبين، والتدفئة تحبط تجلط الدماء المنحل. جندينا بأمان!"),
                                            RoleplayOption("تركيب الأنبوب وتركه مسترخياً ومفتوحاً في أجواء الشتاء الشديدة دون غطاء عازل.", false, "خطأ لوجستي! هبوط حرارة المقاتل المصاب يجعلك تخسر حياته بأثر صدمة الصقيع الرئوي! (-20% بقاء)")
                                        )
                                    )
                                )
                            )
                        }
                    }
                }

                // Game UI Render
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightNavy.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (roleplayHp < 50) CoralRed.copy(alpha = 0.5f) else GoldenBrass.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Vital signs & Stat HUD
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = scenarios.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenBrass,
                                    fontFamily = CairoFont,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = if (roleplayHp < 50) CoralRed.copy(alpha = 0.15f) else TacticalGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (roleplayHp < 50) CoralRed else TacticalGreen)
                                ) {
                                    Text(
                                        text = if (roleplayHp < 50) "الحالة: حرجة ⚠️" else "الحالة: مستقرة 🩺",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (roleplayHp < 50) CoralRed else TacticalGreen,
                                        fontFamily = CairoFont,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Stats Grid
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("احتمال البقاء", fontSize = 9.sp, color = Slate400, fontFamily = CairoFont)
                                    Text("$roleplayHp%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (roleplayHp < 50) CoralRed else TacticalGreen)
                                }
                                Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.1f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("التقييم التكتيكي", fontSize = 9.sp, color = Slate400, fontFamily = CairoFont)
                                    Text("$roleplayScore/100", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldenBrass)
                                }
                                Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.1f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("المرحلة الكلية", fontSize = 9.sp, color = Slate400, fontFamily = CairoFont)
                                    Text(
                                        text = when(roleplayStep) {
                                            0 -> "البدء"
                                            1, 2, 3 -> "$roleplayStep / 3"
                                            else -> "تمت"
                                        },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = CairoFont
                                    )
                                }
                            }
                        }

                        // Situation / Prompt Display
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp)
                                .background(Color.White.copy(alpha = 0.01f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (roleplayStep == 0) {
                                // Intro situation
                                Text(
                                    text = "🎖️ موجز ملف السيناريو والحدث:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenBrass,
                                    fontFamily = CairoFont
                                )
                                Text(
                                    text = scenarios.initialSituation,
                                    fontSize = 12.sp,
                                    color = Slate100,
                                    lineHeight = 18.sp,
                                    fontFamily = CairoFont
                                )
                            } else if (roleplayStep in 1..3) {
                                val currentStep = scenarios.steps[roleplayStep - 1]
                                Text(
                                    text = currentStep.prompt,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenBrass,
                                    fontFamily = CairoFont
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Options List
                                currentStep.options.forEachIndexed { optIdx, option ->
                                    val isSelected = selectedOptionIdx == optIdx
                                    val cardColor = when {
                                        isSelected && isOptionEvaluated && option.isCorrect -> TacticalGreen.copy(alpha = 0.12f)
                                        isSelected && isOptionEvaluated && !option.isCorrect -> CoralRed.copy(alpha = 0.12f)
                                        isSelected -> GoldenBrass.copy(alpha = 0.08f)
                                        else -> Color.White.copy(alpha = 0.02f)
                                    }
                                    val borderColor = when {
                                        isSelected && isOptionEvaluated && option.isCorrect -> TacticalGreen
                                        isSelected && isOptionEvaluated && !option.isCorrect -> CoralRed
                                        isSelected -> GoldenBrass
                                        else -> Color.White.copy(alpha = 0.06f)
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = cardColor),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, borderColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !isOptionEvaluated) {
                                                selectedOptionIdx = optIdx
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { if (!isOptionEvaluated) selectedOptionIdx = optIdx },
                                                colors = RadioButtonDefaults.colors(selectedColor = GoldenBrass, unselectedColor = Color.White.copy(alpha = 0.3f))
                                            )
                                            Text(
                                                text = option.text,
                                                fontSize = 11.sp,
                                                color = if (isSelected) Color.White else Slate300,
                                                lineHeight = 16.sp,
                                                fontFamily = CairoFont
                                            )
                                        }
                                    }
                                }

                                if (isOptionEvaluated && stepFeedbackText.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = stepFeedbackText,
                                        fontSize = 10.sp,
                                        color = if (currentStep.options[selectedOptionIdx ?: 0].isCorrect) TacticalGreen else CoralRed,
                                        fontFamily = CairoFont,
                                        lineHeight = 14.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    )
                                }
                            } else if (roleplayStep == 4) {
                                // Success view
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TacticalGreen, modifier = Modifier.size(44.dp))
                                    Text("بطل تكتيكي متميز! 🎖️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TacticalGreen, fontFamily = CairoFont)
                                    Text(
                                        text = "لقد نجحت في معالجة المصاب وإنقاذه تحت النيران طبقاً لبروتوكول MARCH المعتمد بكلية طاووس للعلوم الطبية الحربية.\n\nالتقييم الكلي الخاص بك: $roleplayScore / 100\nمستوى كفاءة البقاء: $roleplayHp%",
                                        fontSize = 11.sp,
                                        color = Slate100,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp,
                                        fontFamily = CairoFont
                                    )
                                }
                            } else {
                                // Failed view
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Dangerous, contentDescription = null, tint = CoralRed, modifier = Modifier.size(44.dp))
                                    Text("فشل المسار الإسعافي الميداني! ⚠️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CoralRed, fontFamily = CairoFont)
                                    Text(
                                        text = "لقد أدت القرارات الخاطئة لموت المصاب التكتيكي بسبب هبوط الدورة الحيوية أو الاختناق.\n\nتذكر دائماً ترتيب أولويات بروتوكول MARCH:\n1. النزيف الشديد المهدد للحياة أولاً.\n2. الممر التنفسي الهوائي ثانياً.\n3. التهوية الصدرية ثالثاً.\n4. مقاومة المرض وهبوط الحرارة رابعاً.",
                                        fontSize = 11.sp,
                                        color = Slate100,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp,
                                        fontFamily = CairoFont
                                    )
                                }
                            }
                        }

                        // Control Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (roleplayStep in 1..3 && !isOptionEvaluated) {
                                Button(
                                    onClick = {
                                        if (selectedOptionIdx != null) {
                                            val currentStep = scenarios.steps[roleplayStep - 1]
                                            val currentOption = currentStep.options[selectedOptionIdx!!]
                                            isOptionEvaluated = true
                                            stepFeedbackText = currentOption.feedback
                                            if (currentOption.isCorrect) {
                                                roleplayHp = (roleplayHp + 5).coerceAtMost(100)
                                                try { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 120) } catch (_: Exception) {}
                                            } else {
                                                roleplayHp = (roleplayHp - 30).coerceAtLeast(0)
                                                roleplayScore = (roleplayScore - 25).coerceAtLeast(0)
                                                try { toneGen?.startTone(ToneGenerator.TONE_SUP_ERROR, 200) } catch (_: Exception) {}
                                            }
                                        }
                                    },
                                    enabled = selectedOptionIdx != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldenBrass, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("تأكيد القرار التكتيكي ⚖️", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                                }
                            } else if (roleplayStep in 1..3 && isOptionEvaluated) {
                                Button(
                                    onClick = {
                                        if (roleplayHp <= 30) {
                                            roleplayStep = 5 // Fail Mode
                                        } else if (roleplayStep == 3) {
                                            roleplayStep = 4 // Success Mode
                                        } else {
                                            roleplayStep++
                                        }
                                        selectedOptionIdx = null
                                        isOptionEvaluated = false
                                        stepFeedbackText = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldenBrass, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (roleplayStep == 3) "إنهاء المحاكاة وحصد التقييم 🎖️" else "المرور للخطوة التالية ◀", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                                }
                            } else if (roleplayStep == 0) {
                                Button(
                                    onClick = {
                                        roleplayStep = 1
                                        roleplayHp = 90
                                        roleplayScore = 100
                                        selectedOptionIdx = null
                                        isOptionEvaluated = false
                                        stepFeedbackText = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("دخول ساحة المحاكاة وبث الصوت الذكي 🎙️", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                                }
                            } else {
                                // Success or Failure screen buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            roleplayStep = 0
                                            roleplayHp = 95
                                            roleplayScore = 100
                                            selectedOptionIdx = null
                                            isOptionEvaluated = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("إعادة المحاكاة 🔁", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                                    }
                                    Button(
                                        onClick = {
                                            // Share assessment score
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "لقد أتممت بنجاح محاكاة السيناريو الطبي العسكري لبرنامج: ${program.title} بكفاءة بقاء $roleplayHp% وتقييم تكتيكي $roleplayScore/100 بنظام الشهيد طاووس الذكي!")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الدرجة"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldenBrass, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("مشاركة النتيجة 🎖️", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // MODE 2: STUDENT DOSSIER AUDITOR PANEL (التحليل الأكاديمي والمطابقة ومسارات التعويض)
                val presetDossiers = remember {
                    listOf(
                        AcademicDossier(
                            title = "دبلوم إسعاف حربي وطوارئ (إمكانيات عاصم الميدانية) 🚑",
                            backgroundText = "دبلوم 3 فصول دراسية، 75 ساعة معتمدة في مهارات إخلاء الجرحى تحت النيران، تدريب سريري بالمستشفيات العامة لمدة 6 أشهر.",
                            hours = 75,
                            experience = "مساعد إسعاف ميداني لمدة سنتين في العمليات اللوجستية وتضميد الجروح.",
                            coursesStudied = listOf("الإسعاف الأساسي الأولوي", "تمريض الصدمات الأساسي", "علم التشريح الإنساني المبسط", "لوجستيات الإخلاء والتعبئة")
                        ),
                        AcademicDossier(
                            title = "دبلوم تمريض متوسط عام مدني (الرعاية بالمراكز الصحية) 🩺",
                            backgroundText = "دبلوم دراسي معتمد بـ 90 ساعة معتمدة في علوم التمريض الأساسية، الباطنة الجراحية المعتدلة، طب الأطفال والنساء والتوليد بالمشافي المدنية.",
                            hours = 90,
                            experience = "خبرة سنة ونصف في طوارئ وممرض جناح الجراحة بمستشفى الشفاء العام.",
                            coursesStudied = listOf("علم التشريح والوظائف الكامل", "مبادئ علم الصيدلة الإكلينيكية", "تمريض العناية المركزة والطوارئ", "مهارات غرف الجراحات المعقمة")
                        ),
                        AcademicDossier(
                            title = "شهادة خبرة مساعد ممرض ومعاون طوارئ الحدود 🎖️",
                            backgroundText = "مساعد ميداني غير مصنف أكاديمياً، يمتلك شهادة كفاءة الإسعاف تحت إشراف وزارة الصحة لمدة 45 ساعة معتمدة مع مهارات أساسية بالصيدلية النقطية.",
                            hours = 45,
                            experience = "خبرة 4 سنوات في الخطوط الأمامية والإجراءات الإسعافية العاجلة وصدمة الحروب السطحية.",
                            coursesStudied = listOf("مبادئ الإسعافات الأولية الأساسية", "المطهرات والضمادات الجراحية", "رعاية التضميد الميداني المتقدم")
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightNavy.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "📋 مدقق السجلات ومطابقة تطلعات الكفاءة للمنتسبين",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldenBrass,
                                fontFamily = CairoFont
                            )
                            Text(
                                "حدد السيرة الأكاديمية المفترضة لجندي أو منتسب ومقارنتها تلقائياً بمتطلبات برنامج **${program.title}** لتوليد خطة مخصصة لسد الثغرات المهارية المعرفية.",
                                fontSize = 10.sp,
                                color = Slate400,
                                fontFamily = CairoFont,
                                lineHeight = 14.sp
                            )

                            // Preset Selectors Chips
                            Text("أو اختر سيرة مفترضة من المخزن:", fontSize = 9.sp, color = GoldenBrass, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                presetDossiers.forEachIndexed { didx, d ->
                                    val isPicked = selectedPresetDossierIdx == didx
                                    Surface(
                                        onClick = {
                                            selectedPresetDossierIdx = didx
                                            customDossierText = "سيرة المنتسب: ${d.title}\nالخلفية: ${d.backgroundText}\nالساعات المعتمدة: ${d.hours} ساعة\nالخبرة العملية: ${d.experience}"
                                        },
                                        color = if (isPicked) GoldenBrass.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f),
                                        border = BorderStroke(1.dp, if (isPicked) GoldenBrass else Color.White.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = d.title.split(" ")[1], // get shorter name
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPicked) GoldenBrass else Slate300,
                                            fontFamily = CairoFont,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            // Custom Input field
                            OutlinedTextField(
                                value = customDossierText,
                                onValueChange = {
                                    customDossierText = it
                                    selectedPresetDossierIdx = null
                                },
                                label = { Text("أدخل تفاصيل الخلفية الأكاديمية والعملية للمنتسب...", fontSize = 10.sp, fontFamily = CairoFont) },
                                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.White, fontFamily = CairoFont),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(76.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldenBrass,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedTextColor = Color.White
                                )
                            )

                            // Audit Start Button
                            Button(
                                onClick = {
                                    isAuditing = true
                                    scope.launch {
                                        delay(1200) // simulated processing delay
                                        val generatedResult = when {
                                            selectedPresetDossierIdx == 0 -> {
                                                AuditResult(
                                                    matchPercentage = 75,
                                                    waivedCredits = 50,
                                                    waivedCourses = listOf("الإسعاف المعتمد الأولي I", "مهارات الإخلاء العسكري", "التمريض الأساسي ومناورات MARCH"),
                                                    skillGaps = listOf("الصيدلة والتداخل الدوائي الإكلينيكي", "العمل بمجرى الهواء الجراحي المعقد", "الإنعاش المتقدم في ظل التفجير"),
                                                    bridgePlan = listOf(
                                                        "مقرر الصيدلة الدوائية في ميدان القتال - ساعات معتمدة: 5 ساعات (الفصل الأول)",
                                                        "برنامج بضع الغشاء الحلقي الجراحي المكثف - ساعات معتمدة: 4 ساعات (الفصل الأول)",
                                                        "دورة الإنعاش والمحاكاة التكتيكية للبالغين - ساعات معتمدة: 6 ساعات (الفصل الثاني)"
                                                    )
                                                )
                                            }
                                            selectedPresetDossierIdx == 1 -> {
                                                AuditResult(
                                                    matchPercentage = 55,
                                                    waivedCredits = 45,
                                                    waivedCourses = listOf("علم وظائف الجسد الأساسي I", "مسارات الإنعاش المدني الأساسية", "أساسيات تمريض الباطنة"),
                                                    skillGaps = listOf("الصيدلة وعلوم العمليات الميدانية العسكرية", "تدبير النزيف الشرياني والبرشمة عسكرياً (TCCC)", "اللياقة البدنية التكتيكية والرماية الطبية"),
                                                    bridgePlan = listOf(
                                                        "مسار العقيدة الطبية الحربية المتقدمة - ساعات معتمدة: 4 ساعات (الفصل التمهيدي)",
                                                        "حزمة مهارات عاصبة TCCC الشريانية وحشو الجروح - ساعات معتمدة: 6 ساعات (الفصل الأول)",
                                                        "محاكاة التدريب الميداني والنزيف التعبوي - ساعات معتمدة: 8 ساعات (الفصل الثاني)"
                                                    )
                                                )
                                            }
                                            else -> {
                                                AuditResult(
                                                    matchPercentage = 40,
                                                    waivedCredits = 25,
                                                    waivedCourses = listOf("الإسعاف العضوي البسيط", "التطهير ومعاملة الجروح المحدودة"),
                                                    skillGaps = listOf("جميع علوم المناهج التمريضية النظرية والأكاديمية", "التشريح الفسيولوجي النظري العميق", "بضع الصدر الإبري الميداني TCCC"),
                                                    bridgePlan = listOf(
                                                        "فصل تمهيدي تعويضي كامل لعلوم الأكاديمية والتشريح - 15 ساعة معتمدة",
                                                        "مقرر المهارات الجراحية والممر التنفسي عسكرياً - 6 ساعات (الفصل الأول)",
                                                        "برنامج اللياقة الطبية لبعثات طاووس - 3 ساعات (الفصل الأول)"
                                                    )
                                                )
                                            }
                                        }
                                        auditOutput = generatedResult
                                        isAuditing = false
                                        try { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 100) } catch (_: Exception) {}
                                    }
                                },
                                enabled = customDossierText.isNotEmpty() && !isAuditing,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldenBrass, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                if (isAuditing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("جاري فحص وتدقيق المقارنة الأكاديمية...", fontSize = 11.sp, fontFamily = CairoFont)
                                } else {
                                    Icon(Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تحليل السجل وتوليف الخطة التعويضية 📝", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                                }
                            }
                        }
                    }

                    // Audit Result Screen Area
                    auditOutput?.let { res ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightNavy.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, TacticalGreen.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("تقرير تدقيق السجلات الأكاديمي 🎖️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldenBrass, fontFamily = CairoFont)
                                        Text("معادلة: ${res.matchPercentage}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TacticalGreen, fontFamily = CairoFont)
                                    }
                                    LinearProgressIndicator(
                                        progress = { res.matchPercentage / 100f },
                                        color = TacticalGreen,
                                        trackColor = Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    )
                                    Text("مجموع الساعات المحتسبة للأكاديمية: ${res.waivedCredits} ساعة معتمدة.", fontSize = 9.sp, color = Slate300, fontFamily = CairoFont)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                }

                                // Waived
                                item {
                                    Text("✔️ المقررات المستوفاة والمعادلة تلقائياً:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TacticalGreen, fontFamily = CairoFont)
                                    res.waivedCourses.forEach { c ->
                                        Text("  • $c", fontSize = 9.sp, color = Slate300, fontFamily = CairoFont)
                                    }
                                }

                                // Gaps
                                item {
                                    Text("⚠️ الثغرات المهارية المعرفية المكتشفة:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoralRed, fontFamily = CairoFont)
                                    res.skillGaps.forEach { g ->
                                        Text("  • $g", fontSize = 9.sp, color = Slate300, fontFamily = CairoFont)
                                    }
                                }

                                // Bridge Study Plan
                                item {
                                    Text("⚙️ الخطة التعويضية الموصى بها لإتمام التخرج:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldenBrass, fontFamily = CairoFont)
                                    res.bridgePlan.forEach { plan ->
                                        Text("  🛡️ $plan", fontSize = 9.sp, color = Slate100, fontFamily = CairoFont, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Share Plan
                                item {
                                    Button(
                                        onClick = {
                                            val shareText = "مخطط التجسير التعويضي لبرنامج: ${program.title}\nالمعادلة: ${res.matchPercentage}%\n\nالمقررات المستوفاة:\n${res.waivedCourses.joinToString("\n")}\n\nالخطة التعويضية:\n${res.bridgePlan.joinToString("\n")}"
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "تصدير الخطة"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TacticalGreen, contentColor = Color.White),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        Text("تصدير ومشاركة الخطة التعويضية الرقابية 📤", fontSize = 10.sp, fontFamily = CairoFont)
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

private suspend fun askProgramBot(program: Program, query: String): String = withContext(Dispatchers.IO) {
    val apiKey = try {
        BuildConfig.GEMINI_API_KEY
    } catch (e: Exception) {
        ""
    }

    val hasGemini = apiKey.isNotEmpty() && !apiKey.contains("placeholder") && !apiKey.contains("MY_GEMINI")

    // Compile objectives and curriculum details into a cohesive system prompt
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
        - أجب باللغة العربية بدقة متناهية وبأسلوب عسكري، وقور، ملخص ومنظم.
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
                if (!response.isSuccessful) {
                    return@withContext "خطأ جاري الاتصال بخادم الاستشارة الأكاديمي عسكرياً."
                }
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
        } catch (e: Exception) {
            return@withContext "🎖️ لم نتمكن من الوصول لمرشد طاووس السحابي للبرنامج. إليك لمحة محلية:\nهذا البرنامج يتكون من ${program.duration} بلغة تعليمية ${program.language}. للاستفسارات التفصيلية نرجو تفعيل رمز الوصول والمحاكاة السحابية."
        }
    }

    // Local heuristic search (Fallbacks if no API key is set)
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

    // Try faq matching
    val matchedFaq = program.faq.firstOrNull { it.q.contains(query) || query.contains(it.q) }
    if (matchedFaq != null) {
        return@withContext "🎖️ **جـ:** ${matchedFaq.a}"
    }

    return@withContext "🎖️ تم استلام استفسارك الأكاديمي بخصوص برنامج **${program.title}** عسكرياً.\nهذا البرنامج مدته ${program.duration} ويُدرس بـ ${program.language}. الأهداف والخطط الدراسية وتفاصيل القبول معلنة بالكامل في دليل البرامج للتطبيق."
}

data class RoleplayScenario(
    val title: String,
    val initialSituation: String,
    val steps: List<RoleplayStep>
)

data class RoleplayStep(
    val prompt: String,
    val options: List<RoleplayOption>
)

data class RoleplayOption(
    val text: String,
    val isCorrect: Boolean,
    val feedback: String
)

data class AcademicDossier(
    val title: String,
    val backgroundText: String,
    val hours: Int,
    val experience: String,
    val coursesStudied: List<String>
)

data class AuditResult(
    val matchPercentage: Int,
    val waivedCredits: Int,
    val waivedCourses: List<String>,
    val skillGaps: List<String>,
    val bridgePlan: List<String>
)
