package com.mizbamd.zikra.ui.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Modifier.zikraSafeDrawing(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing)

/** Top + side insets for screens that draw their own bottom chrome (nav bar, FAB). */
@Composable
fun Modifier.zikraTopCutout(): Modifier =
    windowInsetsPadding(
        WindowInsets.statusBars
            .union(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
    )
