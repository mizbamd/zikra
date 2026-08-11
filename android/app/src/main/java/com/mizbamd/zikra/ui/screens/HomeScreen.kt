package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.ui.components.DateHeader
import com.mizbamd.zikra.ui.components.FrameCard
import com.mizbamd.zikra.ui.theme.Forest
import com.mizbamd.zikra.ui.theme.ForestDark
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.util.DisplayDates
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    dates: DisplayDates,
    frames: List<FrameToday>,
    doneFrameId: String?,
    onCount: (String) -> Unit,
    onFocus: (String) -> Unit,
    onAdd: () -> Unit,
    onClearDone: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    LaunchedEffect(doneFrameId) {
        if (doneFrameId != null) {
            delay(1600)
            onClearDone()
        }
    }
    Scaffold(
        containerColor = Forest,
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = Gold, contentColor = ForestDark) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_frame))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "date-header") {
                DateHeader(dates)
            }
            if (doneFrameId != null) {
                item(key = "done-quiet") {
                    Text(
                        stringResource(R.string.done_quiet),
                        color = GoldLight,
                        fontSize = 16.sp,
                    )
                }
            }
            items(frames, key = { it.frame.id }) { item ->
                FrameCard(
                    item = item,
                    onCount = { onCount(item.frame.id) },
                    onArabic = { onFocus(item.frame.id) },
                )
            }
        }
    }
}
