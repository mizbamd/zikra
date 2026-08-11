package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.BuildConfig
import com.mizbamd.zikra.R
import com.mizbamd.zikra.ui.components.GoldButton
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.ui.theme.OnGreen
import com.mizbamd.zikra.ui.theme.OnGreenTextStyle
import com.mizbamd.zikra.ui.theme.zikraOnGreenFieldColors

@Composable
fun SignInScreen(
    busy: Boolean,
    error: String?,
    otpSentTo: String?,
    onRequestOtp: (String) -> Unit,
    onVerifyOtp: (String, String) -> Unit,
    onClearOtpSent: () -> Unit,
    onGoogle: () -> Unit,
    onClearError: () -> Unit,
    onBack: () -> Unit,
    onGuest: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val invalidEmail = stringResource(R.string.invalid_email)
    val invalidCode = stringResource(R.string.invalid_code)
    val keyboard = LocalSoftwareKeyboardController.current
    val codeFocus = remember { FocusRequester() }
    val googleReady = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()
    val colors = zikraOnGreenFieldColors()
    val awaitingCode = otpSentTo != null
    val lockedEmail = otpSentTo ?: email.trim()

    LaunchedEffect(otpSentTo) {
        if (otpSentTo != null) {
            runCatching { codeFocus.requestFocus() }
        }
    }

    fun clearErrors() {
        localError = null
        onClearError()
    }

    fun sendCode() {
        keyboard?.hide()
        if (!email.contains("@")) {
            localError = invalidEmail
            return
        }
        onRequestOtp(email.trim())
    }

    fun verify() {
        keyboard?.hide()
        val digits = code.filter { it.isDigit() }
        if (digits.length != 6) {
            localError = invalidCode
            return
        }
        onVerifyOtp(lockedEmail, digits)
    }

    val shownError = localError ?: error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.sign_in),
            color = Cream,
            fontSize = 28.sp,
        )
        Spacer(Modifier.height(24.dp))
        if (!awaitingCode) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    clearErrors()
                },
                label = { Text(stringResource(R.string.email), color = OnGreen) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                textStyle = OnGreenTextStyle,
                colors = colors,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { sendCode() }),
            )
        } else {
            Text(
                stringResource(R.string.code_sent, lockedEmail),
                color = Cream.copy(alpha = 0.85f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { incoming ->
                    code = incoming.filter { it.isDigit() }.take(6)
                    clearErrors()
                },
                label = { Text(stringResource(R.string.enter_code), color = OnGreen) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(codeFocus),
                shape = RoundedCornerShape(14.dp),
                textStyle = OnGreenTextStyle.copy(fontSize = 22.sp, letterSpacing = 6.sp),
                colors = colors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { verify() }),
            )
        }
        if (!shownError.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(shownError, color = GoldLight, fontSize = 14.sp)
        }
        Spacer(Modifier.height(24.dp))
        GoldButton(
            text = stringResource(
                when {
                    busy && awaitingCode -> R.string.signing_in
                    busy -> R.string.sending_code
                    awaitingCode -> R.string.continue_with_code
                    else -> R.string.send_code
                },
            ),
            onClick = { if (awaitingCode) verify() else sendCode() },
            enabled = !busy,
        )
        if (awaitingCode) {
            Spacer(Modifier.height(8.dp))
            QuietTextButton(
                stringResource(R.string.resend_code),
                onClick = {
                    code = ""
                    clearErrors()
                    onRequestOtp(lockedEmail)
                },
                enabled = !busy,
            )
            QuietTextButton(
                stringResource(R.string.change_email),
                onClick = {
                    code = ""
                    clearErrors()
                    onClearOtpSent()
                },
                enabled = !busy,
            )
        }
        if (googleReady) {
            Spacer(Modifier.height(8.dp))
            QuietTextButton(stringResource(R.string.google_sign_in), onClick = onGoogle)
        }
        Spacer(Modifier.height(16.dp))
        QuietTextButton(stringResource(R.string.continue_guest), onClick = onGuest)
        QuietTextButton(stringResource(R.string.back), onClick = onBack)
    }
}
