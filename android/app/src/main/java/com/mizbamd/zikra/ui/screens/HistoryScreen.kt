package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.local.DailyCountEntity
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.ui.theme.CardWhite
import com.mizbamd.zikra.ui.theme.Forest
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.Ink

@Composable
fun HistoryScreen(
    frames: List<FrameToday>,
    history: List<DailyCountEntity>,
    bottomBar: @Composable () -> Unit,
) {
    val names = frames.associate { it.frame.id to it.frame.transliteration }
    val byDate = history.groupBy { it.date }.toSortedMap(compareByDescending { it })

    Scaffold(containerColor = Forest, bottomBar = bottomBar) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
        ) {
            Text(stringResource(R.string.history), color = com.mizbamd.zikra.ui.theme.Cream, fontSize = 24.sp)
            Spacer(Modifier.height(16.dp))
            LazyColumn {
                byDate.forEach { (date, rows) ->
                    item(key = date) {
                        Text(date, color = Gold, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(rows, key = { it.id }) { row ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardWhite)
                                .padding(14.dp),
                        ) {
                            Text(names[row.frameId] ?: row.frameId, color = Ink, fontSize = 16.sp)
                            Text("${row.count}", color = Gold, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
