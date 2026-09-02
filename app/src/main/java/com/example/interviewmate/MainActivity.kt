package com.example.interviewmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.interviewmate.ui.navigation.InterviewMateApp
import com.example.interviewmate.ui.theme.InterviewMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterviewMateTheme {
                InterviewMateApp(
                    container = (application as InterviewMateApplication).container
                )
            }
        }
    }
}
