package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.CompositionLocalProvider
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.CollegeData
import com.example.data.LocalAppSettings
import com.example.data.ProgramsData
import com.example.model.Document
import com.example.service.AuthService
import com.example.service.SearchService
import com.example.service.SpeechManager
import com.example.service.LocalSpeechManager
import com.example.ui.AdminScreen
import com.example.ui.HomeScreen
import com.example.ui.LoginScreen
import com.example.ui.SettingsScreen
import com.example.ui.WelcomeCinematicScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var authService: AuthService
    private lateinit var searchService: SearchService
    private lateinit var speechManager: SpeechManager
    private lateinit var appSettings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Services
        authService = AuthService(applicationContext)
        searchService = SearchService(applicationContext)
        speechManager = SpeechManager(applicationContext)
        appSettings = AppSettings(applicationContext)

        // Asynchronously pre-populate the Room database with college curriculum guidelines
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            // If documents database is empty, seed everything!
            val currentDocs = db.documentDao().searchDocuments("%%")
            if (currentDocs.isEmpty()) {
                // Populate documents
                CollegeData.sections.forEach { section ->
                    db.documentDao().insertDocument(
                        Document(
                            title = section["title"] ?: "",
                            content = section["content"] ?: ""
                        )
                    )
                }
                // Populate programs
                ProgramsData.getAllPrograms().forEach { program ->
                    db.programDao().insertProgram(ProgramsData.toEntity(program))
                }
            }
        }

        enableEdgeToEdge()

        setContent {
            CompositionLocalProvider(
                LocalSpeechManager provides speechManager,
                LocalAppSettings provides appSettings
            ) {
                val themeIndex by appSettings.themeIndex.collectAsState(initial = 0)
                val speechEnabled by appSettings.speechEnabled.collectAsState(initial = true)

                LaunchedEffect(speechEnabled) {
                    speechManager.isEnabled = speechEnabled
                }

                MyApplicationTheme(themeIndex = themeIndex) {
                    val isLoggedIn by authService.isLoggedIn.collectAsState()
                    var adminActive by remember { mutableStateOf(false) }
                    var showWelcomeSequence by remember { mutableStateOf(true) }
                    var settingsActive by remember { mutableStateOf(false) }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        if (settingsActive) {
                            SettingsScreen(
                                settings = appSettings,
                                speechManager = speechManager,
                                onBack = { settingsActive = false },
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else {
                            when {
                                showWelcomeSequence -> {
                                    WelcomeCinematicScreen(
                                        onFinished = {
                                            showWelcomeSequence = false
                                            speechManager.speak("مرحباً بكم في مرشد الشهيد زيد طاووس الذكي للبرامج الطبية والعسكرية")
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                !isLoggedIn -> {
                                    LoginScreen(
                                        authService = authService,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                adminActive -> {
                                    AdminScreen(
                                        onBack = {
                                            adminActive = false
                                            speechManager.speak("العودة إلى لوحة المرشد الرئيسية")
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                else -> {
                                    HomeScreen(
                                        authService = authService,
                                        searchService = searchService,
                                        onAdminClick = {
                                            adminActive = true
                                            speechManager.speak("تم فتح لوحة تحرير وتحديث البيانات والمقررات")
                                        },
                                        onSettingsClick = {
                                            settingsActive = true
                                            speechManager.speak("تم فتح معمل تفضيلات النظام والإعدادات اللغوية")
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        speechManager.shutdown()
        super.onDestroy()
    }
}
