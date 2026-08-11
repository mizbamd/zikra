package com.mizbamd.zikra.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.local.DailyCountEntity
import com.mizbamd.zikra.data.local.HistoryExport
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.CardWhite
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.Forest
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.ui.theme.Ink
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    frames: List<FrameToday>,
    history: List<DailyCountEntity>,
    bottomBar: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val names = frames.associate { it.frame.id to it.frame.transliteration }
    val visible = history.filter { it.count > 0 }
    val byDate = visible.groupBy { it.date }.toSortedMap(compareByDescending { it })
    val locale = Locale.getDefault()
    val monthFmt = remember(locale) { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    val dayFmt = remember(locale) { DateTimeFormatter.ofPattern("EEEE, d MMMM", locale) }
    val csv = remember(visible, names) { HistoryExport.toCsv(visible, names) }

    Scaffold(containerColor = Forest, bottomBar = bottomBar) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.history),
                    color = Cream,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f),
                )
                if (visible.isNotEmpty()) {
                    QuietTextButton(stringResource(R.string.export_history)) {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, csv)
                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.history_export_subject))
                        }
                        context.startActivity(
                            Intent.createChooser(send, context.getString(R.string.export_history)),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (visible.isEmpty()) {
                Text(stringResource(R.string.history_empty), color = GoldLight, fontSize = 16.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    var lastMonth = ""
                    byDate.forEach { (date, rows) ->
                        val parsed = runCatching { LocalDate.parse(date) }.getOrNull()
                        val monthLabel = parsed?.format(monthFmt) ?: date.take(7)
                        if (monthLabel != lastMonth) {
                            lastMonth = monthLabel
                            item(key = "month-$monthLabel") {
                                Text(
                                    monthLabel,
                                    color = Cream,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                                )
                            }
                        }
                        val dayLabel = parsed?.format(dayFmt) ?: date
                        val dayTotal = rows.sumOf { it.count }
                        item(key = "day-$date") {
                            DaySection(
                                label = dayLabel,
                                total = dayTotal,
                                rows = rows,
                                names = names,
                            )
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DaySection(
    label: String,
    total: Int,
    rows: List<DailyCountEntity>,
    names: Map<String, String>,
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = Gold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("$total", color = GoldLight, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        rows.forEach { row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardWhite)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    names[row.frameId] ?: row.frameId,
                    color = Ink,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                Text("${row.count}", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
