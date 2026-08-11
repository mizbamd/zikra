package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.ui.components.GoldButton
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.GoldLight

@Composable
fun WelcomeScreen(
    onGuest: () -> Unit,
    onSignIn: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.wordmark_ar),
            color = GoldLight,
            fontSize = 56.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.app_name),
            color = Cream,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.tagline), color = Cream.copy(alpha = 0.75f), fontSize = 16.sp)
        Spacer(Modifier.height(48.dp))
        GoldButton(stringResource(R.string.continue_guest), onClick = onGuest)
        Spacer(Modifier.height(8.dp))
        QuietTextButton(stringResource(R.string.sign_in), onClick = onSignIn)
    }
}
