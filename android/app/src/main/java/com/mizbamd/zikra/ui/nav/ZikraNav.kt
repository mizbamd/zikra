package com.mizbamd.zikra.ui.nav

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mizbamd.zikra.BuildConfig
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.local.FrameEntity
import com.mizbamd.zikra.data.local.SessionMode
import com.mizbamd.zikra.ui.ZikraViewModel
import com.mizbamd.zikra.ui.screens.EditFrameScreen
import com.mizbamd.zikra.ui.screens.FocusedScreen
import com.mizbamd.zikra.ui.screens.GuestScreen
import com.mizbamd.zikra.ui.screens.HistoryScreen
import com.mizbamd.zikra.ui.screens.HomeScreen
import com.mizbamd.zikra.ui.screens.LocationPermissionEffect
import com.mizbamd.zikra.ui.screens.SignInScreen
import com.mizbamd.zikra.ui.screens.WelcomeScreen
import com.mizbamd.zikra.ui.screens.YouScreen
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.ForestDark
import com.mizbamd.zikra.ui.theme.Gold
import org.koin.androidx.compose.koinViewModel

@Composable
fun ZikraNav(vm: ZikraViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(state.settings.mode) {
        val target = when (state.settings.mode) {
            SessionMode.WELCOME -> "welcome"
            SessionMode.GUEST -> "guest"
            SessionMode.SIGNED_IN -> "home"
        }
        val current = nav.currentDestination?.route
        if (current != target && current != "signin") {
            nav.navigate(target) {
                popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        }
        if (state.settings.mode == SessionMode.SIGNED_IN && current == "signin") {
            nav.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    if (state.settings.mode != SessionMode.WELCOME) {
        LocationPermissionEffect(
            enabled = state.settings.locationEnabled,
            onCoordinates = vm::setCoordinates,
            onSampleLocation = vm::useSampleLocation,
        )
    }

    val barColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Gold,
        selectedTextColor = Gold,
        unselectedIconColor = Cream.copy(alpha = 0.7f),
        unselectedTextColor = Cream.copy(alpha = 0.7f),
        indicatorColor = Cream.copy(alpha = 0.12f),
    )

    @Composable
    fun Bottom(selected: String) {
        NavigationBar(containerColor = ForestDark, tonalElevation = 0.dp) {
            NavigationBarItem(
                selected = selected == "home",
                onClick = {
                    nav.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                label = { Text(stringResource(R.string.home)) },
                colors = barColors,
            )
            NavigationBarItem(
                selected = selected == "history",
                onClick = { nav.navigate("history") { launchSingleTop = true } },
                icon = { Icon(Icons.Outlined.History, contentDescription = null) },
                label = { Text(stringResource(R.string.history)) },
                colors = barColors,
            )
            NavigationBarItem(
                selected = selected == "you",
                onClick = { nav.navigate("you") { launchSingleTop = true } },
                icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                label = { Text(stringResource(R.string.you)) },
                colors = barColors,
            )
        }
    }

    NavHost(navController = nav, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onGuest = vm::continueGuest,
                onSignIn = { nav.navigate("signin") },
            )
        }
        composable("signin") {
            SignInScreen(
                busy = state.authBusy,
                error = state.authError,
                onLogin = vm::login,
                onRegister = vm::register,
                onGoogle = {
                    val msg = if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
                        context.getString(R.string.google_missing)
                    } else {
                        "Google Sign-In token verification is not implemented in v1 yet."
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                },
                onBack = { nav.popBackStack() },
                onGuest = vm::continueGuest,
            )
        }
        composable("guest") {
            val frame = state.frames.firstOrNull()
            GuestScreen(
                dates = vm.displayDates(),
                frame = frame,
                showDone = state.doneFrameId != null,
                onCount = { frame?.let { vm.increment(it.frame.id) } },
                onUndo = { frame?.let { vm.undo(it.frame.id) } },
                onReset = { frame?.let { vm.resetToday(it.frame.id) } },
                onArabic = { frame?.let { nav.navigate("focused/${it.frame.id}") } },
                onYou = { nav.navigate("you") },
                onSignIn = { nav.navigate("signin") },
                onClearDone = vm::clearDone,
            )
        }
        composable("home") {
            HomeScreen(
                dates = vm.displayDates(),
                frames = state.frames,
                doneFrameId = state.doneFrameId,
                onCount = vm::increment,
                onFocus = { nav.navigate("focused/$it") },
                onAdd = { nav.navigate("edit/new") },
                onClearDone = vm::clearDone,
                bottomBar = { Bottom("home") },
            )
        }
        composable("history") {
            HistoryScreen(
                frames = state.frames,
                history = state.history,
                bottomBar = { Bottom("history") },
            )
        }
        composable("you") {
            YouScreen(
                settings = state.settings,
                onHaptics = vm::setHaptics,
                onVolumeUp = vm::setVolumeUp,
                onResetAt = vm::setResetAt,
                onLanguage = vm::setLanguage,
                onLocationEnabled = vm::setLocationEnabled,
                onSignOut = vm::signOut,
                onSignIn = { nav.navigate("signin") },
                onBack = if (state.settings.mode != SessionMode.SIGNED_IN) {
                    { nav.popBackStack() }
                } else {
                    null
                },
                bottomBar = {
                    if (state.settings.mode == SessionMode.SIGNED_IN) {
                        Bottom("you")
                    }
                },
            )
        }
        composable(
            "focused/{frameId}",
            arguments = listOf(navArgument("frameId") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("frameId")
            val frame = state.frames.firstOrNull { it.frame.id == id }
            FocusedScreen(
                frame = frame,
                volumeUpEnabled = state.settings.volumeUpIncrement,
                showDone = state.doneFrameId == id,
                onCount = { id?.let(vm::increment) },
                onUndo = { id?.let(vm::undo) },
                onReset = { id?.let(vm::resetToday) },
                onBack = { nav.popBackStack() },
                onEdit = { id?.let { nav.navigate("edit/$it") } },
                onClearDone = vm::clearDone,
            )
        }
        composable(
            "edit/{frameId}",
            arguments = listOf(navArgument("frameId") { type = NavType.StringType }),
        ) { entry ->
            val raw = entry.arguments?.getString("frameId")
            val id = raw?.takeIf { it != "new" }
            var existing by remember(id) { mutableStateOf<FrameEntity?>(null) }
            LaunchedEffect(id) {
                existing = id?.let { vm.frame(it) }
            }
            EditFrameScreen(
                existing = existing,
                onSave = { arabic, transliteration, target ->
                    vm.saveFrame(id, arabic, transliteration, target)
                    nav.popBackStack()
                },
                onDelete = id?.let {
                    {
                        vm.deleteFrame(it)
                        nav.popBackStack()
                    }
                },
                onBack = { nav.popBackStack() },
            )
        }
    }
}
