package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.local.FrameEntity
import com.mizbamd.zikra.ui.components.GoldButton
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.ForestDark
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.ui.theme.OnGreen
import com.mizbamd.zikra.ui.theme.OnGreenTextStyle
import com.mizbamd.zikra.ui.theme.zikraOnGreenFieldColors
import com.mizbamd.zikra.util.DhikrLexicon
import com.mizbamd.zikra.util.DhikrPair

@Composable
fun EditFrameScreen(
    existing: FrameEntity?,
    atLimit: Boolean = false,
    maxFrames: Int = 10,
    onSave: (arabic: String, transliteration: String, target: Int?) -> Unit,
    onDelete: (() -> Unit)?,
    onBack: () -> Unit,
) {
    var arabic by remember { mutableStateOf(existing?.arabic.orEmpty()) }
    var transliteration by remember { mutableStateOf(existing?.transliteration.orEmpty()) }
    var target by remember { mutableStateOf(existing?.target?.toString().orEmpty()) }
    var latinSuggesting by remember { mutableStateOf(false) }
    var arabicSuggesting by remember { mutableStateOf(false) }

    LaunchedEffect(existing?.id) {
        arabic = existing?.arabic.orEmpty()
        transliteration = existing?.transliteration.orEmpty()
        target = existing?.target?.toString().orEmpty()
        latinSuggesting = false
        arabicSuggesting = false
    }

    val latinHits = remember(transliteration, latinSuggesting) {
        if (latinSuggesting) DhikrLexicon.searchLatin(transliteration) else emptyList()
    }
    val arabicHits = remember(arabic, arabicSuggesting) {
        if (arabicSuggesting) DhikrLexicon.searchArabic(arabic) else emptyList()
    }

    fun pick(pair: DhikrPair) {
        arabic = pair.arabic
        transliteration = pair.latin
        latinSuggesting = false
        arabicSuggesting = false
    }

    val trimmedArabic = arabic.trim()
    val trimmedLatin = transliteration.trim()
    val currentExisting = existing
    val unchangedExisting = currentExisting != null &&
        trimmedArabic == currentExisting.arabic.trim() &&
        trimmedLatin == currentExisting.transliteration.trim()
    val catalogPair = remember(trimmedArabic, trimmedLatin) {
        DhikrLexicon.matchPair(trimmedArabic, trimmedLatin)
    }
    val canSave = unchangedExisting || catalogPair != null

    val colors = zikraOnGreenFieldColors()

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Cream)
        }
        Text(
            stringResource(if (existing == null) R.string.add_frame else R.string.edit_frame),
            color = Cream,
            fontSize = 24.sp,
        )
        if (existing == null && atLimit) {
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.frame_limit_reached, maxFrames),
                color = GoldLight,
                fontSize = 16.sp,
            )
            return@Column
        }
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = arabic,
            onValueChange = { value ->
                arabic = value
                arabicSuggesting = true
                latinSuggesting = false
            },
            label = { Text(stringResource(R.string.arabic), color = OnGreen) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            textStyle = OnGreenTextStyle,
            colors = colors,
        )
        if (arabicHits.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            DhikrSuggestionList(items = arabicHits, onPick = ::pick)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = transliteration,
            onValueChange = { value ->
                transliteration = value
                latinSuggesting = true
                arabicSuggesting = false
            },
            label = { Text(stringResource(R.string.transliteration), color = OnGreen) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            textStyle = OnGreenTextStyle,
            colors = colors,
        )
        if (latinHits.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            DhikrSuggestionList(items = latinHits, onPick = ::pick)
        }
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
        if (!canSave) {
            Text(
                stringResource(R.string.choose_dhikr_from_list),
                color = GoldLight,
                fontSize = 15.sp,
            )
            Spacer(Modifier.height(12.dp))
        }
        GoldButton(stringResource(R.string.save), onClick = {
            val preserved = currentExisting.takeIf { unchangedExisting }
            when {
                preserved != null ->
                    onSave(preserved.arabic, preserved.transliteration, target.toIntOrNull())
                catalogPair != null ->
                    onSave(catalogPair.arabic, catalogPair.latin, target.toIntOrNull())
            }
        }, enabled = canSave)
        if (onDelete != null) {
            QuietTextButton(stringResource(R.string.delete), onClick = onDelete)
        }
    }
}

@Composable
private fun DhikrSuggestionList(
    items: List<DhikrPair>,
    onPick: (DhikrPair) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ForestDark)
            .verticalScroll(rememberScrollState()),
    ) {
        items.forEachIndexed { index, pair ->
            if (index > 0) {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Gold.copy(alpha = 0.18f)),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPick(pair) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    pair.arabic,
                    color = Cream,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    pair.latin,
                    color = GoldLight,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
