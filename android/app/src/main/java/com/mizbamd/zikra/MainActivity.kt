package com.mizbamd.zikra

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mizbamd.zikra.ui.nav.ZikraNav
import com.mizbamd.zikra.ui.theme.Forest
import com.mizbamd.zikra.ui.theme.ZikraTheme
import com.mizbamd.zikra.util.VolumeUpBus

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            ZikraTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Forest) {
                    ZikraNav()
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP &&
            event.action == KeyEvent.ACTION_DOWN &&
            VolumeUpBus.shouldHandle
        ) {
            VolumeUpBus.emit()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}
