package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.BuildConfig
import com.mizbamd.zikra.R
import com.mizbamd.zikra.ui.components.GoldButton
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.Forest
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.OnGreen
import com.mizbamd.zikra.ui.theme.OnGreenTextStyle
import com.mizbamd.zikra.ui.theme.zikraOnGreenFieldColors
import com.mizbamd.zikra.ui.theme.zikraSafeDrawing

@Composable
fun SignInScreen(
    busy: Boolean,
    error: String?,
    otpSentTo: String?,
    passwordFallback: Boolean,
    onRequestOtp: (String) -> Unit,
    onVerifyOtp: (String, String) -> Unit,
    onClearOtpSent: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
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
    var latchedPasswordForm by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(passwordFallback, error) {
        if (passwordFallback || looksLikePasswordFallback(error)) {
            latchedPasswordForm = true
        }
    }

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
        clearErrors()
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
            .zikraSafeDrawing()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (passwordFallback || latchedPasswordForm) {
            PasswordFallbackForm(
                busy = busy,
                email = email,
                onEmailChange = {
                    email = it
                    clearErrors()
                },
                shownError = shownError,
                googleReady = googleReady,
                onLogin = onLogin,
                onRegister = onRegister,
                onGoogle = onGoogle,
                onClearError = { clearErrors() },
                onGuest = onGuest,
                onBack = onBack,
            )
            return@Column
        }

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
            Text(shownError, color = Cream, fontSize = 16.sp)
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
            enabled = !busy && if (awaitingCode) {
                code.filter { it.isDigit() }.length == 6
            } else {
                email.contains("@")
            },
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

@Composable
private fun PasswordFallbackForm(
    busy: Boolean,
    email: String,
    onEmailChange: (String) -> Unit,
    shownError: String?,
    googleReady: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onGoogle: () -> Unit,
    onClearError: () -> Unit,
    onGuest: () -> Unit,
    onBack: () -> Unit,
) {
    var createAccount by rememberSaveable { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val invalidEmail = stringResource(R.string.invalid_email)
    val passwordTooShort = stringResource(R.string.password_too_short)
    val passwordsDontMatch = stringResource(R.string.passwords_dont_match)
    val keyboard = LocalSoftwareKeyboardController.current
    val passwordFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }
    val colors = zikraOnGreenFieldColors()
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = Gold,
        selectedLabelColor = Forest,
        containerColor = Cream.copy(alpha = 0.12f),
        labelColor = Cream,
    )
    val transformation =
        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()

    fun clearLocal() {
        localError = null
        onClearError()
    }

    fun submit() {
        keyboard?.hide()
        clearLocal()
        when {
            !email.contains("@") -> localError = invalidEmail
            password.length < 8 -> localError = passwordTooShort
            createAccount && password != confirm -> localError = passwordsDontMatch
            createAccount -> onRegister(email.trim(), password)
            else -> onLogin(email.trim(), password)
        }
    }

    val formValid = email.contains("@") && password.length >= 8
    val explanation = shownError?.takeIf { looksLikePasswordFallback(it) }
        ?: stringResource(R.string.otp_not_live_use_password)
    val errorText = (localError ?: shownError)?.takeUnless { looksLikePasswordFallback(it) }

    LaunchedEffect(Unit) {
        runCatching { passwordFocus.requestFocus() }
    }

    Text(
        stringResource(if (createAccount) R.string.create_account else R.string.sign_in),
        color = Cream,
        fontSize = 28.sp,
    )
    Spacer(Modifier.height(12.dp))
    Text(
        explanation,
        color = Cream.copy(alpha = 0.85f),
        fontSize = 15.sp,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.Center) {
        FilterChip(
            selected = !createAccount,
            onClick = {
                createAccount = false
                clearLocal()
            },
            label = { Text(stringResource(R.string.sign_in)) },
            colors = chipColors,
            modifier = Modifier.padding(end = 8.dp),
        )
        FilterChip(
            selected = createAccount,
            onClick = {
                createAccount = true
                clearLocal()
            },
            label = { Text(stringResource(R.string.create_account)) },
            colors = chipColors,
        )
    }
    Spacer(Modifier.height(24.dp))
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text(stringResource(R.string.email), color = OnGreen) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        textStyle = OnGreenTextStyle,
        colors = colors,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = password,
        onValueChange = {
            password = it
            clearLocal()
        },
        label = { Text(stringResource(R.string.password), color = OnGreen) },
        singleLine = true,
        visualTransformation = transformation,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(passwordFocus),
        shape = RoundedCornerShape(14.dp),
        textStyle = OnGreenTextStyle,
        colors = colors,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = if (createAccount) ImeAction.Next else ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onNext = { confirmFocus.requestFocus() },
            onDone = { submit() },
        ),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = painterResource(
                        if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility,
                    ),
                    contentDescription = stringResource(
                        if (passwordVisible) R.string.hide_password else R.string.show_password,
                    ),
                    tint = Cream.copy(alpha = 0.75f),
                )
            }
        },
    )
    if (createAccount) {
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = {
                confirm = it
                clearLocal()
            },
            label = { Text(stringResource(R.string.confirm_password), color = OnGreen) },
            singleLine = true,
            visualTransformation = transformation,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(confirmFocus),
            shape = RoundedCornerShape(14.dp),
            textStyle = OnGreenTextStyle,
            colors = colors,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility,
                        ),
                        contentDescription = stringResource(
                            if (passwordVisible) R.string.hide_password else R.string.show_password,
                        ),
                        tint = Cream.copy(alpha = 0.75f),
                    )
                }
            },
        )
    }
    if (!errorText.isNullOrBlank()) {
        Spacer(Modifier.height(12.dp))
        Text(errorText, color = Cream, fontSize = 16.sp)
    }
    Spacer(Modifier.height(24.dp))
    GoldButton(
        text = stringResource(
            when {
                busy && createAccount -> R.string.creating_account
                busy -> R.string.signing_in
                createAccount -> R.string.create_account
                else -> R.string.sign_in
            },
        ),
        onClick = { submit() },
        enabled = !busy && formValid,
    )
    if (googleReady) {
        Spacer(Modifier.height(8.dp))
        QuietTextButton(stringResource(R.string.google_sign_in), onClick = onGoogle)
    }
    Spacer(Modifier.height(16.dp))
    QuietTextButton(stringResource(R.string.continue_guest), onClick = onGuest)
    QuietTextButton(stringResource(R.string.back), onClick = onBack)
}

private fun looksLikePasswordFallback(message: String?): Boolean {
    val msg = message.orEmpty()
    return msg.contains("Use your password", ignoreCase = true) ||
        msg.contains("one-time codes", ignoreCase = true)
}
