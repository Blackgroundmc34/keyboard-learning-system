package com.privatecloset.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.privatecloset.app.ui.navigation.PrivateClosetApp
import com.privatecloset.app.ui.theme.BgBlack
import com.privatecloset.app.ui.theme.PrivateClosetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as PrivateClosetApplication).container
        setContent {
            PrivateClosetTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BgBlack) {
                    PrivateClosetApp(repository = container.learningRepository)
                }
            }
        }
    }
}
