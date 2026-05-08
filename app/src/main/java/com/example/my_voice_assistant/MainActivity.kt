package com.example.my_voice_assistant

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            var spokenText by remember {
                mutableStateOf("Press button and speak")
            }

            val speechLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->

                if (result.resultCode == Activity.RESULT_OK) {

                    val data = result.data

                    val results = data?.getStringArrayListExtra(
                        android.speech.RecognizerIntent.EXTRA_RESULTS
                    )

                    val recognizedText =
                        results?.get(0) ?: "No speech detected"

                    spokenText = recognizedText

                    // OPEN ANY APP
                    if (recognizedText.lowercase().startsWith("open")) {

                        val appName = recognizedText
                            .lowercase()
                            .replace("open", "")
                            .trim()

                        spokenText = "Opening $appName"

                        openApp(appName)
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = spokenText,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(16.dp)
                    )

                    Button(
                        onClick = {

                            val intent = Intent(
                                android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                            )

                            intent.putExtra(
                                android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )

                            intent.putExtra(
                                android.speech.RecognizerIntent.EXTRA_LANGUAGE,
                                Locale.getDefault()
                            )

                            intent.putExtra(
                                android.speech.RecognizerIntent.EXTRA_PROMPT,
                                "Speak now..."
                            )

                            speechLauncher.launch(intent)
                        }
                    ) {

                        Text("Speak")
                    }
                }
            }
        }
    }

    private fun openApp(appName: String) {

        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN, null)

        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = pm.queryIntentActivities(intent, 0)

        for (app in apps) {

            val label = app.loadLabel(pm).toString()

            if (label.lowercase().contains(appName.lowercase())) {

                val packageName = app.activityInfo.packageName

                val launchIntent =
                    pm.getLaunchIntentForPackage(packageName)

                if (launchIntent != null) {

                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    applicationContext.startActivity(launchIntent)

                    return
                }
            }
        }
    }
}
