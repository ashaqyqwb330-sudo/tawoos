package com.example.ui

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

// Navigation Enum for our Cinematic Welcome Onboarding flow
enum class WelcomeStep {
    CINEMATIC_3D_INTRO,         // Phase 1: Rotating 3D Emblem & Universe Title
    SERVICES_WINDOWS_SHOWCASE,  // Phase 2: Beautiful windows displaying all application details interactively
    SMART_GUIDE_WELCOME,        // Phase 3: Dialogue terminal and custom intro to Dr. Zaid Tawoos Smart Advisor
    DEVELOPER_DOOR_REVEAL       // Phase 4: Sliding double-doors entry showcasing Engineer Idris Al-Madani, then Launcher
}

data class ServiceWindow(
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val glowColor: Color
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WelcomeCinematicScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(WelcomeStep.CINEMATIC_3D_INTRO) }
    val coroutineScope = rememberCoroutineScope()
    
    // Tone generator for sci-fi tech sound effects
    val toneGen = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            null
        }
    }
    
    // Play sound helper
    val playBeep = { toneType: Int ->
        try {
            toneGen?.startTone(toneType, 110)
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B19),
                        Color(0xFF0F172A),
                        Color(0xFF050811)
                    )
                )
            )
    ) {
        // Starry Cinematic Background Canvas
        StarryCanvasField()

        // Content Holder
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(700)) + slideInVertically(
                    animationSpec = tween(700),
                    initialOffsetY = { it / 3 }
                ) with fadeOut(animationSpec = tween(500)) + slideOutVertically(
                    animationSpec = tween(500),
                    targetOffsetY = { -it / 3 }
                )
            },
            label = "WelcomeWizardTransition"
        ) { step ->
            when (step) {
                WelcomeStep.CINEMATIC_3D_INTRO -> {
                    Cinematic3DIntroView(
                        onNext = {
                            playBeep(ToneGenerator.TONE_PROP_BEEP)
                            currentStep = WelcomeStep.SERVICES_WINDOWS_SHOWCASE
                        }
                    )
                }
                WelcomeStep.SERVICES_WINDOWS_SHOWCASE -> {
                    ServicesWindowsShowcaseView(
                        onNext = {
                            playBeep(ToneGenerator.TONE_PROP_ACK)
                            currentStep = WelcomeStep.SMART_GUIDE_WELCOME
                        },
                        onBack = {
                            playBeep(ToneGenerator.TONE_PROP_NACK)
                            currentStep = WelcomeStep.CINEMATIC_3D_INTRO
                        }
                    )
                }
                WelcomeStep.SMART_GUIDE_WELCOME -> {
                    SmartGuideWelcomeView(
                        onNext = {
                            playBeep(ToneGenerator.TONE_CDMA_PIP)
                            currentStep = WelcomeStep.DEVELOPER_DOOR_REVEAL
                        },
                        onBack = {
                            playBeep(ToneGenerator.TONE_PROP_NACK)
                            currentStep = WelcomeStep.SERVICES_WINDOWS_SHOWCASE
                        }
                    )
                }
                WelcomeStep.DEVELOPER_DOOR_REVEAL -> {
                    DeveloperDoorRevealView(
                        onFinished = {
                            playBeep(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
                            onFinished()
                        },
                        onBack = {
                            playBeep(ToneGenerator.TONE_PROP_NACK)
                            currentStep = WelcomeStep.SMART_GUIDE_WELCOME
                        }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STARRY CANVAS FIELD
// -------------------------------------------------------------
@Composable
fun StarryCanvasField() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starsAlpha"
    )
    val floatMovement by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingY"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val starsCount = 60
        // Seed points
        for (i in 0 until starsCount) {
            val x = (i * 927) % size.width
            val y = ((i * 413) % size.height) + (sin(i.toDouble() + floatMovement / 10f) * 15f).toFloat()
            val radius = ((i * 3) % 4 + 2).toFloat()
            val isGolden = i % 7 == 0
            val color = if (isGolden) GoldenBrass else Color.White
            
            drawCircle(
                color = color.copy(alpha = alphaAnim * (0.3f + (i % 5) / 10f)),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

// -------------------------------------------------------------
// STEP 1: CINEMATIC 3D INTRO VIEW
// -------------------------------------------------------------
@Composable
fun Cinematic3DIntroView(onNext: () -> Unit) {
    var dragX by remember { mutableStateOf(0f) }
    var dragY by remember { mutableStateOf(0f) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "logoGlow")
    val selfRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "selfRot"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emblemGlowScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Badge Intro Glow Text
        Surface(
            color = GoldenBrass.copy(alpha = 0.08f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.35f)),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "🎖️ الإصدار الذكي الفاخر • مرشد الأكاديمية والميدان 🎖️",
                color = GoldenBrass,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFont,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // INTERACTIVE 3D FLOATING EMBLEM (Drag to see perspective)
        Box(
            modifier = Modifier
                .size(240.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragX = (dragX + dragAmount.x / 1.5f).coerceIn(-45f, 45f)
                            dragY = (dragY - dragAmount.y / 1.5f).coerceIn(-45f, 45f)
                        },
                        onDragEnd = {
                            dragX = 0f
                            dragY = 0f
                        }
                    )
                }
                .graphicsLayer {
                    rotationX = dragY
                    rotationY = dragX + (selfRotation / 12f) // Combines gesture + subtle constant spin
                    cameraDistance = 14f * density
                },
            contentAlignment = Alignment.Center
        ) {
            // Neon shadow background
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .scale(glowScale)
                    .blur(16.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GoldenBrass.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Outer Shield/Border
            Box(
                modifier = Modifier
                    .size(175.dp)
                    .border(
                        BorderStroke(4.dp, Brush.linearGradient(listOf(GoldenBrass, TacticalGreen, GoldenBrass))),
                        CircleShape
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Spinning Military-Tech Ring inside
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = -selfRotation }
                        .border(BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.5f)), CircleShape)
                ) {
                    // Small teeth on the gear ring
                    for (i in 0 until 12) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .align(Alignment.TopCenter)
                                .graphicsLayer {
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 15.5f)
                                    rotationZ = (i * 30).toFloat()
                                }
                                .background(GoldenBrass, RoundedCornerShape(1.dp))
                        )
                    }
                }

                // Core Emblem Card containing Dr Zaid Tawoos identity outline
                Box(
                    modifier = Modifier
                        .size(135.dp)
                        .background(Color(0xFF112240), CircleShape)
                        .border(BorderStroke(2.dp, GoldenBrass.copy(alpha = 0.8f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Military Badge",
                            tint = GoldenBrass,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SMART",
                            color = TacticalGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "GUIDE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Main Title
        Text(
            text = "مرشد الشهيد د. زيد طاؤوس الذكي",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontFamily = CairoFont,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Arabic Subtitle with local intelligence mention
        Text(
            text = "منصة الذكاء اللغوي المصممة خصيصاً تخليداً لمسيرته الأكاديمية والسريرية الخالدة بجامعة اليمن وكلية العلوم الطبية.",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Slate300,
            textAlign = TextAlign.Center,
            fontFamily = CairoFont,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Feature Tags Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = {},
                label = { Text("المطابقة اللغوية", fontSize = 10.sp, color = GoldenBrass, fontFamily = CairoFont) },
                leadingIcon = { Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(12.dp), tint = GoldenBrass) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.03f))
            )
            Spacer(modifier = Modifier.width(6.dp))
            AssistChip(
                onClick = {},
                label = { Text("التجسير الأكاديمي", fontSize = 10.sp, color = TacticalGreen, fontFamily = CairoFont) },
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(12.dp), tint = TacticalGreen) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.03f))
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Primary Guided Button with interactive pulse border
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldenBrass,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ابدأ استكشاف المزايا والخدمات",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFont
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "اسحب الشعار الدائري العلوي لتجربة مؤثر المنظور ثلاثي الأبعاد 3D",
            fontSize = 9.sp,
            color = Slate400,
            fontFamily = CairoFont
        )
    }
}

