package com.shishir.automatrix.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shishir.automatrix.ui.motion.FlowHaptics
import com.shishir.automatrix.ui.motion.FlowMotion
import com.shishir.automatrix.ui.screens.builder.RoutineBuilderScreen
import com.shishir.automatrix.ui.screens.developer.DeveloperScreen
import com.shishir.automatrix.ui.screens.home.HomeScreen
import com.shishir.automatrix.ui.screens.logs.AutomationLogsScreen
import com.shishir.automatrix.ui.screens.modes.ModeDetailScreen
import com.shishir.automatrix.ui.screens.modes.ModesScreen
import com.shishir.automatrix.ui.screens.permissions.PermissionCenterScreen
import com.shishir.automatrix.ui.screens.routines.RoutineDetailScreen
import com.shishir.automatrix.ui.screens.routines.RoutinesScreen
import com.shishir.automatrix.ui.screens.settings.SettingsScreen
import com.shishir.automatrix.ui.screens.templates.TemplatesGalleryScreen
import com.shishir.automatrix.ui.theme.LocalFlowGlassColors
import com.shishir.automatrix.ui.viewmodel.FlowViewModel

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Routines : Screen("routines", "Routines")
    object Modes : Screen("modes", "Modes")
    object Settings : Screen("settings", "Settings")

    object RoutineDetail : Screen("routine_detail/{routineId}", "Routine Details") {
        fun createRoute(id: String) = "routine_detail/$id"
    }

    object RoutineBuilder : Screen("routine_builder?routineId={routineId}", "Routine Editor") {
        fun createRoute(id: String? = null) = if (id != null) "routine_builder?routineId=$id" else "routine_builder"
    }

    object PermissionCenter : Screen("permission_center", "Permissions")
    object AutomationLogs : Screen("automation_logs", "Logs")
    object Developer : Screen("developer", "Developer")
    object Templates : Screen("templates", "Templates")
    object ModeDetail : Screen("mode_detail/{modeId}", "Mode Details") {
        fun createRoute(id: String) = "mode_detail/$id"
    }
}

private data class TabItem(
    val screen: Screen,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
)

