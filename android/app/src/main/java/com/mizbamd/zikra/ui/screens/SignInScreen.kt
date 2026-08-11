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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.BuildConfig
import com.mizbamd.zikra.R
import com.mizbamd.zikra.ui.components.GoldButton
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.Forest
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.ui.theme.OnGreen
import com.mizbamd.zikra.ui.theme.OnGreenTextStyle
import com.mizbamd.zikra.ui.theme.zikraOnGreenFieldColors

@Composable
fun SignInScreen(
    busy: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onGoogle: () -> Unit,
    onClearError: () -> Unit,
    onBack: () -> Unit,
    onGuest: () -> Unit,
) {
    var createAccount by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
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
    val googleReady = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = Gold,
        selectedLabelColor = Forest,
        containerColor = Cream.copy(alpha = 0.12f),
        labelColor = Cream,
    )
    val colors = zikraOnGreenFieldColors()

    fun clearErrors() {
        localError = null
        onClearError()
    }

    fun submit() {
        keyboard?.hide()
        when {
            !email.contains("@") -> localError = invalidEmail
            password.length < 8 -> localError = passwordTooShort
            createAccount && password != confirm -> localError = passwordsDontMatch
            createAccount -> onRegister(email.trim(), password)
            else -> onLogin(email.trim(), password)
        }
    }

    val shownError = localError ?: error
    val transformation =
        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()

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
            stringResource(if (createAccount) R.string.create_account else R.string.sign_in),
            color = Cream,
            fontSize = 28.sp,
        )
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            FilterChip(
                selected = !createAccount,
                onClick = {
                    createAccount = false
                    clearErrors()
                },
                label = { Text(stringResource(R.string.sign_in)) },
                colors = chipColors,
                modifier = Modifier.padding(end = 8.dp),
            )
            FilterChip(
                selected = createAccount,
                onClick = {
                    createAccount = true
                    clearErrors()
                },
                label = { Text(stringResource(R.string.create_account)) },
                colors = chipColors,
            )
        }
        Spacer(Modifier.height(24.dp))
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
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                clearErrors()
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
                    clearErrors()
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
        if (!shownError.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(shownError, color = GoldLight, fontSize = 14.sp)
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
            enabled = !busy,
        )
        if (googleReady) {
            Spacer(Modifier.height(8.dp))
            QuietTextButton(stringResource(R.string.google_sign_in), onClick = onGoogle)
        }
        Spacer(Modifier.height(16.dp))
        QuietTextButton(stringResource(R.string.continue_guest), onClick = onGuest)
        QuietTextButton(stringResource(R.string.back), onClick = onBack)
    }
}
