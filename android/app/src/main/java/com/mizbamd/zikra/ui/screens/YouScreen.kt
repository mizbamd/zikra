package com.mizbamd.zikra.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.local.ResetAt
import com.mizbamd.zikra.data.local.SessionMode
import com.mizbamd.zikra.data.local.Settings
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.Forest
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight

/**
 * Approximate (COARSE) location only. Sunset / Hijri math is insensitive to ~1–3 km error,
 * so ACCESS_FINE_LOCATION / GPS is not requested (easier Play review).
 */
@SuppressLint("MissingPermission")
@Composable
fun LocationPermissionEffect(
    enabled: Boolean,
    onCoordinates: (Double, Double, Boolean) -> Unit,
    onSampleLocation: () -> Unit,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val loc = readApproxLocation(context.getSystemService(LocationManager::class.java))
            if (loc != null) onCoordinates(loc.latitude, loc.longitude, true)
            else onSampleLocation()
        } else {
            onSampleLocation()
        }
    }

    LaunchedEffect(enabled) {
        if (!enabled) {
            onSampleLocation()
            return@LaunchedEffect
        }
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (coarse == PackageManager.PERMISSION_GRANTED) {
            val loc = readApproxLocation(context.getSystemService(LocationManager::class.java))
            if (loc != null) onCoordinates(loc.latitude, loc.longitude, true)
            else onSampleLocation()
        } else {
            launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
}

@SuppressLint("MissingPermission")
private fun readApproxLocation(lm: LocationManager?): android.location.Location? {
    if (lm == null) return null
    val network = runCatching { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
    val passive = runCatching { lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) }.getOrNull()
    return listOfNotNull(network, passive).maxByOrNull { it.time }
}

@Composable
fun YouScreen(
    settings: Settings,
    authBusy: Boolean = false,
    authError: String? = null,
    onHaptics: (Boolean) -> Unit,
    onVolumeUp: (Boolean) -> Unit,
    onResetAt: (ResetAt) -> Unit,
    onLanguage: (String) -> Unit,
    onLocationEnabled: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit = {},
    onSignIn: () -> Unit,
    onBack: (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = Gold,
        selectedLabelColor = Forest,
        containerColor = Cream.copy(alpha = 0.12f),
        labelColor = Cream,
    )

    Scaffold(containerColor = Forest, bottomBar = bottomBar) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Cream)
                }
            }
            Text(stringResource(R.string.you), color = Cream, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                if (settings.mode == SessionMode.SIGNED_IN) settings.email
                else stringResource(R.string.guest_label),
                color = GoldLight,
                fontSize = 16.sp,
            )
            Spacer(Modifier.height(24.dp))

            SettingRow(stringResource(R.string.haptics), settings.haptics, onHaptics)
            SettingRow(stringResource(R.string.volume_up), settings.volumeUpIncrement, onVolumeUp)
            SettingRow(stringResource(R.string.location), settings.locationEnabled, onLocationEnabled)

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.reset_at), color = Cream.copy(alpha = 0.8f), fontSize = 14.sp)
            Row {
                FilterChip(
                    selected = settings.resetAt == ResetAt.MIDNIGHT,
                    onClick = { onResetAt(ResetAt.MIDNIGHT) },
                    label = { Text(stringResource(R.string.midnight)) },
                    colors = chipColors,
                    modifier = Modifier.padding(end = 8.dp),
                )
                FilterChip(
                    selected = settings.resetAt == ResetAt.FAJR,
                    onClick = { onResetAt(ResetAt.FAJR) },
                    label = { Text(stringResource(R.string.fajr)) },
                    colors = chipColors,
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.language_label), color = Cream.copy(alpha = 0.8f), fontSize = 14.sp)
            Row {
                FilterChip(
                    selected = settings.language != "ar",
                    onClick = { onLanguage("en") },
                    label = { Text(stringResource(R.string.english)) },
                    colors = chipColors,
                    modifier = Modifier.padding(end = 8.dp),
                )
                FilterChip(
                    selected = settings.language == "ar",
                    onClick = { onLanguage("ar") },
                    label = { Text(stringResource(R.string.arabic_lang)) },
                    colors = chipColors,
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.calendar_method), color = Cream.copy(alpha = 0.8f), fontSize = 14.sp)
            FilterChip(
                selected = true,
                onClick = {},
                label = { Text(stringResource(R.string.umm_al_qura)) },
                colors = chipColors,
            )

            Spacer(Modifier.height(24.dp))
            if (settings.mode == SessionMode.SIGNED_IN) {
                QuietTextButton(stringResource(R.string.sign_out), onClick = onSignOut)
                QuietTextButton(
                    stringResource(R.string.delete_account),
                    onClick = { confirmDelete = true },
                )
            } else {
                QuietTextButton(stringResource(R.string.sign_in), onClick = onSignIn)
            }
        }
    }

    if (confirmDelete && settings.mode == SessionMode.SIGNED_IN) {
        AlertDialog(
            onDismissRequest = { if (!authBusy) confirmDelete = false },
            title = { Text(stringResource(R.string.delete_account_confirm_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.delete_account_confirm_body))
                    if (!authError.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(authError, color = GoldLight, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDeleteAccount, enabled = !authBusy) {
                    Text(
                        stringResource(
                            if (authBusy) R.string.deleting_account else R.string.delete_account_confirm,
                        ),
                        color = Color(0xFFB54A4A),
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }, enabled = !authBusy) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Cream, modifier = Modifier.weight(1f), fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Forest,
                checkedTrackColor = Gold,
                uncheckedTrackColor = Cream.copy(alpha = 0.25f),
            ),
        )
    }
}
