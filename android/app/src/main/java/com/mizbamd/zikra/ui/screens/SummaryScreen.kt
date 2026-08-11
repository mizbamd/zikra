package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.ui.components.FrameCard
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.Forest
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.util.DhikrLexicon
import java.time.LocalDate

@Composable
fun SummaryScreen(
    frames: List<FrameToday>,
    onCount: (String) -> Unit,
    onFocus: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    val dua = remember { DhikrLexicon.duaForDay(LocalDate.now().dayOfYear) }
    val plaque = RoundedCornerShape(20.dp)

    Scaffold(containerColor = Forest, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.summary),
                color = Cream,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Cream.copy(alpha = 0.06f), plaque)
                    .border(1.5.dp, Gold.copy(alpha = 0.9f), plaque)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    dua.arabic,
                    color = Cream,
                    fontSize = 22.sp,
                    lineHeight = 30.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    dua.latin,
                    color = GoldLight.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
            if (frames.isEmpty()) {
                Text(
                    stringResource(R.string.summary_empty),
                    color = GoldLight,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            } else {
                frames.forEach { item ->
                    FrameCard(
                        item = item,
                        onCount = { onCount(item.frame.id) },
                        onArabic = { onFocus(item.frame.id) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.summary_thanks),
                color = Cream.copy(alpha = 0.55f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(R.string.summary_thanks_signoff),
                color = Gold.copy(alpha = 0.72f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
    }
}