// -------------------------------------------------------------
// STEP 2: INTERACTIVE SERVICES SHOWCASE WINDOWS
// -------------------------------------------------------------
@Composable
fun ServicesWindowsShowcaseView(
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var selectedWindowIndex by remember { mutableStateOf<Int?>(null) }
    
    val windows = listOf(
        ServiceWindow(
            "بوابة الاستشارة والمحادثة المعرفية",
            "Smart AI Advisor Console",
            "نظام محادثة عصبي مدرب على لوائح وقوانين الكلية وبرامج البكالوريوس والدبلومات العسكرية للإجابة في الزمن الحقيقي.",
            "Instant answers regarding study terms, duration, courses, and schedules.",
            Icons.Default.ChatBubble,
            TacticalGreen
        ),
        ServiceWindow(
            "مستشار تجسير المناهج العابر",
            "Cross-Program Super Adapter",
            "تطبيق احتساب نسبة تداخل المناهج بين تخصصات التخدير والتمريض ومقارنة الساعات وصياغة خطط مسارات العبور تلقائياً.",
            "Compare custom credits overlap and draw instant progression paths.",
            Icons.Default.AccountTree,
            GoldenBrass
        ),
        ServiceWindow(
            "حقيبة البرامج والمقررات المفصلة",
            "Interactive Academic Dossier",
            "موسوعة شاملة للمقررات والخطط السنوية من الكلية والمعهد الطبي العسكري ورموز المواد وساعاتها وسير مخرجات التعلم.",
            "Full access to the core curriculums library, prerequisites and credit hours.",
            Icons.Default.Book,
            Color(0xFFFF9F43)
        ),
        ServiceWindow(
            "المطابقة والمصادقة والامتحانات",
            "Quality & Credentials Lounge",
            "لوحة جودة المنهج واحتساب ساعات الخبرات الميدانية واختبارات القبول الطبية الميدانية والمحاكاة OSCE السنوية.",
            "Academic audits dashboard including mock OSCE simulator tests.",
            Icons.Default.Verified,
            CoralRed
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "نوافذ الخدمات الشمولية للتطبيق",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GoldenBrass,
                fontFamily = CairoFont,
                textAlign = TextAlign.Center
            )

            Text(
                text = "انقر على أي نافذة أدناه لاستعراض شرحها التقني والوظيفي المتكامل قبل فتح مرشد الشهيد.",
                fontSize = 11.sp,
                color = Slate300,
                textAlign = TextAlign.Center,
                fontFamily = CairoFont,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Showcase 2x2 Grid using standard Column/Row
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in 0 until 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0 until 2) {
                            val index = row * 2 + col
                            val win = windows[index]
                            val isSelected = selectedWindowIndex == index
                            
                            val bounceScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.05f else 1.0f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "bounce"
                            )

                            // Glassmorphic Window Card with interactive hover glow and 3D angle
                            Card(
                                onClick = {
                                    selectedWindowIndex = if (isSelected) null else index
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) win.glowColor.copy(alpha = 0.15f) else Color(0xFF112240).copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isSelected) win.glowColor else Color.White.copy(alpha = 0.11f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer {
                                        scaleX = bounceScale
                                        scaleY = bounceScale
                                        rotationX = if (isSelected) 4f else 0f
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(
                                                win.glowColor.copy(alpha = 0.15f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = win.icon,
                                            contentDescription = win.titleAr,
                                            tint = win.glowColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = win.titleAr,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = CairoFont,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = win.titleEn,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Slate400,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Info Reveal Panel when Window is selected
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, GoldenBrass.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedWindowIndex != null) {
                    val activeWin = windows[selectedWindowIndex!!]
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💡 تفاصيل: " + activeWin.titleAr,
                            color = activeWin.glowColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFont
                        )
                        Text(
                            text = activeWin.descAr,
                            color = Slate100,
                            fontSize = 10.sp,
                            fontFamily = CairoFont,
                            lineHeight = 15.sp
                        )
                        Text(
                            text = activeWin.descEn,
                            color = Slate350,
                            fontSize = 9.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                } else {
                    Text(
                        text = "ℹ️ انقر على أي لوحة / نافذة أعلاه لمشاهدة التفاصيل المتقدمة وخطة التشغيل.",
                        color = Slate400,
                        fontSize = 10.sp,
                        fontFamily = CairoFont,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate400.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = "عودة للخلف",
                    fontSize = 12.sp,
                    fontFamily = CairoFont
                )
            }

            Button(
                onClick = onNext,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldenBrass,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .weight(2.2f)
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "الانتقال للمرشد الذكي والتعريف ⚡",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFont
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 3: OFFICIAL SMART GUIDE TERMINAL WELCOME
// -------------------------------------------------------------
@Composable
fun SmartGuideWelcomeView(
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var printedCharactersCount by remember { mutableStateOf(0) }
    
    val fullStatementText = "أهلاً بكم في مرشد الشهيد د. زيد طاؤوس الذكي!\n\nيسرنا ويسعدنا تمكينكم من التفاعل مع هذا النظام الاستشاري الذكي المصمم بعناية فائقة، ليجيبكم عن كافة الأسئلة والاستفسارات التنظيمية والأكاديمية والسريرية واللوائحية المتعلقة بكلية الطب والعلوم الصحية وبرامجها المتنوعة كالمعهد الطبي العسكري، ليكون رفيقاً مخلصاً للجميع في مسيرتهم التعليمية والجهادية المباركة."
    
    // Typewriter effect trigger
    LaunchedEffect(Unit) {
        printedCharactersCount = 0
        while (printedCharactersCount < fullStatementText.length) {
            delay(12) // Typing speed milliseconds
            printedCharactersCount++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(12.dp))

            // Terminal Emblem Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(BorderStroke(1.dp, GoldenBrass.copy(alpha = 0.5f)), CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GoldenBrass.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = "AI Agent",
                        tint = GoldenBrass,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "الترحيب والتعريف بالاستشاري الذكي",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GoldenBrass,
                fontFamily = CairoFont,
                textAlign = TextAlign.Center
            )

            Text(
                text = "أهلاً بكم في مرشد الشهيد د. زيد طاؤوس الذكي",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = CairoFont,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Glowing AI Dialogue Chat Frame
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.45f)),
                border = BorderStroke(1.5.dp, TacticalGreen.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Chat header mimicking command shell terminal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(CoralRed, CircleShape))
                            Box(modifier = Modifier.size(8.dp).background(GoldenBrass, CircleShape))
                            Box(modifier = Modifier.size(8.dp).background(TacticalGreen, CircleShape))
                        }
                        
                        Text(
                            text = "SMART_ADVISOR_TERMINAL_V2.0",
                            fontSize = 8.sp,
                            color = Slate400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Typewriter output
                    Text(
                        text = fullStatementText.take(printedCharactersCount),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontFamily = CairoFont,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp)
                    )

                    // Terminal Footer prompt blinking cursor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓ النظام اللغوي في أتم الاستعداد والجاهزية",
                            fontSize = 9.sp,
                            color = TacticalGreen,
                            fontFamily = CairoFont,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Blinking text cursor symbol
                        val cursorTransition = rememberInfiniteTransition(label = "cursor")
                        val cursorAlpha by cursorTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "cursorAlpha"
                        )
                        Text(
                            text = "▋",
                            color = TacticalGreen,
                            fontSize = 10.sp,
                            modifier = Modifier.alpha(cursorAlpha)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate400.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = "عودة للخلف",
                    fontSize = 12.sp,
                    fontFamily = CairoFont
                )
            }

            Button(
                onClick = onNext,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalGreen,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .weight(2.2f)
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "الدخول البوابي للتطبيق 🛡️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFont
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 4: PORTAL GATE & DEVELOPER TRIBUTE DOUBLE-DOOR OPENING
// -------------------------------------------------------------
@Composable
fun DeveloperDoorRevealView(
    onFinished: () -> Unit,
    onBack: () -> Unit
) {
    var doorsOpened by remember { mutableStateOf(false) }
    
    // Slide transition for open/close animation of dual gates
    val doorOffsetFraction by animateFloatAsState(
        targetValue = if (doorsOpened) 1.0f else 0.0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "doorOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "البوابات الدفاعية والمعرفية للمطور",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoldenBrass,
                fontFamily = CairoFont
            )

            Text(
                text = "انقر على زر فتح الأبواب أدناه للكشف والرمز الفني للمطور.",
                fontSize = 11.sp,
                color = Slate300,
                fontFamily = CairoFont,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // DUAL-DOOR SLIDING CONTAINER WITH THE REVEAL CONTENT INSIDE
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, GoldenBrass.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .background(Color(0xFF0A0F1D))
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                val maxWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { maxWidth.toPx() }

                // INSIDE THE GATES: REVEALED DEVELOPER CERTIFICATION
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .scale(1.05f)
                            .blur(if (!doorsOpened) 12.dp else 0.dp)
                    ) {
                        Surface(
                            color = Color(0xFF132B4F).copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, GoldenBrass.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Quality Seal",
                                    tint = GoldenBrass,
                                    modifier = Modifier.size(54.dp)
                                )
                                
                                Text(
                                    text = "براءة التصميم والتوليف الهندسي",
                                    color = GoldenBrass,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFont,
                                    textAlign = TextAlign.Center
                                )

                                HorizontalDivider(
                                    color = GoldenBrass.copy(alpha = 0.25f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Text(
                                    text = "أُعد هذا التطبيق ليلبي طلباتكم بالتعرف عن برامج كلية الطب والعلوم الصحية وأهدافها ورسالتها وذلك من قبل المطور والمهندس إدريس المداني.",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFont,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Status",
                                        tint = TacticalGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "المصمم والمطور المعتمد • م. إدريس المداني",
                                        color = TacticalGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFont
                                    )
                                }
                            }
                        }
                    }
                }

                // THE SLIDING GATES (Renders on top and slides out!)
                // Left Door Gate
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterStart)
                        .offset {
                            IntOffset(
                                x = -((maxWidthPx / 2) * doorOffsetFraction).toInt(),
                                y = 0
                            )
                        }
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1B2B47),
                                    Color(0xFF0F1829)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (doorsOpened) Color.Transparent else GoldenBrass.copy(alpha = 0.3f)
                            )
                        ),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    // Artistic Gate Patterns
                    Column(
                        modifier = Modifier.padding(end = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Door Handle L",
                            tint = GoldenBrass.copy(alpha = 0.6f),
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "بوابة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldenBrass.copy(alpha = 0.5f),
                            fontFamily = CairoFont
                        )
                    }
                }

                // Right Door Gate
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterEnd)
                        .offset {
                            IntOffset(
                                x = ((maxWidthPx / 2) * doorOffsetFraction).toInt(),
                                y = 0
                            )
                        }
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F1829),
                                    Color(0xFF1B2B47)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (doorsOpened) Color.Transparent else GoldenBrass.copy(alpha = 0.3f)
                            )
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Artistic Gate Patterns
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Door Handle R",
                            tint = GoldenBrass.copy(alpha = 0.6f),
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "العبور",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldenBrass.copy(alpha = 0.5f),
                            fontFamily = CairoFont
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Open doors trigger
            Button(
                onClick = { doorsOpened = !doorsOpened },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (doorsOpened) CoralRed.copy(alpha = 0.2f) else GoldenBrass.copy(alpha = 0.25f),
                    contentColor = if (doorsOpened) CoralRed else GoldenBrass
                ),
                border = BorderStroke(1.dp, if (doorsOpened) CoralRed else GoldenBrass),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (doorsOpened) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Door Action"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (doorsOpened) "إغلاق الأبواب لتأمين لوحة التعريف" else "افتح الأبواب لدخول بوابة التكريم 🚪",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFont
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Navigation and finished buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate400.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = "عودة للخلف",
                    fontSize = 12.sp,
                    fontFamily = CairoFont
                )
            }

            Button(
                onClick = onFinished,
                enabled = doorsOpened, // Must unlock/view developer tribute first to launch
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalGreen,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .weight(2.2f)
                    .height(48.dp)
                    .border(
                        BorderStroke(
                            1.dp,
                            if (doorsOpened) TacticalGreen else Color.White.copy(alpha = 0.1f)
                        ), RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (doorsOpened) "ابتدئ الرحلة التعليمية الآن ✓" else "يرجى فتح الأبواب أعلاه أولاً 🔒",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFont
                    )
                }
            }
        }
    }
}