@Composable
fun FlowAppContent(
    viewModel: FlowViewModel,
    navController: NavHostController = rememberNavController()
) {
    val glassColors = LocalFlowGlassColors.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for user feedback messages
    LaunchedEffect(Unit) {
        viewModel.userFeedback.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs = remember {
        listOf(
            TabItem(Screen.Home, Icons.Filled.Home, Icons.Outlined.Home),
            TabItem(Screen.Routines, Icons.Filled.Bolt, Icons.Outlined.Bolt),
            TabItem(Screen.Modes, Icons.Filled.Tune, Icons.Outlined.Tune),
            TabItem(Screen.Settings, Icons.Filled.Settings, Icons.Outlined.Settings)
        )
    }

    val isTopLevelDestination = remember(currentRoute) {
        tabs.any { it.screen.route == currentRoute }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isTopLevelDestination) {
                val hapticFeedback = LocalHapticFeedback.current
                val view = LocalView.current

                // iOS tab bar: flat surface with a hairline top separator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(glassColors.glassSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(glassColors.glassBorder)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(top = 6.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        tabs.forEach { tab ->
                            val isSelected = currentRoute == tab.screen.route
                            val icon = if (isSelected) tab.filledIcon else tab.outlinedIcon
                            val interactionSource = remember { MutableInteractionSource() }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = ripple(bounded = false),
                                        role = Role.Tab
                                    ) {
                                        if (currentRoute != tab.screen.route) {
                                            FlowHaptics.tick(hapticFeedback, view)
                                            navController.navigate(tab.screen.route) {
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                    .padding(vertical = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.screen.title,
                                    tint = if (isSelected) glassColors.accent else glassColors.textMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = tab.screen.title,
                                    fontSize = 10.sp,
                                    color = if (isSelected) glassColors.accent else glassColors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = FlowMotion.PageEnterOffset
                ) + fadeIn(animationSpec = FlowMotion.PageEnter)
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -(it * 0.28f).toInt() },
                    animationSpec = FlowMotion.PageExitOffset
                ) + fadeOut(animationSpec = FlowMotion.PageExit)
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -(it * 0.28f).toInt() },
                    animationSpec = FlowMotion.PageEnterOffset
                ) + fadeIn(animationSpec = FlowMotion.PageEnter)
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = FlowMotion.PageExitOffset
                ) + fadeOut(animationSpec = FlowMotion.PageExit)
            }
        ) {
            composable(Screen.Home.route) {
                val rawRoutines by viewModel.rawRoutines.collectAsState()
                val modes by viewModel.modes.collectAsState()
                val logs by viewModel.executionLogs.collectAsState()

                HomeScreen(
                    routines = rawRoutines,
                    modes = modes,
                    logs = logs,
                    onNavigateToCreateRoutine = {
                        navController.navigate(Screen.RoutineBuilder.createRoute(null))
                    },
                    onNavigateToRoutineDetail = { id ->
                        navController.navigate(Screen.RoutineDetail.createRoute(id))
                    },
                    onToggleRoutine = { viewModel.toggleRoutineEnabled(it) },
                    onToggleMode = { viewModel.toggleModeActive(it) },
                    onNavigateToModeDetail = { id ->
                        navController.navigate(Screen.ModeDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Routines.route) {
                val filteredRoutines by viewModel.filteredRoutines.collectAsState()
                val searchQuery by viewModel.routineSearchQuery.collectAsState()
                val filterFavorites by viewModel.routineFilterFavorites.collectAsState()
                val sortOrder by viewModel.routineSortOrder.collectAsState()

                RoutinesScreen(
                    routines = filteredRoutines,
                    searchQuery = searchQuery,
                    filterFavorites = filterFavorites,
                    sortOrder = sortOrder,
                    onSearchQueryChange = { viewModel.routineSearchQuery.value = it },
                    onFilterFavoritesChange = { viewModel.routineFilterFavorites.value = it },
                    onSortOrderChange = { viewModel.routineSortOrder.value = it },
                    onNavigateToCreate = {
                        navController.navigate(Screen.RoutineBuilder.createRoute(null))
                    },
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.RoutineDetail.createRoute(id))
                    },
                    onToggleRoutine = { viewModel.toggleRoutineEnabled(it) },
                    onToggleFavorite = { viewModel.toggleRoutineFavorite(it) },
                    onDuplicateRoutine = { viewModel.duplicateRoutine(it) },
                    onDeleteRoutine = { viewModel.deleteRoutine(it) },
                    onRunRoutine = { viewModel.runRoutineNow(it) },
                    onNavigateToTemplates = { navController.navigate(Screen.Templates.route) }
                )
            }

            composable(Screen.Modes.route) {
                val modes by viewModel.modes.collectAsState()

                ModesScreen(
                    modes = modes,
                    onToggleMode = { viewModel.toggleModeActive(it) },
                    onNavigateToModeDetail = { id ->
                        navController.navigate(Screen.ModeDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Settings.route) {
                val themeMode by viewModel.themeMode.collectAsState()
                val isDeveloperMode by viewModel.isDeveloperMode.collectAsState()
                val isFlipToShhhEnabled by viewModel.isFlipToShhhEnabled.collectAsState()
                val isShakeTriggerEnabled by viewModel.isShakeTriggerEnabled.collectAsState()
                val isCalendarTriggerEnabled by viewModel.isCalendarTriggerEnabled.collectAsState()
                val phoneOrientation by viewModel.phoneOrientation.collectAsState()
                val modes by viewModel.modes.collectAsState()

                SettingsScreen(
                    themeMode = themeMode,
                    isDeveloperMode = isDeveloperMode,
                    isFlipToShhhEnabled = isFlipToShhhEnabled,
                    isShakeTriggerEnabled = isShakeTriggerEnabled,
                    isCalendarTriggerEnabled = isCalendarTriggerEnabled,
                    phoneOrientation = phoneOrientation,
                    modes = modes,
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    onDeveloperModeChange = { viewModel.setDeveloperMode(it) },
                    onFlipToShhhChange = { viewModel.setFlipToShhhEnabled(it) },
                    onShakeTriggerChange = { viewModel.setShakeTriggerEnabled(it) },
                    onCalendarTriggerChange = { viewModel.setCalendarTriggerEnabled(it) },
                    onSimulateNfcTap = { viewModel.simulateNfcTap(it) },
                    onExportBackup = { viewModel.exportBackup() },
                    onImportBackup = { viewModel.importBackup(it) },
                    onNavigateToPermissions = { navController.navigate(Screen.PermissionCenter.route) },
                    onNavigateToLogs = { navController.navigate(Screen.AutomationLogs.route) },
                    onNavigateToDeveloper = { navController.navigate(Screen.Developer.route) }
                )
            }

            composable(
                route = Screen.RoutineDetail.route,
                arguments = listOf(navArgument("routineId") { type = NavType.StringType })
            ) { backStackEntry ->
                val rawRoutines by viewModel.rawRoutines.collectAsState()
                val routineId = backStackEntry.arguments?.getString("routineId")
                val routine = remember(rawRoutines, routineId) {
                    rawRoutines.firstOrNull { it.id == routineId }
                }

                RoutineDetailScreen(
                    routine = routine,
                    onNavigateBack = { navController.popBackStack() },
                    onEditRoutine = {
                        navController.navigate(Screen.RoutineBuilder.createRoute(routineId))
                    },
                    onDeleteRoutine = {
                        if (routineId != null) {
                            viewModel.deleteRoutine(routineId)
                            navController.popBackStack()
                        }
                    },
                    onToggleEnabled = {
                        if (routine != null) viewModel.toggleRoutineEnabled(routine)
                    },
                    onRunNow = {
                        if (routine != null) viewModel.runRoutineNow(routine)
                    }
                )
            }

            composable(
                route = Screen.RoutineBuilder.route,
                arguments = listOf(navArgument("routineId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val rawRoutines by viewModel.rawRoutines.collectAsState()
                val routineId = backStackEntry.arguments?.getString("routineId")
                val existingRoutine = remember(rawRoutines, routineId) {
                    if (routineId != null) rawRoutines.firstOrNull { it.id == routineId } else null
                }

                RoutineBuilderScreen(
                    initialRoutine = existingRoutine,
                    onSave = { savedRoutine ->
                        viewModel.saveRoutine(savedRoutine)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PermissionCenter.route) {
                val permissions by viewModel.permissions.collectAsState()

                PermissionCenterScreen(
                    permissions = permissions,
                    onRefresh = { viewModel.refreshPermissions() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AutomationLogs.route) {
                val logs by viewModel.executionLogs.collectAsState()

                AutomationLogsScreen(
                    logs = logs,
                    onClearLogs = { viewModel.clearLogs() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Developer.route) {
                DeveloperScreen(
                    onTriggerCondition = { viewModel.testSimulatedTrigger(it) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Templates.route) {
                TemplatesGalleryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onImportTemplate = { routine ->
                        viewModel.saveRoutine(routine)
                        navController.popBackStack()
                    },
                    onCustomizeTemplate = { routine ->
                        viewModel.saveRoutine(routine)
                        navController.navigate(Screen.RoutineBuilder.createRoute(routine.id))
                    }
                )
            }

            composable(
                route = Screen.ModeDetail.route,
                arguments = listOf(navArgument("modeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val modeId = backStackEntry.arguments?.getString("modeId")
                val modes by viewModel.modes.collectAsState()
                val selectedMode = modes.firstOrNull { it.id == modeId }

                ModeDetailScreen(
                    mode = selectedMode,
                    onNavigateBack = { navController.popBackStack() },
                    onToggleMode = { viewModel.toggleModeActive(it) }
                )
            }
        }
    }
}
