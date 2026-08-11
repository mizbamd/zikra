package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            DateHeader(dates)
            Spacer(Modifier.height(16.dp))
            if (doneFrameId != null) {
                Text(
                    stringResource(R.string.done_quiet),
                    color = GoldLight,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
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
}
