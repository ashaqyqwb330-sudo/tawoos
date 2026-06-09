package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.model.Document
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.GoldenBrass
import com.example.ui.theme.CardBackground
import com.example.ui.theme.TacticalGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val presetDocuments = listOf(
        Document(
            title = "بروتوكول السيطرة على مجرى الهواء والنزيف لعام 2026",
            content = "دليل السيطرة الميدانية الطبية العسكرية الأحدث: يركز البروتوكول على التقييم الأولي السريع للمصابين باستخدام خوارزمية (ABCDE) في الخطوط الأمامية. يتم إيقاف النزيف فوراً باستخدام المرقأة العسكرية (CAT Tourniquet) والضمادات الشاشية ذات الضغط الفائق، وتأمين مجرى الهواء المصاب عبر وضعية الإفاقة أو بضع الغشاء الحلقي الدرقي عسكرياً للوقاية من الاختناق."
        ),
        Document(
            title = "الدليل التكتيكي لإسعاف المقاتل تحت النار (TCCC)",
            content = "منهجية الرعاية الطبية في إصابات المعارك (TCCC): تنقسم إلى رعاية تحت النار (Care Under Fire)، رعاية في بيئة تكتيكية (Tactical Field Care)، ورعاية أثناء الإخلاء الطبي التكتيكي (TACEVAC). يركز هذا المنهج على فرز الحالات السريعة، معالجة الصدر الضاغط المنخمص بإبرة إزالة الضغط (Needle Decompression)، وحقن مسكنات الألم ومضادات الالتهاب الميدانية بشكل فوري."
        ),
        Document(
            title = "دليل التخدير العام وإدارة الصدمة الحادة عسكرياً",
            content = "بروتوكول التخدير المتطور للجناح الجراحي الميداني: يوصى باستخدام الكيتامين كمخدر رئيسي في البيئات الصعبة نظراً لحفاظه على استقرار ضغط الدم وتوفير تسكين عميق وصحي ومستمر للجسم. تتم مراقبة المؤشرات الحيوية كنبض القلب، الأكسجين، والحرارة باستخدام الشاشات التكتيكية التي تعمل بالبطاريات المحمولة لتفادي مضاعفات التخدير كالتأق والحمى الخبيثة."
        ),
        Document(
            title = "دليل الرعاية الطبية المطولة في الميدان (PFC)",
            content = "بروتوكول الرعاية المطولة للجرحى عند تأخر الإخلاء (PFC): يهدف للحفاظ على حياة المصابين لمدة تزيد عن 24 ساعة في الجبهات النائية أو ظروف الحصار. يشمل تقديم السوائل الدافئة ومنتجات الدم الكامل، المراقبة المستمرة لمستوى البول، المضادات الحيوية واسعة المفعول لمكافحة الإنتانات، والتدوير المنظم لمواقع الجرحى تفادياً للقرح الانضغاطية عسكرياً."
        )
    )

    var indexedCount by remember { mutableIntStateOf(0) }
    var indexingInProgress by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بوابة الإدارة والفهرسة الذكية", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkNavy,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description Panel
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = "تقوم هذه البوابة بضغط وفهرسة المستندات والكتيبات الميدانية عسكرياً وإضافتها في قاعدة البيانات لزيادة معرفة مساعد الشهيد طاووس الذكي.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = "الكتيبات المتوفرة للفهرسة الحية (PDF/DOCX)",
                fontWeight = FontWeight.Bold,
                color = GoldenBrass,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )

            // Documents List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(presetDocuments) { doc ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(GoldenBrass.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = GoldenBrass,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = doc.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "حجم النص: ${doc.content.length} حرفاً - جاهز للتدقيق والإدراج",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            if (indexingInProgress) {
                CircularProgressIndicator(color = GoldenBrass, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            indexingInProgress = true
                            try {
                                presetDocuments.forEach { doc ->
                                    db.documentDao().insertDocument(doc)
                                }
                                indexedCount += presetDocuments.size
                                snackbarHostState.showSnackbar("تمت فهرسة وإدراج ${presetDocuments.size} مراجع تكتيكية بنجاح إلى قاعدة البيانات الذكية!")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("خطأ أثناء الإدراج: ${e.localizedMessage}")
                            } finally {
                                indexingInProgress = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenBrass,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "محاكاة الفهرسة لـ الكتيبات الأربعة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Text(
                text = "إجمالي المستندات المضافة حالياً في هذه الجلسة: $indexedCount",
                fontSize = 12.sp,
                color = GoldenBrass,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
