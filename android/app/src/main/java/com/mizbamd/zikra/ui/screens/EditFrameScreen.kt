package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.local.FrameEntity
import com.mizbamd.zikra.ui.components.GoldButton
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.OnGreen
import com.mizbamd.zikra.ui.theme.OnGreenTextStyle
import com.mizbamd.zikra.ui.theme.zikraOnGreenFieldColors
import com.mizbamd.zikra.util.DhikrLexicon

@Composable
fun EditFrameScreen(
    existing: FrameEntity?,
    onSave: (arabic: String, transliteration: String, target: Int?) -> Unit,
    onDelete: (() -> Unit)?,
    onBack: () -> Unit,
) {
    var arabic by remember { mutableStateOf(existing?.arabic.orEmpty()) }
    var transliteration by remember { mutableStateOf(existing?.transliteration.orEmpty()) }
    var target by remember { mutableStateOf(existing?.target?.toString().orEmpty()) }
    var arabicTouched by remember { mutableStateOf(false) }
    var latinTouched by remember { mutableStateOf(false) }
    var autoArabic by remember { mutableStateOf("") }
    var autoLatin by remember { mutableStateOf("") }

    LaunchedEffect(existing?.id) {
        arabic = existing?.arabic.orEmpty()
        transliteration = existing?.transliteration.orEmpty()
        target = existing?.target?.toString().orEmpty()
        arabicTouched = existing != null
        latinTouched = existing != null
        autoArabic = ""
        autoLatin = ""
    }

    val colors = zikraOnGreenFieldColors()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Cream)
        }
        Text(
            stringResource(if (existing == null) R.string.add_frame else R.string.edit_frame),
            color = Cream,
            fontSize = 24.sp,
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = arabic,
            onValueChange = { value ->
                arabic = value
                arabicTouched = true
                val match = DhikrLexicon.fromArabic(value)
                if (match != null && (!latinTouched || transliteration.isBlank() || transliteration == autoLatin)) {
                    transliteration = match.latin
                    autoLatin = match.latin
                    latinTouched = false
                }
            },
            label = { Text(stringResource(R.string.arabic), color = OnGreen) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            textStyle = OnGreenTextStyle,
            colors = colors,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = transliteration,
            onValueChange = { value ->
                transliteration = value
                latinTouched = true
                val match = DhikrLexicon.fromLatin(value)
                if (match != null && (!arabicTouched || arabic.isBlank() || arabic == autoArabic)) {
                    arabic = match.arabic
                    autoArabic = match.arabic
                    arabicTouched = false
                }
            },
            label = { Text(stringResource(R.string.transliteration), color = OnGreen) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            textStyle = OnGreenTextStyle,
            colors = colors,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = target,
            onValueChange = { target = it.filter { ch -> ch.isDigit() } },
            label = { Text(stringResource(R.string.target_optional), color = OnGreen) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            textStyle = OnGreenTextStyle,
            colors = colors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Spacer(Modifier.height(28.dp))
        GoldButton(stringResource(R.string.save), onClick = {
            onSave(arabic.trim(), transliteration.trim(), target.toIntOrNull())
        }, enabled = arabic.isNotBlank() && transliteration.isNotBlank())
        if (onDelete != null) {
            QuietTextButton(stringResource(R.string.delete), onClick = onDelete)
        }
    }
}
