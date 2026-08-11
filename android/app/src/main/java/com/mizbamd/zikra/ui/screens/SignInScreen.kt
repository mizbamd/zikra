package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.BuildConfig
import com.mizbamd.zikra.R
import com.mizbamd.zikra.ui.components.GoldButton
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight

@Composable
fun SignInScreen(
    busy: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onGoogle: () -> Unit,
    onBack: () -> Unit,
    onGuest: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val googleMissing = BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()

    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.sign_in), color = Cream, fontSize = 28.sp)
        Spacer(Modifier.height(24.dp))
        val colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = Cream.copy(alpha = 0.4f),
            focusedTextColor = Cream,
            unfocusedTextColor = Cream,
            focusedLabelColor = GoldLight,
            unfocusedLabelColor = Cream.copy(alpha = 0.7f),
            cursorColor = Gold,
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = colors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = colors,
        )
        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(error, color = GoldLight, fontSize = 14.sp)
        }
        Spacer(Modifier.height(24.dp))
        GoldButton(
            text = stringResource(R.string.sign_in),
            onClick = { onLogin(email, password) },
            enabled = !busy && email.isNotBlank() && password.length >= 8,
        )
        Spacer(Modifier.height(8.dp))
        QuietTextButton(stringResource(R.string.create_account)) {
            onRegister(email, password)
        }
        QuietTextButton(stringResource(R.string.google_sign_in), onClick = onGoogle)
        if (googleMissing) {
            Text(
                stringResource(R.string.google_missing),
                color = Cream.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        QuietTextButton(stringResource(R.string.continue_guest), onClick = onGuest)
        QuietTextButton("Back", onClick = onBack)
    }
}
